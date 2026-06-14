# bpmt-lite v1.9.0 Release Notes

## 范围

`v1.9.0` 是基于 `v1.8.1` 的桌面后台壳层与全局前端缺陷修复版本。

本版本修复：

- 弹框遮罩和窗体层级低于部分页面按钮的问题。
- 弹框右上角关闭按钮尺寸不一致的问题。
- 查询从空结果变为有结果时，空状态和结果表格同时存在的问题。
- logo、菜单、顶部功能按钮、主功能展示区域的高度和宽度不对齐问题。
- 非菜单 zTree hover 前后尺寸变形问题。

## 实现摘要

- `platform/src/main/webapp/css/bpmt-modern.css`
  - 新增全局 layer ladder。
  - 将 `.ui-widget-overlay` 固定为 `z-index: 5000`，`.ui-dialog` 固定为 `z-index: 5010`。
  - 统一 `.ui-dialog-titlebar-close` 和 dialogExtend titlebar buttonpane 按钮为 `32 x 32`。
  - 将全站 `.ztree` 基础行高、链接高度、padding 和 border 统一为稳定盒模型。
  - 将旧 `.frame.*` 桌面壳层中的 logo 区和菜单区视觉合并为左侧导航列，并让顶部按钮区和主内容区共用同一工作区起点。
- `platform/src/main/webapp/js/ws-core-ext.js`
  - 修复 `Core.applyEmptyTableState` 的生命周期。当前表格已有数据行时，会移除旧 `.bpmt-empty-table-row`。

## 设计与计划证据

- `docs/v2.0.0/v1.9.0-desktop-shell-navigation-design.md`
- `docs/v2.0.0/v1.9.0-plan-design-review-2026-06-14.md`
- `design/bpmt-v1.9.0-shell-navigation.pen`
- `design/v1.9.0-shell-navigation-source-export/`
- `docs/v2.0.0/v1.9.0-prototype-review-2026-06-14.md`
- `docs/v2.0.0/v1.9.0-plan-eng-review-2026-06-14.md`
- `docs/superpowers/plans/2026-06-14-bpmt-lite-v1.9.0-shell-navigation-hardening.md`

## 验证

已完成：

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn -s settings.local.xml -DskipTests compile
```

结果：`BUILD SUCCESS`。

镜像构建验证：

```bash
scripts/build-image.sh
scripts/build-api-image.sh
```

结果：

- Web 镜像 `ghcr.io/wodenwang/bpmt-lite:1.9.0` 构建与脚本内置 smoke 通过。
- API 镜像 `ghcr.io/wodenwang/bpmt-lite-api:1.9.0` 构建与脚本内置 smoke 通过。
- Web 多架构镜像已推送并回读：
  - `ghcr.io/wodenwang/bpmt-lite:1.9.0`
  - `ghcr.io/wodenwang/bpmt-lite:latest`
  - digest：`sha256:76653fa22ac0dba72733ee0e6b24fc86e088609bfc1de0b6b8fe5f7a0ac1f209`
  - 平台：`linux/amd64`、`linux/arm64`
- API 多架构镜像已推送并回读：
  - `ghcr.io/wodenwang/bpmt-lite-api:1.9.0`
  - `ghcr.io/wodenwang/bpmt-lite-api:latest`
  - digest：`sha256:1ced7f2182c154b55685049c6887b7966346992e3bd61f3b5d7404189d896705`
  - 平台：`linux/amd64`、`linux/arm64`

运行态验证：

- 将修改后的 `bpmt-modern.css` 和 `ws-core-ext.js` 临时复制到当前 `bpmt-web` 容器。
- `curl -I http://127.0.0.1:18080/` 返回 `HTTP/1.1 200 OK`。
- 容器内 grep 已确认运行态文件包含 `Layer ladder` 和 `$emptyRows.remove()`。
- 最终本地镜像内 grep 已确认：
  - `ghcr.io/wodenwang/bpmt-lite:1.9.0` 包含 `bpmt-modern.css` 的 `Layer ladder`、`ws-core-ext.js` 的 `$emptyRows.remove()`、`html_include.jsp` 的 `20260614-v190-shell` 和 `20260614-v190-empty-state`。
  - `ghcr.io/wodenwang/bpmt-lite-api:1.9.0` 包含 `/api/openapi.json` 和 `/api/docs/index.html`。
- 远端 tag 通过 `docker run --pull=always` 回读确认：
  - `ghcr.io/wodenwang/bpmt-lite:1.9.0` digest 为 `sha256:76653fa22ac0dba72733ee0e6b24fc86e088609bfc1de0b6b8fe5f7a0ac1f209`，并包含本次 CSS/JS/cache-busting 标记。
  - `ghcr.io/wodenwang/bpmt-lite-api:1.9.0` digest 为 `sha256:1ced7f2182c154b55685049c6887b7966346992e3bd61f3b5d7404189d896705`，并包含 API docs/openapi 资产。

Playwright 验证证据：

- `docs/v2.0.0/screenshots/v1.9.0-shell-navigation/01-home-1440.png`
- `docs/v2.0.0/screenshots/v1.9.0-shell-navigation/02-home-1366.png`
- `docs/v2.0.0/screenshots/v1.9.0-shell-navigation/03-dialog-layer.png`
- `docs/v2.0.0/screenshots/v1.9.0-shell-navigation/04-empty-to-results.png`
- `docs/v2.0.0/screenshots/v1.9.0-shell-navigation/05-non-menu-ztree-hover.png`
- `docs/v2.0.0/screenshots/v1.9.0-shell-navigation/verification.json`
- `docs/v2.0.0/screenshots/v1.9.0-shell-navigation/shell-layout.json`

关键验证数据：

```json
{
  "modal": {
    "overlayZ": "5000",
    "dialogZ": "5010",
    "highButtonZ": "999",
    "closeWidth": "32px",
    "closeHeight": "32px"
  },
  "emptyState": {
    "afterEmpty": 1,
    "afterData": 0,
    "dataRows": 1
  },
  "ztree": {
    "base": {
      "height": "32px"
    },
    "hover": {
      "height": "32px"
    }
  }
}
```

待发布收口时补充：

- Git tag 和 GitHub Release。
