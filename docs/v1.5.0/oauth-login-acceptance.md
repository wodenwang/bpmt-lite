# v1.5.0 外部系统 OAuth 登录验收清单

## 验收范围

`v1.5.0` 验收范围是 BPMT Web 作为 OAuth2 Authorization Code 服务端，为外部系统提供统一登录入口。主流程完全在 `bpmt-web/platform`，不改 `bpmt-api`，不实现 `userid + thirdpartKey` 独立权限校验 API。菜单第三方 URL / iframe 只作为辅助入口，不作为 OAuth 主流程。

## 验收清单

| 序号 | 项目 | 通过标准 |
| --- | --- | --- |
| 1 | HBM 映射与初始化 SQL 存在 | `CM_THIRDPART.hbm.xml`、`CM_THIRDPART_AUTH_CODE.hbm.xml`、`CM_THIRDPART_ACCESS_TOKEN.hbm.xml` 在 platform classpath 可加载；`database/bpmt.sql.gz` 和 `database/bpmt-min.sql.gz` 默认初始化后包含三张表 |
| 2 | 外部系统管理菜单 | 后台 `系统开发` 菜单组中存在 `第三方系统`，位于 `用户菜单` 下方，入口地址为 `/thirdpart/ThirdpartAction/index.shtml` |
| 2.1 | 第三方系统权限配置 | `权限组管理` 编辑页中存在 `第三方系统权限` 页签，可和菜单权限、视图权限、控件权限并列配置外部系统访问权限 |
| 3 | 外部系统主数据新增 | 可新增外部系统，保存 `THIRDPART_KEY`、`CLIENT_ID`、`REDIRECT_URIS`、`HOME_URL`、`PRI_KEY` 等字段 |
| 4 | 外部系统主数据编辑 | 可编辑名称、回调白名单、入口 URL、权限点等非敏感配置 |
| 5 | 外部系统启停 | 停用后 authorize/token 不再允许该 client 继续完成登录链路 |
| 6 | `clientSecret` 只展示一次 | 新增外部系统时系统生成明文 `clientSecret` 并只展示一次；编辑页手工输入“重置密钥”后保存 hash，不生成也不回显新 secret |
| 7 | authorize 未登录回登录页 | 未登录访问 `/oauth/authorize` 时进入现有 BPMT 登录页 |
| 8 | 登录后回 authorize | 用户完成 BPMT 登录后回到原始 authorize 请求，并继续生成授权码 |
| 9 | `redirect_uri` 白名单 | 不在 `CM_THIRDPART.REDIRECT_URIS` 白名单内的回调地址被拒绝，不回跳不可信地址 |
| 10 | 权限不足 `access_denied` | 非超管用户没有 `CM_THIRDPART.PRI_KEY` 权限时，返回或回跳 OAuth 错误 `access_denied`，并明确提示无权限访问目标第三方系统；`admin` 作为系统级超管可绕过该权限校验 |
| 11 | code 只能用一次 | 同一个授权码第一次换 token 成功后，再次使用返回 `invalid_grant` |
| 12 | code 过期失败 | 超过授权码有效期后换 token 返回 `invalid_grant` |
| 13 | token 正常 | 合法 code 可通过 `/oauth/token` 换取响应，包含 `access_token`、`token_type`、`expires_in`、`userid` |
| 14 | userinfo 正常 | 使用 `Authorization: Bearer <access_token>` 调 `/oauth/userinfo` 返回当前 BPMT 用户基础信息 |
| 15 | OAuth JSON 响应 | `/oauth/token` 和 `/oauth/userinfo` 使用 OAuth JSON，不使用 `success/data/error` 包装 |
| 16 | INFO 日志 | authorize、token、userinfo 的开始、结果、错误码和关键状态以 `INFO` 级别登记，并可通过 requestId 或等价标识串联 |
| 17 | 日志不含明文敏感值 | 运行日志中不出现明文 `code`、`access_token`、`client_secret`、`password` |
| 18 | 菜单第三方 URL | BPMT 菜单项可配置第三方 URL，并在主工作区 iframe 打开；OAuth 仍由第三方页面自行发起 |
| 19 | `/api/docs/` 回归 200 | `curl -fsSI http://127.0.0.1/api/docs/` 返回 `HTTP/1.1 200`，确认本任务未改 `bpmt-api` 文档入口 |
| 20 | `/api/openapi.json` 回归 200 | `curl -fsSI http://127.0.0.1/api/openapi.json` 返回 `HTTP/1.1 200`，确认本任务未改 `bpmt-api` OpenAPI |
| 21 | `userid + thirdpartKey` API 排除 | 不存在新的 `userid + thirdpartKey` 独立权限校验 API；该能力不在 `v1.5.0` 验收范围 |
| 22 | demo 仓库排除 | 本仓不包含独立 `bpmt-thirdpart-login-demo` 实现；后续 demo 按参考文档单独实现 |

## 建议命令

检查仓库配置和文档入口：

```bash
docker compose config
curl -fsSI http://127.0.0.1/
curl -fsSI http://127.0.0.1/ueditor/
curl -fsSI http://127.0.0.1/api/docs/
curl -fsSI http://127.0.0.1/api/openapi.json
```

OAuth 主流程可按以下顺序人工或自动化验收：

```text
1. 创建启用状态的外部系统，记录 client_id、clientSecret、redirect_uri、PRI_KEY。
2. 使用无 BPMT 登录态浏览器访问 /oauth/authorize。
3. 登录 admin/admin，确认系统级超管可回到 authorize 并回跳第三方 redirect_uri。
4. 使用 code 调 /oauth/token。
5. 使用 access_token 调 /oauth/userinfo。
6. 重复使用同一个 code，确认 invalid_grant。
7. 使用两个非超管测试账号访问同一 client：一个账号所属角色绑定目标第三方系统权限组，应成功获得 code；另一个账号不绑定该权限组，应返回 access_denied。
8. 检查 runtime/platform-logs/ 下 OAuth INFO 日志和敏感值脱敏情况。
```

日志敏感值自查示例：

```bash
rg -n "code=|access_token|client_secret|password" runtime/platform-logs runtime/tomcat-logs
```

出现字段名不一定失败；如果日志包含明文授权码、token、secret 或密码值，则验收失败。
