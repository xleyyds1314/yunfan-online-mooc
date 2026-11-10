"""【LangGraph 编排】两张图：普通对话 Agent 图 + 知识库 RAG 图 —— 对应原 AiConfig 两个 AiServices。

概念映射（LangChain4j -> LangGraph）：
    AiServices.builder(...)        -> StateGraph(状态图) + compile()
    @MemoryId + ChatMemoryProvider -> 记忆不走图，由 services/memory.py 管（读历史 -> 拼进初始消息）
    @SystemMessage                 -> SystemMessage() 放在消息列表最前
    .tools(toolsService)           -> llm.bind_tools(tools) + ToolNode(tools) + tools_condition
    TokenStream（流式）            -> graph.astream_events(version="v2") 抽取 token

图 1：普通聊天（agent ⇄ tools 循环）
    START -> agent(调模型) --有 tool_calls?--> tools(执行工具) -> agent 再想一步
                    └--------没有 tool_calls--------> END

图 2：RAG 知识库问答（retrieve -> generate ⇄ tools）
    START -> retrieve(检索知识库、拼提示词) -> generate(调模型) ⇄ tools -> END
"""

import asyncio
import logging
from typing import Annotated, AsyncIterator, List, Optional, TypedDict

from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from langchain_openai import ChatOpenAI
from langgraph.graph import START, MessagesState, StateGraph
from langgraph.graph.message import add_messages
from langgraph.prebuilt import ToolNode, tools_condition

from app.config import settings
from app.services import memory, prompt, rag, tools

logger = logging.getLogger(__name__)

# ---- 普通聊天的系统提示词 ----
SYSTEM_PROMPT = (
    "你叫小天，是云帆学堂的智能学习助手。"
    "每当用户提到以下任何一种内容时，你【必须且只能】调用 query_course 工具来获取真实课程数据，"
    "绝对不允许自己编造、猜测或假设平台有什么课程，也绝对不要参考历史对话中提到过的任何课程信息：\n"
    "- 想知道有哪些课程 / 所有课程 / 全部课程 / 课程列表\n"
    "- 想学习某类技术（Java、前端、后端、数据库、Python、微服务等）\n"
    "- 让你推荐课程 / 问问有什么课 / 想学点什么 / 学什么好\n"
    "- 列出课程 / 把课程都列出来\n"
    "\n"
    "【重要】历史对话里可能出现过不准确的课程数量或课程名，请全部忽略，"
    "每次遇到课程相关问题都必须重新调用 query_course 获取最新真实数据。\n"
    "\n"
    "query_course 工具的 name 参数传空字符串 = 查询全部课程；"
    "传关键词（如 'Java'）= 模糊搜索包含该关键词的课程。\n"
    "只有当 query_course 工具返回了结果后，你才能基于返回的数据回答。\n"
    "\n"
    "回答格式：用 Markdown 表格或列表。\n"
    "- 每门课必须用 Markdown 链接：[课程名](/#/details?id=课程ID)，课程ID来自工具返回。\n"
    "- 价格单位是分，展示时换算成元（price / 100）。\n"
    "- 表格格式：| 课程名称 | 价格 | 老师 | 章节数 |\n"
    "\n"
    "如果工具返回空列表，如实告知当前没有相关课程。\n"
    "严禁编造任何课程名称、ID 或价格——必须完全基于工具返回结果。"
)


# ---- 意图识别关键词 ----
_ALL_KW = ("全部课程", "所有课程", "课程列表", "全部的课程", "所有的课程", "全部", "所有", "列举")
_COURSE_KW = ("课", "课程", "推荐", "想学", "学习", "选修", "学什么", "学哪些", "有什么")


def _detect_course_intent(message: str) -> str:
    """检测课程查询意图，返回 'all' / 'some' / ''。"""
    msg = message.strip()
    if any(kw in msg for kw in _ALL_KW) and any(kw in msg for kw in _COURSE_KW):
        return "all"
    if any(kw in msg for kw in _COURSE_KW):
        return "some"
    return ""


def _fetch_courses_for_prompt(message: str, user_id: int = 0) -> str:
    """预查课程数据并格式化为注入 SystemMessage 的文本。

    策略：不依赖模型决定是否调工具，在进入模型之前就把课程数据查好注入提示词。
    """
    intent = _detect_course_intent(message)
    if not intent:
        return ""

    try:
        if intent == "all":
            # 优先用工具（直连搜索服务），拿真实课程
            raw = tools.query_course.invoke({"name": ""})
        else:
            raw = tools.query_course.invoke({"name": message})
    except Exception:
        raw = []

    if not raw:
        return ""

    lines = ["平台课程列表（必须从中推荐）：\n"]
    for c in raw:
        cid = str(c.get("id", ""))
        name = c.get("name", "")
        price = c.get("price", 0) or 0
        try:
            price = float(price)
        except (TypeError, ValueError):
            price = 0
        teacher = c.get("teacher", "")
        sections = c.get("sections", 0) or 0
        price_yuan = price / 100
        link = f"[详情](/#/details?id={cid})"
        lines.append(
            f"- {name} {link} | 价格：{'免费' if price == 0 else f'{price_yuan}元'}"
            + (f" | 老师：{teacher}" if teacher else "")
            + (f" | 章节：{sections}" if sections else "")
        )
    lines.append("")
    return "\n".join(lines)

# ---- 知识库问答的系统提示词（原 KnowledgeAdvisor @SystemMessage 原文）----
KNOWLEDGE_PROMPT = """你是一位智能学习助手，核心职责是基于学生上传的学习资料以及平台课程知识库精准回答问题。请严格遵循以下规则：
资料处理规则
格式识别：参考资料会以 Markdown 格式呈现，且被固定标记包裹：
[SYS_CONTEXT_BEGIN]（参考资料起始）
[SYS_CONTEXT_END]（参考资料结束）
注意：标记后直接跟随学生提问，无需处理代码标记。
内容处理：
- 以【平台课程】开头的资料是平台真实课程信息，包含课程名称、价格、介绍、适合人群等；
- 仅参考[SYS_CONTEXT_BEGIN]与[SYS_CONTEXT_END]之间的资料内容，不解释代码逻辑；
- 回答需使用清晰、简洁的语言，避免专业术语堆砌；
- 若提问超出资料范围，可基于常识回答，但需在结尾附加提示：
"这个问题好像不在你的笔记中，建议补充相关资料或查阅更多学习资源哦～"
回答规范
准确性：优先依据资料内容，确保信息正确；
简洁性：直接针对问题核心，避免冗余扩展；
友好性：语气亲和，适合学生理解（例如用 "可以试试..." 替代 "建议采取..."）。
课程推荐：
- 如果学生询问课程或学习建议，优先基于【平台课程】资料推荐；
- 推荐课程时用 Markdown 链接格式：[课程名](/#/details?id=课程ID)，链接直接使用资料中给出的课程详情链接；
- 课程ID和名称必须来自资料，严禁编造。
示例响应逻辑：
若资料包含 "光合作用原理"，学生提问 "植物如何制造氧气？"，则回答：
植物通过光合作用制造氧气。叶绿体利用光能将二氧化碳和水转化为有机物，同时释放氧气。
请始终以提供的资料为首要参考依据，在保持专业性的同时确保回答易于理解。"""


def build_llm(bind_tools: bool = True, streaming: bool = True) -> ChatOpenAI:
    """构造 DeepSeek 模型客户端（OpenAI 兼容协议）。

    - streaming=True：SSE 逐字输出靠 astream_events 收 token；
    - bind_tools：把工具的"名字/描述/参数 schema"注入请求，模型才知道能调什么。
    """
    llm = ChatOpenAI(
        api_key=settings.LLM_API_KEY,
        base_url=settings.LLM_BASE_URL,
        model=settings.LLM_MODEL,
        temperature=0,
        streaming=streaming,
    )
    return llm.bind_tools(tools.get_tools()) if bind_tools else llm


def _normalize_content(content) -> str:
    """模型返回的 content 可能是 str 或多模态块列表，统一转纯文本。"""
    if isinstance(content, str):
        return content
    if isinstance(content, list):
        parts = []
        for part in content:
            if isinstance(part, dict):
                parts.append(part.get("text") or "")
            else:
                parts.append(str(part))
        return "".join(parts)
    return str(content or "")


def _pick_final_answer(messages: list) -> str:
    """从图的输出里倒序找最后一条有内容的 AIMessage 作为最终回答。"""
    for message in reversed(messages):
        if isinstance(message, AIMessage):
            text = _normalize_content(message.content).strip()
            if text:
                return text
    return ""


# =============================================================================
# 图 1：普通对话 Agent 图（对应 assistantRedis）
# =============================================================================


def build_chat_graph():
    """搭"agent ⇄ tools"循环图。每个节点/边的含义见文件头注释。"""
    llm = build_llm(bind_tools=True)
    tool_list = tools.get_tools()

    async def agent_node(state: MessagesState):
        # 把当前整段对话（历史 + 工具回填结果）发给模型，返回 AIMessage
        response = await llm.ainvoke(state["messages"])
        return {"messages": [response]}

    graph = StateGraph(MessagesState)
    graph.add_node("agent", agent_node)                 # 思考节点：调大模型
    graph.add_node("tools", ToolNode(tool_list))        # 执行节点：跑工具
    graph.add_edge(START, "agent")                      # 起点先让模型想一次
    graph.add_conditional_edges("agent", tools_condition)  # 有 tool_calls 去 tools，否则 END
    graph.add_edge("tools", "agent")                    # 工具结果回填后回 agent 再想
    return graph.compile()


# =============================================================================
# 图 2：RAG 知识库问答图（对应 KnowledgeAdvisor）
# =============================================================================


class RagState(TypedDict):
    """RAG 图的共享状态：节点间靠它传数据。

    messages 用 add_messages 归约器：节点返回的消息会被【追加】而不是覆盖。
    """

    user_id: int
    question: str                       # 用户原始问题（检索用）
    history: list                       # 历史对话（LangChain 消息对象）
    messages: Annotated[list, add_messages]  # generate 节点的完整输入与累积
    answer: str                         # 最终回答（非流式取结果用）


def build_rag_graph():
    """搭"retrieve -> generate ⇄ tools"图（advisor 也带工具，与原版一致）。"""
    llm = build_llm(bind_tools=True)
    tool_list = tools.get_tools()

    async def retrieve_node(state: RagState):
        question = state["question"]
        user_id = state["user_id"]

        # 1) 意图识别：是否想查全部课程？
        intent = _detect_course_intent(question)

        if intent == "all":
            # 想查全部：枚举课程知识库 + 用户个人资料
            user_hits = await asyncio.to_thread(rag._search_user_docs, user_id, question) if user_id else []
            course_hits = await asyncio.to_thread(rag.list_all_courses_in_rag)
            for h in course_hits:
                h["type"] = "course"
            hits = [{"type": "doc", **h} for h in user_hits] + course_hits
        elif intent == "some":
            # 想查某类课程：向量检索（课程知识库 + 用户资料）
            hits = await asyncio.to_thread(rag.search, user_id, question)
        else:
            # 非课程意图：只搜用户资料
            user_hits = await asyncio.to_thread(rag._search_user_docs, user_id, question) if user_id else []
            hits = [{"type": "doc", **h} for h in user_hits]

        # 2) 组装上下文
        context_parts = []
        for hit in hits:
            if hit.get("type") == "course":
                cid = hit.get("course_id", "")
                name = hit.get("name", "")
                link = f"[课程详情](/#/details?id={cid})"
                context_parts.append(f"【平台课程】{name} {link}\n{hit['text']}")
            else:
                context_parts.append(f"- {hit['text']}")
        context = "\n".join(context_parts)

        # 3) 用固定标记包装资料 + 拼接原始问题
        wrapped = prompt.build_system_message(context, question)
        messages = [
            SystemMessage(content=KNOWLEDGE_PROMPT),
            *state["history"],
            HumanMessage(content=wrapped),
        ]
        return {"messages": messages}

    async def generate_node(state: RagState):
        response = await llm.ainvoke(state["messages"])
        return {"messages": [response], "answer": _normalize_content(response.content)}

    graph = StateGraph(RagState)
    graph.add_node("retrieve", retrieve_node)           # 检索节点：RAG 的 R
    graph.add_node("generate", generate_node)           # 生成节点：RAG 的 G
    graph.add_node("tools", ToolNode(tool_list))
    graph.add_edge(START, "retrieve")
    graph.add_edge("retrieve", "generate")
    graph.add_conditional_edges("generate", tools_condition)  # 需要 -> tools，否则 END
    graph.add_edge("tools", "generate")
    return graph.compile()


# =============================================================================
# 对外入口：非流式 / 流式
# =============================================================================


def _lc_history(history: List[dict], limit: int) -> list:
    """把记忆里的 [{role, content}] 转成 LangChain 消息对象，并截取最近 N 条。"""
    converted = []
    for item in history:
        role = item.get("role")
        content = (item.get("content") or "").strip()
        if not content:
            continue
        if role == "user":
            converted.append(HumanMessage(content=content))
        elif role == "ai":
            converted.append(AIMessage(content=content))
    return converted[-limit:]


async def simple_chat(user_id: int, session_id: str, message: str) -> str:
    """非流式普通聊天（GET /chat/simple）：跑完图，返回最终文本。"""
    history = _lc_history(
        await memory.get_history(user_id, session_id), settings.MEMORY_MAX_MESSAGES
    )
    # 纯净 SystemMessage，让模型自己决定调不调工具
    graph = build_chat_graph()
    result = await graph.ainvoke(
        {"messages": [SystemMessage(content=SYSTEM_PROMPT), *history, HumanMessage(content=message)]},
        config={"recursion_limit": 10},
    )
    answer = _pick_final_answer(result.get("messages") or [])
    if not answer:
        answer = "这个问题我暂时还不清楚，你可以问问老师或查阅资料哦～"

    await memory.append_message(user_id, session_id, "user", message)
    await memory.append_message(user_id, session_id, "ai", answer)
    return answer


async def stream_chat(
    user_id: int, session_id: str, message: str, use_rag: bool = False
) -> AsyncIterator[str]:
    """流式聊天（/chat/ 与 /chat/file 共用）：边跑图边 yield token 片段。"""
    history = _lc_history(
        await memory.get_history(user_id, session_id), settings.MEMORY_MAX_MESSAGES
    )
    answer_parts: List[str] = []

    if use_rag:
        graph = build_rag_graph()
        inputs = {"user_id": user_id, "question": message, "history": history}
        answer_node = "generate"
    else:
        graph = build_chat_graph()
        inputs = {
            "messages": [SystemMessage(content=SYSTEM_PROMPT), *history, HumanMessage(content=message)]
        }
        answer_node = "agent"

    async for event in graph.astream_events(inputs, version="v2", config={"recursion_limit": 10}):
        if event["event"] != "on_chat_model_stream":
            continue
        if event.get("metadata", {}).get("langgraph_node") != answer_node:
            continue
        chunk = event["data"]["chunk"]
        text = _normalize_content(getattr(chunk, "content", None))
        if text:
            answer_parts.append(text)
            yield text

    answer = "".join(answer_parts)
    await memory.append_message(user_id, session_id, "user", message)
    await memory.append_message(user_id, session_id, "ai", answer)


async def stream_raw(message: str) -> AsyncIterator[str]:
    """裸流式（GET /chat/test）：不挂记忆、不挂工具，直连模型 —— 调试用，对应原版 test 接口。"""
    llm = build_llm(bind_tools=False)
    async for chunk in llm.astream(message):
        text = _normalize_content(chunk.content)
        if text:
            yield text
