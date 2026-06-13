# Configuration Workbench Lite Verification - 2026-06-13

## Commands

- `mvn -s settings.local.xml -DskipTests compile`: `BUILD SUCCESS`
- `docker cp platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/xhtml/thirdpart/ThirdpartAction/list.jsp`: 用于把本地 JSP 同步到当前 `18080` 验证容器。
- `docker cp platform/src/main/webapp/xhtml/dyn/list.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/xhtml/dyn/list.jsp`: 用于把动态表列表底部行修复同步到当前 `18080` 验证容器。
- `docker cp platform/src/main/webapp/xhtml/report/list.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/xhtml/report/list.jsp`: 用于把报表列表底部行修复同步到当前 `18080` 验证容器。
- `docker cp platform/src/main/webapp/xhtml/flow/view/list.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/xhtml/flow/view/list.jsp`: 用于把流程列表底部行修复同步到当前 `18080` 验证容器。
- `docker cp platform/src/main/webapp/css/bpmt-modern.css bpmt-web:/usr/local/tomcat/webapps/ROOT/css/bpmt-modern.css`: 用于把本地 CSS 同步到当前 `18080` 验证容器。
- `docker cp platform/src/main/webapp/css/jquery-ui-ext.css bpmt-web:/usr/local/tomcat/webapps/ROOT/css/jquery-ui-ext.css`: 用于把表格 hover 稳定性修复同步到当前 `18080` 验证容器。
- `docker cp platform/src/main/webapp/include/html_include.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/include/html_include.jsp`: 用于把 `bpmt-modern.css` cache-busting 参数同步到当前 `18080` 验证容器。
- `docker compose restart bpmt-web`: 清理 Web 容器应用缓存后重新验证第三方系统列表。

## Browser Checks

- 首页 zTree：通过。DOM 中仍存在 `.ztree`、折叠 `switch`、sprite `button`，没有替换成现代文本菜单。强制刷新 `bpmt-modern.css` 后，节点行高为 `26px`，sprite opacity 为 `0.72`。
- 第三方系统列表：通过。表头包含操作、系统标识、系统名称、Client ID、状态、首页地址、微信登录、创建时间、更新时间、说明。临时数据行渲染后出现 5 个 `.bpmt-text-clip`，长文本 computed style 为 `white-space: nowrap`、`overflow: hidden`、`word-break: normal`、`overflow-wrap: normal`。
- AI 接入提示词弹窗：通过。弹窗顶部展示系统标识、Client ID、OAuth 回调地址；元信息区为 grid 三列布局；提示词文本使用当前行数据；复制按钮返回“已复制提示词。”。
- 全局 footer：通过。登录页和登录后框架页均加载当前版本化 `bpmt-modern.css`；`.frame.footer` computed `position` 为 `fixed`，宽度等于当前右侧浏览器视口宽度，主框架 `.frame.center` 保留 `46px` 底部空间。
- icon+文本按钮：通过。登录按钮为 `inline-flex`，图标与文字间距 `6px`，图标、文字、按钮中心线偏差为 `0`。
- 普通按钮条：通过。登录页 `.ws-bar` 为 flex 居中，按钮条、`.center`、按钮、图标、文字中心线偏差为 `0`；普通按钮条最小高度为 `48px`。
- 主内容横向越界：通过。服务端 HTML 返回 `bpmt-modern.css?v=20260613-top-menu`；CSS 返回 `body { min-width: 0; }` 和 `.frame.main { min-width: 0 !important; overflow-x: auto; }`；`08-table-bottom-stable.png` 显示登录后框架在桌面宽视口下没有整页横向推出。
- 表格行 hover 稳定性：通过。`jquery-ui-ext.css` 不再在 hover 时增加 `border-bottom-width` 或下一行 `border-top-width`；`bpmt-modern.css` 显式锁定表格单元格 `border-width`、`line-height`、`padding`。右侧浏览器对 `操作` 表头测量 hover 前后 `cellW/cellH/rowW/rowH` delta 均为 `0`。
- BPMT logo：代码和服务端资源验证通过。新版 SVG 源文件位于 `design/bpmt-logo-horizontal.svg`、`design/bpmt-logo-frame-new.svg`，导出的 PNG 保持原尺寸 `206x50` 和 `150x87`。服务端 HTML 返回 `logo.png?v=20260613-logo`，PNG 资源返回 200。
- 查询条件按钮条：通过。服务端 HTML 返回 `bpmt-modern.css?v=20260613-top-menu`；`th.ws-bar` 专门恢复 `display: table-cell`，内部 `.left/.right/.center` 按钮组保留左/右/中对齐语义，同时用 `inline-flex` 稳定图标文本间距；员工通讯录查询区按钮左中右布局正常。
- 表格底部样式：通过。服务端 HTML 返回 `bpmt-modern.css?v=20260613-top-menu`；`dyn`、`report`、`flow/view` 三类列表底部 summary 按钮行已改为 `colspan="99"`，无按钮时隐藏；员工通讯录页面底部只显示完整分页信息条，不再出现局部空白块或双层断裂边框。
- 表格复选框/操作列宽：通过。服务端 HTML 返回 `bpmt-modern.css?v=20260613-top-menu`；1219x869 视口下员工通讯录表格首列 `.ws-checkbox` computed `width/min-width/max-width` 均为 `40px`，紧邻操作列为 `64px`。
- zTree 菜单 hover 稳定性：通过。服务端 HTML 返回 `bpmt-modern.css?v=20260613-top-menu`；`用户权限` 菜单项默认态与 selected/hover 等价态均为 `78x26`，宽高 delta 均为 `0`。
- 顶部栏 icon+文本稳定性：通过。服务端 HTML 返回 `bpmt-modern.css?v=20260613-top-menu`；`设置` 顶部菜单项默认态和 hover 高亮态 `li` 均为 `60x24`，`a` 均为 `58x22`，图标/文字中心线差为 `0`。

## Screenshots

- `01-home-ztree.png`
- `02-thirdpart-list.png`
- `03-ai-prompt-dialog.png`
- `04-footer-fixed-global.png`
- `05-icon-text-button-login.png`
- `06-button-bar-centered-login.png`
- `07-table-hover-stable.png`
- `08-table-bottom-stable.png`
- `09-table-checkbox-action-width.png`
- `10-ztree-hover-stable.png`
- `11-top-menu-icon-text-stable.png`
- `design/bpmt-logo-preview.png`

## Temporary Data

验证期间临时插入：

- `CM_THIRDPART.THIRDPART_KEY = codex_ui_verify`
- `CM_PRI.PRI_KEY = codex_ui_verify`

验证完成后已删除，复核计数：

- `remaining_thirdpart = 0`
- `remaining_pri = 0`

## Residual Risk

- 本切片不覆盖 H5 页面。
- 当前 `18080` 来自 `ghcr.io/wodenwang/bpmt-lite:1.8.0` 容器，不是源码热加载；浏览器验证通过 `docker cp` 同步 JSP/CSS 到容器运行目录完成。
- 浏览器可能缓存 `bpmt-modern.css`；本轮验证通过 cache-busting 参数确认最终 CSS 生效。
- zTree 仍依赖历史 sprite 资源；本次只做 CSS 降噪，不替换图标系统。
