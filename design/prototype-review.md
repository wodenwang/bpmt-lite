# Configuration Workbench Lite 原型复审

## 结论

第 3 步 Pencil 原型已产出，第 4 步 `gstack /plan-design-review on prototype` 已完成。

当前通过稿为：

```text
design/configuration-workbench-lite-v2.pen
design/configuration-workbench-lite-v2.png
```

结论是：可以进入第 5 步 `gstack /plan-eng-review`，但工程实现必须继续遵守 `DESIGN.md` 的低成本边界。本次原型只确认视觉和信息结构方向，不授权替换 zTree、jQuery UI、旧 table/form、OAuth 或 AI 接入业务逻辑。

## 当前评审截图

推荐优先查看：

```text
design/configuration-workbench-lite-v2.png
```

v2 评审稿覆盖四个第一阶段样板页：

1. 登录页。
2. 登录后首页。
3. 第三方系统列表。
4. AI 接入提示词弹窗。

## 7 项设计复审评分

| 维度 | 评分 | 判断 |
| --- | ---: | --- |
| 信息架构 | 8/10 | 四个样板页的当前位置、主任务和主操作已经清楚；实现时还要确认旧壳层标题来源。 |
| 交互状态覆盖 | 8/10 | 已体现查询、空图表、复制任务和分页；错误、无权限、复制失败在工程计划中继续补齐。 |
| 用户路径 | 8/10 | 登录、首页定位、第三方系统配置、复制提示词路径连贯。 |
| AI 生成感风险 | 8/10 | 已从理想化现代后台收敛为旧系统可落地的配置工作台；避免了营销化、卡片化和大面积装饰。 |
| 设计系统一致性 | 9/10 | 明确保留 zTree、jQuery UI、旧表格表单和轻量状态标签。 |
| 响应与可访问性 | 7/10 | 桌面后台方向可用；键盘 focus、title/aria、窄桌面仍需在工程计划里验证。 |
| 未决设计决策 | 8/10 | 没有阻塞工程评审的问题；仍有实现阶段取舍项。 |

整体评分：`6/10 -> 8/10`。第 4 步通过。

## 已解决的原阻塞项

### B1. zTree 被弱化得过头

状态：已解决到可进入工程评审。

v2 在首页和第三方系统页左侧导航中恢复了更接近真实 zTree 的视觉信号：

- 展开/折叠符号。
- folder/file sprite-like 占位。
- 层级缩进。
- 当前节点高亮。

工程落地要求：

- 不替换 zTree DOM。
- 不重绘整套 sprite。
- 只做字体、颜色、hover、focus、当前态、点击区域和降噪。

### B2. 第三方系统表格列与现有页面不够一致

状态：已解决到可进入工程评审。

v2 第三方系统列表已经覆盖接近现有 BPMT 的字段：

```text
操作、系统标识、系统名称、Client ID、状态、首页地址、微信登录、创建时间、更新时间、说明
```

工程落地要求：

- 保留旧 table/list 输出结构。
- 行操作继续用现有按钮或 jQuery UI 机制。
- URL、ID、说明等长文本使用截断和 `title`，不改变字段含义和提交参数。

### B3. AI 接入提示词弹窗缺少 metadata 行

状态：已解决到可进入工程评审。

v2 弹窗在提示词正文上方补充了元信息行：

```text
系统标识、Client ID、OAuth 回调地址
```

工程落地要求：

- 继续使用 jQuery UI dialog 气质。
- 提示词区域使用 readonly 文本区域或现有只读容器。
- 复制成功/失败使用现有 `Ui.msg` 或局部状态提示。
- 不改变 OAuth 或第三方系统业务逻辑。

### B4. Pencil 修正版生成受外部余额阻塞

状态：已绕过。

历史失败仍保留为工具记录：

- 默认 `claude-opus-4-6` 返回 `API Error: 402 Insufficient Balance`。
- 低成本 `claude-haiku-4-5 --effort low` 也返回同样错误。
- `design/configuration-workbench-lite-v2-usage.json` 记录了 agent 路径失败。

本次最终采用 Pencil interactive headless 模式，通过结构化 MCP 操作手工创建 v2 评审画板，不依赖 Claude agent 余额：

```text
pencil interactive --in design/configuration-workbench-lite-review.pen --out design/configuration-workbench-lite-v2.pen
```

并通过 `export_nodes` 导出：

```text
design/configuration-workbench-lite-v2.png
```

## 非范围

- 不升级前端技术栈。
- 不引入 React、Vue、Ant Design 运行时或新 SPA。
- 不替换 zTree、jQuery UI、chosen、UEditor、ECharts 2.x。
- 不重写动态表、报表、流程、权限、表单提交或 Ajax 生命周期。
- 不把 v2 原型当作像素级还原稿。

## 复用现有模式

- `bpmt-modern.css` 继续作为低风险覆盖层。
- zTree 继续作为左侧导航来源。
- 旧 `table/form` 继续承载搜索区、列表区和表单区。
- jQuery UI dialog 继续承载 AI 提示词弹窗。
- 旧按钮、分页和行操作机制继续保留，只补视觉层级、`title`、`aria-label` 和状态反馈。

## Implementation Tasks

Synthesized from this review's findings. Each task derives from the prototype review and should feed the next `gstack /plan-eng-review`.

- [ ] **T1 (P1, human: ~2h / CC: ~20min)** — Shell/zTree — Define low-cost zTree styling boundary.
  - Surfaced by: B1 — v2 restores zTree visual signals, but implementation must not replace zTree DOM or sprite behavior.
  - Files: `platform/src/main/webapp/css/bpmt-modern.css`, possibly `platform/src/main/webapp/js/ws-ui.js`.
  - Verify: desktop screenshot of logged-in home and third-party system page showing zTree hover/current/focus states.

- [ ] **T2 (P1, human: ~2h / CC: ~25min)** — Third-party list — Map v2 table fields to the current JSP/list output without changing business parameters.
  - Surfaced by: B2 — table must include operation, identifiers, Client ID, status, URL, WeChat, timestamps and notes.
  - Files: `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/*.jsp`, `platform/src/main/webapp/css/bpmt-modern.css`.
  - Verify: third-party system list screenshot and manual check that edit/delete/new/open operations still work.

- [ ] **T3 (P1, human: ~1.5h / CC: ~20min)** — AI prompt dialog — Add metadata row and copy feedback without changing OAuth logic.
  - Surfaced by: B3 — dialog needs system key, Client ID and OAuth callback URL above the prompt body.
  - Files: `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/*.jsp`, relevant existing JS for prompt copying.
  - Verify: open AI prompt dialog, copy success, copy failure/manual fallback, and close behavior.

- [ ] **T4 (P2, human: ~1h / CC: ~15min)** — Accessibility states — Add `title`/`aria-label` and visible focus for icon-only and dialog controls.
  - Surfaced by: responsive/accessibility score 7/10.
  - Files: CSS overlay plus small non-business JSP/JS attributes where required.
  - Verify: keyboard Tab path through list buttons, tree nodes, dialog close and copy button.

## 下一步

进入第 5 步：

```text
gstack /plan-eng-review
```

工程评审需要重点验证：

- 哪些修改可以只通过 CSS 覆盖完成。
- 哪些 JSP 属性或状态容器属于非业务增强。
- 第三方系统列表字段来自现有页面还是需要仅在视觉上折中。
- AI 提示词 metadata 的数据来源是否已在当前页面上下文中存在。
- 验证命令、浏览器截图和回滚方式。
