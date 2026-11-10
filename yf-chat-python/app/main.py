"""【服务入口】FastAPI 应用装配。

启动方式（项目根 yf-ai-chat/ 下）：
    uvicorn app.main:app --host 0.0.0.0 --port 8094 --reload

启动前自检清单：
    1) docker compose up -d                 # chroma + redis 中间件
    2) mysql < sql/yf_chat.sql              # 建库建表
    3) .env 里填好密钥
"""

import json as _json
import logging

from fastapi import FastAPI, Response
from fastapi.responses import JSONResponse

from app.api import chat, docs, session

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)

# 对应原 yf-chat 的端口 8094（服务名 chat-service）
app = FastAPI(
    title="yf-ai-chat",
    description="云帆学堂 AI 模块 Python 重写版：LangChain + LangGraph + ChromaDB（替代 LangChain4j + Qdrant）。"
    "所有接口用请求头 X-User-Id 模拟网关注入的用户身份，不传默认 1。",
    version="1.0.0",
)

# 跨域：由网关统一处理（网关已注入 CORS 头），本地不再重复添加，
# 否则响应会出现两个 Access-Control-Allow-Origin 头导致浏览器拦截（net::ERR_FAILED）
# app.add_middleware(
#     CORSMiddleware,
#     allow_origins=["*"],
#     allow_credentials=True,
#     allow_methods=["*"],
#     allow_headers=["*"],
# )


@app.middleware("http")
async def wrap_response(request, call_next):
    """统一响应包装：把 2xx JSON 裸返回包装成 Java 风格 {code, msg, data}。

    前端 request.js 拦截器只认 res.code === 200，裸返回（如 {"id":...} 或 []）
    会被判定为失败。SSE（text/event-stream）、非 JSON、4xx/5xx 原样透传。
    """
    response = await call_next(request)
    content_type = response.headers.get("content-type", "")
    if (
        response.status_code >= 400
        or "text/event-stream" in content_type
        or not content_type.startswith("application/json")
    ):
        return response
    body = b""
    async for chunk in response.body_iterator:
        body += chunk
    try:
        data = _json.loads(body)
    except Exception:
        return Response(content=body, status_code=response.status_code, media_type=content_type)
    # 已经是 {code, msg, data} 结构则不再重复包裹
    if isinstance(data, dict) and "code" in data:
        return Response(content=body, status_code=response.status_code, media_type=content_type)
    return JSONResponse(
        {"code": 200, "msg": "OK", "data": data}, status_code=response.status_code
    )

# 挂载三组路由（三组路由）
app.include_router(chat.router)     # /chat  聊天
app.include_router(session.router)  # /session 会话管理
app.include_router(docs.router)     # /file  知识库


@app.get("/health", tags=["系统"], summary="健康检查")
def health():
    return {"status": "up", "service": "yf-ai-chat"}
