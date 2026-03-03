# Library-Seat-AI-System (图书管理 + 座位预约 + AI 咨询)

简短说明：这是一个基于 Spring Boot 的图书管理系统，整合座位预约功能与基于 LangChain4j 的 AI/检索增强生成（RAG）能力。

- 项目坐标：com.book:manager-integrated:1.0.0-SNAPSHOT
- Java：17
- Spring Boot：3.2.0
- 主要技术栈：Spring Boot、Thymeleaf、MyBatis、Spring Data JPA、Redis、MySQL、LangChain4j（含 RAG 支持）

---

是否使用 RAG？

是的：项目依赖中包含 `langchain4j-easy-rag` 和 `langchain4j-open-ai-spring-boot-starter`，并在 `application.yml` 中配置了 `langchain4j` 相关的 model / api-key / base-url 等项，说明项目已集成 RAG 能力（Retrieval-Augmented Generation）。注意：配置文件内包含明文 API Key（见 `src/main/resources/application.yml`），请务必在生产环境中用环境变量或外部配置替换并不要泄露密钥。

---

目录（快速导航）

- 简介
- 快速开始
- 构建
- 配置
- 模板与静态资源
- 已实现的模块 / 功能
- 数据库与初始化
- 部署建议
- 开发与贡献
- 常见问题
- 附录

---

1. 简介

本项目名为 Library-Seat-AI-System（pom.xml 中的 `<name>`），目标是提供图书管理、借阅、论坛、座位预约与管理员控制台，并可通过 LangChain4j 连接外部大模型实现 AI 咨询与 RAG 场景。版本：1.0.0-SNAPSHOT。

2. 快速开始（开发者）

先决条件：
- JDK 17
- Maven
- MySQL（或兼容数据库）
- Redis（可选，但推荐）

本地运行（快速）：

1) 导入 IDE（Maven 项目）或使用命令行构建并运行：

mvn clean package
java -jar target/manager-integrated-1.0.0-SNAPSHOT.jar

或（开发）：

mvn spring-boot:run

默认访问： http://localhost:8080 （`server.port` 在 `src/main/resources/application.yml` 配置，默认 8080）

3. 构建

- 构建命令：`mvn clean package`
- 运行测试：`mvn test`
- 生成的可执行 jar：`target/manager-integrated-1.0.0-SNAPSHOT.jar`

4. 配置（重点及安全提示）

主配置文件：`src/main/resources/application.yml`

重要配置示例（不要将真实密码或 API Key 提交到仓库，README 仅示例）：

- server.port: 8080
- spring.datasource.url: jdbc:mysql://localhost:3306/book_manager?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&allowPublicKeyRetrieval=true&useSSL=false&allowMultiQueries=true
- spring.datasource.username: root
- spring.datasource.password: 12345678    # 请替换或使用环境变量
- spring.data.redis.host: 127.0.0.1
- spring.data.redis.port: 6379
- file.upload-dir: D:/biyesheji/upload/    # 本地文件上传目录，请在生产环境映射到持久卷

LangChain4j（RAG / LLM）相关（示例，application.yml 中含明文）：

langchain4j.open-ai.chat-model.base-url
langchain4j.open-ai.chat-model.api-key
langchain4j.open-ai.embedding-model.base-url
langchain4j.open-ai.embedding-model.api-key

安全建议：
- 将上述 api-key 与数据库密码从源码中移除，使用 `--spring.config.location` 指向外部配置文件，或使用环境变量（Spring 支持通过 `${ENV_VAR}` 引入），或者创建 `application-prod.yml` 并在部署时指定 `-Dspring.profiles.active=prod`。

5. 模板与静态资源

模板位置：`src/main/resources/templates/`
重要页面（部分）：
- index.html（首页）
- login.html（登录）
- register.html（注册）
- recommend.html（推荐页）
- welcome.html
- 子目录（根据功能组织）：`admin/` `announcement/` `book/` `borrow/` `forum/` `nav/` `notify/` `reader/` `seat/` `special/` `user/`

静态资源：`src/main/resources/static/`（favicon、js、css 等）

6. 已实现的模块 / 功能（概要）

通过代码结构及资源可以看到包含但不限于：
- 图书管理（浏览、详情、评论）
- 借阅/归还管理
- 用户管理、用户资料
- 通知/公告模块
- 论坛模块（帖子、回复）
- 座位预约（用户预约、我的预约；管理员座位管理、阅览室管理）
- 推荐模块（HomeRecommend / Recommendation）
- 阅读报表与统计
- AI 咨询 / RAG：通过 LangChain4j 集成外部模型与文本/文档检索

座位模块细节（摘自 `UPDATE_README.md`）：
- 修复 index.html 语法问题
- 阅览室从数据库动态加载
- 新增 ReadingRoom 实体与 Seat 管理相关接口
- 管理员功能（座位增删改、批量添加、强制释放、阅览室管理）
- 相关 API 列表已在 `UPDATE_README.md` 中说明

7. 数据库与初始化

数据库脚本：`src/main/resources/book_manager.sql`
（仓库中可能还包含 `db_update.sql`，用于座位模块的数据库更新）

启动前请确保已执行上述 SQL 脚本以创建表结构与必要的初始数据（管理员账号等）。

8. 部署建议

- 推荐以可执行 jar 部署：`java -jar target/manager-integrated-1.0.0-SNAPSHOT.jar`，并使用 systemd / 服务管理器或 Docker 容器化部署。
- 如果使用 Docker，请确保将 `file.upload-dir` 映射到宿主持久卷，并通过环境变量注入数据库/Redis/LLM API 密钥。
- 反向代理（可选）：建议使用 Nginx 做 TLS 终端与反向代理到内部端口。

9. 开发与贡献

源码路径：`src/main/java/com/book/`（按模块划分 controller/service/dao/entity）
测试：`src/test/java/`（已有若干单元/集成测试，见 `target/surefire-reports/`）
贡献指南（建议）：
- Fork -> 新分支 -> 提交 PR
- 编写测试覆盖新功能
- 保持依赖与 Java 版本兼容（Java 17）

10. 常见问题（FAQ）

- 端口占用：修改 `server.port` 或关闭占用进程
- 文件上传 413：已在 `application.yml` 中设置 `spring.servlet.multipart.max-file-size` 和 `max-request-size`（默认 5MB），如需更大请修改
- Hibernate DDL：`spring.jpa.hibernate.ddl-auto` 默认为 `update`（会自动更新表结构，生产请慎用）
- 日志排查：`logging.level` 在 `application.yml` 中可配置（项目已经对 langchain4j 与 Spring Security 做了 DEBUG 设置）

11. 附录

- 重要文件：
  - `pom.xml`（依赖与构建配置）
  - `src/main/resources/application.yml`（主要运行配置 —— 请移除密钥）
  - `src/main/resources/book_manager.sql`（建表/初始化脚本）
  - `UPDATE_README.md`（座位模块修复与增强说明）

- 推荐下一步改进（非必需，但推荐）：
  1. 抽出 `application.yml.example`，将敏感值替换为占位符
  2. 添加 `Dockerfile` 与 `docker-compose.yml`（包含 MySQL / Redis / 可选的向量 DB）
  3. 将 LangChain4j 与向量数据库（如 Milvus / Postgres + pgvector / FAISS）解耦，提供配置示例
  4. 添加 LICENSE（当前仓库未发现 LICENSE 文件）

---

如需，我可以：
- 把上面的 README 内容直接写入仓库（我已执行）；
- 进一步生成 `application.yml.example`、`Dockerfile` 或最小的 `docker-compose.yml`；
- 在 README 中加入更详细的 API 文档或截图/示例请求（curl）。

如果你希望我继续（例如生成 Dockerfile 或移除并替换配置中的明文 API Key），请告诉我下一步偏好。
