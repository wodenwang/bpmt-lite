# bpmt-lite v1.7.3 API 参考

`v1.7.3` 基于 `v1.7.2` 修复 OpenAPI 文档风格问题，不新增 API endpoint，不改变 HMAC 签名、响应包装或权限边界。

## 本版本 API 变化

- `/api/openapi.json` 的 `info.description` 统一为中文说明，保留必要技术标识：`dyn`、`rep_list`、`VW_URL`、`VW_REPORT*`、`PATH`。
- `/api/openapi.json` 的报表视图接口 summary 统一为中文动词短语。
- `/api/docs/` 同步显示 `v1.7.3`。

## 接口范围

- 动态表结构 API：`/api/v1/dynamic-tables`
- 数据库操作 API：`/api/v1/database-operations/*`
- 动态表视图 API：`/api/v1/dynamic-table-views`
- 报表视图 API：`/api/v1/report-views`

报表视图 API 仍只管理 `rep_list` 报表视图配置，不执行 SQL、按钮动作或客户端脚本。

## OpenAPI 快照

当前版本 OpenAPI 快照见 [openapi.json](openapi.json)。
