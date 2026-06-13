# bpmt-lite v1.8.1 发布说明

## 版本定位

`v1.8.1` 是基于 `v1.8.0` 的前端缺陷加固补丁版本。它继续沿用 `desktop admin clarity baseline` 方向，把 v1.8.0 暴露出的桌面后台可用性、可访问性和状态反馈问题集中归拢到 `v1.8.*` 修复线。

本版本不新增业务功能，不新增 API endpoint，不改变数据库结构、Docker Compose 拓扑、OAuth 主流程或 HMAC API 鉴权规则。

## 修复内容

- 通用 jQuery UI button 初始化会从按钮原始文本自动补齐缺失的 `title` 和 `aria-label`，覆盖分页、动态表、工作流、邮件和第三方系统等 icon-only 按钮。
- 第三方系统“重置查询”改为显式清空筛选条件并重新提交，避免在筛选无结果时 reset 回到当前 query 参数。
- AI 接入提示词弹窗补充底部“关闭”按钮，复制失败路径统一选中文本并提示手动复制。
- `bpmt-modern.css` 补充链接/树节点焦点、禁用按钮和分页条 flex 布局，降低键盘操作不可见、禁用态不清楚和分页信息重叠风险。
- 通用 `query="true"` 表单的“重置查询”会清空筛选、重置分页/排序并重新提交，避免多个后台列表停留在当前筛选值。
- 动态表、工作流、报表和待办列表补充无匹配数据空状态，减少空表格误判。
- 全局 `Ui.msg`、`Ui.alert` 和 `Ui.confirm` 补充 `role`、`aria-live` 和关闭按钮可访问名称，改善旧页面反馈和确认弹窗的可访问性。
- 邮件、微信应用、公众号、定时任务、队列、数据表和视图配置列表补充空状态，并在通用初始化层为剩余旧列表表格增加空状态兜底，覆盖更多后台管理筛选场景。
- 新登录页补齐输入框 label、autocomplete、验证码图片说明和登录链接按钮语义，改善键盘与辅助技术操作。
- 更新桌面 UI QA 清单和 v1.8.1 修复计划。

## 文档

- 执行计划：[docs/superpowers/plans/2026-06-13-bpmt-lite-v1.8.1-frontend-defect-hardening.md](superpowers/plans/2026-06-13-bpmt-lite-v1.8.1-frontend-defect-hardening.md)
- QA 清单：[docs/v2.0.0/desktop-ui-qa-checklist.md](v2.0.0/desktop-ui-qa-checklist.md)

## 验证记录

- [x] `node --check platform/src/main/webapp/js/jquery-ui-ext.js`
- [x] `node --check platform/src/main/webapp/js/ws-core-ext.js`
- [x] `node --check platform/src/main/webapp/js/ws-ui.js`
- [x] `docker compose config`
- [x] `mvn -s settings.local.xml -DskipTests compile`
- [x] `scripts/build-image.sh`，本地生成并验证 `ghcr.io/wodenwang/bpmt-lite:1.8.1`。
- [x] `BPMT_HTTP_PORT=18080 docker compose up -d --force-recreate bpmt-web bpmt-nginx`
- [x] HTTP smoke：`/`、`/api/docs/`、`/css/bpmt-modern.css`
- [x] HTTP 登录：`POST /frame/LoginAction/login.shtml` 使用 `admin/admin` 返回 `{"flag":true}`。
- [x] 第三方系统主页面片段包含 `resetThirdpartQuery`、页面标题说明和“新增外部系统”主操作。
- [x] 第三方系统列表 Ajax 片段包含“没有匹配的外部系统”、AI 提示词复制兜底、`role="status"`、只读提示词标签和底部关闭按钮。
- [x] Web 镜像重建后验证容器内通用查询重置脚本和低代码列表空状态；登录后待办 Ajax 片段已渲染“没有待办任务”。
- [x] Web 镜像重建后验证容器内高频管理列表空状态、`ws-ui.js` 弹窗语义和 `Core.applyEmptyTableState` 通用兜底。
- [x] Web 镜像重建后验证新登录页 label 关联、autocomplete 和登录链接按钮语义。
- [x] 内置浏览器人工标注问题已逐项修复并用 Playwright 补齐截图证据：全局底部栏、icon+文本按钮、按钮垂直居中、表格 hover 不变形、表格底部样式、复选框/操作列宽、zTree 菜单 hover、顶部栏 icon+文本。
