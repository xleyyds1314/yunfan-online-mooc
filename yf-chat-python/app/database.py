"""【数据库层】创建 SQLAlchemy 引擎与本地会话工厂。

对应原 Java 项目的 MyBatis-Plus 数据源配置。SQLAlchemy 2.0 的三层概念：
    Engine      —— 连接池（全局一个，管所有 MySQL 连接）
    Session     —— 一次数据库"工作单元"（查询/事务），用完即关
    Base        —— 所有 ORM 模型的声明基类（models/ 里的表都继承它）
"""

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

from app.config import settings

# pool_pre_ping=True：每次取连接前先 ping 一下，MySQL 断连后能自动恢复（8 小时空闲断连问题）
engine = create_engine(settings.DATABASE_URL, pool_pre_ping=True)

# sessionmaker：Session 的工厂；expire_on_commit=False 让提交后对象属性还能读（不回查库）
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)


class Base(DeclarativeBase):
    """所有 ORM 模型的公共基类。"""


def get_db():
    """FastAPI 依赖：每个请求拿一个独立 Session，用完自动关闭。

    用法：db: Session = Depends(get_db)
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
