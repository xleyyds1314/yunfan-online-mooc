# yf-chat-python —— 云帆学堂 AI 服务

平台内的智能问答服务，基于 **FastAPI + LangChain + LangGraph + ChromaDB** 实现，通过网关对外提供流式对话接口。

核心能力：

- **流式对话**：SSE 逐字输出，前端可直接用 `EventSource` 接入
- **工具调用**：模型可自主调用计算、课程查询等工具
- **个人知识库**：上传 Markdown 文档自动切分、向量化，按用户维度隔离检索
- **会话管理**：多会话、聊天记录分页
- **对话记忆**：Redis 承载热数据，静默后异步落库 MySQL

## 技术选型

| 能力 | 方案 |
|------|------|
| Web 框架 | FastAPI + Uvicorn |
| 编排 | LangGraph `StateGraph` + `astream_events` |
| 向量知识库 | ChromaDB（Docker 部署） |
| Embedding | 硅基流动在线 **BAAI/bge-m3**（OpenAI 兼容协议） |
| LLM | **DeepSeek**（OpenAI 兼容协议，可换任意兼容服务） |
| 对话记忆 | Redis List + asyncio 去抖任务落库 MySQL |
| 数据访问 | SQLAlchemy 2.x + PyMySQL |

## 架构总览

```
                          ┌─────────────────────────────────────────────┐
   前端/Swagger           │              yf-chat-python (FastAPI:8094)  │
      │  X-User-Id       │                                             │
      ▼                  │  /chat/simple  ──► 图1: agent ⇄ tools       │
   ┌──────┐   SSE流式    │  /chat/        ──►  (LangGraph 编排)        │──► DeepSeek
   │ 网关 │ ───────────► │  /chat/file    ──► 图2: retrieve→generate   │    (LLM)
   └──────┘              │  /session/*    ──► MySQL user_session       │
                         │  /file/*       ──► 切分→向量→ChromaDB        │──► 硅基流动
                         │                                             │    (bge-m3)
                         └───────────┬────────────────┬────────────────┘
                                    │                │
                              Redis:6379        ChromaDB:8000
                             （对话记忆，        （知识库向量，
                              静默10s落库MySQL）   按 user_id 隔离）
                                    │                │
                              MySQL yf_chat     Docker compose
                             （chat_session 等     一键拉起
                              三张表）
```

## 快速开始

### 0. 前置条件

- Python 3.10+
- Docker Desktop
- 本机 MySQL（密码与 `.env` 保持一致）

### 1. 启动中间件

```bash
cd yf-chat-python
docker compose up -d          # 拉起 yf-chroma(8000) + yf-redis(6379)
docker compose ps             # 确认两个容器 Up
```

> 数据卷 `chroma_data` / `redis_data` 已配置持久化，`docker compose down` 不丢数据。

### 2. 建库建表

```bash
mysql -uroot -p < ../sql/yf_chat.sql
```

### 3. 配置密钥

```bash
cp .env.example .env   # 填写 LLM_API_KEY / SILICONFLOW_API_KEY / DATABASE_URL
```

密钥只放在 `.env`，该文件已被 `.gitignore` 排除。

### 4. 安装依赖并启动

```bash
python -m venv .venv
.venv\Scripts\pip install -r requirements.txt      # Windows
.venv\Scripts\uvicorn app.main:app --host 0.0.0.0 --port 8094 --reload
```

打开 <http://127.0.0.1:8094/docs> 查看可调试的 Swagger 文档。

## 接口说明

所有接口通过请求头 `X-User-Id` 获取当前用户（由网关注入，独立调试时默认 1）。

```bash
# 1. 非流式普通聊天（带记忆 + 工具）
curl "http://127.0.0.1:8094/chat/simple?message=帮我算一下3.5加2.1&sessionId=1"

# 2. 流式普通聊天（-N 关闭缓冲，逐字输出）
curl -N "http://127.0.0.1:8094/chat/?message=你好&sessionId=1"

# 3. 上传知识库文档（.md，≤2MB；level=按几级标题切分）
curl -X POST "http://127.0.0.1:8094/file/upload?level=2" \
     -H "X-User-Id: 1" -F "file=@你的笔记.md"

# 4. RAG 流式问答（检索知识库后回答）
curl -N "http://127.0.0.1:8094/chat/file?message=笔记里讲了什么&sessionId=2"

# 5. 查看检索命中的片段
curl "http://127.0.0.1:8094/file/chat?message=光合作用"

# 6. 建会话 / 会话列表 / 聊天记录分页
curl -X POST "http://127.0.0.1:8094/session" -H "Content-Type: application/json" \
     -d "{\"name\":\"测试会话\",\"tag\":\"demo\",\"sessionId\":\"1\"}"
curl "http://127.0.0.1:8094/session/list"
curl "http://127.0.0.1:8094/chat/records?sessionId=1&pageNo=1&pageSize=10"
```

SSE 事件格式：`event: message`，token 中空格替换为 `&nbsp;`，结束时发送 `data: [DONE]`。

## 代码结构

```
yf-chat-python/
├── docker-compose.yml          # chroma + redis
├── requirements.txt            # 依赖清单
├── .env / .env.example         # 密钥与连接配置
├── sql/yf_chat.sql             # 三张表：chat_session / user_session / user_markdown_docs
└── app/
    ├── main.py                 # FastAPI 应用装配
    ├── config.py               # 配置加载
    ├── database.py             # SQLAlchemy 引擎与 Session
    ├── models/chat.py          # 三张表 ORM 模型
    ├── schemas/__init__.py     # 通用 DTO
    ├── api/
    │   ├── deps.py             # 从请求头解析当前用户
    │   ├── chat.py             # /chat 系列 + SSE 格式化
    │   ├── session.py          # /session 系列
    │   └── docs.py             # /file 系列
    └── services/
        ├── prompt.py           # 系统上下文包装与剥离
        ├── tools.py            # 工具：sum / square_root / query_course
        ├── memory.py           # Redis 记忆 + 静默落库
        ├── rag.py              # Markdown 切分 + ChromaDB 入库/检索
        └── graph.py            # 两张 LangGraph 编排图
```

## 两条编排链路

- **图 1 普通聊天**：`START → agent ⇄ tools → END`。`tools_condition` 检查模型输出是否包含
  `tool_calls` 决定走向，`recursion_limit=10` 防止死循环。
- **图 2 RAG**：`START → retrieve → generate ⇄ tools → END`。retrieve 节点检索知识库并用
  `[SYS_CONTEXT_BEGIN/END]` 包装资料，`add_messages` 归约器负责消息追加。

## 记忆落库机制

1. 每轮对话结束，把最新的 user/ai 消息 append 进 Redis List `chat:memory:{userId}:{sessionId}`；
2. user 消息入列前剥掉 `[SYS_CONTEXT_BEGIN]...[SYS_CONTEXT_END]` 标记，只存原始问题；
3. 静默 10 秒后（期间来新消息则推迟），把列表中的消息按 `segment_index` 续号写入
   `chat_session` 表，随后清空 Redis key；
4. Redis miss 时按 `segment_index` 升序回源 MySQL 还原历史。

## 说明

- **query_course**：未配置 `COURSE_SERVICE_BASE_URL` 时返回内置的示例课程数据，配置后走 HTTP 查询课程服务。
- **用户身份**：由网关注入 `X-User-Id`，独立调试时也可直接指定该请求头切换用户。
