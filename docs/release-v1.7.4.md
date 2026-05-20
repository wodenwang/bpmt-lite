# bpmt-lite v1.7.4 发布说明

## 版本定位

`v1.7.4` 是基于 `v1.7.3` 的小补丁版本，修复登录后偶发出现灰色底蒙版的问题。

本版本不新增 API endpoint，不改变 HMAC 签名、响应包装、OAuth 主流程、数据库初始化结构或 Docker Compose 拓扑。

## 修复内容

- 新增整页遮罩清理逻辑：页面初始化、加载完成和浏览器 `pageshow` 恢复时，会清理孤儿 `#loading`、`.ui-widget-overlay`、`.am-dimmer` 和加载 modal。
- 保留正常活动弹窗遮罩：只有没有可见活动 dialog / modal 时才移除孤儿遮罩。
- 整页模板底部从单纯隐藏 `#loading` 改为优先调用 `Core.clearPageMask()`。
- 增加 `LoginMaskCleanupTest`，覆盖全局遮罩清理入口和模板调用关系。

## 文档

- 执行计划：[docs/superpowers/plans/2026-05-20-bpmt-lite-v1.7.4-login-mask.md](superpowers/plans/2026-05-20-bpmt-lite-v1.7.4-login-mask.md)
- API 参考：[docs/v1.7.4/api-reference.md](v1.7.4/api-reference.md)
- OpenAPI 快照：[docs/v1.7.4/openapi.json](v1.7.4/openapi.json)

## 验证记录

- [x] `node --check platform/src/main/webapp/js/ws-core.js`
- [x] `mvn -s settings.local.xml -pl platform -am -Dtest=LoginMaskCleanupTest -DfailIfNoTests=false test`
- [x] `mvn -s settings.local.xml -pl api -am -Dtest=ApiDocsContractTest -DfailIfNoTests=false test`
- [x] `cmp -s api/src/main/webapp/openapi.json docs/v1.7.4/openapi.json`
- [x] `python3 -m json.tool docs/v1.7.4/openapi.json >/tmp/bpmt-v174-openapi.json`
- [x] `scripts/verify-repo.sh`
- [x] `docker compose config`
- [x] `mvn -s settings.local.xml -DskipTests compile`
- [x] `scripts/build-multiarch-images.sh` 已完成 Web/API package 和 Docker context 生成；Docker Desktop buildx 一次性 `--push` 在 GHCR manifest 提交前卡住，改用逐架构推送加 `imagetools create` 完成等价 multi-arch 发布。
- [x] `docker buildx imagetools inspect ghcr.io/wodenwang/bpmt-lite:1.7.4`
- [x] `docker buildx imagetools inspect ghcr.io/wodenwang/bpmt-lite-api:1.7.4`
- [x] `docker buildx imagetools inspect ghcr.io/wodenwang/bpmt-lite:latest`
- [x] `docker buildx imagetools inspect ghcr.io/wodenwang/bpmt-lite-api:latest`

## 发布结果

- Maven 项目版本、默认 Web/API 镜像 tag、安装脚本默认 release tag 和 README 当前版本已切到 `1.7.4`。
- Git tag：`v1.7.4`。
- GitHub Release：`https://github.com/wodenwang/bpmt-lite/releases/tag/v1.7.4`。
- `ghcr.io/wodenwang/bpmt-lite:1.7.4` 已推送，manifest digest 为 `sha256:11fc93e989b3dce4698f63b2fff082097600f4c6f6c8cf9b815cb343f2d4c77b`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite-api:1.7.4` 已推送，manifest digest 为 `sha256:2567aaa64f02a440e5a40016356ef0b15bf57accba33c7501ab5c0821b1ded06`，包含 `linux/amd64` 和 `linux/arm64`。
- `ghcr.io/wodenwang/bpmt-lite:latest` 已同步到 `1.7.4` manifest digest `sha256:11fc93e989b3dce4698f63b2fff082097600f4c6f6c8cf9b815cb343f2d4c77b`。
- `ghcr.io/wodenwang/bpmt-lite-api:latest` 已同步到 `1.7.4` manifest digest `sha256:2567aaa64f02a440e5a40016356ef0b15bf57accba33c7501ab5c0821b1ded06`。
