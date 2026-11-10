"""【工具集】给 Agent 用的"外挂技能" —— 对应原 Java 的 ToolsService.java（@Tool 注解）。

LangChain 与 LangChain4j 的工具写法对照：
    LangChain4j:  @Tool("描述") double sum(double a, double b)
    LangChain:    @tool + 函数 docstring 当描述（注意：@tool("xxx") 的字符串参数
                  是【工具名】而不是描述，且必须匹配 ^[a-zA-Z0-9_-]+$，不能写中文！）

原理完全一样：装饰器读取【函数名 + docstring/描述 + 参数类型注解】自动生成
function-calling 的 schema，随请求发给模型；模型"看到"schema 后决定何时调用、传什么参。

工具如何接进图？见 graph.py：
    llm.bind_tools(tools)      -> 告诉模型有哪些工具
    ToolNode(tools)            -> 图里真正执行工具的节点
"""

import logging
import math
import re

import httpx
from langchain_core.tools import tool

from app.config import settings

logger = logging.getLogger(__name__)

# 去掉搜索结果里的 <em> 高亮标签
_EM_RE = re.compile(r"</?em>")

# ---- mock 课程数据：COURSE_SERVICE_BASE_URL 未配置时 query_course 返回这些 ----
# 返回格式为 [{id, name, price}]。
MOCK_COURSES = [
    {"id": 1, "name": "JavaSE基础课程", "price": 0.0},
    {"id": 2, "name": "JavaWeb课程", "price": 99.0},
    {"id": 3, "name": "SSM&SpringBoot原理实战", "price": 199.0},
    {"id": 4, "name": "微服务SpringCloud课程", "price": 299.0},
    {"id": 5, "name": "AI应用开发课程（LangChain4j）", "price": 399.0},
    {"id": 6, "name": "AI应用开发课程（LangChain & LangGraph）", "price": 399.0},
    {"id": 7, "name": "Python数据分析实战", "price": 149.0},
    {"id": 8, "name": "MySQL数据库从入门到精通", "price": 59.0},
]


@tool
def sum_tool(a: float, b: float) -> float:
    """对给定的 2 个数字求和（函数名不能叫 sum，会遮蔽内置函数）。"""
    return a + b


@tool
def square_root(x: float) -> float:
    """返回给定数字的平方根。"""
    return math.sqrt(x)


@tool
def query_course(name: str = "") -> list:
    """查询云帆学堂平台的真实课程列表。

    当用户问"有哪些课程"、"全部课程"、"所有课程"、"课程列表"、"推荐课程"、
    "想学XXX"、"XXX方向有什么课"时，你必须调用此工具。

    参数 name:
      - 传空字符串 "" → 返回平台所有课程
      - 传关键词（如 "Java"、"前端"、"微服务"）→ 返回包含该关键词的课程
    返回: [{"id": 课程ID, "name": 课程名, "price": 价格(分), "teacher": 老师, "sections": 章节数}, ...]
    """
    base = settings.COURSE_SERVICE_BASE_URL
    keyword = name.strip()
    want_all = (
        keyword in ("", "全部", "全部课程", "所有课程", "课程列表", "所有", "查一下课程", "有哪些课程")
        or ("全部" in keyword and "课程" in keyword)
    )
    if base:
        try:
            search_kw = "" if want_all else keyword
            resp = httpx.get(
                f"{base.rstrip('/')}/courses/portal",
                params={"keyword": search_kw, "pageNo": 1, "pageSize": 100},
                timeout=5,
            )
            resp.raise_for_status()
            data = resp.json()
            raw_list = data.get("list") or data.get("data", {}).get("list") or []
            result = []
            for c in raw_list:
                result.append({
                    "id": c.get("id"),
                    "name": _EM_RE.sub("", c.get("name", "")),
                    "price": c.get("price", 0),
                    "teacher": c.get("teacher", ""),
                    "sections": c.get("sections", 0),
                })
            logger.info("query_course(name=%s, want_all=%s) 命中 %d 门真实课程", name, want_all, len(result))
            return result
        except Exception as e:
            logger.warning("query_course 调真实服务失败，回退 mock：%s", e)
            if want_all:
                return [{**c, "teacher": "", "sections": 0} for c in MOCK_COURSES]
            return [{**c, "teacher": "", "sections": 0} for c in MOCK_COURSES if keyword in c["name"]]
    if want_all:
        return [{**c, "teacher": "", "sections": 0} for c in MOCK_COURSES]
    return [{**c, "teacher": "", "sections": 0} for c in MOCK_COURSES if keyword in c["name"]]


def get_tools() -> list:
    """返回本项目的全部工具 —— build 图时传给 bind_tools / ToolNode。"""
    return [sum_tool, square_root, query_course]
