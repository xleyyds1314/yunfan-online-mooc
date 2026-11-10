"""【RAG 知识库服务】Markdown 切分 + ChromaDB 向量入库/检索 —— 替代原 Qdrant 方案。

对应原 Java 代码：
    MarkdownSplitter.java      -> 本文件"切分"部分的三个函数
    MarkdownDocsServiceImpl    -> add_document / delete_by_doc / search
    QdrantConfig + QdrantClient-> chromadb.HttpClient（连 Docker 里的 chroma 容器）

整条链路：
    上传 .md --切分(按标题 level)--> 文本块 --硅基流动 bge-m3--> 向量 --存--> ChromaDB
    提问   --同一个 embedding 模型--> 向量 --余弦相似度--> Top-K 片段 --拼进提示词--> 模型

关键点：
    1) 写入和查询必须用【同一个 embedding 模型】，否则向量空间不同、检索全错；
    2) metadata 里带 user_id / doc_id，检索时 where 过滤 —— 实现多用户知识隔离
       （原版 Qdrant 的 matchKeyword("user_id", ...) 就是同一件事）。
"""

import logging
import re
from dataclasses import dataclass
from typing import List, Optional

from app.config import settings

logger = logging.getLogger(__name__)

# ---- 模块级单例：chroma 客户端与集合只初始化一次 ----
_client = None
_collection = None
_course_collection = None
_embedding_function = None

# 代码围栏行（``` 开头），其中的 "# xxx" 不是标题，切分时要跳过
_FENCE_RE = re.compile(r"^\s*(```|~~~)")
_HEADING_RE = re.compile(r"^(#{1,6})\s+(.*)$")


@dataclass
class MarkdownChunk:
    """一个文本块 = 一个标题 + 标题下的正文。

    对应原 Java 的 MarkdownChunk(title, content)；
    存入向量库的文本格式统一为 "# {title}\\n\\n{content}"，检索时还能反解。
    """

    title: str
    content: str

    def to_text(self) -> str:
        """入库格式：标题做前缀，保留章节语境（提升检索与阅读质量）。"""
        if self.title:
            return f"# {self.title}\n\n{self.content}"
        return self.content

    @classmethod
    def from_text(cls, text: str) -> "MarkdownChunk":
        """从入库文本反解出标题与正文（供 /file/chat 返回 title/content）。"""
        parts = text.split("\n\n", 1)
        first = parts[0].strip() if parts else ""
        if first.startswith("#"):
            heading_re = re.match(r"^#{1,6}\s+(.*)$", first)
            title = heading_re.group(1).strip() if heading_re else first.lstrip("#").strip()
            rest = parts[1].strip() if len(parts) > 1 else ""
            return cls(title=title, content=rest)
        # 没有标题的块（整文档一块的情况）
        return cls(title="", content=text.strip())


# =============================================================================
# 一、Markdown 切分（对应 MarkdownSplitter.java）
# =============================================================================


def _iter_logical_lines(markdown: str):
    """逐行产出 (是否在代码块内, 行文本)，避免把代码里的 `# 注释` 当标题。"""
    in_fence = False
    for line in markdown.splitlines():
        if _FENCE_RE.match(line):
            in_fence = not in_fence
        yield in_fence, line


def find_max_heading_level(markdown: str) -> int:
    """找出文档里最大的标题级别（# 是 1，###### 是 6；没有标题返回 0）。"""
    max_level = 0
    for in_fence, line in _iter_logical_lines(markdown):
        if in_fence:
            continue
        match = _HEADING_RE.match(line)
        if match:
            max_level = max(max_level, len(match.group(1)))
    return max_level


def split_by_heading(markdown: str, level: int) -> List[MarkdownChunk]:
    """按指定标题级别切块（对应 getMarkdownChunksByH）。

    规则与原版 flexmark 一致：
    - 遇到 level 级标题 -> 开一个新块，标题记为 current_title；
    - 其它行（包括更低级的标题行）只要"已经有 current_title"就追加进正文；
    - 第一个 level 级标题之前的内容（无归属）丢弃 —— 与原版行为相同；
    - 代码围栏内的 `#` 行不是标题。
    """
    chunks: List[MarkdownChunk] = []
    current_title: Optional[str] = None
    current_lines: List[str] = []

    for in_fence, line in _iter_logical_lines(markdown):
        match = _HEADING_RE.match(line)
        if match and len(match.group(1)) == level and not in_fence:
            # 封存上一个块
            if current_title is not None:
                chunks.append(MarkdownChunk(current_title, "\n".join(current_lines).strip()))
                current_lines = []
            current_title = match.group(2).strip()
        elif current_title is not None:
            current_lines.append(line)

    # 收尾：最后一个块
    if current_title is not None:
        content = "\n".join(current_lines).strip()
        if content:
            chunks.append(MarkdownChunk(current_title, content))
    return chunks


def smart_split(markdown: str) -> List[MarkdownChunk]:
    """智能切分（对应 smartSplitByHeading）：没有标题就把整篇文档当一块。"""
    max_level = find_max_heading_level(markdown)
    if max_level == 0:
        return [MarkdownChunk("", markdown.strip())]
    return split_by_heading(markdown, max_level)


# =============================================================================
# 二、ChromaDB 客户端（对应 QdrantConfig + QdrantClient）
# =============================================================================


def _get_embedding_function():
    """硅基流动在线 Embedding（BAAI/bge-m3），兼容 OpenAI 协议。

    注意：api_base 不要拼 /embeddings，chromadb 会自动加。
    写入与检索都用它，保证向量空间一致。
    """
    global _embedding_function
    if _embedding_function is None:
        # 延迟 import：首次真正用到才加载 chromadb，加快服务启动
        from chromadb.utils.embedding_functions import OpenAIEmbeddingFunction

        _embedding_function = OpenAIEmbeddingFunction(
            api_key=settings.SILICONFLOW_API_KEY,
            api_base=settings.SILICONFLOW_BASE_URL,
            model_name=settings.EMBEDDING_MODEL,
        )
        logger.info("知识库向量模型：硅基流动在线 %s", settings.EMBEDDING_MODEL)
    return _embedding_function


def get_collection():
    """获取（或创建）ChromaDB 集合 —— HttpClient 连 Docker 里的 chroma 服务。"""
    global _client, _collection
    if _collection is None:
        import chromadb

        _client = chromadb.HttpClient(
            host=settings.CHROMA_HOST, port=settings.CHROMA_PORT
        )
        _collection = _client.get_or_create_collection(
            name=settings.CHROMA_COLLECTION,
            embedding_function=_get_embedding_function(),
            metadata={"hnsw:space": "cosine"},  # 余弦距离，适合文本语义
        )
        logger.info("已连接 ChromaDB：%s:%s 集合 %s", settings.CHROMA_HOST, settings.CHROMA_PORT, settings.CHROMA_COLLECTION)
    return _collection


def get_course_collection():
    """获取（或创建）课程知识库集合 —— 平台所有课程的向量库，全局共享（不按用户隔离）。"""
    global _client, _course_collection
    if _course_collection is None:
        import chromadb

        if _client is None:
            _client = chromadb.HttpClient(
                host=settings.CHROMA_HOST, port=settings.CHROMA_PORT
            )
        _course_collection = _client.get_or_create_collection(
            name=settings.CHROMA_COLLECTION + "_courses",
            embedding_function=_get_embedding_function(),
            metadata={"hnsw:space": "cosine"},
        )
        logger.info("已连接课程知识库集合：%s", settings.CHROMA_COLLECTION + "_courses")
    return _course_collection


# =============================================================================
# 三、入库 / 删除 / 检索（对应 MarkdownDocsServiceImpl 的 saveSegment 等）
# =============================================================================


def add_document(user_id: int, doc_id: int, markdown: str, level: int) -> int:
    """把一篇 Markdown 切分、向量化并写入 ChromaDB，返回写入的块数。

    注意一个对原版的小修正：原版 split 结果为空时调用了 smartSplitByHeading
    却没有使用返回值（无标题文档实际什么都存不进去）；这里顺手修复：
    按指定级别切不出来时，退回 smart_split（整文档一块）。

    本函数是同步实现（embedding 是 HTTP 调用、较耗时）：
    异步接口里请用 `await asyncio.to_thread(rag.add_document, ...)` 调用，
    避免阻塞事件循环。
    """
    chunks = split_by_heading(markdown, level) or smart_split(markdown)
    if not chunks:
        return 0

    collection = get_collection()
    documents = [chunk.to_text() for chunk in chunks]
    ids = [f"{doc_id}:{i}" for i in range(len(chunks))]  # 块 id：{doc_id}:0、{doc_id}:1...
    metadatas = [{"user_id": str(user_id), "doc_id": str(doc_id)} for _ in chunks]
    collection.add(documents=documents, ids=ids, metadatas=metadatas)
    logger.info("文档 %s 入库成功，共 %d 块", doc_id, len(chunks))
    return len(chunks)


def delete_by_doc(doc_id: int) -> None:
    """按 doc_id 删除该文档的全部向量（更新/删除文档时调用）。"""
    collection = get_collection()
    collection.delete(where={"doc_id": str(doc_id)})
    logger.info("已删除文档 %s 的全部向量", doc_id)


def search(user_id: int, message: str, top_k: Optional[int] = None) -> List[dict]:
    """向量检索：按 user_id 过滤，返回 Top-K 相似片段。

    返回元素：{"score", "title", "content", "text", "doc_id"}
    对应原版 qdrantClient.search + matchKeyword("user_id", ...) + limit(3)。
    """
    top_k = top_k or settings.RAG_TOP_K
    collection = get_collection()
    result = collection.query(
        query_texts=[message],
        n_results=top_k,
        where={"user_id": str(user_id)},  # 多用户隔离：只搜自己的知识库
    )

    hits: List[dict] = []
    documents = (result.get("documents") or [[]])[0]
    distances = (result.get("distances") or [[]])[0]
    metadatas = (result.get("metadatas") or [[]])[0]
    for doc, dist, meta in zip(documents, distances, metadatas):
        chunk = MarkdownChunk.from_text(doc)
        hits.append(
            {
                "score": 1 - dist,  # 余弦距离转相似度，越接近 1 越相关
                "title": chunk.title,
                "content": chunk.content,
                "text": doc,
                "doc_id": (meta or {}).get("doc_id", ""),
            }
        )
    for hit in hits:
        logger.info("检索命中 score=%.4f title=%s", hit["score"], hit["title"])
    return hits


# =============================================================================
# 四、课程知识库（平台课程全局共享，不按用户隔离）
# =============================================================================


def _course_to_text(course: dict) -> str:
    """把课程信息拼成一段自然语言文本，方便向量化检索。"""
    name = course.get("name", "")
    introduce = course.get("introduce", "") or ""
    use_people = course.get("usePeople", "") or ""
    detail = course.get("detail", "") or ""
    price = course.get("price", 0)
    price_yuan = (price or 0) / 100
    teacher = course.get("teacher", "") or ""
    sections = course.get("sections", 0) or 0
    cate_names = course.get("cateNames", "") or ""
    lines = [f"课程名称：{name}"]
    if cate_names:
        lines.append(f"分类：{cate_names}")
    lines.append(f"价格：{'免费' if price == 0 else f'{price_yuan}元'}")
    if teacher:
        lines.append(f"授课老师：{teacher}")
    if sections:
        lines.append(f"章节数：{sections}")
    if use_people:
        lines.append(f"适合人群：{use_people}")
    if introduce:
        lines.append(f"课程介绍：{introduce}")
    if detail and detail != introduce:
        lines.append(f"课程详情：{detail}")
    return "\n".join(lines)


def add_courses(courses: List[dict]) -> int:
    """批量导入课程到课程知识库，返回写入数量。

    每门课作为一个文档块入库，id 用课程 id，metadata 带课程 id 方便回查。
    已存在的课程会被覆盖（先删后加）。
    """
    collection = get_course_collection()
    documents = []
    ids = []
    metadatas = []
    for c in courses:
        cid = str(c.get("id"))
        if not cid:
            continue
        documents.append(_course_to_text(c))
        ids.append(cid)
        metadatas.append({
            "course_id": cid,
            "name": c.get("name", ""),
            "price": str(c.get("price", 0)),
        })
    # 先删除已存在的（幂等：重复导入不会产生重复向量）
    try:
        collection.delete(ids=ids)
    except Exception:
        pass
    if not documents:
        return 0
    collection.add(documents=documents, ids=ids, metadatas=metadatas)
    logger.info("课程知识库导入 %d 门课程", len(documents))
    return len(documents)


def search_courses(message: str, top_k: int = 5) -> List[dict]:
    """在课程知识库里检索相关课程，返回 [{score, course_id, name, price, text}]。"""
    collection = get_course_collection()
    result = collection.query(
        query_texts=[message],
        n_results=top_k,
    )
    hits: List[dict] = []
    documents = (result.get("documents") or [[]])[0]
    distances = (result.get("distances") or [[]])[0]
    metadatas = (result.get("metadatas") or [[]])[0]
    for doc, dist, meta in zip(documents, distances, metadatas):
        meta = meta or {}
        hits.append({
            "score": 1 - dist,
            "course_id": meta.get("course_id", ""),
            "name": meta.get("name", ""),
            "price": meta.get("price", "0"),
            "text": doc,
        })
    return hits


def list_all_courses_in_rag() -> List[dict]:
    """枚举课程知识库里的全部课程（用于用户问"全部课程"这类意图时）。"""
    collection = get_course_collection()
    count = collection.count()
    result = collection.get(include=["documents", "metadatas"])
    hits: List[dict] = []
    documents = result.get("documents") or []
    metadatas = result.get("metadatas") or []
    for doc, meta in zip(documents, metadatas):
        meta = meta or {}
        hits.append({
            "score": 1.0,
            "course_id": meta.get("course_id", ""),
            "name": meta.get("name", ""),
            "price": meta.get("price", "0"),
            "text": doc,
        })
    logger.info("枚举全部课程知识库：%d 门", len(hits))
    return hits


def search(user_id: int, message: str, top_k: Optional[int] = None) -> List[dict]:
    """向量检索：先搜用户个人知识库，再搜课程知识库，合并返回。"""
    user_hits = _search_user_docs(user_id, message, top_k)
    # 课程知识库额外搜一些（用更低阈值或固定 top_k）
    course_hits = []
    try:
        course_hits = search_courses(message, top_k=3)
    except Exception as e:
        logger.warning("课程知识库检索失败：%s", e)
    # 给课程结果打上 type 标记，方便提示词区分
    for hit in course_hits:
        hit["type"] = "course"
    for hit in user_hits:
        hit["type"] = hit.get("type", "doc")
    all_hits = user_hits + course_hits
    all_hits.sort(key=lambda x: x.get("score", 0), reverse=True)
    return all_hits


def _search_user_docs(user_id: int, message: str, top_k: Optional[int] = None) -> List[dict]:
    """用户个人知识库检索（原 search 逻辑，拆出供合并调用）。"""
    top_k = top_k or settings.RAG_TOP_K
    collection = get_collection()
    result = collection.query(
        query_texts=[message],
        n_results=top_k,
        where={"user_id": str(user_id)},
    )
    hits: List[dict] = []
    documents = (result.get("documents") or [[]])[0]
    distances = (result.get("distances") or [[]])[0]
    metadatas = (result.get("metadatas") or [[]])[0]
    for doc, dist, meta in zip(documents, distances, metadatas):
        chunk = MarkdownChunk.from_text(doc)
        hits.append({
            "score": 1 - dist,
            "title": chunk.title,
            "content": chunk.content,
            "text": doc,
            "doc_id": (meta or {}).get("doc_id", ""),
        })
    for hit in hits:
        logger.info("用户知识库命中 score=%.4f title=%s", hit["score"], hit["title"])
    return hits
