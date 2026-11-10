"""【PromptBuilder】参考资料包装/解析工具 —— 对应原 Java 的 PromptBuilder.java。

核心约定（前后端 + 模型三方都认这套标记）：
    [SYS_CONTEXT_BEGIN]检索到的资料[SYS_CONTEXT_END]用户的原始问题

为什么要包一层？
    RAG 流程里，"检索到的资料"和"用户的问题"要一起发给模型，但不能混为一谈：
    - 包装后模型能明确区分"哪些是资料、哪些是提问"（配合 KnowledgeAdvisor 系统提示词）；
    - 存对话记忆前，必须把标记剥掉只留原始问题 —— 否则下一轮历史里全是标记，
      既浪费 token，还会被模型当成要回答的内容。
"""

import re

# 固定标记字符串（与原版保持一致）
CONTEXT_START = "[SYS_CONTEXT_BEGIN]"
CONTEXT_END = "[SYS_CONTEXT_END]"


def build_system_message(context: str, message: str) -> str:
    """包装参考资料并拼接用户问题 —— 拼好后作为整条用户消息发给模型。"""
    context = context or ""
    message = message or ""
    return CONTEXT_START + context + CONTEXT_END + message


def extract_original_message(system_message: str) -> str:
    """剥掉标记及其中的资料，只留用户原始问题（存记忆前必调）。

    例：[SYS_CONTEXT_BEGIN]xxx资料[SYS_CONTEXT_END]什么是光合作用？
        -> 什么是光合作用？
    没有标记时原样返回（普通聊天的消息不需要剥）。
    """
    if not system_message:
        return ""
    # re.DOTALL 让 . 能匹配换行 —— 资料里可能有换行符
    pattern = re.escape(CONTEXT_END) + "(.*)$"
    match = re.search(pattern, system_message, re.DOTALL)
    if match:
        return match.group(1).strip()
    return system_message.strip()
