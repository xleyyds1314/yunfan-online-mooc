-- =============================================================================
-- 【建库建表脚本】yf-ai-chat（与原 yf-chat 模块三张表保持同构）
-- 执行方式：mysql -uroot -p123456 < sql/yf_chat.sql
--           或在 Navicat / DataGrip 里直接跑一遍
-- =============================================================================

CREATE DATABASE IF NOT EXISTS yf_chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE yf_chat;

-- ----------------------------------------------------------------------------
-- 1. chat_session：聊天记录分片表（延迟落库的目标表）
--    设计：Redis 里存最近的完整对话；静默 10 秒后由后台任务把消息逐条写进本表，
--    segment_index 记录全局序号（续接上一次落库的最大值），content 是单条消息的 JSON。
--    Redis 丢失时，按 segment_index 升序读回即可还原对话。
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_session (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id       BIGINT       NOT NULL                COMMENT '用户id（网关注入）',
    session_id    VARCHAR(64)  NOT NULL                COMMENT '会话id（前端生成，如 uuid）',
    segment_index INT          NOT NULL DEFAULT 0      COMMENT '消息序号，跨批次递增',
    content       TEXT                                 COMMENT '单条消息 JSON：{"role":"user|ai","content":"..."}',
    create_time   DATETIME     NULL                    COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_session (user_id, session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天对话记录（分片存储）';

-- ----------------------------------------------------------------------------
-- 2. user_session：会话元信息表（左侧会话列表）
--    前端每开一个新会话就插一行；删除会话时同时清掉 Redis 里对应的记忆。
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_session (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT      NOT NULL                COMMENT '用户id',
    name        VARCHAR(128) NULL                   COMMENT '会话名称',
    tag         VARCHAR(32)  NULL                   COMMENT '会话标签',
    session_id  VARCHAR(64) NOT NULL                COMMENT '会话id',
    create_time DATETIME    NULL                    COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话关联表';

-- ----------------------------------------------------------------------------
-- 3. user_markdown_docs：用户上传的知识库文档表
--    只存原文与切割等级；切分后的向量进 ChromaDB（元数据带 user_id / doc_id）。
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_markdown_docs (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键（即向量库里的 doc_id）',
    user_id     BIGINT      NOT NULL                COMMENT '用户id',
    file_name   VARCHAR(255) NULL                   COMMENT '文件名',
    content     LONGTEXT                            COMMENT 'Markdown 原文',
    level       INT         NULL DEFAULT 2          COMMENT '标题切割等级（2=按##切，3=按###切）',
    create_time DATETIME    NULL                    COMMENT '创建时间',
    update_time DATETIME    NULL                    COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户上传的 Markdown 文档表';
