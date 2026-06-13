# Configuration Workbench Lite 工程方案评审

日期：2026-06-13

关联设计产物：

- `DESIGN.md`
- `design/configuration-workbench-lite-v2.pen`
- `design/configuration-workbench-lite-v2.png`
- `design/prototype-review.md`

## 结论

第 5 步 `gstack /plan-eng-review` 通过，可以进入第 6 步 Superpowers `writing-plans`。

推荐的实现策略是“小切片、保守增强”：

1. 不重写前端架构。
2. 不替换 zTree、jQuery UI、旧 table/form、chosen、UEditor、ECharts。
3. 优先改 `bpmt-modern.css`。
4. 只在第三方系统 JSP 中补非业务展示结构和可访问性属性。
5. 用浏览器截图、console、键盘 focus 和主路径操作证明效果。

## Step 0：Scope Challenge

### 现有代码已解决什么

- 第三方系统页面已经有页面标题、说明、查询区、主操作和重置查询，见 `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/main.jsp:82` 到 `main.jsp:117`。
- 第三方系统列表已经覆盖 v2 原型要求的主要字段，见 `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp:149` 到 `list.jsp:161`。
- 行操作已有 `title` 和 `aria-label`，见 `list.jsp:167` 到 `list.jsp:177`。
- AI 提示词弹窗已经有数据收集、生成、复制成功/失败、手动复制 fallback，见 `list.jsp:23` 到 `list.jsp:90`。
- 空状态已经区分“无数据”和“无匹配”，见 `list.jsp:195` 到 `list.jsp:215`。
- `bpmt-modern.css` 已经覆盖按钮、dialog、表格、分页、登录页、zTree 和第三方系统样式，见 `platform/src/main/webapp/css/bpmt-modern.css:1` 到 `bpmt-modern.css:18`、`bpmt-modern.css:687` 到 `bpmt-modern.css:713`。

### 最小必要改动

本轮实现不应超过以下边界：

- CSS：细化 zTree hover/current/focus、列表长文本截断、AI 弹窗 metadata 行、窄桌面稳定性。
- JSP：在 AI 提示词弹窗中增加只读 metadata 展示区，复用已有 row hidden data，不改变 `buildAiPrompt()` 参数语义。
- JS：如有必要，只增加 `renderAiPromptMeta($dialog)` 这类展示函数；不改变 OAuth、copy、toggleActive、create/edit 提交流程。

复杂度判断：预计触碰 2 到 4 个文件，不新增类、不新增服务、不新增接口。复杂度未触发大范围重构警戒。

## Architecture Review

### 数据流

```text
第三方系统列表行
  ├─ hidden textarea 保存 thirdpartKey / thirdpartName / clientId / redirectUris / homeUrl
  ├─ 点击 AI 接入提示词按钮
  │    ├─ list.jsp 收集当前行数据
  │    ├─ 写入 dialog.data('bpmtAiPromptData')
  │    ├─ renderAiPromptMeta($dialog) 只更新展示区
  │    ├─ renderAiPrompt($dialog) 继续生成提示词正文
  │    └─ jQuery UI dialog 打开
  └─ 复制按钮
       ├─ navigator.clipboard 成功：状态显示已复制
       ├─ navigator.clipboard 失败：选中文本并提示手动复制
       └─ execCommand fallback：兼容旧浏览器
```

### 工程判断

- `[P1] (confidence: 9/10) list.jsp:219` — AI 弹窗目前只有可编辑密钥输入和提示词正文，缺少 v2 要求的只读 metadata 展示行。数据已在 `list.jsp:25` 到 `list.jsp:30` 收集，不需要新后端。
- `[P2] (confidence: 8/10) bpmt-modern.css:687` — zTree 已有降噪样式，但当前只覆盖链接状态。实现 v2 时应避免改 sprite，重点补 switch/icon 间距、当前态可见性和 focus。
- `[P2] (confidence: 8/10) list.jsp:184` 到 `list.jsp:192` — 第三方系统表格字段齐全，但 URL、Client ID、说明可能溢出。建议只加 CSS 截断和 `title`，不改字段顺序。

## Code Quality Review

### 推荐边界

- 不新增通用组件。
- 不把 dialog 改成独立模板系统。
- 不把查询区旧 table 改成 grid/flex 新结构。
- 不把 zTree 变成自定义菜单。

### 需要避免的实现风险

- 不要在 `buildAiPrompt()` 里拼接 DOM。它只负责文本生成。
- 不要把真实 secret 写入 DOM 默认值、日志或文档。
- 不要依赖现代浏览器 API 作为唯一复制路径，当前 fallback 必须保留。
- 不要把隐藏 textarea 改成 `input type=hidden` 后丢失多行 URL 白名单兼容性，除非验证 `redirectUris` 不含换行。

## Test Review

### 覆盖图

```text
CODE PATHS                                             USER FLOWS
[+] thirdpart list page                                [+] Third-party system list
  ├── [★★ TESTED BY PLAN] query form submit              ├── [→E2E] 查询 / 重置查询
  ├── [★★ TESTED BY PLAN] create/edit tab open           ├── [→E2E] 新增 / 编辑入口仍可打开
  ├── [★★ TESTED BY PLAN] toggle active confirm          ├── [→E2E] 启用 / 停用确认后刷新列表
  ├── [GAP] metadata row render                          ├── [→E2E] 打开 AI 提示词弹窗看到系统标识、Client ID、OAuth 回调地址
  ├── [★★ TESTED BY PLAN] copy success path              ├── [→E2E] 点击复制提示词显示成功状态
  └── [★★ TESTED BY PLAN] copy fallback path             └── [→E2E] 剪贴板失败时文本被选中并提示手动复制

[+] CSS visual layer                                    [+] Desktop visual QA
  ├── [GAP] zTree current/hover/focus                     ├── [→E2E] 首页和第三方系统页左树当前态可见
  ├── [GAP] long URL / Client ID / description            ├── [→E2E] 表格长文本不撑破布局
  └── [GAP] dialog viewport fit                           └── [→E2E] 1440x900 和较窄桌面下弹窗不遮挡底部按钮

COVERAGE: 当前计划需覆盖 10 条路径，其中 5 条是新验证缺口。
QUALITY: 以浏览器 E2E/手动验证为主，Java 单测只在修改后端行为时需要。本轮不应修改后端行为。
```

### 必跑验证

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn -s settings.local.xml -DskipTests compile
```

浏览器验证：

- `http://127.0.0.1:18080/` 登录页截图。
- 登录后首页截图，确认 zTree current/hover/focus。
- 第三方系统列表截图，确认字段、查询区、空状态、长文本。
- AI 接入提示词弹窗截图，确认 metadata 行、复制成功、复制失败 fallback。
- console error 检查。
- 键盘 Tab focus 检查。

## Performance Review

无新增后端查询、无新增 API、无新增大资源。性能风险主要来自 CSS 和 DOM 展示：

- metadata 行只读取当前行已有隐藏字段，性能影响可以忽略。
- 表格截断必须通过 CSS 完成，不应在 JSP 中增加复杂字符串处理。
- zTree 样式不要使用昂贵的全局后代选择器；选择器应限制在 `.ztree` 或 `.frame.menu` 范围内。

## Failure Modes

| 失败模式 | 当前计划是否处理 | 用户表现 | 要求 |
| --- | --- | --- | --- |
| 当前行缺少 `redirectUris` | 部分处理 | metadata 行显示空或占位 | 展示区使用占位文案，不抛 JS 错误 |
| `navigator.clipboard` 被浏览器阻止 | 已处理 | 文本被选中并提示手动复制 | 保留现有 fallback |
| URL 或说明过长 | 待补 | 表格横向撑破 | CSS 截断 + `title` |
| zTree focus 不明显 | 待补 | 键盘用户不知道焦点位置 | CSS focus ring |
| 弹窗内容过高 | 待补 | 底部按钮不可见 | 限制 textarea/dialog 高度 |

没有发现必须阻塞实现的后端架构问题。

## Worktree Parallelization

Sequential implementation, no parallelization opportunity.

原因：本轮第一个 vertical slice 主要集中在 `platform/src/main/webapp/css/bpmt-modern.css` 和 `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp`，并且需要同一浏览器路径联调。并行 worktree 会增加合并成本，不会明显缩短交付。

## NOT in scope

- 不修改 Java Action、数据库、OAuth token/code 逻辑。
- 不改 API OpenAPI 文档。
- 不改 Docker、安装脚本、镜像 tag。
- 不改动态表、报表、流程页面。
- 不替换 zTree、jQuery UI、chosen、UEditor、ECharts。
- 不为移动端 H5 做新适配。

## What already exists

- `bpmt-modern.css` 是既有低成本覆盖层，应继续复用。
- 第三方系统 list/main JSP 已经有完整列表、搜索、空状态、AI prompt dialog 和复制 fallback。
- 现有 jQuery UI dialog 可直接承载 v2 弹窗。
- 现有隐藏 textarea 已经提供 metadata 所需字段。

## Implementation Tasks

Synthesized from this review's findings. Each task derives from a specific finding above.

- [ ] **T1 (P1, human: ~1.5h / CC: ~20min)** — AI prompt dialog — Add readonly metadata display row.
  - Surfaced by: Architecture Review — metadata data exists but is not visibly shown above prompt text.
  - Files: `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp`, `platform/src/main/webapp/css/bpmt-modern.css`.
  - Verify: AI prompt dialog shows 系统标识、Client ID、OAuth 回调地址 for a selected row.

- [ ] **T2 (P1, human: ~1h / CC: ~15min)** — zTree style — Tighten zTree current/hover/focus without replacing sprite.
  - Surfaced by: Architecture Review — current CSS covers basic link states but v2 requires stronger zTree visual signal.
  - Files: `platform/src/main/webapp/css/bpmt-modern.css`.
  - Verify: home and third-party system screenshots show preserved zTree indentation, switch/icon signal, current state and keyboard focus.

- [ ] **T3 (P2, human: ~1h / CC: ~15min)** — Third-party table — Add long-text safety for Client ID, URLs and descriptions.
  - Surfaced by: Architecture Review — fields are complete, but long values can break layout.
  - Files: `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp`, `platform/src/main/webapp/css/bpmt-modern.css`.
  - Verify: table remains stable with long `clientId`, `homeUrl`, `redirectUris` and `description`.

- [ ] **T4 (P1, human: ~1h / CC: ~15min)** — Verification — Capture desktop visual and interaction evidence.
  - Surfaced by: Test Review — this is a UI slice and needs browser evidence.
  - Files: `docs/v2.0.0/design-audit-2026-06-13/` or a new dated verification directory.
  - Verify: screenshots, console check, focus check, copy success/fallback evidence.

## Completion Summary

- Step 0: Scope Challenge — scope accepted as a conservative UI slice.
- Architecture Review: 3 issues found, 0 blockers.
- Code Quality Review: 0 blockers, 4 implementation guardrails.
- Test Review: diagram produced, 5 verification gaps identified.
- Performance Review: 0 backend concerns, CSS selector caution noted.
- NOT in scope: written.
- What already exists: written.
- TODOS.md updates: 0 items proposed; all relevant work belongs in this slice.
- Failure modes: 0 critical silent gaps if the implementation tasks are followed.
- Outside voice: skipped; design review already produced approved v2 visual artifact.
- Parallelization: sequential implementation.
- Lake Score: 4/4 recommendations chose complete low-cost option rather than shortcut.
