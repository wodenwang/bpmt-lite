# bpmt-lite v1.7.2 发布说明

## 版本定位

`v1.7.2` 是基于 `v1.7.1` 的 GitHub issue bugfix 版本，优先修复 API 和管理界面中的已知运行问题。

本版本不新增 API 路径，不改变 HMAC 认证、OAuth、HTTPS、H5 和 Docker 运行栈。

## 修复内容

- 修复报表视图 API 创建失败：`validate`、`dryRun` 通过后，实际创建时为 `VW_REPORT.SORT` 写入默认值，避免数据库非空约束触发 500。
- 修复动态表视图和报表视图配置写入后的缓存问题：写入成功后清理 Hibernate session、相关视图实体缓存和查询缓存，避免运行时继续读取旧配置。
- 修复数据库操作 `save` 显式主键写入问题：当 SQL 已显式指定主键且 JDBC 不返回自增 key 时，返回 `null` 而不是抛出 500。
- 修复控制面板注册信息版本显示：WAR manifest 写入 Maven `Implementation-Version`，运行时缺失时按 `bpmt.version`、`BPMT_VERSION`、`snapshot` 兜底。

## 文档归档

- API 参考：[docs/v1.7.2/api-reference.md](v1.7.2/api-reference.md)
- OpenAPI 快照：[docs/v1.7.2/openapi.json](v1.7.2/openapi.json)
- 执行计划：[docs/superpowers/plans/2026-05-16-bpmt-lite-v1.7.2-github-issue-bugfixes.md](superpowers/plans/2026-05-16-bpmt-lite-v1.7.2-github-issue-bugfixes.md)

## 验证清单

- [x] `python3 -m json.tool docs/v1.7.2/openapi.json >/tmp/bpmt-v172-openapi.json`
- [x] `cmp -s api/src/main/webapp/openapi.json docs/v1.7.2/openapi.json`
- [x] `git diff --check`
- [x] `mvn -s settings.local.xml -pl api -am -DfailIfNoTests=false -Dtest='ReportView*Test,OrmReportViewRepositoryTest' test`
- [x] `BPMT_API_DBOPS_EXECUTE_ENABLED=true mvn -s settings.local.xml -pl api -am -DfailIfNoTests=false -Dtest=DatabaseOperationServiceTest test`
- [x] `mvn -s settings.local.xml -pl api -am -DfailIfNoTests=false -Dtest='DynamicTableViewServiceTest,OrmReportViewRepositoryTest' test`
- [x] `mvn -s settings.local.xml -pl platform -am -DfailIfNoTests=false -Dtest=PlatformVersionTest test`
- [x] `mvn -s settings.local.xml -DskipTests compile`
- [x] `mvn -s settings.local.xml -pl platform -am -DskipTests package`
- [x] `unzip -p platform/target/platform-1.7.2.war META-INF/MANIFEST.MF`
- [x] `scripts/verify-repo.sh`
- [x] 用户本地 Docker 环境验收通过。
- [x] `scripts/build-multiarch-images.sh`
- [x] `docker buildx imagetools inspect ghcr.io/wodenwang/bpmt-lite:1.7.2`
- [x] `docker buildx imagetools inspect ghcr.io/wodenwang/bpmt-lite-api:1.7.2`

## 发布结果

- Maven 项目版本已切到 `1.7.2`。
- 默认 Web/API 镜像 tag、安装脚本默认 release tag 和 README 当前版本已切到 `1.7.2`。
- `ghcr.io/wodenwang/bpmt-lite:1.7.2` 已推送，manifest digest 为 `sha256:2568da6d7531dab48b96ce81c06195be7f90a8dac5e3af4b2da7b15bd21c2976`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.7.2` 已推送，manifest digest 为 `sha256:831812d553f66799559d993ab3c301e37e14e8657d10d9be1a8b7172fca8b51a`，包含 `linux/amd64` 和 `linux/arm64`。
- 两个 `latest` tag 已同步到 `1.7.2` manifest digest。
- 用户本地 Docker 环境已使用 `1.7.2` Web/API 镜像验收通过。

## 发布边界

- 不升级 Java、Tomcat、MariaDB 或 nginx。
- 不改变 compose 中第三方容器版本策略。
- 不新增 API 能力，只修复 v1.7.1 暴露出的运行问题。
- 不把安装或升级状态写入业务数据库。
