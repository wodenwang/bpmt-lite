# Configuration Workbench Lite Code Review - 2026-06-13

## Scope

my-harness 第 12 步：对本次低成本配置工作台 UI 切片做落地前代码审查。

审查边界限定为本轮目标文件和相关证据：

- `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp`
- `platform/src/main/webapp/css/bpmt-modern.css`
- `IMPLEMENTATION_PLAN.md`
- `docs/v2.0.0/configuration-workbench-lite-*`

当前工作树中存在大量前置未提交改动，不属于本轮审查结论覆盖范围。

## Findings

### Fixed During Review

1. `navigator.clipboard.writeText(prompt)` 同步抛错时不会进入 fallback。
   - File: `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp`
   - Risk: 少数浏览器或权限状态下复制按钮可能静默失败，用户看不到手动复制提示。
   - Fix: 给 `navigator.clipboard.writeText(prompt)` 增加 `try/catch`，同步异常和 rejected Promise 都进入同一 fallback。
   - Recheck: Playwright 注入 `writeText: () => { throw new Error(...) }` 后，状态显示 `浏览器阻止自动复制，已选中文本，请手动复制。`，`promptText` 获得焦点并选中 1417 字符。

## Review Checks

| Check | Result | Notes |
|---|---|---|
| 后端逻辑边界 | Pass | 未修改 Java Action、OAuth token/userinfo、数据库表结构或权限校验逻辑 |
| 第三方系统字段完整性 | Pass | 列表保留操作、系统标识、系统名称、Client ID、状态、首页地址、微信登录、创建时间、更新时间、说明 |
| XSS/HTML 注入边界 | Pass | 行数据正文使用 `<c:out>`；弹窗元信息用 jQuery `.text()`；title 使用 `fn:escapeXml` |
| Secret 暴露 | Pass | 弹窗默认只展示 `CLIENT_SECRET_PLACEHOLDER`，不读取或展示真实 `clientSecret` |
| 复制反馈 | Pass | 支持成功、Promise reject、同步 throw、`execCommand` fallback |
| zTree 约束 | Pass | 只调整 CSS，保留 zTree DOM、sprite、折叠开关和层级结构 |
| CSS 风险 | Pass with note | 使用 `#frame-menu-panel` 提高 specificity 来覆盖后加载的 `zTreeStyle.css`，未改变加载顺序 |
| 验证数据清理 | Pass | `CM_THIRDPART/CM_PRI` 临时记录清理后计数为 0，并重启 Web 清理缓存 |

## Verification

- `mvn -s settings.local.xml -DskipTests compile`: `BUILD SUCCESS`
- Playwright:
  - 第三方系统空状态、无结果状态、重置查询通过
  - 临时数据行、AI 弹窗、复制成功和 fallback 通过
  - 新增/编辑入口打开通过
  - 同步 throw copy fallback 通过

## Residual Risks

1. 当前 `18080` 运行态不是源码热加载。
   - Mitigation: 验证期间使用 `docker cp` 同步 JSP/CSS 到 `bpmt-web`，并在需要时重启 Web 清理应用缓存。

2. 本轮未测试真实启停提交和新增/编辑保存。
   - Reason: 本切片只改视觉和弹窗生成逻辑；为避免污染本地最小库，QA 只验证入口打开，不提交表单。

## Result

第 12 步代码审查通过。没有剩余必须修复的 P0/P1/P2 问题。
