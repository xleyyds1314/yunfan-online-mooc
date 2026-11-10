"""【会话管理接口】/session 系列 —— 对应原 UserSessionController.java。"""

import logging
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException

from app.api.deps import get_user_id
from app.database import SessionLocal
from app.models import UserSession
from app.schemas import UserSessionCreate, UserSessionUpdate
from app.services import memory

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/session", tags=["用户会话接口"])


@router.post("", summary="创建用户会话关联")
def create_session(dto: UserSessionCreate, user_id: int = Depends(get_user_id)):
    """前端每开一个新会话，先调这里拿会话列表展示。"""
    with SessionLocal() as db:
        row = UserSession(
            user_id=user_id,
            name=dto.name,
            tag=dto.tag,
            session_id=dto.sessionId,
            create_time=datetime.now(),
        )
        db.add(row)
        db.commit()
        db.refresh(row)
        return {
            "id": row.id,
            "userId": row.user_id,
            "name": row.name,
            "tag": row.tag,
            "sessionId": row.session_id,
            "createTime": row.create_time.strftime("%Y-%m-%d %H:%M:%S"),
        }


@router.put("/{id}", summary="修改用户会话关联")
def update_session(id: int, dto: UserSessionUpdate, user_id: int = Depends(get_user_id)):
    """改名/改标签（只允许改自己的会话）。"""
    with SessionLocal() as db:
        row = db.query(UserSession).filter(UserSession.id == id, UserSession.user_id == user_id).first()
        if not row:
            raise HTTPException(status_code=400, detail="会话不存在")
        if dto.name is not None:
            row.name = dto.name
        if dto.tag is not None:
            row.tag = dto.tag
        db.commit()
        return {"msg": "ok"}


@router.get("/list", summary="查询用户会话列表")
def list_sessions(user_id: int = Depends(get_user_id)):
    with SessionLocal() as db:
        rows = (
            db.query(UserSession)
            .filter(UserSession.user_id == user_id)
            .order_by(UserSession.id.desc())
            .all()
        )
        return [
            {
                "id": row.id,
                "userId": row.user_id,
                "name": row.name,
                "tag": row.tag,
                "sessionId": row.session_id,
                "createTime": row.create_time.strftime("%Y-%m-%d %H:%M:%S")
                if row.create_time
                else None,
            }
            for row in rows
        ]


@router.delete("/{id}", summary="根据ID删除用户会话关联")
async def delete_session(id: int, user_id: int = Depends(get_user_id)):
    """删会话时同步清掉 Redis 里的对话记忆（对应原版 deleteMessages 的语义）。"""
    with SessionLocal() as db:
        row = db.query(UserSession).filter(UserSession.id == id, UserSession.user_id == user_id).first()
        if not row:
            raise HTTPException(status_code=400, detail="会话不存在")
        session_id = row.session_id
        db.delete(row)
        db.commit()
    await memory.delete_memory(user_id, session_id)
    return {"msg": "ok"}
