# AGENTS.md

## 作用范围

本文件是 `bpmt-lite` 仓库的本地协作与交接文档。
后续 Codex agent 在本仓处理环境、编译、打包、Docker 运行、配置覆盖、文档更新时，必须先读本文件，再做判断。

## 项目定位

- 仓库：`bpmt-lite`
- 目标：对遗留 BPMT 平台做简化发行工程
- 核心原则：只调整代码结构、打包方式、配置方式、部署方式
- 明确边界：不升级技术栈、不重写功能、不额外增加功能
- 运行栈：Java 8、Maven 3、Tomcat 7、MariaDB

## README 约定

- `v1.0.0` 是首个正式 Docker 化版本。
- `v1.1.0` 是已发布的第二个 Docker 化版本。
- `v1.3.0` 是 H5 修复发布版本。
- `v1.4.0` 是新增独立 `api` 子项目和独立 API Docker 容器的发布版本。
- `v1.4.1` 是新增 `nginx` 单入口、API 模块化路径重整，以及数据库操作模块接口的发布版本。
- `v1.5.0` 是新增外部系统 OAuth 登录能力的发布版本。
- `v1.5.1` 是基于 `v1.5.0` 修复 issue #10 工作流待办“查看/处理”跳转 `_ORD_ID=null` 问题的补丁版本。
- `v1.5.2` 是基于 `v1.5.1` 增强外部系统 OAuth 登录态切换体验的补丁版本。
- `v1.5.3` 是基于 `v1.5.2` 修复 `nginx` 转发非 80 端口时 OAuth 回跳地址丢端口的问题。
- `v1.5.4` 是基于 `v1.5.3` 补齐 Web/API 镜像 multi-arch 发布能力的补丁版本。
- `v1.6.0` 是新增 HTTPS 入口支持的发布版本，支持内置 nginx TLS 和可信上游 TLS。
- `v1.6.1` 是基于 `v1.6.0` 增强微信生态第三方 OAuth 登录态传导的补丁版本。
- `v1.6.2` 是修复第三方系统管理界面和 OAuth 无权限提示，并新增安装/升级脚本的补丁版本。
- `v1.7.0` 是动态表视图配置 API 发布版本，开放 `/{viewKey}.view` 对应的 dyn 视图完整配置管理能力。
- `v1.7.1` 是基于 `v1.7.0` 新增报表视图配置 API，开放 `rep_list` 对应的 `/{viewKey}.view` 配置管理能力。
- `v1.7.2` 是基于 `v1.7.1` 修复报表视图创建、视图缓存、版本显示和数据库操作 save 显式主键写入问题的补丁版本。
- `v1.7.3` 是基于 `v1.7.2` 修复 OAuth 微信登录失败页提示和 `/api/openapi.json` 文档风格问题的补丁版本。
- `v1.7.4` 是基于 `v1.7.3` 修复登录后偶发灰色底蒙版问题的补丁版本。
- `v1.7.5` 是基于 `v1.7.4` 增强第三方系统 AI 接入提示词生成的补丁版本。
- `v1.8.0` 是桌面后台清晰度基线版本；原 `modern-theme foundation` / CSS-only 主题方向已被判定失败，仅保留为历史记录。
- `v1.8.1` 是基于 `v1.8.0` 的桌面后台前端缺陷加固补丁版本，修复人工浏览器校验中标注的 footer、按钮、表格、zTree、顶部栏、logo 和通用空状态等问题。
- `v1.9.0` 是基于 `v1.8.1` 的桌面后台壳层、全局弹框、空状态生命周期和非菜单 zTree 缺陷修复版本。
- 默认 Web 镜像：`ghcr.io/wodenwang/bpmt-lite:1.9.0`
- 默认 API 镜像：`ghcr.io/wodenwang/bpmt-lite-api:1.9.0`
- 同步镜像 tag：发布后同步到 `ghcr.io/wodenwang/bpmt-lite:latest` 和 `ghcr.io/wodenwang/bpmt-lite-api:latest`
- 默认访问地址：`http://127.0.0.1/`
- HTTPS 访问地址：`https://127.0.0.1/`，需要 `BPMT_HTTPS_ENABLED=1`
- 默认 API 文档：`http://127.0.0.1/api/docs/`
- 默认 OpenAPI：`http://127.0.0.1/api/openapi.json`
- `ROOT` 应用对应 BPMT `platform`
- 额外包含 `/ueditor` 应用
- MariaDB 默认初始化数据库名：`bpmt`
- MariaDB 最小初始化数据库名：`bpmt_min`
- README 中记录的发布验收基线是：
  - 使用 `database/bpmt-min.sql.gz` 最小库初始化后 176 张表
  - 使用 `database/bpmt.sql.gz` 完整库初始化后 380 张表
  - `/` 返回 200
  - `/ueditor/` 返回 200

## 文档与沟通规则

- 本项目沟通和文档统一使用简体中文。
- 代码、命令、配置键名、Maven 坐标、镜像名等技术标识保持原样。
- 如果后续 agent 更新运行说明、维护说明或交接说明，应与 README 的中文风格保持一致。
- v1.3.0 期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/v1.3.0/*` -> `docs/v1.2.0/*` -> `README.md` -> implementation。
- v1.4.0 API 规划和开发期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/specs/2026-05-02-bpmt-lite-v1.4.0-api-design.md` -> `docs/v1.4.0/*` -> `README.md` -> implementation。
- v1.5.0 OAuth 登录开发期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/specs/2026-05-03-bpmt-lite-v1.5.0-oauth-login-design.md` -> `docs/v1.5.0/*` -> `README.md` -> implementation。
- 涉及 Docker、数据库、初始化脚本、发布验收、公开文档的变更，必须同步更新对应文档，不能只改代码。
- v1.6.0 HTTPS 开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/specs/2026-05-05-bpmt-lite-v1.6.0-https-design.md` -> `docs/superpowers/plans/2026-05-05-bpmt-lite-v1.6.0-https.md` -> `docs/v1.6.0/*` -> `README.md` -> implementation。
- v1.6.1 开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/specs/2026-05-05-bpmt-lite-v1.6.1-wechat-oauth-thirdpart-design.md` -> `docs/superpowers/plans/2026-05-05-bpmt-lite-v1.6.1-wechat-oauth-thirdpart.md` -> `docs/v1.6.1/*` -> `README.md` -> implementation。
- v1.6.2 开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/specs/2026-05-06-bpmt-lite-v1.6.2-install-upgrade-readme-issues-design.md` -> `docs/superpowers/plans/2026-05-06-bpmt-lite-v1.6.2-install-upgrade-readme-issues.md` -> `docs/release-v1.6.2.md` -> `README.md` -> implementation。
- v1.7.0 动态表视图 API 开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/specs/2026-05-10-bpmt-lite-v1.7.0-dynamic-table-view-api-design.md` -> `docs/superpowers/plans/2026-05-10-bpmt-lite-v1.7.0-dynamic-table-view-api.md` -> `docs/v1.7.0/*` -> `docs/release-v1.7.0.md` -> `README.md` -> implementation。
- v1.7.1 报表视图 API 开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/specs/2026-05-15-bpmt-lite-v1.7.1-report-view-api-design.md` -> `docs/superpowers/plans/2026-05-15-bpmt-lite-v1.7.1-report-view-api.md` -> `docs/v1.7.1/*` -> `docs/release-v1.7.1.md` -> `README.md` -> implementation。
- v1.7.2 GitHub issue bugfix 开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/plans/2026-05-16-bpmt-lite-v1.7.2-github-issue-bugfixes.md` -> `docs/release-v1.7.2.md` -> `README.md` -> implementation。
- v1.7.3 GitHub issue bugfix 开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/plans/2026-05-16-bpmt-lite-v1.7.3-github-issue-bugfixes.md` -> `docs/release-v1.7.3.md` -> `README.md` -> implementation。
- v1.7.4 登录后灰色蒙版 bugfix 开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/plans/2026-05-20-bpmt-lite-v1.7.4-login-mask.md` -> `docs/release-v1.7.4.md` -> `README.md` -> implementation。
- v1.7.5 第三方系统 AI 接入提示词开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/superpowers/plans/2026-05-30-bpmt-lite-v1.7.5-thirdpart-ai-onboarding-prompt.md` -> `docs/release-v1.7.5.md` -> `docs/v1.5.0/oauth-login-reference.md` -> `README.md` -> implementation。
- v1.8.0 `modern-theme foundation` 分支已被 Product Design 判定为方向失败，不再作为后续实现依据。旧文档仅保留为失败方向证据。
- v1.8.0 已重新规划并收口为 `desktop admin clarity baseline`，source-of-truth 顺序是：`AGENTS.md` -> `docs/release-v1.8.0.md` -> `docs/v2.0.0/desktop-admin-clarity-replan.md` -> `docs/superpowers/specs/2026-06-11-bpmt-lite-v1.8.0-desktop-admin-clarity-design.md` -> `docs/superpowers/plans/2026-06-11-bpmt-lite-v1.8.0-desktop-admin-clarity.md` -> `README.md` -> implementation。
- v1.8.0 不以全局 CSS-only 现代主题作为发布目标，而是围绕登录后首页、第三方系统列表、AI 接入提示词弹窗三条代表路径改善当前位置、页面任务、主操作、空/加载状态和复制反馈。
- v1.8.1 前端缺陷加固开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/release-v1.8.1.md` -> `docs/superpowers/plans/2026-06-13-bpmt-lite-v1.8.1-frontend-defect-hardening.md` -> `docs/v2.0.0/configuration-workbench-lite-qa-2026-06-13.md` -> `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/README.md` -> `README.md` -> implementation。
- v1.9.0 桌面壳层与全局缺陷修复开发和验收期间的 source-of-truth 顺序是：`AGENTS.md` -> `docs/release-v1.9.0.md` -> `docs/superpowers/plans/2026-06-14-bpmt-lite-v1.9.0-shell-navigation-hardening.md` -> `docs/v2.0.0/v1.9.0-plan-eng-review-2026-06-14.md` -> `design/bpmt-v1.9.0-shell-navigation.pen` -> `README.md` -> implementation。

## 已验证的本地编译基线

2026-04-26 在当前 checkout 已验证：

- JDK：`/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home`
- Maven 本地仓库：`/Volumes/vm/maven/repository`
- Maven settings：`settings.local.xml`
- 全仓编译命令：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn -s settings.local.xml -DskipTests compile
```

验证结果：

- Reactor 模块：`riversoft-product`、`parent`、`util`、`magic`、`magic-api`、`magic-api-impl`、`dbtools`、`platform`
- 结果：`BUILD SUCCESS`

## 不可变的环境规则

- 本仓所有构建、依赖导入、IDE Java language server 都必须使用 Java 8。
- 不要假设本仓 Maven 使用 `~/.m2/repository`。
- 当前机器在本仓应使用的 Maven 本地仓库路径是 `/Volumes/vm/maven/repository`。
- 优先使用 `mvn -s settings.local.xml ...`，不要默认裸跑 `mvn`。
- 如果 VS Code 出现大量 Java 编译错误，先检查：
  - 工作区 JDK 是否为 Java 8
  - Maven settings 是否指向 `settings.local.xml`
  - `/Volumes/vm/maven/repository` 是否可读

## Maven 配置规则

- `settings.local.xml` 是当前 checkout 的有效本地配置。
- `settings.example.xml` 是公开示例配置，不能写死本机 Maven 本地仓库路径。
- 已退役的 RiverSoft 私有仓库地址不得重新引入：
  - `https://nexus.riversoft.com.cn/repository/maven-public/`
  - `https://nexus.riversoft.com.cn/repository/Riversoft-release/`
  - `https://nexus.riversoft.com.cn/repository/Riversoft-snapshot/`
- 当前仓库策略：
  - `settings.example.xml` 使用 Maven 默认本地仓库
  - 本机 `settings.local.xml` 可以继续使用 `/Volumes/vm/maven/repository`
  - 公共镜像：Aliyun mirror of Central
  - Central：兜底仓库定义

## VS Code / IDE 规则

当前机器可以继续保留本地 VS Code 设置，但 `.vscode/` 从 v1.2.0 起视为本地 IDE 配置，不再提交 GitHub。

如果需要在本机恢复 VS Code 配置，规则仍然是：

- Java runtime 固定为 Java 8
- Maven / Java import 使用 `settings.local.xml`

如果 IDE 仍显示旧错误，按以下顺序处理：

1. 执行 `Java: Clean Java Language Server Workspace`
2. Reload VS Code 窗口
3. 重新触发 Maven project import

## Docker 与运行约定

- 默认启动方式是 `docker compose up -d`。
- v1.4.0 API 方案使用独立 `api` 容器，不进入 `web` 容器内部。
- Web 和 API 各自内嵌 Hazelcast，不单独引入 Hazelcast Server 容器。
- Docker Compose 服务名和固定容器名统一使用 `bpmt-` 前缀：`bpmt-nginx`、`bpmt-web`、`bpmt-api`、`bpmt-mariadb`。
- 默认 `docker-compose.yml` 只发布 HTTP；启用 HTTPS 时必须同时使用 `docker-compose.https.yml`，例如 `docker compose -f docker-compose.yml -f docker-compose.https.yml up -d`。
- `certs/` 是运行证书目录，不提交真实证书和私钥。
- HTTPS 启用时 HTTP 默认跳转 HTTPS，可通过 `BPMT_HTTP_REDIRECT=false` 关闭。
- v1.6.0 起，后端公开 URL 统一通过 `Actions.Util.getContextPath()` 和 `Actions.Util.getFullURL()` 生成；不要在业务 Action 中手工拼接 HTTPS URL。
- 后端会信任 `X-Forwarded-*` 头，生产部署不得把 `bpmt-web` 或 `bpmt-api` 直接暴露到不可信网络；上游网关必须覆盖并规范设置 `X-Forwarded-Proto`、`X-Forwarded-Host` 和 `X-Forwarded-Port`。
- Web/API 通过 compose 网络和 `HAZELCAST_TCPIP_MEMBERS=bpmt-web,bpmt-api` 加入同一 Hazelcast 集群。
- 缓存不能关闭，`HIBERNATE_CACHE` 应保持 `true`。
- 快速体验允许只拉起容器而不导入业务数据。
- 若要得到完整初始化业务数据，使用 `scripts/init-db.sh` 从 `database/bpmt.sql.gz` 解压生成 `db/init/bpmt.sql`。
- 若要得到最小初始化库，使用 `scripts/init-db.sh min` 从 `database/bpmt-min.sql.gz` 解压生成 `db/init/bpmt-min.sql`。
- `scripts/install.sh` 是面向使用者的从零安装入口，负责创建运行目录并带出 `run.sh`、`upgrade.sh`。
- `scripts/run.sh` 是面向使用者的一键运行入口，负责下载 compose、下载初始化脚本、解压 SQL 并启动服务；默认完整库，`min` 参数使用最小库。
- `scripts/upgrade.sh` 是运行目录内的升级入口；默认跟随 GitHub 最新 release/tag，拉取 Web/API `latest` 镜像，执行版本间 SQL 升级脚本，下载目标版本 compose 参考文件，不覆盖当前 `docker-compose.yml`。
- 升级状态必须记录在项目运行目录 `.bpmt-lite/`，不得写入 BPMT 业务数据库。
- 升级脚本不得自动升级或替换第三方容器镜像，例如 `mariadb`、`nginx`。
- MariaDB 只会在首次创建 `db/data` 时自动导入 `db/init/*.sql`。
- 如果已经启动过，再替换初始化 SQL 不会自动重新导入。
- 需要重新初始化数据库时，先确认数据已备份，再执行：

```bash
docker compose down
rm -rf db/data
docker compose up -d
```

## 默认访问与常用配置

- 平台入口：`http://127.0.0.1/`
- UEditor：`http://127.0.0.1/ueditor/`
- API 文档：`http://127.0.0.1/api/docs/`
- OpenAPI：`http://127.0.0.1/api/openapi.json`
- 常用环境变量：
  - `BPMT_HTTP_PORT`
  - `BPMT_DB_PORT`
  - `BPMT_IMAGE_TAG`
  - `BPMT_API_IMAGE_TAG`
  - `BPMT_API_APP_KEY`
  - `BPMT_API_APP_SECRET`
  - `BPMT_API_ACT_AS`
  - `BPMT_HAZELCAST_PASSWORD`
  - `DB_HOST`
  - `DB_NAME`
  - `DB_USER`
  - `DB_PASSWORD`

后续 agent 如果修改 `docker-compose.yml`、镜像构建脚本或 README，不能破坏这些默认约定，除非用户明确要求变更。

## 运行目录约定

以下目录是 README 明确约定的运行目录，后续 agent 不应随意改语义：

- `db/init/*.sql`
  - 首次初始化数据库备份或由 `scripts/init-db.sh` 生成的导入文件
  - 不提交 git
- `db/data/`
  - MariaDB 数据目录
  - 不提交 git
- `db/logs/`
  - MariaDB 日志目录
  - 不提交 git
- `runtime/attachment/`
  - BPMT 附件目录
  - 不提交 git
- `runtime/download/`
  - BPMT 下载目录
  - 不提交 git
- `runtime/ueditor-upload/`
  - UEditor 上传目录
  - 不提交 git
- `runtime/platform-logs/`
  - 平台日志目录
  - 不提交 git
- `runtime/tomcat-logs/`
  - Tomcat 日志目录
  - 不提交 git
- `runtime/api-platform-logs/`
  - API 容器 BPMT 平台日志目录
  - 不提交 git
- `runtime/api-tomcat-logs/`
  - API 容器 Tomcat 日志目录
  - 不提交 git
- `config/overrides/`
  - properties 覆盖目录
  - 不提交具体覆盖文件

额外规则：

- `config/overrides/*.properties` 会追加到容器启动时生成的同名 properties 文件之后。
- 覆盖文件中的同名 key 优先级更高。
- Docker 默认 `LOG_PATH` 指向 `/usr/local/tomcat/webapps/logs`，对应宿主机 `runtime/platform-logs/`；不要把 BPMT 业务日志重新混入 Tomcat 日志目录。

## API 开发规则

- 后续所有 API 开发必须遵守 `docs/v1.4.0/api-guidelines.md`。
- 对外接口统一挂载在 `/api/v1/*`，公开文档固定为 `/api/openapi.json` 与 `/api/docs/`。
- 业务接口统一使用 `appKey/appSecret` 的 HMAC-SHA256 签名。
- HMAC canonical path 必须包含公开 context path，例如 `/api/v1/dynamic-tables`，不能只签 `/v1/dynamic-tables`。
- `BPMT_API_APP_KEY` 和 `BPMT_API_APP_SECRET` 从 Docker compose 环境变量注入；正式部署必须覆盖默认值。
- `BPMT_API_ACT_AS` 是固定技术用户；未配置或用户不可用时兜底 `admin`。
- JSON 响应统一为 `success/data/error` 包装，错误响应必须包含稳定 `code` 和 `requestId`。
- 每个对外接口必须同步更新 OpenAPI、Web 文档和单测。
- 动态表 API 只管理结构，不管理业务数据；动态表删除等危险能力默认不暴露。
- v1.7.0 动态表视图 API 只管理 `dyn` 视图配置，不管理菜单、首页卡片、按钮入口或动态表业务数据。
- 动态表视图 API 固定路径为 `/api/v1/dynamic-table-views`、`/api/v1/dynamic-table-views:validate`、`/api/v1/dynamic-table-views/{viewKey}`、`/api/v1/dynamic-table-views/{viewKey}/{section}`；`dryRun` 是 query 参数，不新增 `/dry-run` 或 `/sections/{section}` 路径。
- 动态表视图写接口支持 `validate`、`dryRun`、创建、整体替换、分区 patch 和带 `confirmViewKey` 的删除；删除视图不得删除底层动态表、业务数据、日志表或日志数据。
- 动态表视图快照必须覆盖基础信息、字段、分组、页签、区块、系统按钮、自定义按钮、查询区、前后置处理器、预置变量、父页面变量、权限和脚本风险提示。
- v1.7.0 不支持在查询区、变量和处理器上写入权限；请求中这些位置的非空 `permissions` 必须按 `UNSUPPORTED_PERMISSION` 拒绝。
- 每次调整动态表视图 API 都必须同步更新 `api/src/main/webapp/openapi.json`、`api/src/main/webapp/docs/index.html`、`docs/v1.7.0/api-reference.md` 和 `docs/v1.7.0/openapi.json`。
- v1.7.1 报表视图 API 只管理 `rep_list` 视图配置，不管理菜单、首页卡片、外部入口或报表业务数据。
- 报表视图 API 固定路径为 `/api/v1/report-views`、`/api/v1/report-views:validate`、`/api/v1/report-views/{viewKey}`、`/api/v1/report-views/{viewKey}/{section}`；`dryRun` 是 query 参数。
- 报表视图 API 写接口支持 `validate`、`dryRun`、创建、整体替换、分区 patch 和带 `confirmViewKey` 的删除；删除视图不得删除菜单、首页卡片、外部入口、业务表或业务数据。
- 报表视图 API 不执行 `mainSql`、查询 SQL、约束 SQL、PK SQL、按钮动作或客户端脚本；文档和响应必须保留风险提示。
- 报表视图 API 的风险提示至少覆盖 `SQL_SCRIPT_PRESENT`、`CLIENT_SCRIPT_PRESENT`、`BUTTON_ACTION_PRESENT`、`EXTERNAL_DB_KEY_PRESENT` 和 `UNEXECUTED_SQL_SEMANTICS`。
- v1.7.1 报表视图 API 只支持 `base`、`columns`、`queries`、`limits`、`variables`、`subviews`、`buttons`、`weixin`、`scripts` 这几类 section patch，不新增 `/sections/{section}` 或单项级 patch 路径。
- 每次调整报表视图 API 都必须同步更新 `api/src/main/webapp/openapi.json`、`api/src/main/webapp/docs/index.html`、当前版本 `docs/v*/api-reference.md` 和 `docs/v*/openapi.json`。

## v1.5.0 OAuth 开发规则

- v1.5.0 OAuth 主流程完全在 `bpmt-web/platform`，不进入也不改 `bpmt-api`。
- `bpmt-api` 的 `/api/docs/` 和 `/api/openapi.json` 只作为回归验收入口，不为 OAuth 新增 API 内容。
- BPMT 作为 OAuth2 Authorization Code 服务端，不实现 OIDC，不提供 `refresh_token`。
- OAuth 端点固定为 `/oauth/authorize`、`/oauth/token`、`/oauth/userinfo`。
- `/oauth/token` 和 `/oauth/userinfo` 使用 OAuth JSON 响应，不使用 `success/data/error` 包装；`/oauth/authorize` 是浏览器跳转或错误页。
- 外部系统主数据是 `CM_THIRDPART`，授权码状态是 `CM_THIRDPART_AUTH_CODE`，token 状态是 `CM_THIRDPART_ACCESS_TOKEN`。
- 三张 OAuth 表必须进入 `database/bpmt.sql.gz`、`database/bpmt-min.sql.gz` 和 `database/bpmt-db.sql` 默认初始化路径；Docker 默认 `HIBERNATE_AUTOUPDATE=false`，不能依赖 Hibernate 自动建表。
- 默认初始化数据必须包含后台菜单 `系统开发 -> 第三方系统`，菜单 ID 和菜单权限资源均为 `sys_thirdpart`，入口地址为 `/thirdpart/ThirdpartAction/index.shtml`。
- `code` 和 `access_token` 只保存 hash，DB 是 OAuth 运行态 source of truth。
- 外部系统 `clientSecret` 只展示一次，DB 只保存 `CLIENT_SECRET_HASH`。
- 权限边界使用 `CM_THIRDPART.PRI_KEY` 对接现有 BPMT 权限体系，并在 `权限组管理 -> 第三方系统权限` 页签中配置。
- 菜单第三方 URL / iframe 只是辅助入口，不是 OAuth 主流程；第三方页面无登录态时应自行发起 OAuth。
- `userid + thirdpartKey` 独立权限校验 API 暂不纳入 v1.5.0 验收范围。
- OAuth 主流程必须登记 `INFO` 日志，覆盖 authorize、token、userinfo 的开始、结果、错误码和关键状态。
- 日志禁止记录明文 `code`、`access_token`、`client_secret`、`password`。

## v1.6.1 微信生态 OAuth 规则

- 第三方系统仍只接入标准 `/oauth/authorize`、`/oauth/token`、`/oauth/userinfo`。
- 微信生态下没有 BPMT 登录态时，只有 `CM_THIRDPART.WECHAT_LOGIN_ENABLED=1` 的外部系统才走微信 OAuth 登录。
- 不得按 UA 自动猜测企业号或服务号配置；必须使用外部系统绑定的 `WECHAT_TYPE`、`WECHAT_KEY`、`WECHAT_SCOPE`。
- 微信登录成功后必须回到原 authorize 请求，再按标准 OAuth code 回调第三方。
- 本机验收使用 fake provider；真实微信登录作为部署后人工验收项。
- fake provider 不得默认启用，只能显式环境变量/系统属性或 smoke 临时启用。

## 维护者构建约定

README 中的维护者构建入口是：

```bash
cp settings.example.xml settings.local.xml
scripts/build-image.sh
scripts/build-api-image.sh
```

`scripts/build-image.sh` 和 `scripts/build-api-image.sh` 是本机单架构 smoke 入口。`v1.5.4` 起正式发布 GHCR 镜像必须使用：

```bash
scripts/build-multiarch-images.sh
```

后续正式版本的 Web/API 镜像必须同时包含 `linux/amd64` 和 `linux/arm64`；发布后必须用 `docker buildx imagetools inspect` 验证 manifest。不能再只把 Apple Silicon 本机 `docker build` 产生的 `linux/arm64` 镜像推送为正式 tag 或 `latest`。

维护相关约束：

- 维护者需要 Java 8、Maven、Docker，以及可访问历史依赖的 Maven 仓库。
- `settings.local.xml` 是本地文件，不应提交到 git。
- 发布 multi-arch 镜像前需要 Docker buildx builder 和 GHCR push 权限。
- 更多维护和发布细节见 `docs/maintenance.md`。

## 构建与排障顺序

遇到构建或导入异常时，按下面顺序排查：

1. 确认 `JAVA_HOME` 指向上面的 Java 8 JDK
2. 确认 Maven 使用 `-s settings.local.xml`
3. 确认 `/Volumes/vm/maven/repository` 已挂载且可读
4. 先跑窄范围模块，再跑全仓

常用验证命令：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
java -version
mvn -version
mvn -s settings.local.xml -pl dbtools -am -DskipTests compile
mvn -s settings.local.xml -DskipTests compile
docker compose ps
```

## 当前状态

截至 2026-04-26，本仓当前状态：

- VS Code 工作区已修正为 Java 8 + repo-local Maven settings
- `settings.local.xml` 与 `settings.example.xml` 已对齐到 `/Volumes/vm/maven/repository`
- 根 `pom.xml` 中已移除退役私服和旧的 distribution-management 引用
- 全仓 `mvn -s settings.local.xml -DskipTests compile` 已验证成功
- Maven 项目版本已切到 `1.1.0`
- 默认 `docker-compose.yml` 镜像 tag 已切到 `1.1.0`
- `database/bpmt-db.sql` 是 v1.1.0 最小初始化库，导入后 173 张表，其中 Activiti 24 张、Quartz 11 张
- `docker-compose.yml` 已完成配置瘦身，高级配置继续通过 `config/overrides/*.properties` 覆盖
- `scripts/build-image.sh` 已验证可构建本地镜像 `ghcr.io/wodenwang/bpmt-lite:1.1.0`
- 使用 `database/bpmt-db.sql` + 本地 `1.1.0` 镜像的临时 compose 验证通过：`/` 和 `/ueditor/` 均返回 200
- 使用 public-only 临时 Maven settings + 空本地 Maven 仓库执行 `mvn -s <tmp-settings> -DskipTests compile` 已验证成功

## v1.2.0 发布状态

截至 2026-04-28，v1.2.0 已完成发布收口，当前状态如下：

- 第一阶段修复 GitHub issue 已完成并关闭：
  - `#6`：已清理 `settings.example.xml` 中的本机 Maven 仓库路径和非必要镜像配置
  - `#7`：已通过 `ModelerServiceServlet` 恢复 Activiti Modeler `/service/*` 兼容端点，并验证 editor 打开、保存、关闭路径
  - `#8`：已通过 `log.path` 修复 BPMT 业务日志映射目录，`platform.log` 等日志落到 `runtime/platform-logs/`
- Docker 运行问题已推进：
  - Web 镜像构建不再强制 `linux/amd64`，本机 Apple Silicon 构建结果为 `linux/arm64`
  - `docker/Dockerfile` 改为 `eclipse-temurin:8-jdk-jammy` 并手动安装 Tomcat 7.0.109
  - 镜像内安装 `fonts-wqy-zenhei`，`activiti.font` 默认改为 `WenQuanYi Zen Hei`
  - `scripts/build-image.sh` 构建后会启动临时容器验证 `ROOT`、`ueditor`、entrypoint 和 CJK 字体
  - 已用容器内 Java2D 和 Activiti `DefaultProcessDiagramGenerator` 生成临时 PNG，确认中文可读；旧部署缓存 PNG 不会自动重绘
- 第二阶段整理初始化数据库正在推进：
  - `bpmt` 使用完整 `kyq` 数据源整理出的初始化 SQL
  - `bpmt_min` 使用最小初始化 SQL
  - 两个 database 允许在同一个 MariaDB 实例中共存
  - 默认初始化脚本导入 `bpmt`，参数 `min` 导入 `bpmt_min`
  - 当前删改后的 `bpmt` 已导出为本地 `database/bpmt.sql` 并压缩提交为 `database/bpmt.sql.gz`
  - 最小库也统一压缩提交为 `database/bpmt-min.sql.gz`
  - `bpmt` 完整库的 `admin` 密码已调整为 `admin`
  - 原始 `database/bpmt.sql` 约 127 MiB，超过 GitHub 普通仓库单文件限制，已加入 `.gitignore`
  - 旧 `db/init/kyq.sql` 已按用户要求从项目运行目录删除；用户另有源文件备份
- 第三阶段重构 README 已推进：
  - README 顶部已改为初学者 Docker 运行路径，不再先讲 Java/Maven 历史
  - 默认账号密码已放在启动说明旁边
  - 已说明 `bpmt` 与 `bpmt_min` 的共存、切换和重新初始化方式
  - README 已说明完整库 `database/bpmt.sql.gz` 会由初始化脚本自动解压为 `db/init/bpmt.sql`
- 第四阶段补齐团队开发模式：
  - 每个阶段要有可验证结果
  - 大改前先写 `docs/v1.2.0/*`
  - 需要 reviewer gate 时，先用文档清单审查再收口
- 第五阶段清理品牌信息：
  - 默认 logo 替换为透明底纯色 `BPMT` PNG
  - 默认 copyright 去掉 `Riversoft Designs`
  - 当前许可证为 MIT，主要作者为 `wodenwang` 和 `borball`
- 发布验收 gate 已推进：
  - `scripts/verify-repo.sh`、`docker compose config`、`mvn -s settings.local.xml -DskipTests compile` 已通过
  - `scripts/build-image.sh` 已通过
  - GitHub raw `main` 路径的一键脚本已验证完整库和最小库下载解压；发布后 README 使用 `v1.2.0` tag 路径
  - `bpmt` 与 `bpmt_min` 共存、两种 DB_NAME 下 `/` 与 `/ueditor/` 返回 200 已验证
  - 默认 logo、copyright、业务日志目录映射已完成运行验证
  - Maven 项目版本已切到 `1.2.0`
  - `ghcr.io/wodenwang/bpmt-lite:1.2.0` 与 `ghcr.io/wodenwang/bpmt-lite:latest` 已推送，digest 为 `sha256:083aeae6de6d1bc42c6c92a53599e431b5c87b839decc2f1b395f2d2ae715bef`
  - Git tag：`v1.2.0`
  - GitHub Release：`https://github.com/wodenwang/bpmt-lite/releases/tag/v1.2.0`

v1.2.0 文档见：

- `docs/v1.2.0/roadmap.md`
- `docs/superpowers/plans/2026-04-28-bpmt-lite-v1.2.0.md`

## v1.3.0 H5 状态

截至 2026-05-01，v1.3.0 已按保守 H5 策略收口，目标是恢复业务视图的移动端 H5 可浏览和主路径可操作能力：

- 已完成 A 方式止血：修复登录、首页、菜单、资源加载和 H5 路由。
- v1.3.0 已放弃激进重构，采用保守止血：保留原 AmazeUI H5 页面结构，只修复阻断 bug、资源缺失、路由分流和 `_action_mode=h5` 链路。
- 必达范围是 `dyn`、`flowbasic`、`rep_list`、`note`，以及 `viewer` 兼容降级。
- `dyn` 动态表当前按原 AmazeUI H5 页面恢复列表、查询、详情和新增入口；完整 CRUD 自动化闭环留作后续版本。
- `flowbasic` 工作流当前恢复入口、列表、详情、表单和普通办理主路径，并修复 H5 审批意见中文编码。
- `rep_list` 报表当前恢复移动端列表、查询、详情和下载兼容。
- `note` 公告当前恢复移动端列表、详情和返回 H5 链路。
- `viewer` 只做兼容降级：HTML/消息可读，下载类不破；不作为完整移动端页面框架重构对象。
- 后台管理模块“功能设置”域不属于 v1.3.0 移动端适配范围。
- 每一类视图必须列出具体业务 URL 逐条验收。
- C 级冒烟选择动态表代表路径和工作流普通办理路径，不要求本轮覆盖全部业务提交。
- 原始 BPMT 项目只作为路径和历史写法参考，不允许整包覆盖。
- 缺失 H5 运行文件优先参考 `/Users/wenzhewang/workspace/bpmt_project/运行时参考/platform`。

2026-05-01 使用完整 `bpmt` 数据库重新盘点业务视图：

- `dyn`：52 个。
- `flowbasic`：35 个。
- `rep_list`：21 个。
- `note`：1 个。
- `viewer`：32 个。

2026-05-01 已发现并修复一个业务视图入口阻断问题：

- 直接访问 `/dyn/A...Action/list.shtml`、`/flow/view/A...Action/main.shtml`、`/report/A...Action/list.shtml` 时，如果运行期动态 Action 类尚未由 `.view` 入口生成，会在 `ActionServlet.initAttributes` 中触发空指针并返回 500。
- 处理方式：`Actions.Util.getActionClass` 在类缺失且匹配动态业务视图 Action 命名时，按 `DynViewAction`、`FlowBasicViewAction`、`ReportListViewAction` 既有规则补生成运行期 Action 类；`ActionServlet` 对空 action URL 做防御，避免掩盖真实类缺失原因。
- 还从稳定运行时参考目录恢复 `/fonts/fontawesome-webfont.{woff2,woff,ttf}`，避免 AmazeUI 图标字体在 H5 业务页持续 404。

当前 v1.3.0 业务视图设计和验收文档：

- `docs/superpowers/specs/2026-05-01-bpmt-lite-v1.3.0-h5-business-view-design.md`
- `docs/superpowers/plans/2026-05-01-bpmt-lite-v1.3.0-h5-business-views.md`
- `docs/v1.3.0/h5-acceptance.md`

2026-05-01 当前实现进度：

- 2026-05-01 用户重新确认策略：v1.3.0 先保守止血，只修复 H5 阻断 bug，不改变原有 AmazeUI 业务页面结构和交互。
- 已修复并验证动态业务视图直连路由：`dyn`、`flowbasic`、`rep_list` 的 `A...Action` 直连 URL 不再因动态 Action 类未生成而 500。
- 已恢复 FontAwesome 字体资源，避免 AmazeUI 图标字体 404。
- 已撤回业务视图 JSP 中主动新增的 `bpmt-h5-*` 结构类和全局 `bpmt-h5.css/js` 引用；保留必要的 `_action_mode=h5` 链路修复、`Wxui.showLoading()` 防御和 `flow/view/form.jsp` 变量误用修复。
- 原 AmazeUI H5 动态表新增入口位于顶部左侧加号图标，受 `config.addBtn.pri` 权限控制；当前 `OA-事项资料库` 已验证该入口存在并指向 `createZone.shtml?_action_mode=h5`。
- H5 登录页在登录成功后应 `window.location.reload(true)`，这样登录态过期后由受保护业务 URL forward 到登录页时，成功登录会回到原目标 URL；不要改成固定跳首页。
- `.xhtml` 域入口是桌面入口，即使带 `_action_mode=h5` 也不做 H5 兜底；`DomainDirectFilter` 会强制按 `xhtml` 分流。H5 入口应使用 `/frame/FrameAction/domain.shtml?...&_action_mode=h5` 或业务 `.view`/Action H5 URL。
- `h5/frame_new/frame.jsp`、`h5/frame_new/menu.jsp`、`h5/frame_new/panel.jsp` 已删除；H5 不再为 `page.frame.new=true` 生成 frame_new 兜底壳。
- 已验证 `viewer` 按兼容降级处理：HTML/文本类返回 200，下载类保持下载响应，不改造成完整移动端组件。
- 代表 URL 验收已覆盖 `dyn`、`flowbasic`、`rep_list`、`note`、`viewer`，详见 `docs/v1.3.0/h5-acceptance.md`。
- 尚未完成真实写入闭环：动态表 CRUD 测试数据的新建/编辑/删除仍是后续 C 级冒烟项；工作流请假普通办理已验证 H5 中文审批意见可正常提交和显示。

2026-05-01 本地 smoke 状态：

- `scripts/build-image.sh` 已通过，生成本地镜像 `ghcr.io/wodenwang/bpmt-lite:1.3.0`。
- 临时 compose 项目 `bpmt-h5-smoke` 使用 `bpmt_min` 在 `http://127.0.0.1:18080/` 验证通过。
- H5 登录页、核心本地 CSS/JS、`admin/admin` 登录、首页、菜单、首页面板已通过移动端浏览器验收。
- 菜单页已验证不再生成 `http://frame`、`//frame/FrameAction` 等异常链接。
- C 级已冒烟“数据字典”和“流程设置”两条高频路径，浏览器 console 无 error/warn。
- 工作流请假单 `HRLE2605001` 已通过 H5 提交中文意见 `同意审批中文验证`，页面和数据库均显示 UTF-8 正常。
- 本机默认 `8080` 环境存在旧 `db/data`，本轮未删除用户本地数据；需要完整复验时应另起临时 compose 或先备份再重建数据目录。

## v1.4.0 API 规划状态

截至 2026-05-02，v1.4.0 已确认规划方向：新增一套面向 BPMT 的 API 接口层，优先服务 AI agent、飞书集成平台、N8N 等外部系统。

v1.4.0 API 总体方案：

- 新增独立 Maven 子模块 `api`，依赖 `platform`、`util`、`dbtools` 等现有模块。
- `api` 产出 `api.war`，运行在独立 Docker 服务中，默认访问路径为 `http://127.0.0.1:8081/api/`。
- `web` 继续作为现有 BPMT Web UI 服务，默认访问路径为 `http://127.0.0.1:8080/`。
- `web` 和 `api` 共用 MariaDB。
- `web` 和 `api` 都不能关闭 Hibernate cache。
- `web` 和 `api` 两个容器各自内嵌 Hazelcast member，通过 Docker Compose 网络启用 TCP/IP 组网。
- v1.4.0 不新增独立 Hazelcast Server 容器。
- API 文档公开发布，业务 API 使用 HMAC-SHA256 签名认证。

v1.4.0 首批 API 范围：

- 只开放动态表结构管理能力。
- 可以开放动态表列表、详情、创建、字段调整、DDL 同步、索引读取、索引调整、模板列表、模板预览、按模板建表。
- 不开放动态表删除接口。
- 不开放动态表业务数据 CRUD。
- 不开放批量业务数据导入、导出接口。
- 动态表结构写接口必须复用 `TableService`、`TbTable`、`TbColumn`、`TbIndex` 等现有对象，不能绕过平台服务直接写 `TB_TABLE`、`TB_COLUMN`、`TB_INDEX`、`TB_INDEX_COLUMN`。

统一 API 认证规范：

- 认证配置通过 Docker Compose 环境变量注入：
  - `BPMT_API_APP_KEY`
  - `BPMT_API_APP_SECRET`
  - `BPMT_API_ACT_AS`
- `BPMT_API_ACT_AS` 缺失或用户不存在时，兜底使用 `admin`。
- 业务 API 请求 Header 使用：
  - `X-BPMT-App-Key`
  - `X-BPMT-Timestamp`
  - `X-BPMT-Nonce`
  - `X-BPMT-Signature`
- 签名算法为 HMAC-SHA256。
- 签名字符串固定为：

```text
METHOD
PATH
NORMALIZED_QUERY
TIMESTAMP
NONCE
SHA256_HEX(BODY)
```

- `PATH` 必须是公开请求 URI，例如 `/api/v1/dynamic-tables`。
- `NORMALIZED_QUERY` 必须按解码后的参数名和值排序，再 URL encode。
- `appSecret` 不允许明文出现在请求 body 或 query 中。
- 文档端点不需要 HMAC 认证，业务 API 必须认证。

统一 API 技术用户规范：

- API 认证通过后，必须在当前请求线程初始化 `RequestContext`、`SessionContext`、`VariableContext`。
- 技术用户上下文必须按 BPMT 现有 `SessionManager` 语义填充 `USER`、`GROUP`、`ROLE`、`RELATION_SHIP`、`PRI_GROUP`、`PRI_POINT_LIST`、`SUPER_PRI_FLAG`、`DATE`、`IP`、`LOG`。
- 这样可以保证 `TableService.executeCreateTable()` 等现有逻辑中的 `SessionManager.getUser().getUid()` 正常工作。

统一 API 错误模型：

```json
{
  "success": false,
  "error": {
    "code": "DYNAMIC_TABLE_ALREADY_EXISTS",
    "message": "表[XXX]已存在。",
    "details": {},
    "requestId": "..."
  }
}
```

HTTP 状态约定：

- `400`：请求 JSON、字段类型或必填参数错误。
- `401`：缺少认证信息、appKey 不存在或签名错误。
- `403`：认证通过但技术用户不可用或权限不足。
- `404`：动态表或模板不存在。
- `409`：表已存在、字段冲突、索引冲突。
- `422`：动态表规则校验或 DDL 执行失败。
- `500`：未预期系统异常。

统一 API 文档规范：

- 公开 `GET /api/openapi.json`，用于 AI agent、N8N、飞书集成平台和后续 skill 生成。
- 公开 `GET /api/docs/`，用于 Web 方式人工阅读和调试。
- 每个对外接口必须包含 `summary`、`description`、请求 JSON Schema、响应 JSON Schema、错误码列表、HMAC 签名说明和 `curl` 示例。
- 每个对外接口必须标注：
  - `x-bpmt-writes-metadata`
  - `x-bpmt-executes-ddl`
  - `x-bpmt-risk-level`
- 文档中必须明确说明动态表结构写接口会修改数据库结构和 BPMT 元数据表。
- 文档中必须明确说明 v1.4.0 不支持删除表，不支持动态表业务数据 CRUD。

统一 API 测试和验收规范：

- 每个对外发布接口都必须完成单测。
- 单测至少覆盖路由、JSON 解析、认证失败、错误响应结构、字段校验、索引规则、技术用户兜底和错误码映射。
- 集成验收必须覆盖 `bpmt-web + bpmt-api + bpmt-mariadb` compose 启动。
- 集成验收必须先从 `web` 读取目标动态表结构，再通过 `api` 创建或调整动态表，最后在不重启 `web` 的情况下确认 Web 侧读到最新结构。
- 集成验收必须确认 `web` 和 `api` 日志中 Hazelcast 已加入同一集群。
- 如果 Hazelcast 双 member 验证失败，fallback 是收缩写接口发布范围，而不是关闭缓存。

v1.4.0 API 设计文档：

- `docs/superpowers/specs/2026-05-02-bpmt-lite-v1.4.0-api-design.md`
- `docs/v1.4.0/api-guidelines.md`
- `docs/v1.4.0/api-acceptance.md`
- `docs/v1.4.0/api-reference.md`
- `docs/v1.4.0/openapi.json`

2026-05-02 当前发布状态：

- `api` Maven 子模块已创建，产物为 `api.war`。
- API 统一 JSON 响应、HMAC 认证、固定技术用户上下文和 admin 兜底已实现。
- 已实现动态表结构接口：列表、详情、创建、更新、DDL 同步、模板列表。
- 已发布公开 `openapi.json` 和 `/docs/` 静态 Web 文档。
- 已归档 Markdown 版 API 文档 `docs/v1.4.0/api-reference.md` 和 OpenAPI 快照 `docs/v1.4.0/openapi.json`，后续给 AI agent、N8N、飞书集成平台封装时优先读取 OpenAPI 快照。
- 已新增独立 API Dockerfile、`scripts/build-api-image.sh`、compose `api` 服务和 `scripts/smoke-api.sh`。
- 本地已验证 `docker compose config`、Java 8 全仓编译、API 单测、`scripts/build-image.sh`、`scripts/build-api-image.sh` 和 `scripts/smoke-api.sh` 通过，生成 `ghcr.io/wodenwang/bpmt-lite:1.4.0` 与 `ghcr.io/wodenwang/bpmt-lite-api:1.4.0`。
- Maven 版本、compose 默认 Web/API 镜像 tag 和发布文档已切换到 `1.4.0`。
- 2026-05-02 人工完整复测使用 `TMP_COWORK_V2`：总计 19 项，PASS 16 项，FAIL 0 项，SKIP 3 项。
- 本地 Web/API Hazelcast 双 member 验证通过，日志显示 `Members [2]`。
- `ghcr.io/wodenwang/bpmt-lite:1.4.0` 与 `ghcr.io/wodenwang/bpmt-lite-api:1.4.0` 已推送，匿名拉取验证通过；两个 `latest` tag 已同步到对应 digest。
- 发布后已用 `v1.4.0` raw `scripts/run.sh`、最小库 `bpmt_min` 和发布镜像做独立临时 compose 验证；`/`、`/ueditor/`、API 文档、API smoke 和 Hazelcast 双 member 均通过。

## v1.5.0 OAuth 登录状态

截至 2026-05-03，v1.5.0 已完成发布收口：在 `bpmt-web/platform` 内新增外部系统 OAuth 登录能力，让 BPMT 作为 OAuth2 Authorization Code 服务端复用现有用户、登录页和权限体系。

当前状态摘要：

- OAuth 主流程只在 `bpmt-web/platform`，不改 `bpmt-api`。
- `bpmt-api` 的 `/api/docs/` 和 `/api/openapi.json` 仍需作为回归验收项返回 200。
- 外部系统主数据使用 `CM_THIRDPART`，授权码使用 `CM_THIRDPART_AUTH_CODE`，token 使用 `CM_THIRDPART_ACCESS_TOKEN`。
- `database/v1.5.0-oauth-tables.sql` 是三张 OAuth 表的初始化 SQL 片段，已追加进完整库、最小库和旧明文最小库。
- 默认初始化菜单包含 `sys_thirdpart`，后台入口为 `系统开发 -> 第三方系统`，位于 `用户菜单` 下方。
- `权限组管理` 必须包含 `第三方系统权限` 页签，用于分配 `CM_THIRDPART.PRI_KEY`。
- `code`、`access_token`、`clientSecret` 明文不入库；DB 只保存 hash，DB 是 OAuth 运行态 source of truth。
- `/oauth/token` 和 `/oauth/userinfo` 使用 OAuth JSON，不使用 `success/data/error` 包装；`/oauth/authorize` 是浏览器跳转或错误页。
- OAuth 主流程必须有 `INFO` 日志，且不能记录明文 `code`、`access_token`、`client_secret`、`password`。
- 菜单第三方 URL / iframe 是辅助入口，不是 OAuth 主流程。
- `userid + thirdpartKey` 独立权限校验 API 暂不纳入 v1.5.0，后续版本单独设计。
- v1.5.0 不包含独立 `bpmt-thirdpart-login-demo` 仓库。
- Maven 项目版本已切到 `1.5.0`。
- 默认 Web/API 镜像 tag 已切到 `1.5.0`。
- `scripts/install.sh` 和 `scripts/run.sh` 默认 tag 已切到 `v1.5.0`。
- 本地已验证 `docker compose config`、Java 8 全仓编译、API 单测、OAuth/第三方系统目标测试、Web/API 镜像构建和临时 compose smoke。
- `ghcr.io/wodenwang/bpmt-lite:1.5.0` 与 `ghcr.io/wodenwang/bpmt-lite-api:1.5.0` 已推送；两个 `latest` tag 已同步到对应 digest。

v1.5.0 文档见：

- `docs/v1.5.0/oauth-login-reference.md`
- `docs/v1.5.0/oauth-login-acceptance.md`
- `docs/release-v1.5.0.md`

## v1.5.1 issue #10 补丁状态

截至 2026-05-04，v1.5.1 是已发布补丁版本，目标是修复完整库 `bpmt` 下 `/flow/CommonFlowAction/taskList.shtml` 的“查看/处理”跳转问题，确认不再生成 `_ORD_ID=null`。

发布状态摘要：

- Maven 项目版本已切到 `1.5.1`。
- 默认 Web/API 镜像 tag 已切到 `1.5.1`。
- `scripts/install.sh`、`scripts/run.sh` 和 `scripts/init-db.sh` 默认 release/raw tag 已切到 `v1.5.1`。
- `api/src/main/webapp/openapi.json` 仅更新 `info` 标题和版本到 `v1.5.1`；不新增 OAuth 端点，不改 API 路径。
- v1.5.0 OAuth 能力继续保留，`bpmt-api` 仍作为回归验收入口。
- v1.5.1 验收必须使用完整库 `bpmt` 浏览器实点 `/flow/CommonFlowAction/taskList.shtml` 的“查看/处理”，确认网络请求中无 `_ORD_ID=null`。

v1.5.1 文档见：

- `docs/v1.5.1/issue-10-acceptance.md`
- `docs/release-v1.5.1.md`

## v1.5.2 OAuth 登录态切换状态

截至 2026-05-04，v1.5.2 是已发布补丁版本，基于 `v1.5.1` 增强外部系统 OAuth 登录体验：

- 浏览器已有 BPMT 登录态时，`/oauth/authorize` 继续复用当前 BPMT 用户。
- 当前 BPMT 用户无目标第三方系统权限时，显示 BPMT 内部提示页。
- 用户可以退出当前 BPMT 账号并重新登录其他账号，也可以取消并返回第三方 `access_denied`。
- README 增加 demo 项目链接 `https://github.com/wodenwang/bpmt-oauth-demo`。
- `clientSecret` 保持原规则：新增时只展示一次，编辑时只能重置，数据库只保存 hash。

## v1.5.3 nginx Host 转发补丁状态

截至 2026-05-04，v1.5.3 是基于 `v1.5.2` 修复发布验收 review 发现的阻塞点：

- `docker/nginx/nginx.conf` 的三处代理转发统一使用 `proxy_set_header Host $http_host;`，保留 `BPMT_HTTP_PORT=18080` 等非 80 端口。
- OAuth 登录页、授权页、无权限切换页和第三方回调地址在非 80 端口运行时不得丢失端口。
- v1.5.2 的 OAuth 登录态切换能力继续保留。
- v1.5.1 issue #10 工作流待办“查看/处理”跳转修复继续保留。
- 默认 Web/API 镜像 tag、脚本默认 release tag 和 OpenAPI info 已切到 `1.5.3`。

## v1.5.4 multi-arch 发布状态

截至 2026-05-05，已确认 `ghcr.io/wodenwang/bpmt-lite:1.5.3` 和 `ghcr.io/wodenwang/bpmt-lite-api:1.5.3` 的正式 tag 只包含 `linux/arm64` manifest，x86_64 Linux 服务器无法拉取匹配架构镜像。

`v1.5.4` 及之后版本的发布修复原则：

- 不需要为 multi-arch 改业务代码，Java WAR 产物保持同一份。
- Web/API 镜像正式发布统一走 `scripts/build-multiarch-images.sh`。
- 默认平台是 `linux/amd64,linux/arm64`。
- 默认同步版本 tag 和 `latest`。
- 发布验收必须记录 Web/API 两个镜像的 manifest，确认包含 `linux/amd64` 和 `linux/arm64`。
- 发布验收必须至少在 x86_64 Linux 环境完成一次 `docker compose pull` 或 `docker compose up -d` smoke。

2026-05-05 当前发布结果：

- Maven 项目版本已切到 `1.5.4`。
- 默认 Web/API 镜像 tag、脚本默认 release tag 和 OpenAPI info 已切到 `1.5.4`。
- `docker/Dockerfile` 与 `docker/Dockerfile.api` 已区分 amd64 与 ports 架构 apt 镜像源，避免 amd64 multi-arch build 卡在默认 Ubuntu 源。
- `ghcr.io/wodenwang/bpmt-lite:1.5.4` 已推送，manifest digest 为 `sha256:41efc7c12a72ea7d01c175602562bcfc99330f99dd8137f81101a5311048466b`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.5.4` 已推送，manifest digest 为 `sha256:6e8ee82982e74270755790202c9237f7dc70c2c002e8df1a5eacdfef4fcabd78`，包含 `linux/amd64` 和 `linux/arm64`。
- 两个 `latest` tag 已同步到 `1.5.4` manifest digest。
- 已强制 `--platform linux/amd64` 拉取 Web/API `1.5.4` 镜像验证通过。
- 临时 compose 项目 `bpmt-v154-smoke` 使用最小库 `bpmt_min` 验证 `/`、`/ueditor/`、`/api/docs/`、`/api/openapi.json`、API smoke、176 张表和 Hazelcast 双 member 均通过。

## v1.6.0 HTTPS 支持状态

截至 2026-05-05，v1.6.0 已完成发布收口，目标是让 `bpmt-lite` 的 Web、UEditor、OAuth、H5 和 API 都能在 HTTPS 公开入口下正确运行，同时保留默认 HTTP 快速启动体验。

当前实现约定：

- 基础 `docker-compose.yml` 默认只发布 HTTP，不占用 443。
- 启用内置 HTTPS 时使用 `docker-compose.https.yml` 叠加 HTTPS 端口和 `certs/` 证书挂载。
- `scripts/run.sh` 在 `BPMT_HTTPS_ENABLED=1` 时会生成或复用自签证书，并使用 `docker compose -f docker-compose.yml -f docker-compose.https.yml up -d`。
- `scripts/generate-self-signed-cert.sh` 默认生成 `certs/fullchain.pem` 和 `certs/privkey.pem`，证书目录不提交 git。
- `scripts/render-nginx-conf.sh` 支持 HTTP-only、HTTPS redirect、HTTPS no-redirect 三种 nginx 配置。
- HTTP 到 HTTPS 默认 301，可用 `BPMT_HTTP_REDIRECT=false` 关闭。
- 可信上游网关终止 TLS 后转发到本 nginx HTTP 入口时，必须用 `BPMT_UPSTREAM_TLS_ENABLED=1` 重新渲染 nginx 配置，让后端看到公开 `https` scheme。
- 非标准端口必须通过 `BPMT_HTTP_PORT`、`BPMT_HTTPS_PORT` 写入 `X-Forwarded-Port`，redirect 也必须保留公开 HTTPS 端口。
- 后端公开 URL 统一通过 `Actions.Util.getContextPath()` 和 `Actions.Util.getFullURL()` 读取可信 `X-Forwarded-*` 头生成。
- `OAuthDirectFilter` 不信任公网 `/oauth/authorize` 请求自带的 `_full_url` 参数，只从当前 request URI 和 query string 派生转发 URL。
- H5 运行主路径已去掉阻断 HTTPS 的 HTTP CDN 资源；微信官方脚本改用 HTTPS。
- `scripts/smoke-api.sh` 支持 `BPMT_API_CURL_INSECURE=1`，用于自签证书 HTTPS API smoke。
- `ghcr.io/wodenwang/bpmt-lite:1.6.0` 已推送，manifest digest 为 `sha256:65409ca2ab7d187cb71bc1a8ba89a08058a83ccadd7ab72787bfdc8e7b463605`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.6.0` 已推送，manifest digest 为 `sha256:c7ad44f0fd6e0b9d96aa8d555512a3d85017686675c4c921720fb024a8a39452`，包含 `linux/amd64` 和 `linux/arm64`。
- `latest` 已同步到上述 Web/API manifest digest。

v1.6.0 文档见：

- `docs/superpowers/specs/2026-05-05-bpmt-lite-v1.6.0-https-design.md`
- `docs/superpowers/plans/2026-05-05-bpmt-lite-v1.6.0-https.md`
- `docs/v1.6.0/https-acceptance.md`
- `docs/release-v1.6.0.md`

## v1.7.0 动态表视图 API 发布状态

截至 2026-05-10，v1.7.0 已完成发布收口，目标是把原本依赖前端页面操作的 `dyn` 动态表视图配置能力开放给外部系统和 AI agent。

发布结果：

- Maven 项目版本已切到 `1.7.0`。
- 默认 Web/API 镜像 tag、安装脚本默认 release tag 和 OpenAPI info 已切到 `1.7.0`。
- Git tag：`v1.7.0`，对应提交 `fc2e34b`。
- GitHub Release：`https://github.com/wodenwang/bpmt-lite/releases/tag/v1.7.0`。
- `ghcr.io/wodenwang/bpmt-lite:1.7.0` 已推送，manifest digest 为 `sha256:c8ac35468773fdf75eca2c3288d6c78f726b3d9cd56ac1057ecb2639885879f7`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.7.0` 已推送，manifest digest 为 `sha256:f07c57b935ad3b2405dbdefb1c28f8bdd7a33aa74626f23053a1b7203c632dd7`，包含 `linux/amd64` 和 `linux/arm64`。
- 两个 `latest` tag 已同步到 `1.7.0` manifest digest。
- 本地默认 compose 已使用 `1.7.0` Web/API 镜像验证 `/`、`/ueditor/`、`/api/docs/`、`/api/openapi.json` 均返回 200。

已验收能力：

- 动态表视图 API 支持 validate、dry-run、导出、创建、整体替换、分区 patch 和带确认删除。
- 删除视图配置不会删除动态表、业务数据、日志表或日志数据。
- API 只管理 `/{viewKey}.view` 对应的 dyn 视图配置；菜单、首页卡片和按钮入口不属于 v1.7.0 范围。
- Web/API Hazelcast 双 member 验证通过。

v1.7.0 文档见：

- `docs/superpowers/specs/2026-05-10-bpmt-lite-v1.7.0-dynamic-table-view-api-design.md`
- `docs/superpowers/plans/2026-05-10-bpmt-lite-v1.7.0-dynamic-table-view-api.md`
- `docs/v1.7.0/api-reference.md`
- `docs/v1.7.0/openapi.json`
- `docs/v1.7.0/dynamic-table-view-acceptance.md`
- `docs/release-v1.7.0.md`

## v1.7.1 报表视图 API 发布状态

截至 2026-05-16，v1.7.1 已完成发布收口，目标是把原本依赖前端页面操作的 `rep_list` 报表视图配置能力开放给外部系统和 AI agent。

发布结果：

- Maven 项目版本已切到 `1.7.1`。
- 默认 Web/API 镜像 tag、安装脚本默认 release tag 和 README 当前版本已切到 `1.7.1`。
- Git tag：`v1.7.1`。
- GitHub Release：`https://github.com/wodenwang/bpmt-lite/releases/tag/v1.7.1`。
- `ghcr.io/wodenwang/bpmt-lite:1.7.1` 已推送，manifest digest 为 `sha256:8c82eabc4e87193d02c024f62e1d64a42570520cb41de9e3de3e481a88c34009`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.7.1` 已推送，manifest digest 为 `sha256:02ed7e782f1ad027d6500caa065059011971cad06e12e495ae5a455f94790fdc`，包含 `linux/amd64` 和 `linux/arm64`。
- 两个 `latest` tag 已同步到 `1.7.1` manifest digest。
- 临时 compose 使用 `bpmt_min` 和 `1.7.1` Web/API 镜像验证 `/`、`/ueditor/`、`/api/docs/`、`/api/openapi.json` 均返回 200。
- `scripts/smoke-api.sh` 通过 `http://127.0.0.1:18081/api` 签名 smoke。

已验收能力：

- 报表视图 API 支持 validate、dry-run、导出、创建、整体替换、分区 patch 和带确认删除。
- 删除视图配置不会删除业务数据、菜单、首页卡片或外部入口。
- API 只管理 `/{viewKey}.view` 对应的 `rep_list` 视图配置；菜单、首页卡片和外部入口不属于 v1.7.1 范围。
- 报表主 SQL、查询 SQL、约束 SQL、按钮动作、客户端脚本和外部 `dbKey` 会进入脚本风险提示，但 API 不执行这些 SQL 或脚本。

v1.7.1 文档见：

- `docs/superpowers/specs/2026-05-15-bpmt-lite-v1.7.1-report-view-api-design.md`
- `docs/superpowers/plans/2026-05-15-bpmt-lite-v1.7.1-report-view-api.md`
- `docs/v1.7.1/api-reference.md`
- `docs/v1.7.1/openapi.json`
- `docs/release-v1.7.1.md`

## v1.7.2 GitHub issue bugfix 状态

截至 2026-05-16，v1.7.2 已完成发布收口，目标是先处理 GitHub issue 上的已知 bug。

修复范围：

- issue #17：报表视图 API 创建时 `validate`、`dryRun` 通过但实际写入 500。
- issue #15：动态表视图和报表视图配置写入后未清理运行时视图缓存。
- issue #14：控制面板注册信息没有显示实际 bpmt-lite 版本。
- 同步修复数据库操作 `save` 显式主键 insert 无自增 key 时的 500 回归。

版本边界：

- Maven 项目版本、默认 Web/API 镜像 tag、安装脚本默认 release tag 和 README 当前版本已切到 `1.7.2`。
- Git tag：`v1.7.2`。
- GitHub Release：`https://github.com/wodenwang/bpmt-lite/releases/tag/v1.7.2`。
- `ghcr.io/wodenwang/bpmt-lite:1.7.2` 已推送，manifest digest 为 `sha256:2568da6d7531dab48b96ce81c06195be7f90a8dac5e3af4b2da7b15bd21c2976`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.7.2` 已推送，manifest digest 为 `sha256:831812d553f66799559d993ab3c301e37e14e8657d10d9be1a8b7172fca8b51a`，包含 `linux/amd64` 和 `linux/arm64`。
- 两个 `latest` tag 已同步到 `1.7.2` manifest digest。
- 用户本地 Docker 环境已使用 `1.7.2` Web/API 镜像验收通过。
- API 不新增路径，不改变认证模型，不改变 OAuth、HTTPS、H5 或 Docker 第三方容器策略。
- 发布说明和 API 快照归档到 `docs/release-v1.7.2.md`、`docs/v1.7.2/api-reference.md` 和 `docs/v1.7.2/openapi.json`。

v1.7.2 文档见：

- `docs/superpowers/plans/2026-05-16-bpmt-lite-v1.7.2-github-issue-bugfixes.md`
- `docs/v1.7.2/api-reference.md`
- `docs/v1.7.2/openapi.json`
- `docs/release-v1.7.2.md`

## v1.7.3 GitHub issue bugfix 状态

截至 2026-05-16，v1.7.3 已完成发布收口，目标是修复 OAuth 微信登录失败页提示和 OpenAPI 文档风格问题。

修复范围：

- issue #18：企业微信授权成功但 BPMT 本地登录态建立失败时，错误页需要展示更清晰的 BPMT 侧失败原因。
- issue #19：`/api/openapi.json` 的 `info.description` 和报表视图接口 summary 前后风格不一致，需要统一中文文档风格。

版本边界：

- 不新增 API 路径，不改变 HMAC 签名规则。
- 不改变 OAuth 主流程，只增强微信授权后 BPMT 登录态建立失败的错误分类和页面标题。
- 不改变数据库初始化结构、Docker Compose 拓扑或第三方容器版本策略。
- Maven 项目版本、默认 Web/API 镜像 tag、安装脚本默认 release tag 和 README 当前版本已切到 `1.7.3`。
- Git tag：`v1.7.3`。
- GitHub Release：`https://github.com/wodenwang/bpmt-lite/releases/tag/v1.7.3`。
- `ghcr.io/wodenwang/bpmt-lite:1.7.3` 已推送，manifest digest 为 `sha256:b3dd5635108a892a963d9cfddcb6b310cd20db31e7d0a34e9365f142101877e5`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.7.3` 已推送，manifest digest 为 `sha256:001ecf794f1d819ea44a984df5d12bdec910ce3d72ebf6fb0b40dad4518c0845`，包含 `linux/amd64` 和 `linux/arm64`。
- 两个 `latest` tag 已同步到 `1.7.3` manifest digest。
- 发布说明、计划和 API 快照归档到 `docs/release-v1.7.3.md`、`docs/superpowers/plans/2026-05-16-bpmt-lite-v1.7.3-github-issue-bugfixes.md`、`docs/v1.7.3/api-reference.md` 和 `docs/v1.7.3/openapi.json`。

v1.7.3 文档见：

- `docs/superpowers/plans/2026-05-16-bpmt-lite-v1.7.3-github-issue-bugfixes.md`
- `docs/v1.7.3/api-reference.md`
- `docs/v1.7.3/openapi.json`
- `docs/release-v1.7.3.md`

## v1.7.4 登录后灰色蒙版 bugfix 状态

截至 2026-05-20，v1.7.4 正在推进发布收口，目标是修复登录后偶发出现灰色底蒙版的问题。

修复范围：

- 整页加载结束、根页面初始化和浏览器 `pageshow` 恢复时，统一清理孤儿 `#loading`、`.ui-widget-overlay`、`.am-dimmer` 和加载 modal。
- 正常已打开弹窗的遮罩不清理，避免破坏现有 jQuery UI / AmazeUI 弹窗行为。
- `include/html_bottom.jsp` 不再只写入 `#loading{display:none}`，优先调用 `Core.clearPageMask()`。
- 新增 `LoginMaskCleanupTest` 覆盖遮罩清理入口和整页模板调用。

版本边界：

- 不新增 API 路径，不改变 HMAC 签名规则。
- 不改变 OAuth 主流程、数据库初始化结构、Docker Compose 拓扑或第三方容器版本策略。
- Maven 项目版本、默认 Web/API 镜像 tag、安装脚本默认 release tag 和 README 当前版本已切到 `1.7.4`。
- Git tag：`v1.7.4`。
- GitHub Release：`https://github.com/wodenwang/bpmt-lite/releases/tag/v1.7.4`。
- `ghcr.io/wodenwang/bpmt-lite:1.7.4` 已推送，manifest digest 为 `sha256:11fc93e989b3dce4698f63b2fff082097600f4c6f6c8cf9b815cb343f2d4c77b`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.7.4` 已推送，manifest digest 为 `sha256:2567aaa64f02a440e5a40016356ef0b15bf57accba33c7501ab5c0821b1ded06`，包含 `linux/amd64` 和 `linux/arm64`。
- 两个 `latest` tag 已同步到 `1.7.4` manifest digest。

v1.7.4 文档见：

- `docs/superpowers/plans/2026-05-20-bpmt-lite-v1.7.4-login-mask.md`
- `docs/v1.7.4/api-reference.md`
- `docs/v1.7.4/openapi.json`
- `docs/release-v1.7.4.md`

## v1.7.5 第三方系统 AI 接入提示词状态

截至 2026-05-30，v1.7.5 正在推进发布收口，目标是在“第三方系统”管理列表页生成可复制到 Codex 或 Claude Code 第三方项目的 AI 接入提示词。

版本边界：

- 只增强 `bpmt-web/platform` 第三方系统管理体验，不新增 OAuth endpoint。
- 不改变 BPMT API HMAC 鉴权规则，不新增数据库结构。
- 列表页不会从数据库反查或输出明文 `clientSecret`，也不输出 `clientSecretHash`。
- 弹框中临时填写的 `clientSecret`、API App Key 和 API App Secret 只用于浏览器本地生成提示词，不提交、不落库、不写日志。
- 提示词必须覆盖 `AGENTS.md` / `CLAUDE.md` 初始化、BPMT OAuth Authorization Code、`/oauth/authorize`、`/oauth/token`、`/oauth/userinfo`、`state` 校验、`userinfo` 建立第三方系统登录态、BPMT API HMAC 请求头和 canonical path 规则。
- Maven 项目版本、默认 Web/API 镜像 tag、安装脚本默认 release tag、README 当前版本和 OpenAPI 版本已切到 `1.7.5`。
- `ghcr.io/wodenwang/bpmt-lite:1.7.5` 已推送，manifest digest 为 `sha256:1cfe7d9efd790933689da70a8a94defd3fd358326384e71165157bda2995d19f`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.7.5` 已推送，manifest digest 为 `sha256:36e66616d3670bdf4d330304ef15c5a2e3a8160be0624578212c6a024dae29a0`，包含 `linux/amd64` 和 `linux/arm64`。
- 两个 `latest` tag 已同步到 `1.7.5` manifest digest。

v1.7.5 文档见：

- `docs/superpowers/plans/2026-05-30-bpmt-lite-v1.7.5-thirdpart-ai-onboarding-prompt.md`
- `docs/v1.7.5/api-reference.md`
- `docs/v1.7.5/openapi.json`
- `docs/release-v1.7.5.md`

## v1.8.0 桌面后台清晰度基线状态

截至 2026-06-12，`v1.8.0 modern-theme foundation` 分支已被判定为方向失败。失败原因是 CSS-only 主题覆盖只能让页面变浅、变白、变圆角，不能解决后台更关键的当前位置、页面任务、空/加载/错误状态、主操作和导航层级问题。

新的 `v1.8.0` 已按 `desktop admin clarity baseline` 收口，目标是先围绕三条代表路径建立清晰度样板：

- 登录后首页。
- 第三方系统列表。
- AI 接入提示词弹窗。

本版本要求：

- 不再以全局 CSS-only 现代主题作为发布目标。
- 不改移动端 H5、前端技术栈、菜单权限、OAuth、API 鉴权、动态表、报表或流程业务逻辑。
- 保留 `bpmt-modern.css` 文件名作为可回滚覆盖层，但内容定位为代表路径清晰度增强。
- 实现态截图保存到 `docs/v2.0.0/screenshots/v1.8.0-clarity-implemented/`。
- 验证记录保存到 `docs/release-v1.8.0.md` 和 `docs/v2.0.0/desktop-ui-qa-checklist.md`。

v1.8.0 新规划文档见：

- `docs/v2.0.0/desktop-admin-clarity-replan.md`
- `docs/superpowers/specs/2026-06-11-bpmt-lite-v1.8.0-desktop-admin-clarity-design.md`
- `docs/superpowers/plans/2026-06-11-bpmt-lite-v1.8.0-desktop-admin-clarity.md`

旧方向文档仅作为失败方向记录，不再作为实现依据：

- `docs/superpowers/specs/2026-06-11-bpmt-lite-v1.8.0-modern-theme-foundation-design.md`
- `docs/superpowers/plans/2026-06-11-bpmt-lite-v1.8.0-modern-theme-foundation.md`
- `docs/v2.0.0/v1.8.0-modern-theme-foundation-design.md`
- `docs/v1.5.0/oauth-login-reference.md`

## 线上测试环境交接

线上测试环境的非敏感入口、Docker 部署结构、OAuth demo 配置线索和 2026-05-05 微信 OAuth 登录问题结论已记录在：

- `docs/ops/online-test-environment.md`

敏感信息不写入可提交文档。SSH 密码等仅保存在本机忽略文件：

- `docs/ops/local-secrets.md`

后续 agent 跟进线上问题时，应先读 `docs/ops/online-test-environment.md`，再按实时环境验证。不要把本机敏感文件内容提交到 Git 或复制到公开文档。

## 原始项目参考源

当本仓出现历史源码缺失、H5 页面包含关系不清、旧模板路径不一致等问题时，可以参考稳定运行时目录和原始 BPMT 项目，但不能直接整包覆盖本仓：

- 稳定运行时参考目录：`/Users/wenzhewang/workspace/bpmt_project/运行时参考/platform`
- 本机原始项目路径：`/Users/wenzhewang/workspace/bpmt_project/bpmt`
- GitHub：`https://github.com/wodenwang/bpmt`

使用规则：

- 先在本仓确认缺失或异常，再到稳定运行时参考目录中查对应文件、路径和当前可运行写法。
- 原始项目用于补充历史源码对照；当稳定运行时参考目录与原始项目不一致时，优先以稳定运行时参考目录为准。
- 原始项目只能作为对照和借鉴来源，不能恢复已在 `bpmt-lite` 中明确割舍的依赖、模块或私服配置。
- 任何从原始项目借鉴的代码都必须符合本仓边界：Java 8、Maven 3、Tomcat 7、MariaDB，不升级技术栈，不增加无关功能。
- H5 相关对照优先看：
  - `platform/src/main/webapp/h5/**`
  - `platform/src/main/webapp/include/**`
  - `platform/src/main/java/com/riversoft/core/web/Actions.java`
  - `platform/src/main/java/com/riversoft/module/frame/**`
- 2026-05-01 已核对：原始项目中 `h5_head.jsp` 位于 `platform/src/main/webapp/h5/widget/h5_head.jsp`，并不存在 `platform/src/main/webapp/include/h5_head.jsp`；而 H5 JSP 多处 include `/include/h5_head.jsp`。v1.3.0 处理 H5 登录/首页问题时，应把 include 路径恢复或兼容作为显式任务，不能假设原始项目已有正确 include 文件。
- 2026-05-01 已核对：稳定运行时参考目录中存在 `include/h5_head.jsp`，并且本地化了 AmazeUI 资源为 `/css/amazeui.min.css` 与 `/js/amazeui.min.js`。v1.3.0 恢复缺失 H5 文件时，应优先参考该运行时目录。

## 后续 agent 编辑规则

- 保持 Java 8 兼容性。
- 未经用户明确要求，不做技术栈升级。
- 不要凭印象恢复已退役私服配置。
- 涉及运行、打包、初始化数据库、目录语义时，先以 README 和本文件为准。
- 如果用户要求记录当前阶段、当前环境或交接状态，优先更新本文件。
