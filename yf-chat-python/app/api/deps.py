"""【公共依赖】用户身份解析，两种来源（优先级从高到低）：

1. `user-info` 请求头：云帆学堂网关（yf-gateway 的 AccountAuthFilter）校验 JWT 后注入，
   与 Java 微服务的 UserContext.getUser() 读取的是同一个头 —— 生产环境走这条路；
2. `X-User-Id` 请求头：本服务独立调试时用（Swagger 里直接填），不传默认 1（demo 用户）。

这样无论"经网关 /ct/** 转发"还是"直连 :8094 调试"，身份逻辑都一致。
"""

from typing import Optional

from fastapi import Header


def get_user_id(
    user_info: Optional[str] = Header(default=None, description="网关注入的用户id（yf-gateway）"),
    x_user_id: str = Header(default="1", description="独立调试用的用户id"),
) -> int:
    # 网关注入优先（FastAPI 会把 user-info 头映射到 user_info 参数）
    raw = user_info if user_info else x_user_id
    try:
        return int(raw)
    except (ValueError, TypeError):
        return 1

