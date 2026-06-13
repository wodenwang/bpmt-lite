# Configuration Workbench Lite Verification - 2026-06-13

## Commands

- `mvn -s settings.local.xml -DskipTests compile`: `BUILD SUCCESS`
- `docker cp platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/xhtml/thirdpart/ThirdpartAction/list.jsp`: 用于把本地 JSP 同步到当前 `18080` 验证容器。
- `docker cp platform/src/main/webapp/css/bpmt-modern.css bpmt-web:/usr/local/tomcat/webapps/ROOT/css/bpmt-modern.css`: 用于把本地 CSS 同步到当前 `18080` 验证容器。
- `docker compose restart bpmt-web`: 清理 Web 容器应用缓存后重新验证第三方系统列表。

## Browser Checks

- 首页 zTree：通过。DOM 中仍存在 `.ztree`、折叠 `switch`、sprite `button`，没有替换成现代文本菜单。强制刷新 `bpmt-modern.css` 后，节点行高为 `26px`，sprite opacity 为 `0.72`。
- 第三方系统列表：通过。表头包含操作、系统标识、系统名称、Client ID、状态、首页地址、微信登录、创建时间、更新时间、说明。临时数据行渲染后出现 5 个 `.bpmt-text-clip`，长文本 computed style 为 `white-space: nowrap`、`overflow: hidden`、`word-break: normal`、`overflow-wrap: normal`。
- AI 接入提示词弹窗：通过。弹窗顶部展示系统标识、Client ID、OAuth 回调地址；元信息区为 grid 三列布局；提示词文本使用当前行数据；复制按钮返回“已复制提示词。”。

## Screenshots

- `01-home-ztree.png`
- `02-thirdpart-list.png`
- `03-ai-prompt-dialog.png`

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
