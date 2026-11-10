"""【数据模型层】三张表的 ORM 定义，与 sql/yf_chat.sql 一一对应。

对应原 Java 项目 domain/po 下的实体类（MyBatis-Plus @TableName）。
字段用 SQLAlchemy 2.0 的 Mapped[类型] 注解写法，类型即文档。
"""

from datetime import datetime
from typing import Optional

from sqlalchemy import BigInteger, DateTime, Integer, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from app.database import Base


class ChatSession(Base):
    """聊天记录表：一条记录 = 一条消息（Redis 静默后延迟落库到这里）。"""

    __tablename__ = "chat_session"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(BigInteger, comment="用户id")
    session_id: Mapped[str] = mapped_column(String(64), comment="会话id")
    segment_index: Mapped[int] = mapped_column(Integer, default=0, comment="消息序号，跨批次递增")
    content: Mapped[Optional[str]] = mapped_column(Text, comment='单条消息 JSON {"role","content"}')
    create_time: Mapped[Optional[datetime]] = mapped_column(DateTime)


class UserSession(Base):
    """会话元信息表：左侧会话列表，每开一个新会话插一行。"""

    __tablename__ = "user_session"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(BigInteger, comment="用户id")
    name: Mapped[Optional[str]] = mapped_column(String(128), comment="会话名称")
    tag: Mapped[Optional[str]] = mapped_column(String(32), comment="会话标签")
    session_id: Mapped[str] = mapped_column(String(64), comment="会话id")
    create_time: Mapped[Optional[datetime]] = mapped_column(DateTime)


class MarkdownDocs(Base):
    """知识库文档表：存 Markdown 原文；切分后的向量存 ChromaDB。"""

    __tablename__ = "user_markdown_docs"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(BigInteger, comment="用户id")
    file_name: Mapped[Optional[str]] = mapped_column(String(255), comment="文件名")
    content: Mapped[Optional[str]] = mapped_column(Text(), comment="Markdown 原文")
    level: Mapped[Optional[int]] = mapped_column(Integer, default=2, comment="标题切割等级")
    create_time: Mapped[Optional[datetime]] = mapped_column(DateTime)
    update_time: Mapped[Optional[datetime]] = mapped_column(DateTime)
