# bpmt-lite v1.8.0 发布说明

`v1.8.0` 是 `desktop admin clarity baseline` 版本。它停止旧 `modern-theme foundation` / CSS-only 全局主题方向，改为围绕三条代表路径改善后台任务清晰度：登录后首页、第三方系统列表、AI 接入提示词弹窗。

## 版本范围

- 保留 `platform/src/main/webapp/css/bpmt-modern.css` 文件名，但将定位收窄为桌面后台清晰度基线覆盖层。
- 在全局 `html_include.jsp` 最后引入该覆盖层，保留单文件回滚能力。
- 登录后首页增加会话摘要层级，并为首页面板补充加载/空状态说明。
- 第三方系统列表增加页面标题、用途说明、主操作、列表说明和无数据/无匹配空状态。
- AI 接入提示词弹窗改为更清晰的说明、字段分组、只读提示词区域、复制成功/失败反馈。
- 更新 Maven 项目版本、默认 Web/API 镜像 tag、安装脚本默认 ref 到 `1.8.0` / `v1.8.0`。
- 更新 README、AGENTS、v1.8.0 API 归档和 v2.0.0 桌面 UI 重新规划文档。

## 非范围

- 不改移动端 H5。
- 不做全站 CSS-only 现代主题发布。
- 不改 JavaScript 初始化、Ajax 生命周期或表单提交方式。
- 不改菜单、权限、OAuth、API 鉴权、动态表、报表或流程业务逻辑。
- 不升级 Java、Tomcat、Maven、MariaDB、jQuery、jQuery UI、zTree 或 chosen。
- 不新增 API endpoint。

## Product Design 结论

旧 `modern-theme foundation` 方向被判定为体验方向失败：它能让页面变浅、变白、变圆角，但没有改善当前位置、页面任务、空/加载/错误状态、主操作和导航层级。

本版本选择 Product Design 重新规划中的“运营控制台”方向：优先让后台用户知道当前在哪、正在管理什么、下一步能做什么，而不是继续扩大视觉皮肤范围。

## 回滚方式

优先移除或注释：

```jsp
<link href="${_cp}/css/bpmt-modern.css" rel="stylesheet">
```

该引入位于 `platform/src/main/webapp/include/html_include.jsp`。如果只需局部修复，优先调整 `bpmt-modern.css` 或本次新增的代表路径语义文案，不要回退无关业务代码。

## 验证记录

- Maven 编译：`mvn -s settings.local.xml -DskipTests compile` 通过，Reactor 全部 `SUCCESS`。
- Web 镜像：`scripts/build-image.sh` 通过，生成并验证 `ghcr.io/wodenwang/bpmt-lite:1.8.0`。
- API 镜像：`scripts/build-api-image.sh` 通过，生成并验证 `ghcr.io/wodenwang/bpmt-lite-api:1.8.0`。
- 本地服务：`BPMT_HTTP_PORT=18080 docker compose up -d` 通过，`bpmt-web`、`bpmt-api`、`bpmt-nginx`、`bpmt-mariadb` 运行中。
- HTTP smoke：`/`、`/ueditor/`、`/api/docs/`、`/api/openapi.json`、`/css/bpmt-modern.css` 均返回 `200`。
- OpenAPI 元数据：`info.title` 为 `BPMT Lite v1.8.0 API`，`info.version` 为 `1.8.0`。
- Playwright 截图：见 `docs/v2.0.0/screenshots/v1.8.0-clarity-implemented/`。
- 代表路径验证：
  - `/` 登录页可打开并可使用 `admin/admin` 登录。
  - 登录后首页会话摘要和首页面板状态可见。
  - `/thirdpart/ThirdpartAction/index.shtml` 第三方系统列表可打开，主操作和行操作可识别。
  - AI 接入提示词弹窗可打开、生成提示词并显示复制反馈。
  - 第三方系统列表搜索无结果时显示“没有匹配的外部系统”和调整查询引导。
- AI 接入提示词弹窗截图使用本地临时验证行 `codex_ui_qa`，验证完成后已从本地 MariaDB 清理。
