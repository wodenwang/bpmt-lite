# bpmt-lite v1.7.1 发布说明

## 版本定位

`v1.7.1` 是基于 `v1.7.0` 的补丁增强版本，新增 `rep_list` 报表视图配置 API。

本版本只覆盖 `/{viewKey}.view` 对应的报表视图配置，不发布菜单、首页卡片或外部入口。

## 新增能力

- 新增 `/api/v1/report-views` 系列接口。
- 支持报表视图创建、导出、校验、全量替换、局部替换和删除。
- 支持 `POST /api/v1/report-views:validate` 在写入前校验完整快照。
- 支持 `dryRun=true` 返回写入计划但不写库。
- 补齐报表视图 API 的 OpenAPI 与人类文档。

## 风险说明

报表视图 API 不执行 SQL 或脚本，只管理配置元数据。API 会对主 SQL、查询 SQL、约束 SQL、PK SQL、按钮动作、客户端脚本和外部 `dbKey` 返回风险提示，但不会验证 SQL 语义、结果列、权限过滤或运行性能。

发布验收已覆盖 API 文档、OpenAPI JSON、Java 8 编译、API 单测、multi-arch 镜像发布和临时 compose API smoke。

## 文档归档

- API 参考：[docs/v1.7.1/api-reference.md](v1.7.1/api-reference.md)
- OpenAPI 快照：[docs/v1.7.1/openapi.json](v1.7.1/openapi.json)
- 设计说明：[docs/superpowers/specs/2026-05-15-bpmt-lite-v1.7.1-report-view-api-design.md](superpowers/specs/2026-05-15-bpmt-lite-v1.7.1-report-view-api-design.md)
- 执行计划：[docs/superpowers/plans/2026-05-15-bpmt-lite-v1.7.1-report-view-api.md](superpowers/plans/2026-05-15-bpmt-lite-v1.7.1-report-view-api.md)

## 验证清单

- [x] `python3 -m json.tool docs/v1.7.1/openapi.json >/tmp/bpmt-v171-openapi.json`
- [x] `cmp -s api/src/main/webapp/openapi.json docs/v1.7.1/openapi.json`
- [x] `git diff --check`
- [x] `mvn -s settings.local.xml -pl api test`
- [x] `mvn -s settings.local.xml -pl api -am -DskipTests install`
- [x] `scripts/verify-repo.sh`
- [x] `scripts/build-multiarch-images.sh`
- [x] 临时 compose 使用 `bpmt_min` 验证 `/`、`/ueditor/`、`/api/docs/`、`/api/openapi.json` 均返回 200。
- [x] `scripts/smoke-api.sh` 通过 `http://127.0.0.1:18081/api` 签名 smoke。

## 发布结果

- Maven 项目版本已切到 `1.7.1`。
- 默认 Web/API 镜像 tag、安装脚本默认 release tag 和 README 当前版本已切到 `1.7.1`。
- `ghcr.io/wodenwang/bpmt-lite:1.7.1` 已推送，manifest digest 为 `sha256:8c82eabc4e87193d02c024f62e1d64a42570520cb41de9e3de3e481a88c34009`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.7.1` 已推送，manifest digest 为 `sha256:02ed7e782f1ad027d6500caa065059011971cad06e12e495ae5a455f94790fdc`，包含 `linux/amd64` 和 `linux/arm64`。
- 两个 `latest` tag 已同步到 `1.7.1` manifest digest。

## 发布边界

- 不升级 Java、Tomcat、MariaDB 或 nginx。
- 不改变 compose 中第三方容器版本策略。
- 不改变现有 OAuth、HTTPS、H5、数据库操作 API 或动态表视图 API 行为。
- 不把安装或升级状态写入业务数据库。
