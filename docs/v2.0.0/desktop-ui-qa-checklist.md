# 桌面 UI QA 清单

> 状态：`v1.8.0 modern-theme foundation` 的功能 smoke 结果可作为技术证据保留，但 Product Design 已判定该方向体验失败。下表中的 `PASS` 只表示页面可打开、无明显破版和无 console error，不表示方向通过。

本清单用于 `v1.8.0` 及后续桌面 UI 版本。每轮实现后以 1440x900 桌面视口截图和实点操作为准。

## 页面范围

| 页面 | 验收点 | v1.8.0 modern-theme 状态 |
| --- | --- | --- |
| 登录页 `/` | 登录 panel、输入框、按钮、错误提示、footer | PASS，截图 `screenshots/v1.8.0/01-login.png` |
| 登录后首页 | topbar、header、模块按钮、左侧菜单、主内容 panel | PASS，截图 `screenshots/v1.8.0/02-home.png` |
| 功能设置首页 `manage.xhtml` | 当前模块、左侧菜单、主内容空态/首页标签 | PASS，截图 `screenshots/v1.8.0/03-manage.png` |
| 第三方系统列表 | 表格、行操作、状态列、分页、创建按钮 | PASS，截图 `screenshots/v1.8.0/04-thirdpart-list.png` |
| AI 接入提示词弹窗 | 弹窗标题、表单字段、textarea、复制按钮、关闭 | PASS，截图 `screenshots/v1.8.0/05-ai-prompt-dialog.png` |
| API 文档 `/api/docs/` | API 文档可访问且版本显示正确 | PASS，截图 `screenshots/v1.8.0/06-api-docs.png` |

## 视觉检查

- 文本没有重叠、裁切或被按钮遮挡。
- 按钮文案不换行。
- 表格 hover 可见，行高适合扫描。
- 表单控件高度一致，focus 明显。
- 弹窗标题、内容和底部按钮层级清楚。
- zTree 菜单 hover/选中态可识别。
- chosen 下拉不被遮挡。
- footer 不遮挡主要内容。

## 交互检查

- 登录输入和回车提交正常。
- 顶部菜单 hover、点击和全屏切换正常。
- 左侧菜单展开、滚动和点击正常。
- 列表查询、分页和行操作按钮正常。
- AI 接入提示词弹窗可以打开、复制、关闭。
- 浏览器 console 无新增 JavaScript error。

## 回滚检查

- 移除 `html_include.jsp` 中 `bpmt-modern.css` 引入后，旧主题可恢复。
- 回滚不需要改数据库、不需要改业务配置。

## v1.8.0 验证结果

- Playwright 1440x900 路径验证完成，console error 为空。
- 本地 HTTP smoke：`/`、`/ueditor/`、`/api/docs/`、`/api/openapi.json`、`/css/bpmt-modern.css` 均返回 `200`。
- Product Design 自审后已修复按钮层级问题：`ws-bar` 不再把每个区域第一个按钮默认强化为主按钮，只强化明确主操作和当前态。
- Product Design 复盘结论：方向 rejected。CSS-only 主题没有解决当前位置、页面任务、空/加载/错误状态、主操作和导航层级问题。后续执行以 `desktop-admin-clarity-replan.md` 为准。

## v1.8.0 清晰度基线验证结果

| 页面 | 验收点 | v1.8.0 clarity 状态 |
| --- | --- | --- |
| 登录页 `/` | 登录页仍可打开，`admin/admin` 登录可进入桌面后台 | PASS，截图 `screenshots/v1.8.0-clarity-implemented/01-login.png` |
| 登录后首页 | 会话摘要、首页面板 loading/empty 文案、主内容层级 | PASS，截图 `screenshots/v1.8.0-clarity-implemented/02-home.png` |
| 第三方系统列表 | 页面标题、用途说明、主操作、查询区和列表区层级、行操作可识别 | PASS，截图 `screenshots/v1.8.0-clarity-implemented/03-thirdpart-list.png` |
| AI 接入提示词弹窗 | 副说明、字段分组、只读提示词区域、复制按钮 | PASS，截图 `screenshots/v1.8.0-clarity-implemented/04-ai-prompt-dialog.png` |
| AI 接入提示词复制反馈 | 点击复制后显示明确反馈 | PASS，截图 `screenshots/v1.8.0-clarity-implemented/05-ai-prompt-copy-feedback.png` |
| 第三方系统筛选无结果 | 空状态说明当前筛选无结果，并提示调整关键词或重置查询 | PASS，截图 `screenshots/v1.8.0-clarity-implemented/06-thirdpart-empty-filtered.png` |

AI 接入提示词弹窗截图使用本地临时验证行 `codex_ui_qa`，验证完成后已从本地 MariaDB 清理。

验证命令：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn -s settings.local.xml -DskipTests compile
scripts/build-image.sh
scripts/build-api-image.sh
BPMT_HTTP_PORT=18080 docker compose up -d --force-recreate
curl -fsSI http://127.0.0.1:18080/
curl -fsSI http://127.0.0.1:18080/ueditor/
curl -fsSI http://127.0.0.1:18080/api/docs/
curl -fsSI http://127.0.0.1:18080/api/openapi.json
curl -fsSI http://127.0.0.1:18080/css/bpmt-modern.css
```

## v1.8.1 前端缺陷加固检查

| 问题 | 风险 | v1.8.1 处理 |
| --- | --- | --- |
| icon-only 按钮缺少可访问名称 | 键盘和读屏用户无法识别分页、行操作等按钮含义 | 通用 `styleButton` 从按钮原始文本自动补 `title` / `aria-label` |
| 第三方系统重置查询不能清空当前筛选 | 无匹配空状态给出动作，但用户点击后仍停留在筛选结果 | `resetThirdpartQuery` 显式清空文本和 select 后提交 |
| AI 提示词弹窗缺少底部关闭动作 | 用户只能依赖标题栏关闭，长文本复制任务结束路径不够明确 | 弹窗底部增加“关闭”次操作 |
| AI 提示词复制异常路径反馈不完整 | 浏览器阻止 Clipboard API 时，用户不知道如何继续 | 失败时选中文本并提示手动复制 |
| 分页条旧 float/absolute 布局 | 记录数说明和分页按钮在窄内容区可能重叠 | `.ws-bar.page` 改为 flex 覆盖布局 |
| focus / disabled 状态不完整 | 键盘焦点和不可用按钮在旧控件中不够清楚 | CSS 补充链接、树节点、禁用按钮状态 |
| 通用查询表单 reset 不清空服务端回填值 | 动态表、工作流、报表等页面筛选无结果后，重置仍可能停留在当前筛选 | `Core.resetQueryForm` 清空查询字段、重置分页/排序并提交 |
| 低代码列表无结果只显示空表格 | 用户无法区分无数据、筛选过严、权限不足或加载失败 | 动态表、工作流、报表、待办列表补统一空状态 |
| 全局提示和确认弹窗缺少稳定语义 | 保存/删除/错误反馈只靠视觉弹窗，键盘和辅助技术用户不易判断状态 | `Ui.msg`、`Ui.alert`、`Ui.confirm` 补 `role`、`aria-live` 和关闭按钮可访问名称 |
| 后台管理列表无结果只显示空表格 | 邮件、微信、任务、队列、表和视图配置筛选后缺少结果解释 | 高频管理列表补统一空状态，通用初始化层为剩余旧列表补兜底 |
| 新登录页 label 和登录动作语义不完整 | 输入框缺少稳定标签，登录链接不像按钮，空格键不可触发 | 补 label 关联、autocomplete、验证码说明和 `role=button` 键盘触发 |

v1.8.1 验证命令：

```bash
node --check platform/src/main/webapp/js/jquery-ui-ext.js
docker compose config
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn -s settings.local.xml -DskipTests compile
curl -fsSI http://127.0.0.1:18080/css/bpmt-modern.css
```

v1.8.1 当前验证状态：

- `node --check platform/src/main/webapp/js/jquery-ui-ext.js`：PASS。
- `node --check platform/src/main/webapp/js/ws-ui.js`：PASS。
- `docker compose config`：PASS。
- `mvn -s settings.local.xml -DskipTests compile`：PASS。
- `scripts/build-image.sh`：PASS，已构建本地 Web 镜像。
- `BPMT_HTTP_PORT=18080 docker compose up -d --force-recreate bpmt-web bpmt-nginx`：PASS。
- HTTP 登录 `admin/admin`：PASS，返回 `{"flag":true}`。
- 第三方系统主页面和 Ajax 列表片段：PASS，已验证重置查询脚本、无匹配空态、AI 弹窗复制兜底、`role=status` 和关闭按钮。
- 通用查询重置和低代码列表空状态：PASS，容器内 `ws-core-ext.js` 已包含 `Core.resetQueryForm`；动态表、工作流、报表、待办 JSP 已包含空状态；登录后待办 Ajax 片段已渲染“没有待办任务”。
- 高频管理列表和通用兜底空状态：PASS，容器内邮件/微信/任务/队列/表/视图配置 JSP 已包含空状态；HTTP `/js/ws-core-ext.js` 已包含 `Core.applyEmptyTableState`。
- 全局消息/弹窗语义：PASS，容器内 `ws-ui.js` 已包含 `role`、`aria-live` 和关闭按钮 `aria-label`。
- 新登录页语义：PASS，容器内 JSP 和 HTTP `/xhtml/frame_new/login.jsp` 均已验证 label 关联、autocomplete 和登录链接按钮语义。
- 真实浏览器截图和键盘 Tab 焦点巡检：未完成，本轮 Chrome DevTools 只暴露 page list，未暴露导航/截图/交互接口。
