# Configuration Workbench Lite Design Review - 2026-06-13

## Scope

本次是 my-harness 第 10 步：对已实现的低成本配置工作台 UI 切片做视觉和交互 QA。

检查范围：

- 登录后首页左侧 zTree 菜单
- 第三方系统列表
- AI 接入提示词弹窗

不检查范围：

- H5 页面
- OAuth 后端流程
- API 文档与数据库结构
- 全站所有旧后台页面的统一现代化

## Evidence

- `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/01-home-ztree.png`
- `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/02-thirdpart-list.png`
- `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/03-ai-prompt-dialog.png`
- Playwright DOM/computed-style checks:
  - zTree: `.ztree` 存在，24 个节点，sprite `button` 存在；强制刷新 `bpmt-modern.css` 后节点高度 `26px`、sprite opacity `0.72`。
  - 第三方系统列表：表头字段完整，临时数据行出现 5 个 `.bpmt-text-clip`；长文本 computed style 为 `white-space: nowrap`、`overflow: hidden`、`word-break: normal`、`overflow-wrap: normal`。
  - AI 弹窗：元信息区 computed style 为 `display: grid`，三列 `191.5px 191.5px 383px`，文本域高度 `300px`。

## Findings

### Fixed During Review

1. 第三方系统列表长文本仍可能被旧表格全局规则拆行。
   - Impact: 长 Client ID、URL、说明会把列表行撑高，降低后台扫描效率。
   - Fix: 在 `platform/src/main/webapp/css/bpmt-modern.css` 的 `.bpmt-thirdpart-table .bpmt-text-clip` 上补充 `word-break: normal !important` 和 `overflow-wrap: normal !important`。
   - Recheck: Playwright computed style 已确认 `nowrap/hidden/normal` 生效，临时数据行高度为 `60px`。

2. 截图证据曾受浏览器 CSS 缓存影响。
   - Impact: 旧截图无法证明最终 CSS 规则已经生效。
   - Fix: 设计 QA 中对 `bpmt-modern.css` 链接追加 cache-busting 参数后重新截图。
   - Recheck: `02-thirdpart-list.png` 和 `03-ai-prompt-dialog.png` 已覆盖为最新视觉证据。

### Accepted Tradeoffs

1. zTree 仍保留旧 sprite 图标和 dotted 层级线。
   - Reason: 这是 `DESIGN.md` 中明确的低成本约束；本轮只做降噪，不替换组件或 DOM。
   - Risk: 视觉不会达到全新设计系统水准，但能避免重写树组件导致交互和权限入口回归。

2. 第三方系统列表仍是高密度后台表格。
   - Reason: B2 要求字段接近现有 BPMT，不应过度简化。
   - Risk: 小屏桌面会更依赖横向空间；本切片优先保证桌面后台管理可扫描。

## Result

第 10 步设计 QA 通过。

当前没有必须阻塞第 11 步功能 QA 的视觉或交互问题。剩余问题均属于后续全站统一设计系统或更大范围组件治理，不应在本切片中展开。
