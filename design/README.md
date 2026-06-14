# Configuration Workbench Lite 原型说明

## 产物

本目录保存 `bpmt-lite` 桌面 UI 低成本配置工作台方案的 Pencil 原型和导出图。

| 文件 | 用途 |
| --- | --- |
| `configuration-workbench-lite.pen` | Pencil 源文件，包含 4 个 1440x1024 样板页 |
| `configuration-workbench-lite.png` | Pencil 初次导出图，主要展示 AI 接入提示词弹窗页 |
| `configuration-workbench-lite-overview.pen` | 2x2 总览派生源文件 |
| `configuration-workbench-lite-overview.png` | Pencil 原始总览导出，包含画布远端内容，不作为主评审图 |
| `configuration-workbench-lite-review.pen` | 最终评审版 Pencil 源文件 |
| `configuration-workbench-lite-review.png` | 最终评审版原始导出图，仍包含画布远端引用 |
| `configuration-workbench-lite-review-cropped.png` | 推荐评审截图，只保留左侧 2x2 总览区域 |
| `configuration-workbench-lite-v2.pen` | 第 4 步复审通过的 Pencil 源文件，手工结构化修正 B1/B2/B3 |
| `configuration-workbench-lite-v2.png` | 当前推荐评审截图，展示 zTree、第三方系统完整字段和 AI 弹窗 metadata 修正 |
| `qHTYM.png` | Pencil `export_nodes` 原始导出文件，与 `configuration-workbench-lite-v2.png` 内容相同 |

推荐优先查看：

```text
design/configuration-workbench-lite-v2.png
```

## 样板页范围

本轮原型只覆盖第一阶段低成本样板：

1. 登录页。
2. 登录后首页。
3. 第三方系统列表。
4. AI 接入提示词弹窗。

原型目标不是重做前端架构，而是证明 `DESIGN.md` 中的 `Configuration Workbench Lite` 可以形成一致视觉方向，并可通过 CSS 覆盖层和少量非业务 JSP/JS 语义补丁落地。

## 实现映射

### 登录页

- 目标：减少旧表格表单感，提升账号密码登录的可信度和聚焦度。
- 低成本实现方式：
  - 优先调整 `frame_new/login.jsp` / `frame/login.jsp` 的 label、autocomplete、按钮语义和错误提示。
  - 主要视觉通过 `bpmt-modern.css` 覆盖输入框、按钮、登录面板、focus 状态完成。
  - 不新增营销文案、背景插画或复杂认证方式。

### 登录后首页

- 目标：降低顶部账号区、模块按钮、左侧树和主内容同时抢注意力的问题。
- 低成本实现方式：
  - 顶部账号区只调字号、颜色、间距和权重，不改信息来源。
  - 模块导航只强化当前态，不改变菜单/权限逻辑。
  - 左侧导航继续使用 zTree，不替换成新侧边栏组件。
  - 待办、公告、工作量优先补空状态和弱化无数据图表，不新增后端统计。

### 第三方系统列表

- 目标：把第三方系统页变成清楚的外部接入配置入口。
- 低成本实现方式：
  - 查询区仍基于旧 table/form 输出，通过 CSS 做背景、label 对齐、输入框高度和按钮层级。
  - 表格继续使用现有列表结构，统一表头、行高、hover、状态文本或轻量 badge。
  - 行操作保留 jQuery UI button/icon 机制，补 `title` / `aria-label`，不换图标库。
  - 分页优先通过 CSS flex 覆盖旧布局；必要时小改 `xhtml/common/page_bar.jsp`。

### AI 接入提示词弹窗

- 目标：让“复制提示词给第三方 AI agent 或集成开发者”成为清楚短任务。
- 低成本实现方式：
  - 继续使用 jQuery UI dialog 气质：标题栏、内容区、底部按钮区。
  - 提示词内容使用 readonly textarea 或现有只读文本区域。
  - 复制成功/失败使用现有 `Ui.msg` 或局部状态提示。
  - 失败时选中文本并提示手动复制，不引入复杂 clipboard 组件。

## 原型到实现的妥协

本原型是低成本方向稿，不是像素级实现稿。进入工程实现前必须保留以下约束：

- zTree 不能被替换为现代菜单组件。实现时必须保留现有 zTree DOM、缩进、展开逻辑和 sprite 图标体系，只允许降噪、强化当前态、hover、focus 和点击区域。
- 原型中的侧边栏视觉比真实 zTree 更简化。实现时不要删除现有 folder/file/switch 图标，也不要重写懒加载或点击行为。
- 搜索区看起来更整齐，但实现仍应以旧 table 查询区为基础，不拆成全新 grid/filter 组件。
- 状态 badge 只能是轻量文本样式，不引入新组件库。
- 弹窗只做任务表达和复制反馈，不改变 OAuth 或 AI 接入业务逻辑。

## 第 4 步复审结果

`configuration-workbench-lite-review-cropped.png` 曾作为第 3 步初稿评审截图。复审发现三个阻塞点：

- B1：zTree 被弱化得过头。
- B2：第三方系统表格列与现有页面不够一致。
- B3：AI 接入提示词弹窗缺少 metadata 行。

当前 `configuration-workbench-lite-v2.png` 已针对上述问题完成低成本修正，并在 `prototype-review.md` 中标记第 4 步通过。下一步应进入 `gstack /plan-eng-review`，不要重新从设计发散开始。

## 生成记录

Pencil CLI 版本：

```text
pencil 0.2.6
```

生成命令使用了以下参考输入：

- `DESIGN.md`
- `docs/v2.0.0/design-audit-2026-06-13/01-login-current.png`
- `docs/v2.0.0/design-audit-2026-06-13/02-home-current.png`
- `docs/v2.0.0/design-audit-2026-06-13/03-thirdpart-index-current.png`
- Product Design 生成的低成本配置工作台视觉图

v2 修正说明：

- Pencil CLI agent 路径因 `API Error: 402 Insufficient Balance` 不可用。
- 最终通过 `pencil interactive --in design/configuration-workbench-lite-review.pen --out design/configuration-workbench-lite-v2.pen` 使用结构化 MCP 操作生成。
- 通过 `export_nodes` 导出 `design/qHTYM.png`，并复制为稳定文件名 `design/configuration-workbench-lite-v2.png`。

## v1.9.0 桌面壳层与全局缺陷原型

`v1.9.0` 原型聚焦桌面后台壳层、全局弹框层级、空状态生命周期和非菜单 zTree hover 稳定性，不继续扩展 `v1.8.x` 的登录页、第三方系统表格细节或 AI 提示词内容 polish。

| 文件 | 用途 |
| --- | --- |
| `bpmt-v1.9.0-shell-navigation.pen` | Pencil 源文件，包含 4 个 1440x900 目标态画面 |
| `v1.9.0-shell-navigation-source-export/By36y.png` | `01 Home shell`，展示统一导航侧栏、顶部功能按钮、主内容区和空状态生命周期约束 |
| `v1.9.0-shell-navigation-source-export/lGDEH.png` | `02 Settings shell`，展示系统开发配置页的侧栏、顶部栏、内容列表和非菜单 zTree 对齐 |
| `v1.9.0-shell-navigation-source-export/N4pjvS.png` | `03 Modal layering`，展示遮罩覆盖高层级按钮、窗体位于遮罩之上、关闭按钮固定 32 x 32 |
| `v1.9.0-shell-navigation-source-export/Bcx3h.png` | `04 Non-menu zTree`，展示全站 zTree hover 前后尺寸稳定的量化验收 |

正式评审优先查看：

```text
design/bpmt-v1.9.0-shell-navigation.pen
design/v1.9.0-shell-navigation-source-export/
```

Pencil 生成与验证记录：

- `pencil 0.2.7`
- `pencil interactive --out design/bpmt-v1.9.0-shell-navigation.pen`
- `pencil interactive --in design/bpmt-v1.9.0-shell-navigation.pen --out /tmp/bpmt-v1.9.0-shell-navigation-export-read.pen`
- `snapshot_layout({ problemsOnly: true })` 返回 `No layout problems.`
- `export_nodes({ nodeIds: ["By36y","lGDEH","N4pjvS","Bcx3h"], outputDir: ".../design/v1.9.0-shell-navigation-source-export", format: "png", scale: 2 })`

注意：`pencil --out ... --prompt ... --export ...` 的 agent 路径在当前环境仍受 `API Error: 402 Insufficient Balance` 限制；本原型通过 Pencil interactive 的结构化操作落盘。不要用早期内存导出的临时 PNG 目录作为评审依据。
