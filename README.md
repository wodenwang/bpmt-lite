# bpmt-lite

## 项目介绍

`bpmt-lite` 是 BPMT 低代码平台的简化发行工程。BPMT 表示 BPM + table，核心能力是自定义工作流、动态表格、H5 业务视图、开放 API 和外部系统 OAuth 登录。

本仓只处理遗留 BPMT 的发行工程：代码结构、打包方式、配置方式、Docker 运行方式、初始化数据和升级脚本。不升级 Java/Tomcat/MariaDB 技术栈，不重写业务功能。

当前版本：`v1.7.0`

- Web 镜像：`ghcr.io/wodenwang/bpmt-lite:1.7.0`
- API 镜像：`ghcr.io/wodenwang/bpmt-lite-api:1.7.0`
- 同步镜像：`ghcr.io/wodenwang/bpmt-lite:latest`、`ghcr.io/wodenwang/bpmt-lite-api:latest`
- 默认访问地址：`http://127.0.0.1/`
- API 文档：`http://127.0.0.1/api/docs/`
- OpenAPI：`http://127.0.0.1/api/openapi.json`
- 默认账号：`admin/admin`

## Quick Start

不需要 clone 项目，直接执行一条命令完成安装、初始化数据库并启动服务：

```bash
curl -fsSL https://github.com/wodenwang/bpmt-lite/raw/refs/tags/v1.7.0/scripts/install.sh | bash
```

脚本会创建 `bpmt-lite/` 运行目录，下载 `docker-compose.yml`、初始化脚本、升级脚本、nginx 配置模板和默认数据库，并执行 `docker compose up -d`。

访问：

```text
http://127.0.0.1/
```

登录：

```text
用户名：admin
密码：admin
```

启用本地 HTTPS：

```bash
curl -fsSL https://github.com/wodenwang/bpmt-lite/raw/refs/tags/v1.7.0/scripts/install.sh | BPMT_HTTPS_ENABLED=1 bash
```

升级到最新版本：

```bash
cd bpmt-lite
sh ./upgrade.sh
```

`upgrade.sh` 会根据 GitHub 最新 release/tag 下载参考 compose 文件，例如 `docker-compose-v1.7.0.yml`；不会覆盖当前 `docker-compose.yml`。升级时只拉取 BPMT Web/API 的 `latest` 镜像并重启这两个服务，不自动升级 `mariadb`、`nginx` 等第三方容器。升级状态记录在 `.bpmt-lite/` 下，不写入业务数据库。

常用检查：

```bash
docker compose ps
curl -fsSI http://127.0.0.1/
curl -fsSI http://127.0.0.1/ueditor/
curl -fsSI http://127.0.0.1/api/docs/
curl -fsSI http://127.0.0.1/api/openapi.json
```

## 动态表视图 API

`v1.7.0` 新增动态表视图配置 API，面向外部 AI agent、自动化平台和集成脚本开放 `dyn` 视图配置管理能力。

- 接口前缀：`/api/v1/dynamic-table-views`
- 能力范围：读取、校验、创建、整体替换、分区 patch、带确认删除 `/{viewKey}.view` 对应的 dyn 视图配置。
- 安全边界：只管理视图配置，不发布菜单、首页卡片或按钮入口；删除视图不会删除动态表、业务数据、日志表或日志数据。
- 变更预检：写接口支持 `dryRun=true`，也可使用 `POST /api/v1/dynamic-table-views:validate` 做纯校验。
- 文档归档：[动态表视图 API](docs/v1.7.0/api-reference.md)，[OpenAPI 快照](docs/v1.7.0/openapi.json)。

## 报表视图 API

`v1.7.1` 新增报表视图配置 API，面向外部 AI agent、自动化平台和集成脚本开放 `rep_list` 报表视图配置管理能力。

- 接口前缀：`/api/v1/report-views`
- 能力范围：读取、校验、创建、整体替换、分区 patch、带确认删除 `/{viewKey}.view` 对应的 `rep_list` 报表视图配置。
- 安全边界：只管理 `VW_URL` 与 `VW_REPORT*` 元数据，不管理菜单、首页卡片、外部入口或报表业务数据；删除视图不会删除业务数据。
- 风险提示：API 不执行 SQL、按钮动作或客户端脚本，调用方需要通过实际业务页面或验收脚本确认 SQL 语义、结果列、权限过滤和运行性能。
- 变更预检：写接口支持 `dryRun=true`，也可使用 `POST /api/v1/report-views:validate` 做纯校验。
- 文档归档：[v1.7.1 API 归档](docs/v1.7.1/api-reference.md)，[v1.7.1 OpenAPI](docs/v1.7.1/openapi.json)。

## 文件结构

安装后的 `bpmt-lite/` 运行目录主要包含：

| 路径 | 说明 |
| --- | --- |
| `docker-compose.yml` | 当前运行使用的 Docker Compose 配置，升级脚本不会覆盖 |
| `docker-compose-v*.yml` | 升级时下载的目标版本参考配置，仅供对照 |
| `.env` | 本地运行环境变量，升级脚本会把 Web/API 镜像 tag 设为 `latest` |
| `.bpmt-lite/` | 安装与升级状态记录、升级日志、临时 manifest 和备份文件 |
| `run.sh` | 运行目录内的一键启动脚本 |
| `upgrade.sh` | 运行目录内的升级脚本 |
| `init-db.sh` | 初始化 SQL 准备脚本 |
| `db/init/` | MariaDB 首次启动时自动导入的初始化 SQL |
| `db/data/` | MariaDB 数据目录，不提交 git |
| `db/logs/` | MariaDB 日志目录，不提交 git |
| `docker/nginx/` | nginx 配置模板和渲染后的运行配置 |
| `certs/` | HTTPS 证书目录，生产环境放置正式证书 |
| `config/overrides/` | properties 覆盖目录，同名 key 覆盖容器默认配置 |
| `runtime/attachment/` | BPMT 附件目录 |
| `runtime/download/` | BPMT 下载目录 |
| `runtime/ueditor-upload/` | UEditor 上传目录 |
| `runtime/platform-logs/` | Web 容器 BPMT 平台日志 |
| `runtime/tomcat-logs/` | Web 容器 Tomcat 日志 |
| `runtime/api-platform-logs/` | API 容器 BPMT 平台日志 |
| `runtime/api-tomcat-logs/` | API 容器 Tomcat 日志 |

MariaDB 官方镜像只会在首次创建 `db/data/` 时导入 `db/init/*.sql`。已经启动过的环境如果要重新初始化数据库，先确认数据已备份，再执行：

```bash
docker compose down
rm -rf db/data
docker compose up -d
```

## 版本历史

| 版本 | 发布日期 | 说明 | 文档 |
| --- | --- | --- | --- |
| `v1.7.1` | 预发布 | 基于 `v1.7.0` 新增报表视图配置 API，开放 `rep_list` 报表视图的创建、导出、校验、全量替换、局部替换和删除能力。 | [API](docs/v1.7.1/api-reference.md)、[OpenAPI](docs/v1.7.1/openapi.json) |
| `v1.7.0` | 2026-05-10 | 开放动态表视图配置 API，支持 validate、dry-run、导出、创建、替换、分区 patch 和带确认删除。 | [API](docs/v1.7.0/api-reference.md) |
| `v1.6.2` | 2026-05-06 | 修复第三方系统管理界面和 OAuth 无权限提示，新增安装/升级脚本，重构 README。 | [release](docs/release-v1.6.2.md) |
| `v1.6.1` | 2026-05-05 | 增强微信生态第三方 OAuth 登录态传导。 | [release](docs/release-v1.6.1.md) |
| `v1.6.0` | 2026-05-05 | 新增 HTTPS 入口支持，支持内置 nginx TLS 和可信上游 TLS。 | [release](docs/release-v1.6.0.md) |
| `v1.5.4` | 2026-05-05 | 补齐 Web/API 镜像 multi-arch 发布能力。 | [release](docs/release-v1.5.4.md) |
| `v1.5.3` | 2026-05-04 | 修复 nginx 转发非 80 端口时 OAuth 回跳地址丢端口的问题。 | [release](docs/release-v1.5.3.md) |
| `v1.5.2` | 2026-05-04 | 增强外部系统 OAuth 登录态切换体验。 | [release](docs/release-v1.5.2.md) |
| `v1.5.1` | 2026-05-04 | 修复工作流待办“查看/处理”跳转 `_ORD_ID=null` 问题。 | [release](docs/release-v1.5.1.md) |
| `v1.5.0` | 2026-05-03 | 新增外部系统 OAuth 登录能力。 | [release](docs/release-v1.5.0.md) |
| `v1.4.1` | 2026-05-03 | 新增 nginx 单入口、API 模块化路径重整和数据库操作模块接口。 | [API](docs/v1.4.1/api-reference.md) |
| `v1.4.0` | 2026-05-02 | 新增独立 `api` 子项目和独立 API Docker 容器。 | [release](docs/release-v1.4.0.md) |
| `v1.3.0` | 2026-05-01 | 修复移动端 H5 登录、首页、菜单和核心业务视图阻断问题。 | [release](docs/release-v1.3.0.md) |
| `v1.2.0` | 2026-04-28 | 整理初始化数据、修复早期 Docker 运行问题并重构入门文档。 | [roadmap](docs/v1.2.0/roadmap.md) |
| `v1.1.0` | 2026-04-26 | 第二个 Docker 化版本，收敛 compose 配置和本地构建入口。 | [release](docs/release-v1.1.0.md) |
| `v1.0.0` | 2026-04-26 | 首个正式 Docker 化版本。 | [release](docs/release-v1.0.0.md) |

## 文档

- [bpmt-doc](https://github.com/wodenwang/bpmt-doc)：面向低代码用户的 SOP 文档项目。
- [维护说明](docs/maintenance.md)：维护者构建、验证、镜像发布和升级策略。
- [v1.7.1 API 归档](docs/v1.7.1/api-reference.md)：报表视图 API 的 Markdown 归档，完整接口以 OpenAPI 快照为准。
- [v1.7.1 OpenAPI](docs/v1.7.1/openapi.json)：给 AI agent、N8N、飞书集成平台使用的接口契约。
- [v1.7.0 API 文档](docs/v1.7.0/api-reference.md)：动态表视图 API 的 Markdown 归档。
- [v1.7.0 OpenAPI 快照](docs/v1.7.0/openapi.json)：v1.7.0 的接口契约归档。
- [第三方 OAuth 登录](docs/v1.5.0/oauth-login-reference.md)：BPMT 作为 OAuth2 Authorization Code 服务端的接入说明。
- [HTTPS 验收](docs/v1.6.0/https-acceptance.md)：内置 nginx HTTPS 和可信上游 TLS 的验证记录。
- [微信生态 OAuth 登录](docs/v1.6.1/wechat-oauth-thirdpart-acceptance.md)：企业号/服务号登录态传导的验收记录。
- [v1.7.0 设计说明](docs/superpowers/specs/2026-05-10-bpmt-lite-v1.7.0-dynamic-table-view-api-design.md)：动态表视图 API 的需求边界和方案。
- [v1.7.0 执行计划](docs/superpowers/plans/2026-05-10-bpmt-lite-v1.7.0-dynamic-table-view-api.md)：本版本实施步骤和验证清单。
- [v1.7.1 设计说明](docs/superpowers/specs/2026-05-15-bpmt-lite-v1.7.1-report-view-api-design.md)：报表视图 API 的需求边界和方案。
- [v1.7.1 执行计划](docs/superpowers/plans/2026-05-15-bpmt-lite-v1.7.1-report-view-api.md)：本版本实施步骤和验证清单。

## 许可证与作者

本项目采用 [MIT License](LICENSE)。

作者：

- [wodenwang](https://github.com/wodenwang)
- [borball](https://github.com/borball)
