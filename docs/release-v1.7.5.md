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
- 窄测试命令：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn -s settings.local.xml -pl platform -Dtest=com.riversoft.module.thirdpart.ThirdpartJspTest test
```
