# v1.7.5 发布说明

`v1.7.5` 基于 `v1.7.4` 增强“第三方系统”管理页的 AI 接入提示词生成能力，不新增 OAuth endpoint，不改变 HMAC API 鉴权，不新增数据库结构。

## 变更

- 第三方系统列表页新增“AI 接入提示词”按钮。
- 弹框生成可复制到 Codex 或 Claude Code 空白项目中的提示词。
- 提示词覆盖 OAuth Authorization Code、`/oauth/authorize`、`/oauth/token`、`/oauth/userinfo`、BPMT API HMAC 认证、回调地址、`state` 校验和组织用户登录边界。
- Maven 项目版本、默认 Web/API 镜像 tag、安装脚本默认 release tag、README 当前版本和 OpenAPI 版本同步切到 `1.7.5`。

## 安全边界

- `clientSecret` 仍只在新增时一次性展示；列表页不会从数据库反查明文。
- 弹框中临时填写的 `clientSecret`、API appSecret 不提交、不落库、不写日志。
- 正式部署必须使用实际 `BPMT_API_APP_KEY` 和 `BPMT_API_APP_SECRET`，不能沿用开发默认值。

## 验收

- 第三方系统列表页每行出现“AI 接入提示词”按钮。
- 弹框可根据当前记录生成提示词，且提示词包含 `AGENTS.md`、`CLAUDE.md`、BPMT OAuth Authorization Code、三个 OAuth endpoint、HMAC 请求头和 canonical path 规则。
- 页面不输出 `clientSecretHash`，也不输出数据库中的 `CLIENT_SECRET_HASH`。
- API 文档归档：[docs/v1.7.5/api-reference.md](v1.7.5/api-reference.md)。
- OpenAPI 快照：[docs/v1.7.5/openapi.json](v1.7.5/openapi.json)。
- 已验证：
  - `mvn -s settings.local.xml -pl platform -am -DfailIfNoTests=false -Dtest=com.riversoft.module.thirdpart.ThirdpartJspTest test`
  - `mvn -s settings.local.xml -pl platform -am -DfailIfNoTests=false -Dtest=com.riversoft.module.thirdpart.ThirdpartActionTest,com.riversoft.module.thirdpart.ThirdpartServiceTest,com.riversoft.module.oauth.OAuthServiceTest test`
  - `mvn -s settings.local.xml -DskipTests compile`
  - `mvn -s settings.local.xml -pl api -am -DfailIfNoTests=false -Dtest=com.riversoft.api.http.ApiDocsContractTest test`
  - `cmp -s api/src/main/webapp/openapi.json docs/v1.7.5/openapi.json`
  - `python3 -m json.tool docs/v1.7.5/openapi.json`
  - `docker compose config`
  - `scripts/verify-repo.sh`
  - `scripts/build-multiarch-images.sh`

## 发布状态

- Git tag：`v1.7.5`。
- Web 镜像：`ghcr.io/wodenwang/bpmt-lite:1.7.5`，manifest digest `sha256:1cfe7d9efd790933689da70a8a94defd3fd358326384e71165157bda2995d19f`，包含 `linux/amd64` 和 `linux/arm64`。
- API 镜像：`ghcr.io/wodenwang/bpmt-lite-api:1.7.5`，manifest digest `sha256:36e66616d3670bdf4d330304ef15c5a2e3a8160be0624578212c6a024dae29a0`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite:latest` 已同步到 `1.7.5` manifest digest `sha256:1cfe7d9efd790933689da70a8a94defd3fd358326384e71165157bda2995d19f`。
- `ghcr.io/wodenwang/bpmt-lite-api:latest` 已同步到 `1.7.5` manifest digest `sha256:36e66616d3670bdf4d330304ef15c5a2e3a8160be0624578212c6a024dae29a0`。
