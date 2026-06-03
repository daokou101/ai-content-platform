# AI 内容创作平台

基于 Spring Boot 3 + Vue 3 的 AI 内容创作平台，集成 DeepSeek 大模型，支持流式生成、内容管理、版本控制、积分体系等功能。

## 技术栈

### 后端
| 技术 | 用途 |
|------|------|
| Spring Boot 3.2 | 核心框架 |
| Spring Security + JWT | 认证授权 |
| MyBatis-Plus | ORM 框架 |
| MySQL 8.0 | 数据库 |
| Redis 7 | 缓存、分布式锁、在线用户统计 |
| RabbitMQ | 消息队列（通知推送） |
| Redisson | 分布式锁（幂等性保证） |
| SSE / SseEmitter | AI 流式输出（打字机效果） |

### 前端
| 技术 | 用途 |
|------|------|
| Vue 3 + TypeScript | 前端框架 |
| Vite 5 | 构建工具 |
| Pinia | 状态管理 |
| Element Plus | UI 组件库 |
| EventSource (SSE) | 流式接收 AI 生成结果 |

### 部署
| 技术 | 用途 |
|------|------|
| Docker / Docker Compose | 容器化部署 |
| Nginx | 前端静态服务 + API 反向代理 |
| GitHub Actions | CI/CD 自动构建 |

## 项目结构

```
├── backend/                        # Spring Boot 后端
│   ├── src/main/java/com/smarttask/
│   │   ├── common/                 # 公共模块
│   │   │   ├── annotation/         # 自定义注解（@Idempotent）
│   │   │   ├── api/                # 统一返回结果（Result, ResultCode）
│   │   │   ├── constant/           # 常量定义
│   │   │   ├── exception/          # 全局异常处理
│   │   │   └── utils/              # 工具类（JwtUtils, RedisKeyBuilder）
│   │   ├── config/                 # 配置类（线程池, DeepSeekConfig）
│   │   ├── controller/             # 控制器
│   │   │   ├── AiController.java   # AI 生成接口（SSE 流式）
│   │   │   ├── ContentController.java  # 内容 CRUD
│   │   │   ├── AuthController.java # 登录注册
│   │   │   └── DashboardController.java # 仪表盘
│   │   ├── dto/                    # 数据传输对象
│   │   ├── entity/                 # 实体类
│   │   ├── mapper/                 # 数据访问层
│   │   ├── security/               # Spring Security 配置
│   │   │   ├── JwtAuthenticationFilter.java  # JWT 认证（含 SSE token 兼容）
│   │   │   └── SecurityConfig.java           # 安全配置
│   │   └── service/                # 业务逻辑层
│   │       ├── AiService.java      # AI 生成编排
│   │       ├── DeepSeekClient.java # DeepSeek API 流式客户端
│   │       └── ContentService.java # 内容管理 + 版本控制
│   ├── sql/init.sql                # 数据库初始化脚本
│   └── Dockerfile                  # 多阶段构建
├── ai-creator/frontend/            # Vue 3 前端
│   ├── src/
│   │   ├── api/                    # 后端 API 封装
│   │   ├── views/                  # 页面组件
│   │   │   ├── Generate.vue        # AI 内容生成页
│   │   │   ├── Dashboard.vue       # 仪表盘
│   │   │   └── ContentList.vue     # 内容管理
│   │   └── stores/                 # Pinia 状态管理
│   ├── nginx.conf                  # Nginx 配置（SSE 必须关缓冲）
│   └── Dockerfile                  # 多阶段构建
├── docker-compose.yml              # 5 容器编排
└── .github/workflows/maven.yml     # CI/CD
```

## 快速启动

### 方式一：Docker Compose（推荐）

前置条件：安装 [Docker Desktop](https://www.docker.com/products/docker-desktop)

```bash
# 1. 克隆项目
git clone <repo-url>
cd ai-content-platform

# 2. 配置 DeepSeek API 密钥
cp .env.example .env
# 编辑 .env，将 DEEPSEEK_API_KEY 改为你的真实密钥

# 3. 一键启动（5 个容器）
docker compose up -d --build

# 4. 访问 http://localhost
```

### 方式二：本地开发

前置条件：JDK 17、Node.js 20、MySQL 8.0、Redis 7、RabbitMQ

```bash
# 1. 初始化数据库
mysql -u root -p < backend/sql/init.sql

# 2. 启动后端
cd backend
mvn spring-boot:run

# 3. 启动前端（新终端）
cd ai-creator/frontend
npm install
npm run dev

# 4. 访问 http://localhost:5173
```

## 核心功能

### 1. AI 内容生成（DeepSeek + SSE 流式）

```
输入关键词 → 选择模板 → 点击生成 → EventSource 建立 SSE 连接
  → 后端 SseEmitter 立即返回 → 异步调用 DeepSeek API
  → DeepSeek 流式返回文本块 → Bufferedreader 逐行解析
  → 逐字推送 SseEmitter → 前端逐字显示（打字机效果）
```

**技术要点**：
- 前端 `EventSource` 无法设置 HTTP 请求头，JWT 通过 `?token=xxx` 查询参数传递
- `JwtAuthenticationFilter` 同时支持 Authorization 请求头和 token 查询参数两种方式
- Nginx `proxy_buffering off;` 必须关闭，否则 SSE 响应会被缓冲

### 2. 内容版本控制

每次更新内容自动保存快照，支持回滚到任意历史版本：
- **首次保存** → version = 1
- **每次编辑** → version + 1，保存当前快照
- **回滚操作** → 覆盖当前内容 + 保存新版本（可追溯）

### 3. AI 模板存数据库

模板类型存在 `ai_template` 表，不是硬编码。新增模板只需 INSERT，不改代码：
- 文章写作、营销文案、社交媒体、SEO 优化、内容摘要
- 每个模板对应独立的 system_prompt（系统提示词）

### 4. 积分体系

- 注册赠送 100 积分
- 每日签到 +10 积分（Redisson 分布式锁防重复签到）
- 积分排行榜（Redis Sorted Set 或数据库查询）

## API 接口

### AI 生成
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ai/templates` | 获取模板列表 |
| GET | `/api/ai/models` | 获取模型列表 |
| GET | `/api/ai/generate/sse?keywords=&templateType=` | SSE 流式生成 |

### 内容管理
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/contents` | 创建内容（@Idempotent 防重复） |
| GET | `/api/contents?page=&size=` | 分页查询（支持 keyword/templateType 筛选） |
| GET | `/api/contents/{id}` | 内容详情 |
| PUT | `/api/contents/{id}` | 更新内容 |
| DELETE | `/api/contents/{id}` | 删除内容（逻辑删除） |
| POST | `/api/contents/{id}/favorite` | 切换收藏 |
| GET | `/api/contents/{id}/versions` | 版本历史 |
| POST | `/api/contents/{id}/rollback/{version}` | 回滚版本 |

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/register` | 注册 |

## 面试问答

### Q: 为什么用 SSE 而不是 WebSocket？
SSE（Server-Sent Events）是单向的（服务端 → 客户端），适合 AI 生成这种服务端主动推送的场景。WebSocket 是双向的，适合聊天等需要双方通信的场景。SSE 基于 HTTP 协议，实现更简单，不需要额外依赖。前端使用 `EventSource` API，原生支持断线重连。

### Q: JWT 认证怎么在 SSE 中工作？
`EventSource` 无法设置 HTTP 请求头（包括 Authorization），所以 JWT token 通过 URL 查询参数传递：`/api/ai/generate/sse?keywords=xxx&token=xxx`。`JwtAuthenticationFilter` 会先检查请求头，如果没有再从查询参数获取。

### Q: 内容版本控制的实现原理？
每次更新内容前，先把当前状态的快照（title/summary/content）保存到 `ai_content_version` 表（版本号递增）。回滚时，查找目标版本的快照，覆盖当前内容，然后保存一个新版本作为"回滚快照"。这样每次操作都可追溯，不会丢失数据。

### Q: 幂等性注解 @Idempotent 怎么实现的？
使用 AOP + Redisson 分布式锁。前端在请求头中传 `idempotent-token`（UUID），AOP 切面用该 token 作为 Redis key 设置 10 秒过期。同一个 token 10 秒内重复请求会被拒绝。防止用户快速双击"保存"按钮导致重复提交。

### Q: Docker Compose 部署了哪些服务？
5 个容器：MySQL 8.0（数据库）、Redis 7（缓存）、RabbitMQ 3（消息队列）、Spring Boot 后端、Nginx + Vue 前端。`docker compose up -d --build` 一键启动。

## License

MIT
