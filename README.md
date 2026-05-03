# bpmt-lite

`bpmt-lite` 是 BPMT 低代码平台的简化发行工程。BPMT 表示 BPM + table，核心能力是自定义工作流和动态表格。

本项目只整理发行工程：代码结构、打包方式、配置方式、Docker 运行方式和初始化数据。不升级 Java/Tomcat/MariaDB 技术栈，不重写业务功能。

## 当前版本

`v1.4.1` 为当前版本，新增 `nginx` 单入口、API 模块化路径重整，以及数据库操作模块接口。

`v1.3.0` 是上一版 H5 修复版本，按保守策略修复移动端登录、首页、菜单、业务视图入口、工作流意见编码等阻断问题；原 AmazeUI H5 页面结构保持不变。

- 默认 Web 镜像：`ghcr.io/wodenwang/bpmt-lite:1.4.1`
- 默认 API 镜像：`ghcr.io/wodenwang/bpmt-lite-api:1.4.1`
- 默认访问地址：`http://127.0.0.1/`
- API 文档地址：`http://127.0.0.1/api/docs/`
- OpenAPI 地址：`http://127.0.0.1/api/openapi.json`
- Web 应用：Tomcat `ROOT`
- 附带应用：`/ueditor`
- 默认数据库名：`bpmt`
- 最小数据库名：`bpmt_min`
- 默认登录账号：`admin/admin`

## Quick Start

只想先把系统跑起来，不需要 clone 项目。当前公开仓库已经包含初始化库和一键脚本。

如果只是快速体验，推荐先使用最小库启动：

```bash
curl -fsSL https://github.com/wodenwang/bpmt-lite/raw/refs/tags/v1.4.1/scripts/install.sh | bash
```

说明：一行命令会创建 `bpmt-lite/` 运行目录，默认使用最小库 `bpmt_min` 启动。`install.sh` 默认拉取当前 release 资源（当前默认 `v1.4.1`）。如需切换版本，可显式设置 `BPMT_REF`。

访问：

```text
http://127.0.0.1/
```

移动端 H5：

```text
http://127.0.0.1/login.jsp?_action_mode=h5
```

`v1.3.0` 起，登录、首页、菜单、首页面板，以及流程、动态表、报表的核心浏览路径纳入移动端验收范围。

登录：

```text
用户名：admin
密码：admin
```

最小库包含 176 张表和最小系统数据，适合快速体验、自动化验收和 issue 复现。

如果要使用完整业务库，执行：

```bash
curl -fsSL https://github.com/wodenwang/bpmt-lite/raw/refs/tags/v1.4.1/scripts/install.sh | bash -s -- full
```

## 完整库启动

`v1.4.1` 提供完整初始化库压缩包 `database/bpmt.sql.gz`，数据库名为 `bpmt`。初始化脚本会自动解压到 `db/init/bpmt.sql`。

完整库启动命令：

```bash
curl -fsSL https://github.com/wodenwang/bpmt-lite/raw/refs/tags/v1.4.1/scripts/install.sh | bash -s -- full
```

如果你本机已有可导入的完整 SQL，也可以直接放到：

```text
bpmt-lite/db/init/bpmt.sql
```

然后启动：

```bash
docker compose up -d
```

## 数据库选择

`bpmt` 和 `bpmt_min` 可以共存在同一个 MariaDB 容器里，互不覆盖。

| 数据库 | SQL 文件 | 用途 |
| --- | --- | --- |
| `bpmt` | `db/init/bpmt.sql` | 完整业务数据，本地试运行，由 `database/bpmt.sql.gz` 解压生成 |
| `bpmt_min` | `db/init/bpmt-min.sql` | 最小数据，快速体验和验收，由 `database/bpmt-min.sql.gz` 解压生成 |

Web 应用连接哪个库由 `DB_NAME` 决定。

切到最小库：

```bash
DB_NAME=bpmt_min docker compose up -d web
```

切回默认完整库：

```bash
DB_NAME=bpmt docker compose up -d web
```

注意：MariaDB 官方镜像只会在首次创建 `db/data` 时自动执行 `db/init/*.sql`。如果已经启动过，再新增或替换 SQL 文件不会自动重新导入。

## 常用操作

查看容器状态：

```bash
docker compose ps
```

停止服务：

```bash
docker compose down
```

重新初始化数据库前先确认数据已备份，然后删除本地数据目录：

```bash
docker compose down
rm -rf db/data
docker compose up -d
```

如果曾经用旧版 `run.sh` 启动并出现 nginx 配置挂载错误，先清掉 Docker 自动创建的错误目录，再重新下载脚本启动：

```bash
docker compose down
rm -rf docker/nginx/nginx.conf
curl -fsSL https://github.com/wodenwang/bpmt-lite/raw/refs/tags/v1.4.1/scripts/install.sh | bash -s -- full
```

检查入口：

```bash
curl -fsSI http://127.0.0.1/
curl -fsSI http://127.0.0.1/ueditor/
curl -fsSI http://127.0.0.1/api/openapi.json
curl -fsSI http://127.0.0.1/api/docs/
```

期望返回 `HTTP/1.1 200`。

API 业务接口默认需要签名。维护者本地验收可执行：

```bash
scripts/smoke-api.sh
```

## API 使用

`v1.4.1` 使用 `nginx` 对外统一入口，API 默认路径为 `http://127.0.0.1/api/`。动态表模块只管理结构，不管理业务数据，不提供删除接口。

API 文档有两种形态：

| 入口 | 用途 |
| --- | --- |
| `http://127.0.0.1/api/docs/` | Web 文档，适合人工阅读和调试 |
| `http://127.0.0.1/api/openapi.json` | OpenAPI JSON，适合 AI agent、N8N、飞书集成平台和后续 skill 封装 |
| [docs/v1.4.1/api-reference.md](docs/v1.4.1/api-reference.md) | Markdown 归档版 API 文档 |
| [docs/v1.4.1/openapi.json](docs/v1.4.1/openapi.json) | OpenAPI 归档快照 |

首批接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/v1/dynamic-tables` | 查询动态表结构列表 |
| `POST` | `/api/v1/dynamic-tables` | 创建动态表结构 |
| `GET` | `/api/v1/dynamic-tables/{name}` | 查询单个动态表结构 |
| `PUT` | `/api/v1/dynamic-tables/{name}` | 调整动态表结构 |
| `POST` | `/api/v1/dynamic-tables/{name}/ddl:sync` | 同步动态表 DDL |
| `GET` | `/api/v1/dynamic-tables/templates` | 查询动态表模板列表 |
| `GET` | `/api/v1/dynamic-tables/templates/{templateCode}` | 查询模板详情 |
| `POST` | `/api/v1/dynamic-tables/templates/{templateCode}:create-table` | 按模板建表 |
| `POST` | `/api/v1/database-operations/query` | SQL 查询（SELECT） |
| `POST` | `/api/v1/database-operations/find` | SQL 单行查询（SELECT） |
| `POST` | `/api/v1/database-operations/save` | SQL 保存（INSERT） |
| `POST` | `/api/v1/database-operations/exec` | SQL 执行（UPDATE/DELETE） |

业务 API 使用 HMAC-SHA256 签名，请求头为 `X-BPMT-App-Key`、`X-BPMT-Timestamp`、`X-BPMT-Nonce`、`X-BPMT-Signature`。签名 path 必须包含公开 context path，例如 `/api/v1/dynamic-tables`。本地默认 `appKey` 为 `bpmt-api`，默认 `appSecret` 为 `bpmt-api-secret`，正式部署必须覆盖。

API 和 Web 各自内嵌 Hazelcast，并通过 compose 网络组成同一集群；缓存保持开启。动态表结构写接口会同时更新数据库 DDL 和 BPMT 元数据表，因此调用前应明确目标表结构。

## 外部系统 OAuth 登录

`v1.5.0` 增加外部系统 OAuth 登录能力，BPMT 作为 OAuth2 Authorization Code 服务端，复用现有 BPMT 用户、登录页和权限体系。该能力主流程完全在 `bpmt-web/platform` 中实现，不改 `bpmt-api`，不提供 OIDC、`refresh_token` 或 `userid + thirdpartKey` 独立权限校验 API。

OAuth 端点：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/oauth/authorize` | 浏览器授权入口，未登录时回 BPMT 登录页，登录后继续 authorize |
| `POST` | `/oauth/token` | 使用一次性 code 换取不透明 access token 和 `userid` |
| `GET` | `/oauth/userinfo` | 使用 `Authorization: Bearer <access_token>` 读取当前用户基础信息 |

相关表：

| 表 | 说明 |
| --- | --- |
| `CM_THIRDPART` | 外部系统主数据、OAuth client、回调白名单、入口 URL 和 `PRI_KEY` 权限点 |
| `CM_THIRDPART_AUTH_CODE` | 授权码运行态；code 只保存 hash，默认 5 分钟过期且只能使用一次 |
| `CM_THIRDPART_ACCESS_TOKEN` | token 运行态；access token 只保存 hash，默认 2 小时过期 |

三张 OAuth 表已经进入默认初始化 SQL：`database/bpmt.sql.gz` 完整库和 `database/bpmt-min.sql.gz` 最小库都会创建这些表。Docker 默认不会开启 Hibernate 自动建表，因此升级已有数据库时需要自行执行 `database/v1.5.0-oauth-tables.sql`。

`/oauth/token` 和 `/oauth/userinfo` 使用 OAuth JSON 响应，不使用 API 的 `success/data/error` 包装；`/oauth/authorize` 是浏览器跳转或错误页。菜单第三方 URL / iframe 只是辅助入口：BPMT 只负责打开第三方页面，第三方页面没有自己的登录态时，应自行跳转 `/oauth/authorize` 发起 OAuth。

参考文档：

- [docs/v1.5.0/oauth-login-reference.md](docs/v1.5.0/oauth-login-reference.md)
- [docs/v1.5.0/oauth-login-acceptance.md](docs/v1.5.0/oauth-login-acceptance.md)

## 常用配置

默认 `docker-compose.yml` 只保留快速启动需要的常用项。

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `BPMT_HTTP_PORT` | `80` | Nginx 对外访问端口 |
| `BPMT_DB_PORT` | `3306` | MariaDB 暴露到宿主机的端口 |
| `BPMT_IMAGE_TAG` | `1.4.1` | Web 镜像 tag |
| `BPMT_API_IMAGE_TAG` | `1.4.1` | API 镜像 tag |
| `DB_HOST` | `mariadb` | Web 容器访问数据库的主机名 |
| `DB_NAME` | `bpmt` | Web 应用连接的数据库 |
| `DB_USER` | `root` | 数据库用户 |
| `DB_PASSWORD` | `123456` | 数据库密码 |
| `BPMT_API_APP_KEY` | `bpmt-api` | API appKey |
| `BPMT_API_APP_SECRET` | `bpmt-api-secret` | API appSecret |
| `BPMT_API_ACT_AS` | `admin` | API 固定技术用户，未配置或用户不可用时兜底 `admin` |
| `BPMT_API_DBOPS_EXECUTE_ENABLED` | `true` | 是否开启 `database-operations` 写操作（`save/exec`） |
| `BPMT_HAZELCAST_PASSWORD` | `bpmt` | Web/API 内嵌 Hazelcast 集群密码 |
| `LOG_PATH` | `/usr/local/tomcat/webapps/logs` | 容器内 BPMT 业务日志目录 |

示例：把统一入口端口改成 18080。

```bash
BPMT_HTTP_PORT=18080 docker compose up -d
```

高级配置通过 `config/overrides/*.properties` 覆盖。覆盖文件会追加到容器启动时生成的同名 properties 文件后面，因此同名 key 以覆盖文件为准。

## 运行目录

```text
db/init/                 初始化 SQL 目录，不提交私有 SQL
db/data/                 MariaDB 数据目录，不提交 git
db/logs/                 MariaDB 日志目录，不提交 git
runtime/attachment/      BPMT 附件目录，不提交 git
runtime/download/        BPMT 下载目录，不提交 git
runtime/ueditor-upload/  UEditor 上传目录，不提交 git
runtime/platform-logs/   BPMT 平台日志目录，不提交 git
runtime/tomcat-logs/     Tomcat 日志目录，不提交 git
runtime/api-platform-logs/ API 平台日志目录，不提交 git
runtime/api-tomcat-logs/   API Tomcat 日志目录，不提交 git
config/overrides/        properties 覆盖文件目录，不提交具体覆盖文件
```

## 维护者构建

维护者需要 Java 8、Maven、Docker，以及可访问历史依赖的 Maven 仓库。

```bash
cp settings.example.xml settings.local.xml
scripts/build-image.sh
scripts/build-api-image.sh
```

`scripts/build-image.sh` 会构建本地 Web 镜像，并验证 `ROOT`、`ueditor`、entrypoint 和 CJK 字体。`scripts/build-api-image.sh` 会构建本地 API 镜像，并验证 `/api`、`openapi.json`、`docs/index.html` 和 entrypoint。更多维护和发布细节见 [docs/maintenance.md](docs/maintenance.md)。

## 文档

- 低代码操作文档：更详细的表单、流程、动态表和日常使用说明见 [bpmt-doc](https://github.com/wodenwang/bpmt-doc)
- 初始化数据库设计：[docs/v1.2.0/database-init.md](docs/v1.2.0/database-init.md)
- v1.2.0 规划：[docs/v1.2.0/roadmap.md](docs/v1.2.0/roadmap.md)
- 发布验收清单：[docs/v1.2.0/release-checklist.md](docs/v1.2.0/release-checklist.md)
- v1.3.0 发布记录：[docs/release-v1.3.0.md](docs/release-v1.3.0.md)
- v1.3.0 H5 验收清单：[docs/v1.3.0/h5-acceptance.md](docs/v1.3.0/h5-acceptance.md)
- v1.4.1 API 开发规范：[docs/v1.4.0/api-guidelines.md](docs/v1.4.0/api-guidelines.md)
- v1.4.1 API 验收清单：[docs/v1.4.1/api-acceptance.md](docs/v1.4.1/api-acceptance.md)
- v1.4.1 API Markdown 归档：[docs/v1.4.1/api-reference.md](docs/v1.4.1/api-reference.md)
- v1.4.1 OpenAPI 归档：[docs/v1.4.1/openapi.json](docs/v1.4.1/openapi.json)
- v1.5.0 OAuth 登录参考：[docs/v1.5.0/oauth-login-reference.md](docs/v1.5.0/oauth-login-reference.md)
- v1.5.0 OAuth 验收清单：[docs/v1.5.0/oauth-login-acceptance.md](docs/v1.5.0/oauth-login-acceptance.md)
- 维护说明：[docs/maintenance.md](docs/maintenance.md)

## 许可证与作者

未来版本计划使用 MIT 许可证。主要作者记录为 `wodenwang` 和 `borballzhai`。

本项目沟通和文档统一使用简体中文；代码、命令、配置键名、Maven 坐标、镜像名等技术标识保持原样。
