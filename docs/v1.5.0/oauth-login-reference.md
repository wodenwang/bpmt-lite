# v1.5.0 外部系统 OAuth 登录参考

## 总体说明

`v1.5.0` 新增外部系统 OAuth 登录能力。BPMT 在本版本中作为 OAuth2 Authorization Code 服务端，复用现有用户名密码登录、用户体系和权限体系；本版本不是 OIDC Provider，不提供 `id_token`、OIDC discovery、JWKS，也不提供 `refresh_token`。

OAuth 主流程完全在 `bpmt-web/platform` 内闭环，端点由 Web 平台承载：

- `GET /oauth/authorize`
- `POST /oauth/token`
- `GET /oauth/userinfo`

`bpmt-api` 不参与本流程，不新增 OAuth 相关 API，不调整既有 HMAC API 鉴权。`userid + thirdpartKey` 独立权限校验 API 暂不纳入 `v1.5.0`；独立 demo 仓库也不包含在本仓本版本范围内。

## 数据模型

OAuth 外部系统主数据和运行态都落库保存，数据库是 source of truth。授权码和 token 的明文值只在协议交互时出现，数据库中只保存 hash。

| 表 | 用途 | 说明 |
| --- | --- | --- |
| `CM_THIRDPART` | 外部系统主数据 | 保存外部系统、OAuth client、回调白名单、入口 URL、权限点和启停状态 |
| `CM_THIRDPART_AUTH_CODE` | 授权码运行态 | 保存 `CODE_HASH`、`CLIENT_ID`、`THIRDPART_KEY`、`USER_ID`、`REDIRECT_URI`、过期时间和一次性使用状态 |
| `CM_THIRDPART_ACCESS_TOKEN` | token 运行态 | 保存 `TOKEN_HASH`、`CLIENT_ID`、`THIRDPART_KEY`、`USER_ID`、过期、撤销和最后使用状态 |

三张表的初始化 SQL 源片段是 `database/v1.5.0-oauth-tables.sql`，已追加进 `database/bpmt.sql.gz`、`database/bpmt-min.sql.gz` 和旧明文最小库 `database/bpmt-db.sql`。默认 Docker 配置不依赖 Hibernate 自动建表。

默认生命周期：

- 授权码默认 5 分钟过期，只能使用一次。
- access token 默认 2 小时过期。
- `v1.5.0` 不提供 refresh token。

## 管理入口

外部系统管理入口放在现有 BPMT 后台管理体系中，用于维护 OAuth client 和外部系统权限边界。

后台菜单位置为 `系统开发 -> 第三方系统`，初始化数据会把菜单项 `sys_thirdpart` 放在 `用户菜单` 下方。该菜单项对应权限资源也是 `sys_thirdpart`，只控制管理入口是否可见。

外部系统访问授权在 `权限组管理 -> 第三方系统权限` 中维护。该页签和菜单权限、视图权限、控件权限并列，保存的是 `CM_THIRDPART.PRI_KEY` 对应的权限关系。

管理能力包括：

- 新增、编辑、启停外部系统主数据。
- 配置 `THIRDPART_KEY`、`THIRDPART_NAME`、`CLIENT_ID`、`REDIRECT_URIS`、`HOME_URL`、`PRI_KEY`。
- 新增外部系统时由系统生成 `clientSecret`。
- 编辑外部系统时可由维护者手工输入“重置密钥”，保存后不回显。
- 为外部系统选择或同步创建 `PRI_KEY` 权限点。

安全约束：

- 新增外部系统时，系统生成的 `clientSecret` 只展示一次。
- 编辑页手工输入“重置密钥”后，系统保存 hash，不生成也不回显新的明文 secret。
- 数据库只保存 `CLIENT_SECRET_HASH`，不保存明文 `clientSecret`。
- `PRI_KEY` 对应外部系统权限，OAuth `authorize` 时必须校验当前 BPMT 用户是否拥有该权限。
- 菜单权限只控制菜单是否可见；OAuth 登录是否允许进入第三方系统，以 `权限组管理 -> 第三方系统权限` 中配置的 `CM_THIRDPART.PRI_KEY` 为准。

## 端点示例

```text
GET /oauth/authorize?response_type=code&client_id=demo-client&redirect_uri=http%3A%2F%2F127.0.0.1%2Fdemo%2Fcallback&state=abc
POST /oauth/token
GET /oauth/userinfo
```

### `GET /oauth/authorize`

请求参数：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `response_type` | 是 | 固定为 `code` |
| `client_id` | 是 | 外部系统 OAuth client id |
| `redirect_uri` | 是 | 必须严格匹配 `CM_THIRDPART.REDIRECT_URIS` 白名单 |
| `state` | 建议 | 第三方系统自定义状态，原样回传 |

处理流程：

1. 校验 `response_type=code`、`client_id`、`redirect_uri`。
2. 如果当前浏览器没有 BPMT 登录态，跳转到现有登录页；登录成功后回到原始 authorize 请求。
3. 通过 `CM_THIRDPART.PRI_KEY` 校验当前用户是否有外部系统权限。
4. 生成授权码，数据库只保存 `CODE_HASH`。
5. 302 回跳到可信 `redirect_uri`，携带 `code` 和 `state`。

示例回跳：

```text
http://127.0.0.1/demo/callback?code=opaque-code&state=abc
```

### `POST /oauth/token`

请求表单：

| 参数 | 必填 | 说明 |
| --- | --- | --- |
| `grant_type` | 是 | 固定为 `authorization_code` |
| `code` | 是 | authorize 返回的一次性授权码 |
| `redirect_uri` | 是 | 必须与授权码绑定的回调地址一致 |
| `client_id` | 是 | 外部系统 OAuth client id |
| `client_secret` | 是 | 外部系统 OAuth client secret |

成功响应：

```json
{
  "access_token": "opaque-token",
  "token_type": "Bearer",
  "expires_in": 7200,
  "userid": "admin"
}
```

处理约束：

- 只支持 `authorization_code`。
- 授权码必须存在、未过期、未使用。
- 授权码绑定的 `CLIENT_ID` 和 `REDIRECT_URI` 必须与请求一致。
- 换 token 成功后立即标记授权码已使用。
- access token 明文只返回给第三方系统，数据库只保存 `TOKEN_HASH`。

### `GET /oauth/userinfo`

请求头：

```text
Authorization: Bearer <access_token>
```

响应内容包含最小必要用户信息，例如：

```json
{
  "userid": "admin",
  "name": "管理员",
  "group": {
    "groupKey": "default",
    "name": "默认组织"
  },
  "role": {
    "roleKey": "admin",
    "name": "管理员"
  }
}
```

## 响应和错误模型

`/oauth/token` 和 `/oauth/userinfo` 使用 OAuth 风格 JSON 响应，不使用 `success/data/error` 包装。`/oauth/authorize` 是浏览器跳转或 BPMT OAuth 错误页，不是 JSON 业务接口。`success/data/error` 是 `bpmt-api` 业务 API 的响应模型，不适用于 OAuth token 和 userinfo 响应。

稳定错误码：

| 错误码 | 典型场景 |
| --- | --- |
| `invalid_request` | 参数缺失、参数格式错误、`redirect_uri` 不可信 |
| `invalid_client` | `client_id` 不存在、外部系统停用、`client_secret` 错误 |
| `invalid_grant` | code 不存在、过期、已使用或绑定信息不一致 |
| `invalid_token` | access token 不存在、过期、已撤销或格式错误 |
| `unsupported_grant_type` | `grant_type` 不是 `authorization_code` |
| `access_denied` | BPMT 用户没有目标外部系统权限 |

示例错误：

```json
{
  "error": "invalid_grant",
  "error_description": "authorization code is invalid, expired, or already used"
}
```

`client_id` 不存在或 `redirect_uri` 不可信时，不应回跳第三方地址；已确认 `redirect_uri` 可信后，权限不足可回跳 `error=access_denied&state=...`。

## 日志要求

OAuth 主流程必须以 `INFO` 级别记录关键状态，便于企业接入排障和审计。日志应使用 `requestId` 或等价关联标识串联一次登录链路。

建议字段：

- `requestId`
- `endpoint`
- `clientId`
- `thirdpartKey`
- `userid`
- `redirectUriValid`
- `hasBpmtSession`
- `permissionResult`
- `grantResult`
- `tokenResult`
- `error`
- `reason`

禁止记录：

- 明文 `code`
- 明文 `access_token`
- 明文 `client_secret`
- 用户 `password`
- 其他敏感凭证

如需定位 code 或 token，可记录 hash 前缀、记录主键或 requestId，不记录明文。

## 菜单第三方 URL

`v1.5.0` 菜单第三方 URL / iframe 只是辅助入口，不是 OAuth 主流程。

- BPMT 菜单点击后只负责在主工作区 iframe 打开第三方 URL。
- BPMT 不在菜单点击时替第三方系统包装 OAuth，也不直接注入 BPMT 登录态。
- 第三方页面如果没有自己的登录态，应由第三方系统自行跳转 BPMT `/oauth/authorize`。
- 第三方系统需要允许被 BPMT 域 iframe 内嵌；如果第三方页面通过 `X-Frame-Options` 或 CSP 禁止内嵌，本版本不自动降级到新窗口。

## v1.5.0 排除项

本版本明确不包含：

- OIDC Provider、`id_token`、discovery、JWKS。
- refresh token。
- 跨系统单点登出。
- 普通用户授权同意页。
- `userid + thirdpartKey` 独立权限校验 API。
- `bpmt-api` OAuth 相关接口或实现改动。
- 独立 `bpmt-thirdpart-login-demo` 仓库。
