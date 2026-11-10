"""【知识库接口】/file 系列 —— 对应原 MarkdownController.java（文件/文档接口）。

完整链路：上传 -> 校验(.md 且 ≤2MB) -> 存 MySQL -> 切分 -> 向量化 -> 存 ChromaDB
检索：问题向量化 -> user_id 过滤 -> 余弦 Top-K
更新/删除：先动向量库，再动 MySQL（与原版顺序一致）
"""

import asyncio
import logging
from datetime import datetime

import httpx
from fastapi import APIRouter, Depends, HTTPException, UploadFile
from fastapi.responses import PlainTextResponse

from app.api.deps import get_user_id
from app.config import settings
from app.database import SessionLocal
from app.models import MarkdownDocs
from app.schemas import MarkdownChunkVO, MarkdownDocsUpdate, PageDTO
from app.services import rag

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/file", tags=["知识库接口"])

# 课程服务地址（与 query_course 工具共用 COURSE_SERVICE_BASE_URL）
_SEARCH_URL = (settings.COURSE_SERVICE_BASE_URL or "http://127.0.0.1:8083").rstrip("/")
_COURSE_URL = "http://127.0.0.1:8086"  # 课程服务直连


def _doc_dict(row: MarkdownDocs, with_content: bool = True) -> dict:
    """ORM 行转 dict（camelCase 对齐前端习惯）。"""
    data = {
        "id": row.id,
        "userId": row.user_id,
        "fileName": row.file_name,
        "level": row.level,
        "createTime": row.create_time.strftime("%Y-%m-%d %H:%M:%S") if row.create_time else None,
        "updateTime": row.update_time.strftime("%Y-%m-%d %H:%M:%S") if row.update_time else None,
    }
    if with_content:
        data["content"] = row.content
    return data


@router.post("/upload", summary="上传文件到知识库")
async def upload_markdown(
    file: UploadFile,
    level: int = 2,
    user_id: int = Depends(get_user_id),
):
    """上传 .md 文档（≤2MB），入库并完成向量化。level=按几级标题切分（2=##，3=###）。"""
    file_name = file.filename or ""
    if not file_name.lower().endswith(".md"):
        raise HTTPException(status_code=400, detail="只支持上传 .md 格式的文件")

    content_bytes = await file.read()
    if len(content_bytes) > 2 * 1024 * 1024:
        raise HTTPException(status_code=400, detail="文件大小不能超过2MB")
    markdown_content = content_bytes.decode("utf-8")

    # 1) 存 MySQL
    with SessionLocal() as db:
        row = MarkdownDocs(
            user_id=user_id,
            file_name=file_name,
            content=markdown_content,
            level=level,
            create_time=datetime.now(),
            update_time=datetime.now(),
        )
        db.add(row)
        db.commit()
        db.refresh(row)
        doc_id = row.id

    # 2) 切分 + 向量化 + 存 ChromaDB（embedding 是 HTTP 调用，放线程池）
    try:
        chunk_count = await asyncio.to_thread(rag.add_document, user_id, doc_id, markdown_content, level)
    except Exception as exc:
        # 向量入库失败则回滚 MySQL 行，避免出现"有文档却搜不到"的脏数据
        with SessionLocal() as db:
            db.query(MarkdownDocs).filter(MarkdownDocs.id == doc_id).delete()
            db.commit()
        raise HTTPException(status_code=400, detail=f"文件向量化失败：{exc}") from exc

    return {"chunkCount": chunk_count, **_doc_dict(row)}


@router.get("/chat", summary="根据知识库内容检索（不上模型，直接看命中片段）")
async def chat_by_markdown_doc(message: str, user_id: int = Depends(get_user_id)):
    """调试 RAG 检索效果用：返回 Top-K 片段的 score / title / content。"""
    hits = await asyncio.to_thread(rag.search, user_id, message)
    if not hits:
        raise HTTPException(status_code=400, detail="当前您知识库似乎还没有相关内容，快去上传吧~")
    return [
        MarkdownChunkVO(score=hit["score"], title=hit["title"], content=hit["content"])
        for hit in hits
    ]


@router.get("/page", summary="分页查询用户知识库文件列表")
def query_markdown_page(
    pageNo: int = 1, pageSize: int = 10, user_id: int = Depends(get_user_id)
) -> PageDTO:
    """列表不含 content（大字段），需要看全文用 /file/{id}。"""
    with SessionLocal() as db:
        query = db.query(MarkdownDocs).filter(MarkdownDocs.user_id == user_id)
        total = query.count()
        rows = (
            query.order_by(MarkdownDocs.id.desc())
            .offset((pageNo - 1) * pageSize)
            .limit(pageSize)
            .all()
        )
        items = [_doc_dict(row, with_content=False) for row in rows]
    return PageDTO(total=total, pages=(total + pageSize - 1) // pageSize, list=items)


@router.get("/{file_id}", summary="根据文件id查看文件内容", response_class=PlainTextResponse)
def get_markdown(file_id: int, user_id: int = Depends(get_user_id)):
    with SessionLocal() as db:
        row = (
            db.query(MarkdownDocs)
            .filter(MarkdownDocs.id == file_id, MarkdownDocs.user_id == user_id)
            .first()
        )
        if not row:
            raise HTTPException(status_code=400, detail="文件不存在")
        return row.content or ""


@router.put("/update", summary="更新文件内容")
async def update_markdown(dto: MarkdownDocsUpdate, user_id: int = Depends(get_user_id)):
    """更新原文后要重新切分、重新嵌入：先删旧向量，再写新向量。"""
    with SessionLocal() as db:
        row = (
            db.query(MarkdownDocs)
            .filter(MarkdownDocs.id == dto.id, MarkdownDocs.user_id == user_id)
            .first()
        )
        if not row:
            raise HTTPException(status_code=400, detail="文件不存在")
        row.content = dto.content
        row.level = dto.level
        row.update_time = datetime.now()
        db.commit()

    # 向量库：先删后加（doc_id 不变）
    await asyncio.to_thread(rag.delete_by_doc, dto.id)
    chunk_count = await asyncio.to_thread(rag.add_document, user_id, dto.id, dto.content, dto.level)
    return {"msg": "更新成功", "chunkCount": chunk_count}


@router.delete("/{file_id}", summary="根据文件id删除文件")
async def delete_markdown(file_id: int, user_id: int = Depends(get_user_id)):
    with SessionLocal() as db:
        row = (
            db.query(MarkdownDocs)
            .filter(MarkdownDocs.id == file_id, MarkdownDocs.user_id == user_id)
            .first()
        )
        if not row:
            raise HTTPException(status_code=400, detail="文件不存在")
        db.delete(row)
        db.commit()
    # 删向量（原版 deleteSegment：按 doc_id 过滤删除）
    await asyncio.to_thread(rag.delete_by_doc, file_id)
    return {"msg": "ok"}


# =============================================================================
# 课程知识库（平台课程全局共享）
# =============================================================================


@router.post("/courses/import", summary="导入平台全部课程到课程向量知识库")
async def import_courses_to_rag():
    """从搜索服务拉取全部课程 + 课程服务详情，写入课程知识库集合（幂等：重复调用会覆盖）。"""
    def _do_import() -> int:
        # 1) 拉课程列表
        resp = httpx.get(f"{_SEARCH_URL}/courses/portal", params={"keyword": "", "pageNo": 1, "pageSize": 100}, timeout=10)
        resp.raise_for_status()
        data = resp.json()
        courses = data.get("list") or data.get("data", {}).get("list") or []
        # 2) 拉每门课详情
        enriched = []
        for c in courses:
            cid = c.get("id")
            try:
                d = httpx.get(f"{_COURSE_URL}/courses/baseInfo/{cid}", timeout=10)
                d.raise_for_status()
                detail = d.json()
                detail = detail if isinstance(detail, dict) else detail.get("data", detail)
                enriched.append({**c, **detail})
            except Exception as exc:
                logger.warning("课程 %s 详情获取失败：%s", cid, exc)
                enriched.append(c)
        # 3) 写向量库
        return rag.add_courses(enriched)

    count = await asyncio.to_thread(_do_import)
    return {"msg": "ok", "imported": count}


@router.get("/courses/search", summary="在课程知识库中检索（调试用）")
async def search_courses_in_rag(message: str, top_k: int = 5):
    """直接看课程向量库的命中结果，不经过大模型。"""
    hits = await asyncio.to_thread(rag.search_courses, message, top_k)
    return hits
