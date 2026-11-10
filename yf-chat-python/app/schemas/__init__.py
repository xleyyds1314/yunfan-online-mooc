"""【请求/响应模型层】Pydantic Schema，对应原 Java 的 dto / vo / query 包。

FastAPI 用它们做参数校验与文档生成（Swagger 自动可调试）。
"""

from typing import Any, Generic, List, Optional, TypeVar

from pydantic import BaseModel

T = TypeVar("T")


class PageDTO(BaseModel, Generic[T]):
    """通用分页返回，结构为：{total, pages, list}。"""

    total: int = 0
    pages: int = 0
    list: List[T] = []


class UserSessionCreate(BaseModel):
    """POST /session 请求体（对应 UserSessionDTO）。"""

    name: Optional[str] = None
    tag: Optional[str] = None
    sessionId: str


class UserSessionUpdate(BaseModel):
    """PUT /session/{id} 请求体（原版是 query 参数绑定，这里统一用 JSON body，更好调试）。"""

    name: Optional[str] = None
    tag: Optional[str] = None


class MarkdownDocsUpdate(BaseModel):
    """PUT /file/update 请求体：更新文档内容后要重新切分、重新嵌入。"""

    id: int
    content: str
    level: int = 2


class MarkdownChunkVO(BaseModel):
    """GET /file/chat 返回项：检索到的知识片段（对应 MarkdownChunkVO）。"""

    score: float
    title: str
    content: str
