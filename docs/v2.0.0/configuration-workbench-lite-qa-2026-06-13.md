# Configuration Workbench Lite QA - 2026-06-13

## Scope

my-harness 第 11 步：对本次低成本配置工作台 UI 切片做系统化功能 QA。

范围：

- 登录后访问第三方系统管理页
- 第三方系统列表空状态、无结果状态、重置查询
- 临时第三方系统数据行
- AI 接入提示词弹窗元信息、提示词生成、复制成功和复制 fallback
- 新增/编辑入口打开
- 首页 zTree 结构回归

不覆盖：

- H5 页面
- 真实 OAuth 授权码登录闭环
- 真实微信 OAuth 登录
- 新增/编辑表单保存提交
- 启停按钮真实提交，避免污染临时 QA 数据

## Commands

- `mvn -s settings.local.xml -DskipTests compile`: `BUILD SUCCESS`
- `docker cp platform/src/main/webapp/css/jquery-ui-ext.css bpmt-web:/usr/local/tomcat/webapps/ROOT/css/jquery-ui-ext.css`
- `docker cp platform/src/main/webapp/css/bpmt-modern.css bpmt-web:/usr/local/tomcat/webapps/ROOT/css/bpmt-modern.css`
- `docker cp platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/xhtml/thirdpart/ThirdpartAction/list.jsp`
- `docker cp platform/src/main/webapp/xhtml/dyn/list.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/xhtml/dyn/list.jsp`
- `docker cp platform/src/main/webapp/xhtml/report/list.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/xhtml/report/list.jsp`
- `docker cp platform/src/main/webapp/xhtml/flow/view/list.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/xhtml/flow/view/list.jsp`
- `docker cp platform/src/main/webapp/include/html_include.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/include/html_include.jsp`
- `docker compose restart bpmt-web`: 用于清理 BPMT Web 应用缓存，确保数据库临时数据增删能反映到页面。

## Browser QA Results

| Check | Result | Evidence |
|---|---|---|
| 登录后第三方系统空状态 | Pass | `还没有外部系统` 出现，临时行不可见，字段表头完整 |
| 查询无结果状态 | Pass | 输入 `definitely-no-match-qa` 后出现 `没有匹配的外部系统` |
| 重置查询 | Pass | 查询值清空，页面回到空状态 |
| 临时数据行渲染 | Pass | `codex_ui_verify` 可见，编辑/启停/AI 按钮各 1 个 |
| 长文本安全 | Pass | `.bpmt-text-clip` 数量 5，computed style 为 `nowrap/hidden/normal` |
| AI 弹窗元信息 | Pass | 系统标识、Client ID、OAuth 回调地址均来自当前行 |
| 提示词生成 | Pass | 提示词包含当前 Client ID、redirect URI 和 `CLIENT_SECRET_PLACEHOLDER` |
| 复制成功路径 | Pass | 点击复制后显示 `已复制提示词。` |
| 复制 fallback 路径 | Pass | 模拟 `navigator.clipboard.writeText` reject 和同步 throw 后均显示手动复制提示，`promptText` 获得焦点并选中 1417 字符 |
| 编辑入口 | Pass | 点击编辑后打开 `编辑外部系统` 页签，包含当前临时系统上下文，无错误文本 |
| 新增入口 | Pass | 点击新增后打开 `新增外部系统` 页签，包含系统标识、Client ID、保存等表单上下文 |
| 人工标注：全局 footer 漂浮在页面中部 | Pass | `04-footer-fixed-global.png`；登录页和登录后框架页 `.frame.footer` 均为 `position: fixed` |
| 人工标注：icon+文本按钮图文挤压 | Pass | `05-icon-text-button-login.png`；登录按钮为 `inline-flex`，图标和文字间距 `6px`，垂直中心偏差 `0` |
| 人工标注：按钮条内按钮未垂直居中 | Pass | `06-button-bar-centered-login.png`；登录页 `.ws-bar`、`.center`、按钮、图标、文字中心线偏差均为 `0` |
| 人工标注：首页主内容框横向越界 | Pass | `08-table-bottom-stable.png` 同步覆盖登录后框架宽度；服务端返回 `bpmt-modern.css?v=20260613-top-menu`；CSS 已取消 `body min-width:1024px`，`.frame.main` 改为可收缩并内部横向滚动 |
| 人工标注：表格行 hover 导致宽高变形 | Pass | `07-table-hover-stable.png`；右侧浏览器测得 `操作` 表头 hover 前后 cell/row 宽高变化均为 `0` |
| 人工标注：BPMT logo 视觉粗糙 | Fixed, needs human taste check | `design/bpmt-logo-preview.png`；默认 logo URL 已输出 `logo.png?v=20260613-logo`，新版 PNG 保持原尺寸 `206x50` / `150x87` |
| 人工标注：查询条件按钮条变形，左右按钮组混在一起 | Pass | `08-table-bottom-stable.png`；服务端返回 `bpmt-modern.css?v=20260613-top-menu`；`th.ws-bar` 恢复 `table-cell`，内部 `.left/.right` 按钮组恢复左/右对齐 |
| 人工标注：表格底部空白块和边框断裂 | Pass | `08-table-bottom-stable.png`；员工通讯录表格底部为完整分页信息条，底部空的 summary 按钮行已隐藏，滚动容器和表格不再叠加双层边框 |
| 人工标注：复选框/操作列宽度分配不合理 | Pass | `09-table-checkbox-action-width.png`；1219px 视口下员工通讯录表格复选框列锁定为 `40px`，操作列锁定为 `64px` |
| 人工标注：zTree 菜单 hover 晃动 | Pass | `10-ztree-hover-stable.png`；`用户权限` 菜单项默认态和 selected/hover 等价态宽高差均为 `0` |
| 人工标注：顶部栏 icon+文本变形 | Pass | `11-top-menu-icon-text-stable.png`；`设置` 顶部菜单 hover 前后 `li/a` 宽高差均为 `0`，图标和文字中心线差为 `0` |
| Console | Pass with note | Playwright console 只有登录表单 autocomplete verbose 提示；未发现本切片 JS error |

## Temporary Data

验证期间临时插入：

- `CM_THIRDPART.THIRDPART_KEY = codex_ui_verify`
- `CM_PRI.PRI_KEY = codex_ui_verify`

QA 完成后已删除并重启 Web 容器清理应用缓存。复核：

- `remaining_thirdpart = 0`
- `remaining_pri = 0`

## Findings

### Fixed / Handled

1. 全局 footer 在窄视口下出现在页面中部，登录页尤为明显。
   - Root cause: `bpmt-modern.css` 将 `.frame.footer` 保留为普通文档流，登录页内容高度不足时底栏落在卡片区域附近；浏览器旧 CSS 缓存也会持续复现旧布局。
   - Action: `.frame.footer` 改为固定底部、视口宽度、单行省略；`.frame.center` 和 `.frame.login` 增加底部空间；`html_include.jsp` 为 `bpmt-modern.css` 增加版本查询参数。
   - Evidence: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/04-footer-fixed-global.png`。

2. icon+文本按钮图标与文字挤压、按钮条内按钮存在像素级垂直偏差。
   - Root cause: jQuery UI `ui-button-text-icon-primary` 仍使用绝对定位图标；现代样式层清理文字 padding 后没有重建图标/文字布局。普通 `.ws-bar` 仍受旧 `position:absolute;width:95%` 的 `.center` 规则影响，并且 `min-height:34px` 不足以容纳 30px 按钮和 8px 上下留白。
   - Action: `ui-button-text-icon-*` 统一改为 `inline-flex`，图标回到正常流并固定 `6px` 间距；普通 `.ws-bar` 统一改为 flex 居中，`.left/.center/.right` 保留分区语义，普通按钮条最小高度提升到 `48px`。
   - Evidence: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/05-icon-text-button-login.png`、`docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/06-button-bar-centered-login.png`。

3. 首页主内容框在窄视口下横向越界。
   - Root cause: `body` 强制 `min-width:1024px`，基础框架又固定左侧菜单 `250px` 和主区 `min-width:700px`，在 Codex 右侧 `785px` 视口中只能产生整页横向溢出。
   - Action: 现代样式层取消 body 1024px 最小宽度；`.frame.center` 和 `.frame.main` 允许收缩；`.frame.main`、面板、tabs 和 scroll 容器改为容器内横向滚动，避免把整页推出视口。
   - Evidence: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/08-table-bottom-stable.png`；服务端 HTML 已返回 `bpmt-modern.css?v=20260613-top-menu`，CSS 返回 `body { min-width: 0; }`、`.frame.main { min-width: 0 !important; overflow-x: auto; }`。

4. 表格行 hover 导致单元格宽高变形。
   - Root cause: `jquery-ui-ext.css` 在 `table.ui-styled-table tbody tr:hover` 时把 `border-bottom-width` 从 `0px` 改为 `1px`，并给下一行增加 `border-top-width`，hover 直接改变了表格尺寸。
   - Action: `jquery-ui-ext.css` 的 hover 规则改为只保留颜色/背景变化，不改变边框宽度；`bpmt-modern.css` 显式锁定表格单元格的 `border-width`、`line-height` 和 `padding`；`html_include.jsp` 为 `jquery-ui-ext.css` 增加版本查询参数。
   - Evidence: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/07-table-hover-stable.png`；右侧浏览器测得 hover 前后 `cellW/cellH/rowW/rowH` delta 均为 `0`。

5. BPMT 默认 logo 视觉粗糙。
   - Root cause: 旧 PNG 字标粗重、压缩感强，在登录页和左侧品牌区显得不精致。
   - Action: 新增 `design/bpmt-logo-horizontal.svg` 和 `design/bpmt-logo-frame-new.svg` 源文件，导出覆盖默认 `logo.png`；保留现有静态资源路径和原始尺寸；`FrameStyleSwitcher` 为默认 logo 路径增加 `?v=20260613-logo`，避免旧缓存继续显示。
   - Evidence: `design/bpmt-logo-preview.png`；服务端 HTML 返回 `logo.png?v=20260613-logo`，PNG 资源返回 200。
   - Follow-up: 这是第一版确定性字标，需要用户继续做审美确认；如需更品牌化，可基于 SVG 源继续迭代。

6. 查询条件按钮条变形，左右按钮组混在一起。
   - Root cause: 历史查询区使用 `<th class="ws-bar">` 包裹 `.left/.right` 两个按钮组；此前为修复普通按钮条垂直居中，将所有 `.ws-bar:not(.page)` 改为 flex，误伤了表格 `th.ws-bar`，导致 table-cell 语义和左右浮动语义失效。
   - Action: 在 `bpmt-modern.css` 中为 `table.ui-styled-table th.ws-bar` / `table.ws-table th.ws-bar` 增加专门覆盖，恢复 `display: table-cell`；内部按钮组用 `inline-flex` 稳定间距，并保留 `.left` 左浮、`.right` 右浮、`.center` 居中。
   - Evidence: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/08-table-bottom-stable.png`；服务端 HTML 返回 `bpmt-modern.css?v=20260613-top-menu`；CSS 返回 `table.ui-styled-table th.ws-bar { display: table-cell; ... }` 和左右按钮组覆盖规则。

7. 表格底部出现空白块和边框断裂，员工通讯录表格尤为明显。
   - Root cause: 动态表、报表、流程列表模板在 `tbody` 后追加底部 summary 按钮行，但该行只渲染一个 `th.ws-bar` 且没有 `colspan`；当没有 summary 按钮时会形成只占首列/局部宽度的空行。现代样式层又同时给 `.ws-scroll` 和内部表格加了边框，底部视觉变成双层断裂。
   - Action: `dyn/list.jsp`、`report/list.jsp`、`flow/view/list.jsp` 的底部按钮行统一增加 `bpmt-summary-button-row` 和 `colspan="99"`；没有按钮的 summary 行通过 CSS 隐藏；`.ws-scroll` 内部表格去掉外层重复边框。
   - Evidence: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/08-table-bottom-stable.png`；服务端 HTML 返回 `bpmt-modern.css?v=20260613-top-menu`，CSS 返回 `bpmt-summary-button-row` 相关规则。

8. 复选框列和操作列在窄视口下宽度分配不合理。
   - Root cause: 旧 `styles.css` 只给 `.ws-checkbox` 设置 `width:30px`，但现代样式层最后加载并统一覆盖了表格单元格 padding/border；自动表格布局下，复选框列和紧邻操作列没有在最终 CSS 层声明 `min/max-width`，容易随内容和缓存版本出现不稳定分配。
   - Action: `bpmt-modern.css` 为 `.ws-scroll` 内的 `.ws-checkbox` 单元格锁定 `40px`，并为紧邻操作列锁定 `64px`；`html_include.jsp` 持续更新现代样式版本号，避免浏览器继续使用旧 CSS。
   - Evidence: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/09-table-checkbox-action-width.png`；Playwright 1219x869 视口测得复选框列 `40px`、操作列 `64px`，且 `minWidth/maxWidth` 均已锁定。

9. zTree 菜单 hover 时出现明显晃动。
   - Root cause: zTree 菜单项默认态没有保留边框空间，hover/selected 态增加 `1px` border；在历史 zTree inline-block/sprite 布局中会造成视觉宽高或对齐抖动。
   - Action: `bpmt-modern.css` 为 `.ztree li a` 和 `#frame-menu-panel .ztree li a` 默认态补 `1px solid transparent`，并让默认态、hover 态、selected 态共享 `height:26px`、`line-height:24px`、`padding:0 4px`；hover 只改变背景和边框颜色。
   - Evidence: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/10-ztree-hover-stable.png`；Playwright 对 `#frame-menu_14_a` 测得默认态 `78x26`，selected/hover 等价态 `78x26`，宽高 delta 均为 `0`。

10. 顶部栏 icon+文本 hover 时变形。
   - Root cause: 顶部菜单 hover class 加在 `li` 上，`li` 由 jQuery UI `ui-state-highlight` 改变边框/背景；内部图标仍依赖内联 `float:left`，链接是普通 `inline-block`，容易造成图标和文字基线、宽高在 hover 前后不稳定。
   - Action: `bpmt-modern.css` 将 `.frame.topbar ul li` 和 `.frame.topbar a` 改为稳定 `inline-flex`，默认 `li` 保留透明 `1px` 边框；覆盖 `.top-menu` 内图标 `float:none`，用 `gap:4px` 控制图文间距；hover 只改颜色和背景。
   - Evidence: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/11-top-menu-icon-text-stable.png`；Playwright 对 `设置` 顶部菜单项测得 hover 前后 `li/a` 宽高 delta 均为 `0`，图标与文字中心线差为 `0`。

11. 删除临时记录后页面仍显示旧行。
   - Root cause: 当前 BPMT Web 容器存在应用层缓存，DB 删除不会马上反映到列表。
   - Action: QA 中每次临时数据增删后重启 `bpmt-web`，避免污染验证结果。
   - Product impact: 这是 QA 环境处理方式，不属于本次 UI 切片代码缺陷。

### Remaining Notes

1. 登录页 autocomplete verbose 提示仍存在。
   - Severity: Low。
   - Reason: 这是既有登录页浏览器提示，不是本次第三方系统列表或 AI 弹窗变更引入。

## Result

第 11 步 QA 通过，可以进入第 12 步代码审查。
