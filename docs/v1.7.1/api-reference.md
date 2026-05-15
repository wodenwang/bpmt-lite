# bpmt-lite v1.7.1 API 参考

本文是 `bpmt-lite` v1.7.1 的 API 归档版文档，面向人类阅读。机器可读的 OpenAPI 快照见 [openapi.json](openapi.json)。

运行中的公开文档入口：

- Web 文档：`http://127.0.0.1/api/docs/`
- OpenAPI：`http://127.0.0.1/api/openapi.json`

## 版本范围

v1.7.1 在 v1.7.0 动态表结构 API、数据库操作 API 和动态表视图 API 基础上，新增 `rep_list` 报表视图配置 API。

报表视图 API 的边界：

- 只管理 `rep_list` 报表视图配置。
- 只维护 `/{viewKey}.view` 对应的 `VW_URL` 与 `VW_REPORT*` 元数据。
- 不管理菜单、首页卡片、外部入口或报表业务数据。
- 删除报表视图不会删除业务表、业务数据、菜单、首页卡片或外部入口。
- 写接口只写报表视图配置和 API 管理范围内的权限资源，不执行 DDL。

## 认证

业务 API 继续使用 `appKey/appSecret` 的 HMAC-SHA256 签名。默认本地开发配置为：

```text
BPMT_API_APP_KEY=bpmt-api
BPMT_API_APP_SECRET=bpmt-api-secret
BPMT_API_ACT_AS=admin
```

正式部署必须覆盖默认 `appSecret`。请求头：

```text
X-BPMT-App-Key
X-BPMT-Timestamp
X-BPMT-Nonce
X-BPMT-Signature
```

签名原文：

```text
METHOD
PATH
NORMALIZED_QUERY
TIMESTAMP
NONCE
SHA256_HEX(BODY)
```

规则：

- `PATH` 必须包含公开 context path，例如 `/api/v1/report-views`、`/api/v1/report-views:validate`，不能只签 `/v1/report-views`。
- `NORMALIZED_QUERY` 按解码后的参数名和值排序，再 URL encode；无 query 时为空行。
- `BODY` 为空时使用空字符串计算 SHA-256。
- `appSecret` 不允许出现在 query 或 request body 中。
- 不同的 `METHOD`、`PATH`、query 或 body 都必须重新计算 `X-BPMT-Signature`。

## 报表视图 API

业务前缀为 `/api/v1`。

| 方法 | 路径 | 说明 | 风险 |
| --- | --- | --- | --- |
| `GET` | `/api/v1/report-views` | 分页列出 `rep_list` 报表视图 | 只读 |
| `POST` | `/api/v1/report-views?dryRun=true` | 创建报表视图，body 为完整快照，`dryRun` 是 query 参数 | 写元数据，不执行 DDL |
| `POST` | `/api/v1/report-views:validate` | 校验完整快照，不落库 | 只读 |
| `GET` | `/api/v1/report-views/{viewKey}` | 导出完整快照；返回的 `snapshot` 可作为 `PUT` baseline | 只读 |
| `PUT` | `/api/v1/report-views/{viewKey}?dryRun=true` | 全量替换报表视图配置，`dryRun` 是 query 参数 | 写元数据，不执行 DDL |
| `PATCH` | `/api/v1/report-views/{viewKey}/{section}?dryRun=true` | 替换单个 section，没有额外路径前缀 | 写元数据，不执行 DDL |
| `DELETE` | `/api/v1/report-views/{viewKey}?confirmViewKey={viewKey}` | 删除报表视图配置，需要确认参数 | 写元数据，不执行 DDL |

可 patch 的 `section`：

```text
base
columns
queries
limits
variables
subviews
buttons
weixin
scripts
```

写接口在 OpenAPI 中均标注：

```json
{
  "x-bpmt-writes-metadata": true,
  "x-bpmt-executes-ddl": false,
  "x-bpmt-risk-level": "high"
}
```

`GET` 与 `POST /api/v1/report-views:validate` 不写元数据，风险等级为 `read`。

## validate 与 dryRun

`validate` 只校验完整快照并返回规范化结果：

```text
POST /api/v1/report-views:validate
```

`dryRun=true` 支持创建、替换和 section patch：

```text
POST /api/v1/report-views?dryRun=true
PUT /api/v1/report-views/{viewKey}?dryRun=true
PATCH /api/v1/report-views/{viewKey}/{section}?dryRun=true
```

`dryRun=true` 会走真实写入前同一套校验、权限处理和写入计划路径，响应中的 `plan.dryRun` 为 `true`，并列出将创建、更新、删除的配置项和权限资源，但不会写入数据库。

## 风险边界

报表视图 API 会写入 `VW_URL` 与 `VW_REPORT*` 元数据，但不会执行 `mainSql`、查询 SQL、约束 SQL、PK SQL、按钮动作或客户端脚本。调用方必须通过实际业务页面或验收脚本确认 SQL 语义、结果列、权限过滤和运行性能。

风险提示会通过响应中的 `warnings` 返回。首版稳定提示码：

| code | 含义 |
| --- | --- |
| `SQL_SCRIPT_PRESENT` | 存在主 SQL、查询 SQL、约束 SQL 或 PK SQL |
| `CLIENT_SCRIPT_PRESENT` | 存在列表 JS、展示内容脚本、汇总内容脚本或提示脚本 |
| `BUTTON_ACTION_PRESENT` | 存在自定义按钮动作 |
| `EXTERNAL_DB_KEY_PRESENT` | 配置了非默认 `dbKey` |
| `UNEXECUTED_SQL_SEMANTICS` | API 未执行 SQL，不能保证 SQL 语义、结果列、权限过滤或运行性能正确 |

## 错误码

常见错误码：

| code | 含义 |
| --- | --- |
| `REPORT_VIEW_NOT_FOUND` | 报表视图不存在 |
| `REPORT_VIEW_NOT_REP_LIST` | 目标视图不是 `rep_list` 报表视图 |
| `REPORT_VIEW_ALREADY_EXISTS` | 创建时 `viewKey` 已存在 |
| `REPORT_VIEW_INVALID_SNAPSHOT` | 报表视图快照结构、字段引用或 section 内容不合法 |
| `REPORT_VIEW_CONFIRM_REQUIRED` | 删除时缺少 `confirmViewKey` 或确认值不一致 |
| `REPORT_VIEW_UNSUPPORTED_PERMISSION` | 不支持 permissions 的位置传入了 permissions |
| `REPORT_VIEW_UNSUPPORTED_SECTION` | `PATCH` 使用了不支持的 section |
| `REPORT_VIEW_INVALID_SQL_CONFIG` | SQL 配置结构不合法 |
| `REPORT_VIEW_INVALID_SCRIPT_CONFIG` | 脚本配置结构不合法 |
| `API_INVALID_PARAMETER` | 分页、query 或路径参数不合法 |
| `API_AUTH_FAILED` | HMAC 认证失败 |

错误响应仍使用统一包装：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "REPORT_VIEW_INVALID_SNAPSHOT",
    "message": "报表视图快照校验失败。",
    "details": {},
    "requestId": "..."
  }
}
```

## 文档入口

- 版本归档 OpenAPI：[docs/v1.7.1/openapi.json](openapi.json)
- 运行态 Web 文档：`http://127.0.0.1/api/docs/`
- 运行态 OpenAPI：`http://127.0.0.1/api/openapi.json`

