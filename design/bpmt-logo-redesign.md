# BPMT Logo Redesign

## 设计方向

- 关键词：后台工作台、流程、配置、稳定、清晰。
- 图形：三段水平流程线，保留 BPMT 现有“菜单/流程”记忆点，但去掉旧 logo 的粗糙描边和压缩感。
- 字标：保留准确 `BPMT`，使用粗体无衬线字标，适合后台顶部、登录页、小尺寸截图和 favicon 级别识别。
- 色彩：主色使用蓝绿组合，和当前后台的蓝色主按钮、浅色面板兼容；文字使用深蓝黑，避免纯黑过重。

## 落地约束

- 不改业务逻辑。
- 不改 `logoUrl` 生成逻辑。
- 只替换现有静态 PNG 资产，并保留原始尺寸：
  - `platform/src/main/webapp/css/images/logo.png`: `206x50`
  - `platform/src/main/webapp/xhtml/frame_new/images/logo.png`: `150x87`
- SVG 源文件放在 `design/`，后续可继续微调并重新导出 PNG。
