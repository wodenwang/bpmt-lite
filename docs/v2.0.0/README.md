# bpmt-lite v2.0.0 桌面端 UI 规划文档索引

本目录记录 `bpmt-lite` 未来桌面端 UI 改造的分阶段设计文档。该大版本不升级前端技术栈，不重写低代码控件，优先通过代表路径、状态语义、导航层级和小范围样式/JSP 属性补强提升桌面后台体验。

## 当前 Product Design 结论

`v1.8.0 modern-theme foundation` 已被判定为方向失败。它证明了 CSS-only 现代主题可以让页面变浅、变白、变圆角，但没有解决后台用户更关键的问题：当前位置、页面任务、空/加载/错误状态、主操作和导航层级。

后续默认路线改为：

```text
v1.8.0 desktop admin clarity baseline
```

旧 `modern-theme foundation` 文档保留为失败方向证据，不再作为实现依据。

## 文档清单

- `desktop-admin-clarity-replan.md`：Product Design 重新规划，解释旧方向失败原因和新方向。
- `ui-modernization-roadmap.md`：大版本路线、版本拆分、Product Design 工作流接入方式。
- `v1.8.0-modern-theme-foundation-design.md`：CSS-only 现代化主题基础，已停止继续推进，仅作为历史记录。
- `v1.8.1-visual-qa-a11y-hardening-design.md`：视觉 QA 与可访问性加固。
- `v1.9.0-desktop-shell-navigation-design.md`：桌面壳层与导航刷新。
- `v1.9.1-list-form-dialog-polish-design.md`：列表、表单、弹窗体验 polish。
- `v2.0.0-modern-admin-experience-design.md`：现代后台体验基线收敛。

## 推荐执行顺序

1. 以 `desktop-admin-clarity-replan.md` 作为新的设计 brief。
2. 用 Product Design `ideate` 只针对三条代表路径生成 3 个目标态方向。
3. 用户确认方向后，从 `v1.8.0 desktop admin clarity baseline` 开始做 vertical slice。
4. 每个小版本完成后保留当前态、目标态、实现态截图和 QA 记录。
5. 到 `v2.0.0` 再固化 README、AGENTS 和维护规范。

## 固定约束

- 不考虑移动端和小屏幕。
- 不重构前端技术栈。
- 不替换 jQuery UI、zTree、chosen。
- 不改动态表、报表、流程、OAuth、API 的业务逻辑。
- UI 改造必须有当前态、目标态、实现态证据。

## 当前不推荐

- 不再以全局 CSS-only 主题作为第一阶段发布目标。
- 不再用“视觉更现代”作为完成标准。
- 不再只凭页面能打开、console 无错误、截图无重叠判断方向成功。
