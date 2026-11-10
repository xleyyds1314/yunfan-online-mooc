"""导入平台全部课程到向量知识库（课程知识库集合）。

用法：
    python scripts/import_courses_to_rag.py

会从搜索服务拉取全部课程列表，再逐个调课程服务的 baseInfo 拿详情
（introduce/usePeople/detail 等），最后写入 ChromaDB 的 yunfan_docs_courses 集合。
"""

import sys
from pathlib import Path

# 让脚本能 import app.*
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import httpx

from app.config import settings
from app.services import rag

SEARCH_URL = "http://127.0.0.1:8083/courses/portal"  # 直连搜索服务（不经网关）
COURSE_BASE_URL = "http://127.0.0.1:8086/courses"     # 直连课程服务


def fetch_all_courses() -> list[dict]:
    """从搜索服务拉取全部课程（搜索服务直连返回 {total, list}）。"""
    resp = httpx.get(SEARCH_URL, params={"keyword": "", "pageNo": 1, "pageSize": 100}, timeout=10)
    resp.raise_for_status()
    data = resp.json()
    return data.get("list") or []


def fetch_course_detail(course_id) -> dict:
    """调课程服务 baseInfo 拿课程详情（introduce/usePeople/detail/cateNames 等）。"""
    try:
        resp = httpx.get(f"{COURSE_BASE_URL}/baseInfo/{course_id}", timeout=10)
        resp.raise_for_status()
        data = resp.json()
        # 课程服务直连返回就是 VO（网关才包 code/data）
        return data if isinstance(data, dict) else data.get("data", data)
    except Exception as e:
        print(f"  [WARN] 课程 {course_id} 详情获取失败：{e}")
        return {}


def main():
    print("1) 拉取课程列表...")
    courses = fetch_all_courses()
    print(f"   共 {len(courses)} 门课程")

    print("2) 拉取每门课程详情...")
    enriched = []
    for i, c in enumerate(courses, 1):
        cid = c.get("id")
        detail = fetch_course_detail(cid)
        merged = {**c, **detail}
        enriched.append(merged)
        name = merged.get("name", "")
        print(f"   [{i}/{len(courses)}] {cid} {name}")

    print("3) 写入向量知识库...")
    count = rag.add_courses(enriched)
    print(f"   成功导入 {count} 门课程到课程知识库")

    # 验证：搜一下
    print("4) 验证检索...")
    hits = rag.search_courses("java 编程", top_k=3)
    for h in hits:
        print(f"   score={h['score']:.4f} {h['name']} (id={h['course_id']})")


if __name__ == "__main__":
    main()
