# bpmt-lite v1.7.3 发布说明

## 版本定位

`v1.7.3` 是基于 `v1.7.2` 的 GitHub issue bugfix 版本，修复 OAuth 微信登录失败页提示和 OpenAPI 文档风格问题。

本版本不新增 API 路径，不改变 HMAC 认证、OAuth 主流程、HTTPS、H5 和 Docker 运行栈。

## 修复内容

- 修复 GitHub issue #18：企业微信授权成功但 BPMT 本地登录态建立失败时，错误页不再只显示“微信登录失败”，会区分平台维护/暂停、用户不存在、用户停用、IP 白名单、组织角色关系异常等可行动原因。
- 修复 GitHub issue #19：`/api/openapi.json` 的 `info.description` 和报表视图接口 summary 统一为中文描述风格，保留必要技术标识。

## 文档归档

- API 参考：[docs/v1.7.3/api-reference.md](v1.7.3/api-reference.md)
- OpenAPI 快照：[docs/v1.7.3/openapi.json](v1.7.3/openapi.json)
- 执行计划：[docs/superpowers/plans/2026-05-16-bpmt-lite-v1.7.3-github-issue-bugfixes.md](superpowers/plans/2026-05-16-bpmt-lite-v1.7.3-github-issue-bugfixes.md)

## 验证清单

- [x] `python3 -m json.tool docs/v1.7.3/openapi.json >/tmp/bpmt-v173-openapi.json`
- [x] `cmp -s api/src/main/webapp/openapi.json docs/v1.7.3/openapi.json`
- [x] `git diff --check`
- [x] `mvn -s settings.local.xml -pl platform -am -DfailIfNoTests=false -Dtest=OAuthWechatLoginServiceTest test`
- [x] `mvn -s settings.local.xml -pl platform -am -DfailIfNoTests=false -Dtest=OAuthActionTest test`
- [x] `mvn -s settings.local.xml -pl api -am -DfailIfNoTests=false -Dtest=ApiDocsContractTest test`
- [x] `mvn -s settings.local.xml -DskipTests compile`
- [x] `scripts/verify-repo.sh`
- [x] `scripts/build-multiarch-images.sh`
- [x] `docker buildx imagetools inspect ghcr.io/wodenwang/bpmt-lite:1.7.3`
- [x] `docker buildx imagetools inspect ghcr.io/wodenwang/bpmt-lite-api:1.7.3`
- [x] `docker buildx imagetools inspect ghcr.io/wodenwang/bpmt-lite:latest`
- [x] `docker buildx imagetools inspect ghcr.io/wodenwang/bpmt-lite-api:latest`

## 发布结果

- Maven 项目版本、默认 Web/API 镜像 tag、安装脚本默认 release tag 和 README 当前版本已切到 `1.7.3`。
- Git tag：`v1.7.3`。
- GitHub Release：`https://github.com/wodenwang/bpmt-lite/releases/tag/v1.7.3`。
- `ghcr.io/wodenwang/bpmt-lite:1.7.3` 已推送，manifest digest 为 `sha256:b3dd5635108a892a963d9cfddcb6b310cd20db31e7d0a34e9365f142101877e5`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.7.3` 已推送，manifest digest 为 `sha256:001ecf794f1d819ea44a984df5d12bdec910ce3d72ebf6fb0b40dad4518c0845`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite:latest` 已同步到 `1.7.3` manifest digest `sha256:b3dd5635108a892a963d9cfddcb6b310cd20db31e7d0a34e9365f142101877e5`。
- `ghcr.io/wodenwang/bpmt-lite-api:latest` 已同步到 `1.7.3` manifest digest `sha256:001ecf794f1d819ea44a984df5d12bdec910ce3d72ebf6fb0b40dad4518c0845`。

## 发布边界

- 不升级 Java、Tomcat、MariaDB 或 nginx。
- 不改变 compose 中第三方容器版本策略。
- 不新增 API endpoint。
- 不把安装或升级状态写入业务数据库。
