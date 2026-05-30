# bpmt-lite v1.7.5 API 参考

`v1.7.5` 基于 `v1.7.4` 增强第三方系统 AI 接入提示词生成能力，不新增 API endpoint，不改变 HMAC 签名、响应包装或权限边界。

## 本版本 API 变化

- `/api/openapi.json` 与 `/api/docs/` 同步显示 `v1.7.5`。
- 业务 API 范围、HMAC 签名、响应包装和权限边界延续 `v1.7.4`。

## 接口范围

- 动态表结构 API：`/api/v1/dynamic-tables`
- 数据库操作 API：`/api/v1/database-operations/*`
- 动态表视图 API：`/api/v1/dynamic-table-views`
- 报表视图 API：`/api/v1/report-views`

报表视图 API 仍只管理 `rep_list` 报表视图配置，不执行 SQL、按钮动作或客户端脚本。

## OpenAPI 快照

当前版本 OpenAPI 快照见 [openapi.json](openapi.json)。
