"""【配置层】读取 .env 里的环境变量，以类型安全的方式暴露给全项目。

对应原 Java 项目的 application.yml + @Value / @ConfigurationProperties。
区别：Python 这边一个 Settings 类搞定，字段即校验规则，缺配置启动时就报错。

用法：任何模块都 from app.config import settings，不要自己读 .env。
"""

from pathlib import Path

# pydantic-settings：Pydantic 的配置扩展库，BaseSettings 会自动把"同名字段"
# 从环境变量 / .env 文件中加载进来，并用类型注解做校验。
from pydantic_settings import BaseSettings, SettingsConfigDict

# BASE_DIR = yf-ai-chat/ 项目根（app/config.py 的上两级）
# .env 的查找、SQL 脚本路径等都以它为基准，避免硬编码绝对路径。
BASE_DIR = Path(__file__).resolve().parent.parent


class Settings(BaseSettings):
    # ---- 数据库 / 缓存 / 向量库连接 ----
    DATABASE_URL: str = "mysql+pymysql://root:123456@127.0.0.1:3306/yf_chat"
    REDIS_URL: str = "redis://127.0.0.1:6379/0"

    CHROMA_HOST: str = "127.0.0.1"          # Docker 里的 chroma 容器地址
    CHROMA_PORT: int = 8000
    CHROMA_COLLECTION: str = "yunfan_docs"  # 集合名（相当于 Qdrant 的 collection）

    # ---- 大模型（OpenAI 兼容协议；这里配 DeepSeek）----
    # 对应原 AiConfig 里的 qwenChatModel / qwenStreamingChatModel。
    LLM_API_KEY: str = ""
    LLM_BASE_URL: str = "https://api.deepseek.com"
    LLM_MODEL: str = "deepseek-chat"

    # ---- 硅基流动在线 Embedding（把文本转向量）----
    # Embedding 走在线 API，中文效果好且零本地依赖。
    SILICONFLOW_API_KEY: str = ""
    SILICONFLOW_BASE_URL: str = "https://api.siliconflow.cn/v1"
    EMBEDDING_MODEL: str = "BAAI/bge-m3"

    # ---- 业务参数 ----
    RAG_TOP_K: int = 3                  # /chat/file 检索条数（原版固定 top3）
    MEMORY_MAX_MESSAGES: int = 50       # 喂给模型的记忆条数上限（原版窗口 500，这里适当收紧省 token）
    MEMORY_FLUSH_DELAY: float = 10.0    # 静默多少秒后把 Redis 记忆落库（原版延迟队列 10 秒）

    # 课程查询工具的真实服务地址；留空则 query_course 返回内置 mock 数据
    COURSE_SERVICE_BASE_URL: str = ""

    model_config = SettingsConfigDict(
        env_file=BASE_DIR / ".env",     # .env 位置：yf-ai-chat/.env
        env_file_encoding="utf-8",
    )


# 模块加载时实例化一次，全项目共享同一个配置对象。
settings = Settings()
