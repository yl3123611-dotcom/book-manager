# 图书管理与座位预约（含 AI 推荐）——项目说明（中文）

## 简要说明

这是一个集成的图书馆系统（图书管理、座位预约、AI 智能推荐），基于 Spring Boot 和 LangChain4j。项目中包含对检索增强生成（RAG，Retrieval-Augmented Generation）的支持，用于基于馆藏内容做更可靠的 AI 推荐与问答。

说明要点：
- 本项目确实使用了 RAG（基于 langchain4j 的实现）。
- 默认情况下：RAG 的“启动时自动索引”被关闭（为了避免在没有配置好 API 或受到速率限制时导致启动失败）。可以通过配置打开。


## RAG 在本项目中的实现位置（关键类）

- `com.book.manager.modules.ai.rag.BookRagConfig`：配置 EmbeddingStore（默认为 InMemoryEmbeddingStore）和 ContentRetriever（EmbeddingStoreContentRetriever），控制 maxResults、minScore 等检索参数。
- `com.book.manager.modules.ai.rag.BookRagIndexer`：负责把数据库中的图书信息转换为 embedding 并写入 EmbeddingStore。默认通过 `rag.index-on-startup` 配置决定是否在应用启动时构建索引；并有 `rag.startup-limit`、`rag.startup-sleep-ms` 用于节流/限制起始索引量。
- `com.book.manager.modules.ai.tools.BookTools`：一组可被 AI 调用的工具（例如在数据库中搜索书籍），配合 RAG 使用可保证推荐基于真实馆藏。
- `com.book.manager.modules.ai.service.SmartLibrarianService` / `SmartLibrarianRagStreamingService`：定义 AI 行为（SystemMessage），在需要时会使用工具与 content retriever 以实现基于检索的回答/推荐。
- AI 记忆（对话历史）由 `com.book.manager.modules.ai.config.AiMemoryConfig` 管理，使用 Redis 存储简化后的消息结构，避免直接序列化第三方对象。


## 配置要点（主要在 `src/main/resources/application.yml`）

- LangChain4j / OpenAI 相关：
  - `langchain4j.open-ai.embedding-model.*`：嵌入模型的 base-url、api-key、model-name 等（示例中使用第三方兼容服务，务必替换为你自己的 endpoint 与密钥）。
  - `langchain4j.open-ai.chat-model` / `streaming-chat-model`：对话模型配置（用于生成回复与流式 SSE）。

- RAG 相关（在 `BookRagIndexer` 中读取）：
  - `rag.index-on-startup`（默认 false）——是否在应用启动时自动索引馆藏。
  - `rag.startup-limit`（默认 3）——启动时最多索引多少本书（演示用）。
  - `rag.startup-sleep-ms`（默认 1200）——每本书索引间的休眠，降低触发速率限制的风险。

- Redis / 数据库（MySQL）等服务请在 `application.yml` 中配置正确的 host/port/credentials。

注意：仓库中示例 `application.yml` 包含了示例 API key 与 base-url，请立即替换或从配置中移除以防泄露或误用。


## 如何开启与运行（最小步骤）

前提：
- JDK 17
- Maven
- MySQL（并创建 `book_manager` 数据库或修改 `application.yml` 数据库配置）
- Redis（用于 AI 对话记忆）

常用命令（PowerShell）：

```powershell
# 验证环境
mvn -v; java -version

# 打包
mvn clean package -DskipTests

# 直接运行（开发）
mvn spring-boot:run

# 或使用打好的 jar 运行（可通过系统属性启用 startup 索引）
java -Drag.index-on-startup=true -jar target/manager-integrated-1.0.0-SNAPSHOT.jar
```

如果你只想体验 RAG 功能，可以：
1. 在 `application.yml` 中配置好 `langchain4j.open-ai.embedding-model` 的 `base-url` 与 `api-key`（确保配额/权限足够）。
2. 将 `rag.index-on-startup` 设为 `true` 或在运行时加入 `-Drag.index-on-startup=true`。
3. 启动后观察日志（langchain4j 的日志由 `logging.level.dev.langchain4j` 控制），会输出“RAG indexing ...”相关信息。


## 主要 API（部分）

- POST `/ai/chat` —— 智能对话（返回文本）。
- POST `/ai/recommend` —— 结构化推荐（AI 输出 JSON，后端解析并返回书籍列表）。
- GET `/ai/chat/stream?memoryId=...&message=...` —— 流式 SSE 聊天。


## 运行与调试建议 / 注意事项

- 生产环境不要使用 `InMemoryEmbeddingStore`：内存存储重启即丢失。推荐改用持久化向量存储（例如 Milvus、Weaviate、Elastic Vector 或其他 langchain4j 支持的 EmbeddingStore 实现）。
- 成本与速率限制：embedding 与模型调用通常按 token 或请求计费，indexOnStartup 在未做好节流/批量处理时可能触发 RPM 限制。示例中通过 `startup-sleep-ms` 减缓速率。
- 密钥管理：不要把真实 API 密钥提交到版本库，使用环境变量或外部配置中心。示例 `application.yml` 中的密钥请尽快清理。
- 日志级别：项目已开启对 `dev.langchain4j` 的 DEBUG 日志，便于调试检索/嵌入流程；生产环境可降低等级以免输出过多信息。


## 未来改进建议（可选）

- 把索引从“启动时批量索引”改为异步后台索引/事件驱动（例如在书目变更时触发增量更新）。
- 使用持久化、可扩展的向量数据库替换 `InMemoryEmbeddingStore`，并添加 periodic re-index/consistency 检查。
- 增加更多测试覆盖（RAG 检索准确性、工具输出一致性、失败回退策略）。
- 增强对不同 embedding/LLM 提供商的配置适配（OpenAI、Azure、本地 LLM 等）。


## 关键源码位置（快速导航）

- RAG 配置：`src/main/java/com/book/manager/modules/ai/rag/BookRagConfig.java`
- 索引器：`src/main/java/com/book/manager/modules/ai/rag/BookRagIndexer.java`
- AI 服务定义：`src/main/java/com/book/manager/modules/ai/service/SmartLibrarianService.java`
- 流式服务：`src/main/java/com/book/manager/modules/ai/service/SmartLibrarianRagStreamingService.java`
- 工具集合（数据库查询）：`src/main/java/com/book/manager/modules/ai/tools/BookTools.java`
- AI 记忆（Redis）：`src/main/java/com/book/manager/modules/ai/config/AiMemoryConfig.java`


---


