"""【记忆服务】Redis 会话记忆 + MySQL 延迟落库 —— 对应原 PersistentChatMemoryStore + DataDelayTaskHandler。

原版（Java）完整链路：
    1) 每轮对话结束后，只把【最新的 user/ai 消息】追加进 Redis
       List：key = chat:memory:{userId}:{sessionId}
    2) user 消息入列前先剥掉 [SYS_CONTEXT_...] 标记（调 PromptBuilder）；
    3) 同时向 Redisson 延迟队列投一个 10 秒后的落库任务；
    4) 任务醒来时：若 Redis 列表长度 > 投递时的 num，说明用户还在聊，跳过（新任务会接管）；
       否则把列表里全部消息按 segment_index 续号写进 MySQL，然后删掉 Redis key。

Python 版对齐实现（学习要点）：
    - Redisson 延迟队列 -> asyncio.create_task + sleep 的"去抖"落库
      （每次 append 取消上一个未触发的任务，只留最后一个，效果等价且更简单）；
    - 消息格式自定义为 {"role": "user"|"ai", "content": "..."}，
      LangChain4j 的 UserMessage/AiMessage 序列化在这里对应 human/ai 消息。
"""

import asyncio
import json
import logging
from datetime import datetime
from typing import List, Optional

import redis.asyncio as aioredis

from app.config import settings
from app.database import SessionLocal
from app.models import ChatSession, UserSession
from app.services.prompt import extract_original_message

logger = logging.getLogger(__name__)

# ---- 模块级单例（进程内共享一个 Redis 连接池）----
_redis: Optional[aioredis.Redis] = None
# 每个会话 key 对应一个"待触发的落库任务"（实现去抖）
_flush_tasks: dict = {}


def get_redis() -> aioredis.Redis:
    """懒加载异步 Redis 客户端。decode_responses=True：拿到 str 而不是 bytes。"""
    global _redis
    if _redis is None:
        _redis = aioredis.from_url(settings.REDIS_URL, decode_responses=True)
    return _redis


def memory_key(user_id: int, session_id: str) -> str:
    """记忆的 Redis key，与原版格式一致：chat:memory:2:1"""
    return f"chat:memory:{user_id}:{session_id}"


async def get_history(user_id: int, session_id: str) -> List[dict]:
    """读取一个会话的完整对话历史（[{role, content}, ...]）。

    两级读取（对齐原版 getMessages）：
    1) 先读 Redis List —— 热数据，毫秒级；
    2) Redis 没有（服务重启/被清理/已落库删除）就回源 MySQL chat_session，
       按 segment_index 升序把每行的 content JSON 解析出来。
    """
    try:
        r = get_redis()
        raw_list = await r.lrange(memory_key(user_id, session_id), 0, -1)
        if raw_list:
            return [json.loads(item) for item in raw_list]

        # ---- 回源 MySQL ----
        with SessionLocal() as db:
            rows = (
                db.query(ChatSession)
                .filter(ChatSession.user_id == user_id, ChatSession.session_id == session_id)
                .order_by(ChatSession.segment_index.asc())
                .all()
            )
            return [json.loads(row.content) for row in rows if row.content]
    except Exception:
        logger.exception("读取对话历史失败")
        return []


async def append_message(user_id: int, session_id: str, role: str, content: str) -> None:
    """把最新一条消息追加进 Redis 记忆，并调度延迟落库。

    对齐原版 updateMessages 的关键细节：
    - user 消息先剥 [SYS_CONTEXT_...] 标记，只存原始问题；
    - 只追加最新一条（列表本身就承载完整历史）。
    """
    try:
        if role == "user":
            content = extract_original_message(content)
        message = {"role": role, "content": content}
        r = get_redis()
        await r.rpush(memory_key(user_id, session_id), json.dumps(message, ensure_ascii=False))
        _schedule_flush(user_id, session_id)
    except Exception:
        logger.exception("更新对话历史失败")


async def delete_memory(user_id: int, session_id: str) -> None:
    """删除一个会话的 Redis 记忆（删除会话时调用，对齐原版 deleteMessages）。"""
    try:
        await get_redis().delete(memory_key(user_id, session_id))
    except Exception:
        logger.exception("删除对话历史失败")


def _schedule_flush(user_id: int, session_id: str) -> None:
    """调度"静默 N 秒后落库"任务（去抖：新消息总会取消旧任务，只留最后一个）。

    为什么去抖就够？原版靠 "size > num 则跳过" 处理并发任务竞争；
    这里直接让同一会话只有一个未触发任务，语义更清晰，效果一致：
    用户连续聊天时任务不断被推迟，静默 10 秒后才真正落库。
    """
    key = memory_key(user_id, session_id)
    old = _flush_tasks.pop(key, None)
    if old and not old.done():
        old.cancel()  # 取消上一个待触发的任务
    _flush_tasks[key] = asyncio.create_task(_flush_later(user_id, session_id))


async def _flush_later(user_id: int, session_id: str) -> None:
    """延迟落库任务：静默期结束后，把 Redis 里的全部消息写入 MySQL 再清空。

    对齐原版 handleTask：
    1) 会话已被删除 -> 不落库；
    2) segment_index 续接上一次落库的最大值（跨批次递增，永不重复）；
    3) 每条消息一行，content 就是消息 JSON；
    4) 写库成功后删掉 Redis key（下次对话从空列表重新积累）。
    """
    try:
        await asyncio.sleep(settings.MEMORY_FLUSH_DELAY)
        r = get_redis()
        key = memory_key(user_id, session_id)
        message_list = await r.lrange(key, 0, -1)
        if not message_list:
            return

        with SessionLocal() as db:
            # 会话已删除则放弃落库（原版同款判断）
            exists = db.query(UserSession.id).filter(UserSession.session_id == session_id).first()
            if not exists:
                logger.info("会话 %s 已删除，跳过落库", session_id)
                return

            # 续接序号：取该会话已落库的最大 segment_index
            last = (
                db.query(ChatSession)
                .filter(ChatSession.user_id == user_id, ChatSession.session_id == session_id)
                .order_by(ChatSession.segment_index.desc())
                .first()
            )
            index = (last.segment_index + 1) if last else 0

            for offset, content in enumerate(message_list):
                db.add(
                    ChatSession(
                        user_id=user_id,
                        session_id=session_id,
                        segment_index=index + offset,
                        content=content,
                        create_time=datetime.now(),
                    )
                )
            db.commit()

        await r.delete(key)
        logger.info("延迟落库完成：%s，共 %d 条消息（起始序号 %d）", key, len(message_list), index)
    except asyncio.CancelledError:
        # 被新消息去抖取消，正常流程
        raise
    except Exception:
        logger.exception("延迟落库失败：%s", session_id)
