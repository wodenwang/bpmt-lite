# Configuration Workbench Lite QA - 2026-06-13

## Scope

my-harness 第 11 步：对本次低成本配置工作台 UI 切片做系统化功能 QA。

范围：

- 登录后访问第三方系统管理页
- 第三方系统列表空状态、无结果状态、重置查询
- 临时第三方系统数据行
- AI 接入提示词弹窗元信息、提示词生成、复制成功和复制 fallback
- 新增/编辑入口打开
- 首页 zTree 结构回归

不覆盖：

- H5 页面
- 真实 OAuth 授权码登录闭环
- 真实微信 OAuth 登录
- 新增/编辑表单保存提交
- 启停按钮真实提交，避免污染临时 QA 数据

## Commands

- `mvn -s settings.local.xml -DskipTests compile`: `BUILD SUCCESS`
- `docker cp platform/src/main/webapp/css/bpmt-modern.css bpmt-web:/usr/local/tomcat/webapps/ROOT/css/bpmt-modern.css`
- `docker cp platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp bpmt-web:/usr/local/tomcat/webapps/ROOT/xhtml/thirdpart/ThirdpartAction/list.jsp`
- `docker compose restart bpmt-web`: 用于清理 BPMT Web 应用缓存，确保数据库临时数据增删能反映到页面。

## Browser QA Results

| Check | Result | Evidence |
|---|---|---|
| 登录后第三方系统空状态 | Pass | `还没有外部系统` 出现，临时行不可见，字段表头完整 |
| 查询无结果状态 | Pass | 输入 `definitely-no-match-qa` 后出现 `没有匹配的外部系统` |
| 重置查询 | Pass | 查询值清空，页面回到空状态 |
| 临时数据行渲染 | Pass | `codex_ui_verify` 可见，编辑/启停/AI 按钮各 1 个 |
| 长文本安全 | Pass | `.bpmt-text-clip` 数量 5，computed style 为 `nowrap/hidden/normal` |
| AI 弹窗元信息 | Pass | 系统标识、Client ID、OAuth 回调地址均来自当前行 |
| 提示词生成 | Pass | 提示词包含当前 Client ID、redirect URI 和 `CLIENT_SECRET_PLACEHOLDER` |
| 复制成功路径 | Pass | 点击复制后显示 `已复制提示词。` |
| 复制 fallback 路径 | Pass | 模拟 `navigator.clipboard.writeText` reject 和同步 throw 后均显示手动复制提示，`promptText` 获得焦点并选中 1417 字符 |
| 编辑入口 | Pass | 点击编辑后打开 `编辑外部系统` 页签，包含当前临时系统上下文，无错误文本 |
| 新增入口 | Pass | 点击新增后打开 `新增外部系统` 页签，包含系统标识、Client ID、保存等表单上下文 |
| Console | Pass with note | Playwright console 只有登录表单 autocomplete verbose 提示；未发现本切片 JS error |

## Temporary Data

验证期间临时插入：

- `CM_THIRDPART.THIRDPART_KEY = codex_ui_verify`
- `CM_PRI.PRI_KEY = codex_ui_verify`

QA 完成后已删除并重启 Web 容器清理应用缓存。复核：

- `remaining_thirdpart = 0`
- `remaining_pri = 0`

## Findings

### Fixed / Handled

1. 删除临时记录后页面仍显示旧行。
   - Root cause: 当前 BPMT Web 容器存在应用层缓存，DB 删除不会马上反映到列表。
   - Action: QA 中每次临时数据增删后重启 `bpmt-web`，避免污染验证结果。
   - Product impact: 这是 QA 环境处理方式，不属于本次 UI 切片代码缺陷。

### Remaining Notes

1. 登录页 autocomplete verbose 提示仍存在。
   - Severity: Low。
   - Reason: 这是既有登录页浏览器提示，不是本次第三方系统列表或 AI 弹窗变更引入。

## Result

第 11 步 QA 通过，可以进入第 12 步代码审查。
