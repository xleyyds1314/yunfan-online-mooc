<div align="center">

# 云帆学堂

**在线职业技能培训平台**

Java 11 · Spring Cloud Alibaba · Vue 3 · Python AI

</div>

## 项目简介

云帆学堂是一个面向职业技能培训的在线教育平台。平台以在线课程售卖为核心，串联学习计划、考试测评、直播授课、订单交易、营销促销、社区互动与运营数据看板，并提供基于大模型的学习助手。

后端采用 Spring Cloud Alibaba 微服务架构，按业务域拆分为多个独立服务；AI 能力由独立的 Python 服务承载，通过网关对外提供统一接口。

## 功能特性

| 能力 | 说明 |
| --- | --- |
| 课程与学习 | 课程售卖、学习计划、视频进度记录、学习进度统计，形成完整学习闭环 |
| 考试测评 | 题库、试卷、考试记录与成绩管理 |
| 直播授课 | 基于推拉流的直播课程与观众互动 |
| 交易支付 | 订单、优惠与支付，支持支付宝、微信等支付渠道及退款拆单 |
| 营销促销 | 优惠券发放、兑换码、秒杀领取、优惠叠加推荐 |
| 社区互动 | 问答评论、点赞、签到、积分与排行榜 |
| 消息中心 | 系统通知、用户私聊与多人在线群聊 |
| AI 学习助手 | 基于 RAG 的课程问答，支持知识库检索与工具调用 |
| 运营看板 | 基于业务日志与埋点的流量统计、数据大盘与课程推荐 |

## 系统架构

```
浏览器 / 小程序
      │
      ▼
 yf-gateway（统一路由、鉴权、限流、日志埋点）
      │
      ├── yf-auth        认证授权
      ├── yf-user        用户中心
      ├── yf-course      课程中心
      ├── yf-learning    学习中心
      ├── yf-exam        考试中心
      ├── yf-live        直播中心
      ├── yf-trade       交易中心
      ├── yf-pay         支付中心
      ├── yf-promotion   营销中心
      ├── yf-remark      互动中心
      ├── yf-message     消息中心
      ├── yf-media       媒资管理
      ├── yf-search      搜索服务
      ├── yf-data        数据中心
      └── yf-chat-python AI 服务
      │
      ▼
  MySQL · Redis · RabbitMQ · Elasticsearch · MinIO · InfluxDB
```

- 服务注册与配置统一由 Nacos 管理，配置按 `shared-*.yaml` 抽取公共部分。
- 服务间调用通过 `yf-api` 中声明的 Feign 接口完成。
- AI 服务独立部署，走网关暴露接口，与 Java 服务解耦。

## 技术选型

### 后端

| 组件 | 版本 / 说明 |
| --- | --- |
| JDK | 11 |
| Spring Boot | 2.7.2 |
| Spring Cloud | 2021.0.3 |
| Spring Cloud Alibaba | 2021.0.1.0 |
| 服务注册与配置 | Nacos |
| 持久层 | MyBatis-Plus + MySQL 8 |
| 缓存 | Redis + Redisson + Caffeine |
| 消息 | RabbitMQ |
| 任务调度 | XXL-JOB |
| 流量防护 | Sentinel |
| 分布式事务 | Seata |
| 对象存储 | MinIO |
| 音视频 | 腾讯云 VOD |
| 搜索 | Elasticsearch |
| 时序数据 | InfluxDB |
| 接口文档 | Knife4j |

### AI 服务（yf-chat-python）

| 组件 | 版本 / 说明 |
| --- | --- |
| 运行时 | Python 3.11 |
| Web 框架 | FastAPI + Uvicorn |
| 编排 | LangChain + LangGraph |
| 模型接入 | OpenAI 兼容协议（DeepSeek） |
| 向量库 | ChromaDB |
| 数据访问 | SQLAlchemy 2.x + PyMySQL |
| 会话缓存 | Redis |

### 前端

| 组件 | 版本 |
| --- | --- |
| Vue | 3.2 |
| 构建 | Vite 2 |
| UI | Element Plus 2 |
| 状态管理 | Pinia |
| 图表 | ECharts 5 |

## 模块说明

| 模块 | 定位 | 说明 |
| --- | --- | --- |
| yf-gateway | 网关 | 路由转发、统一鉴权、限流与日志埋点 |
| yf-auth | 鉴权中心 | 登录认证、令牌签发、权限校验 |
| yf-user | 用户中心 | 用户信息、角色与账号管理 |
| yf-course | 课程中心 | 课程、章节、教师与课程上下架 |
| yf-learning | 学习中心 | 学习计划、视频进度、签到与积分 |
| yf-exam | 考试中心 | 题库、试卷、考试与成绩 |
| yf-live | 直播中心 | 直播间管理与推拉流 |
| yf-trade | 交易中心 | 订单创建、状态流转与退款 |
| yf-pay | 支付中心 | 支付渠道对接、支付单与对账 |
| yf-promotion | 营销中心 | 优惠券、兑换码与秒杀 |
| yf-remark | 互动中心 | 问答评论、点赞与统计 |
| yf-message | 消息中心 | 通知推送、私聊与群聊 |
| yf-media | 媒资管理 | 文件上传、分片上传与视频处理 |
| yf-search | 搜索服务 | 课程搜索与个性化推荐 |
| yf-data | 数据中心 | 日志分析、流量统计与数据大盘 |
| yf-api | 接口层 | 跨服务调用的 Feign 接口与 DTO |
| yf-common | 公共包 | 通用工具、常量、异常与自动配置 |
| yf-chat-python | AI 服务 | 智能问答、RAG 知识库与工具调用 |

## 前端工程

前端代码位于 `yf-front/`，两个独立应用：

| 工程 | 说明 |
| --- | --- |
| yf-admin | 运营管理端，供平台运营人员、教师使用 |
| yf-protal | 用户端，面向学员，课程浏览、学习与交易 |

开发时通过 Vite 代理转发到网关，具体地址在各自 `vite.config.js` 与 `src/config/proxy.js` 中配置。

## 目录结构

```
├── nacos/            Nacos 配置文件（DEFAULT_GROUP）
├── sql/              数据库脚本（不含数据）
├── sql/test/         数据库脚本（含测试数据）
├── xxl-job/          XXL-JOB 初始化脚本
├── yf-*/             后端微服务模块
├── yf-front/         前端工程（admin / protal）
└── yf-chat-python/   Python AI 服务
```

## 本地启动

1. 准备中间件：MySQL 8、Redis、Nacos、RabbitMQ、Elasticsearch、MinIO、InfluxDB、XXL-JOB。
2. 执行 `sql/` 或 `sql/test/` 下的脚本初始化数据库（库名前缀为 `yf_`）。
3. 将 `nacos/DEFAULT_GROUP` 下的配置导入 Nacos，并按自己的环境修改数据库、中间件地址。
4. 启动 `yf-gateway` 及各业务服务，端口与依赖见各模块的 `bootstrap.yml`。
5. AI 服务按 `yf-chat-python/README.md` 启动，模型与向量库配置通过环境变量注入。
6. 前端在 `yf-front/yf-admin`、`yf-front/yf-protal` 下分别安装依赖并启动。

> 敏感配置（第三方密钥、数据库口令、模型 Key）不写入仓库，通过环境变量注入，示例见 `yf-chat-python/.env.example`。

## 文档

| 文档 | 说明 |
| --- | --- |
| `nacos/` | 各服务的 Nacos 配置 |
| `sql/`、`sql/test/` | 建表脚本（含 / 不含测试数据） |
| `xxl-job/xxl_job.sql` | 定时任务初始化 |
| `yf-chat-python/README.md` | AI 服务说明与部署 |

## 许可证

本项目采用 Apache License 2.0 许可证。
