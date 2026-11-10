"""【聊天接口】/chat 系列 —— 对应原 ChatController.java。

SSE 协议约定与原版完全一致（前端代码可无缝复用）：
    event: message
    data: <token 片段，空格替换为 &nbsp;>     （换行拆成多个 data 行）
    ...
    data: [DONE]                              （结束标志）
"""

import logging

from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse

from app.api.deps import get_user_id
from app.database import SessionLocal
from app.models import ChatSession
from app.schemas import PageDTO
from app.services import graph as graph_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/chat", tags=["聊天接口"])


# =============================================================================
# SSE 工具函数
# =============================================================================


def sse_event(data: str) -> str:
    """按原版 SseEmitter 的行为格式化一条 SSE 消息。

    - 空格替换为 &nbsp;（防浏览器吞空格）；
    - token 里的换行要拆成多个 data: 行（SSE 规范），EventSource 会自动拼回 \n；
    - 事件名统一 message。
    """
    data = data.replace(" ", "&nbsp;")
    data_lines = "".join(f"data: {line}\n" for line in data.split("\n"))
    return f"event: message\n{data_lines}\n"


def sse_response(token_generator) -> StreamingResponse:
    """把 token 生成器包成 SSE 响应：逐 token 转发，最后补 [DONE]。"""

    async def stream():
        try:
            async for token in token_generator:
                yield sse_event(token)
        finally:
            yield sse_event("[DONE]")

    return StreamingResponse(
        stream(),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )


# =============================================================================
# 接口（默认值与原版一致，方便直接复制 curl 测试）
# =============================================================================


@router.get("/simple", summary="普通聊天，非流式")
async def simple_chat(
    message: str = "我叫finch，你叫什么名字？",
    sessionId: str = "1",
    user_id: int = Depends(get_user_id),
) -> str:
    """非流式：跑完整张图再一次性返回文本。"""
    return await graph_service.simple_chat(user_id, sessionId, message)


@router.get("/", summary="流式聊天（SSE）")
async def stream_chat(
    message: str = "我叫finch，你叫什么名字？",
    sessionId: str = "1",
    user_id: int = Depends(get_user_id),
):
    """带记忆 + 工具的流式聊天。curl -N 体验逐字输出。"""
    return sse_response(graph_service.stream_chat(user_id, sessionId, message, use_rag=False))


@router.get("/file", summary="根据知识库内容流式聊天（SSE，RAG）")
async def file_stream_chat(
    message: str = "我叫finch，你叫什么名字？",
    sessionId: str = "1",
    user_id: int = Depends(get_user_id),
):
    """RAG 流式：先检索你的知识库，再把资料包装后交给模型生成。"""
    return sse_response(graph_service.stream_chat(user_id, sessionId, message, use_rag=True))


@router.get("/test", summary="测试流式聊天接口（直连模型，无记忆无工具）")
async def test_stream_chat(message: str = "你好"):
    """最简流式：对应原版 test 接口，用来验证模型连通性。"""
    return sse_response(graph_service.stream_raw(message))


@router.get("/records", summary="分页查询聊天记录")
async def get_records(
    sessionId: str,
    pageNo: int = 1,
    pageSize: int = 10,
    user_id: int = Depends(get_user_id),
) -> PageDTO:
    """分页返回 chat_session 落库记录（每行一条消息 JSON），按创建时间倒序。"""
    with SessionLocal() as db:
        query = db.query(ChatSession).filter(
            ChatSession.user_id == user_id, ChatSession.session_id == sessionId
        )
        total = query.count()
        rows = (
            query.order_by(ChatSession.create_time.desc())
            .offset((pageNo - 1) * pageSize)
            .limit(pageSize)
            .all()
        )
        items = [
            {
                "id": row.id,
                "userId": row.user_id,
                "sessionId": row.session_id,
                "segmentIndex": row.segment_index,
                "content": row.content,
                "createTime": row.create_time.strftime("%Y-%m-%d %H:%M:%S")
                if row.create_time
                else None,
            }
            for row in rows
        ]
    return PageDTO(total=total, pages=(total + pageSize - 1) // pageSize, list=items)
