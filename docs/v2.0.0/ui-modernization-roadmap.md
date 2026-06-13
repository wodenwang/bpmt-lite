# v2.0.0 桌面端 UI 现代化大版本路线

> 状态：本路线中的 `v1.8.0 CSS-only 主题基础` 已被 Product Design 判定为方向失败。后续执行以 `desktop-admin-clarity-replan.md` 和 `v1.8.0 desktop admin clarity baseline` 为准。本文保留为旧路线记录。

## 大版本名称

`v2.0.0 Desktop UI Modernization`

中文名称：桌面端现代后台体验基线。

## 大版本目标

在保持现有 Java 8、JSP、jQuery UI、zTree、chosen 和低代码控件体系稳定的前提下，完成桌面端 UI 现代化：

- 视觉不再像默认 jQuery UI 旧皮肤。
- 后台工作台信息层级更清楚。
- 列表、表单、弹窗、菜单、分页、提示统一。
- 高频页面可读性和操作效率提升。
- 后续新页面有明确 UI 规范可遵循。

## 版本拆分

| 阶段 | 版本 | 主题 | 产出 |
| --- | --- | --- | --- |
| 1 | `v1.8.0` | 桌面后台清晰度基线 | 登录后首页、第三方系统列表、AI 接入提示词弹窗三条代表路径 |
| 2 | `v1.8.1` | 视觉 QA 与可访问性 | focus、disabled、error、icon-only 说明、QA 清单 |
| 3 | `v1.9.0` | 壳层与导航 | topbar、模块 tab、左侧菜单、footer 体验优化 |
| 4 | `v1.9.1` | 列表/表单/弹窗 polish | 搜索区、表格、表单、弹窗、分页统一 |
| 5 | `v2.0.0` | 基线收敛 | 设计系统文档、维护规范、后续开发规则 |

## 设计工作流

每阶段推荐使用 Product Design 插件：

```text
get-context -> audit -> ideate -> prototype/Pencil -> design-qa -> implementation -> Playwright QA
```

阶段开始时先确认：

- 本阶段页面样本。
- 是否只做 CSS。
- 是否允许补 JSP 属性。
- 是否允许调整壳层结构。
- 验收截图目录。

阶段结束时必须保留：

- 当前态截图。
- 目标态设计说明或原型截图。
- 实现后截图。
- 视觉 QA 结论。
- 回滚说明。

## 关键页面样本

### 基础样本

- 登录页 `/`。
- 我的事项首页。
- 功能设置首页 `manage.xhtml`。
- 第三方系统列表。
- AI 接入提示词弹窗。
- API 文档 `/api/docs/`。

### 扩展样本

- 动态表开发列表。
- 视图开发列表。
- 流程设置列表。
- 组织架构。
- 用户权限。
- 报表视图配置。

## 风险策略

### 低风险

- 新增最后加载的 CSS 覆盖层。
- 调整颜色、字体、字号、间距、边框、圆角、hover、focus。
- 为 icon-only 按钮补 `title`、`aria-label`。

### 中风险

- 调整 topbar/header/footer 尺寸。
- 调整左侧菜单点击目标高度。
- 调整表格行高和滚动容器。
- 调整弹窗内容 padding 和底部按钮位置。

### 高风险

- 替换 jQuery UI 或 zTree。
- 改写低代码控件输出。
- 改业务 JSP DOM 层级。
- 改 Ajax 生命周期或表单提交方式。

高风险项不纳入本大版本默认路线。
