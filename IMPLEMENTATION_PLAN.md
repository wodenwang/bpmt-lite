# Configuration Workbench Lite UI Slice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** 在不改前端底层组件逻辑、不改后端业务逻辑的前提下，完成低成本配置工作台第一个可验证 UI 切片：第三方系统列表、AI 接入提示词弹窗、首页 zTree 降噪。

**Architecture:** 本切片只使用现有 JSP、jQuery UI dialog、jQuery zTree、JSTL 和全局 `bpmt-modern.css`。列表字段沿用现有 BPMT 第三方系统数据结构；弹窗只新增只读元信息展示；zTree 只调整现有 class 样式，保留 sprite、折叠开关和节点层级信号。

**Tech Stack:** Java 8、JSP/JSTL、jQuery、jQuery UI、zTree、Maven 3、Tomcat 7。

---

## Scope

本计划只执行第一个 UI vertical slice。目标是让用户在浏览器中能确认：

- 首页左侧 zTree 仍然是低成本可落地的 zTree，而不是重做成现代文本菜单。
- 第三方系统列表保留接近现有 BPMT 的完整字段：操作、系统标识、系统名称、Client ID、状态、首页地址、微信登录、创建时间、更新时间、说明。
- AI 接入提示词弹窗展示系统标识、Client ID、OAuth 回调地址三项元信息，并继续使用现有提示词生成和复制逻辑。

不在本切片范围内：

- 不新增 React/Vue/Ant Design 或新的前端组件体系。
- 不修改 Java Action、OAuth 授权、token、userinfo、权限校验、数据库表结构。
- 不替换 zTree，不重写树节点 DOM，不移除 zTree sprite 图标。
- 不调整 Docker、API、H5 或移动端业务页面。

## File Structure

- Modify: `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp`
  - 负责第三方系统列表字段呈现、AI 接入提示词弹窗 DOM、提示词弹窗只读元信息渲染。
- Modify: `platform/src/main/webapp/css/bpmt-modern.css`
  - 负责 zTree 视觉降噪、第三方系统表格长文本省略、AI 接入提示词弹窗元信息行和文本域尺寸。
- Create: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/`
  - 存放本切片浏览器验证截图和人工检查记录。

## Task 1: AI 接入提示词弹窗补充元信息行

**Files:**
- Modify: `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp`

- [x] **Step 1: 在打开弹窗前渲染只读元信息**

在 `$('button[name=aiPrompt]', $zone).click(function() { ... })` 中，保持现有 `data` 采集逻辑不变，在 `renderAiPrompt($dialog);` 前增加：

```javascript
renderAiPromptMeta($dialog);
renderAiPrompt($dialog);
```

预期结果：弹窗每次打开时，元信息行和提示词文本都使用当前行数据。

- [x] **Step 2: 新增 `renderAiPromptMeta` 函数**

在 `renderAiPrompt($dialog)` 函数前增加：

```javascript
function renderAiPromptMeta($dialog) {
	var data = $dialog.data('bpmtAiPromptData') || {};
	setPromptMetaText($('.bpmt-ai-meta-thirdpart-key', $dialog), data.thirdpartKey || '未配置');
	setPromptMetaText($('.bpmt-ai-meta-client-id', $dialog), data.clientId || '未配置');
	setPromptMetaText($('.bpmt-ai-meta-redirect-uris', $dialog), data.redirectUris || '未配置 OAuth 回调地址');
}

function setPromptMetaText($node, text) {
	$node.text(text);
	$node.attr('title', text);
}
```

预期结果：元信息值使用 `.text()` 写入，避免把数据库字段当 HTML 注入。

- [x] **Step 3: 在弹窗说明下方插入元信息 DOM**

在 `<div class="bpmt-dialog-intro">...</div>` 后、`<div class="bpmt-prompt-grid">` 前插入：

```jsp
<div class="bpmt-prompt-meta" aria-label="第三方系统接入元信息">
	<div>
		<span>系统标识</span>
		<strong class="bpmt-ai-meta-thirdpart-key">未配置</strong>
	</div>
	<div>
		<span>Client ID</span>
		<strong class="bpmt-ai-meta-client-id">未配置</strong>
	</div>
	<div>
		<span>OAuth 回调地址</span>
		<strong class="bpmt-ai-meta-redirect-uris">未配置 OAuth 回调地址</strong>
	</div>
</div>
```

预期结果：弹窗可直接看到系统标识、Client ID、OAuth 回调地址，满足第 4 步 B3。

## Task 2: 第三方系统列表长文本安全和字段完整性

**Files:**
- Modify: `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp`
- Modify: `platform/src/main/webapp/css/bpmt-modern.css`

- [x] **Step 1: 保留表头字段不删减**

确认表头仍然包含以下列，不删除、不改语义：

```jsp
<th style="width: 125px;">操作</th>
<th field="thirdpartKey">系统标识</th>
<th field="thirdpartName">系统名称</th>
<th field="clientId">Client ID</th>
<th field="activeFlag">状态</th>
<th field="homeUrl">首页地址</th>
<th field="wechatType">微信登录</th>
<th field="createTime">创建时间</th>
<th field="updateTime">更新时间</th>
<th field="description">说明</th>
```

预期结果：满足第 4 步 B2，不把列表过度简化成少数字段。

- [x] **Step 2: 为长文本单元格增加省略和 title**

将现有正文单元格替换为下面结构，保留原字段来源：

```jsp
<td class="center"><span class="bpmt-text-clip bpmt-text-key" title="${fn:escapeXml(vo.thirdpartKey)}"><c:out value="${vo.thirdpartKey}" /></span></td>
<td class="left"><span class="bpmt-text-clip bpmt-text-name" title="${fn:escapeXml(vo.thirdpartName)}"><c:out value="${vo.thirdpartName}" /></span></td>
<td class="left"><span class="bpmt-text-clip bpmt-text-client" title="${fn:escapeXml(vo.clientId)}"><c:out value="${vo.clientId}" /></span></td>
<td class="left"><span class="bpmt-text-clip bpmt-text-url" title="${fn:escapeXml(vo.homeUrl)}"><c:out value="${vo.homeUrl}" /></span></td>
<td class="left"><span class="bpmt-text-clip bpmt-text-desc" title="${fn:escapeXml(vo.description)}"><c:out value="${vo.description}" /></span></td>
```

预期结果：长 Client ID、URL、说明不会撑坏表格，鼠标悬停仍能看到完整值。

- [x] **Step 3: 添加表格长文本 CSS**

在 `bpmt-modern.css` 的第三方系统相关样式区域增加：

```css
.bpmt-thirdpart-table .bpmt-text-clip {
	display: inline-block;
	max-width: 160px;
	overflow: hidden;
	text-overflow: ellipsis;
	vertical-align: middle;
	white-space: nowrap;
}

.bpmt-thirdpart-table .bpmt-text-key {
	max-width: 120px;
}

.bpmt-thirdpart-table .bpmt-text-client {
	max-width: 150px;
}

.bpmt-thirdpart-table .bpmt-text-url {
	max-width: 210px;
}

.bpmt-thirdpart-table .bpmt-text-desc {
	max-width: 180px;
}
```

预期结果：列表仍是信息密度较高的后台表格，但不会因为长字段破版。

## Task 3: zTree 低成本降噪，保留 sprite 和层级信号

**Files:**
- Modify: `platform/src/main/webapp/css/bpmt-modern.css`

- [x] **Step 1: 修正当前选中态高度变化**

将现有 zTree hover/selected 样式中 `height: 24px;` 改为 `height: 26px;`，并给 `a` 增加 `box-sizing: border-box;`：

```css
.ztree li a {
	border-radius: 4px;
	box-sizing: border-box;
	color: #344054 !important;
	height: 26px;
	line-height: 26px;
	padding: 0 4px;
}

.ztree li a:hover,
.ztree li a.curSelectedNode {
	background: #eff6ff;
	border: 1px solid #bfdbfe;
	color: #1d4ed8 !important;
	height: 26px;
	opacity: 1;
}
```

预期结果：树节点 hover/选中不会因边框导致高度跳动。

- [x] **Step 2: 保留 zTree sprite 图标并降低噪声**

在 zTree 样式后增加：

```css
.ztree li span.button {
	opacity: 0.72;
}

.ztree li a:hover span.button,
.ztree li a.curSelectedNode span.button {
	opacity: 0.95;
}

.ztree li span.button.switch {
	margin-right: 2px;
}

.ztree li span.node_name {
	display: inline-block;
	max-width: 168px;
	overflow: hidden;
	text-overflow: ellipsis;
	vertical-align: top;
	white-space: nowrap;
}
```

预期结果：折叠开关、文件夹/叶子节点图标、层级缩进仍由原 zTree 提供，只降低视觉噪声。

## Task 4: AI 接入提示词弹窗样式收敛

**Files:**
- Modify: `platform/src/main/webapp/css/bpmt-modern.css`

- [x] **Step 1: 添加元信息行样式**

在 `.bpmt-ai-prompt-dialog` 相关样式附近增加：

```css
.bpmt-ai-prompt-modal .ui-dialog-content {
	max-height: calc(90vh - 72px);
	overflow: auto;
}

.bpmt-prompt-meta {
	background: #f8fafc;
	border: 1px solid #e5e7eb;
	border-radius: 6px;
	display: grid;
	gap: 8px;
	grid-template-columns: 1fr;
	margin: 0 0 12px;
	padding: 10px 12px;
}

.bpmt-prompt-meta div {
	min-width: 0;
}

.bpmt-prompt-meta span {
	color: #667085;
	display: block;
	font-size: 12px;
	line-height: 18px;
}

.bpmt-prompt-meta strong {
	color: #1f2937;
	display: block;
	font-size: 13px;
	font-weight: 600;
	line-height: 20px;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

@media (min-width: 1100px) {
	.bpmt-prompt-meta {
		grid-template-columns: 1fr 1fr 2fr;
	}
}
```

预期结果：元信息行在宽屏三列展示，在窄弹窗中纵向堆叠，不遮挡输入项。

- [x] **Step 2: 固定提示词 textarea 的可读高度**

保持现有等宽字体规则，补充高度：

```css
.bpmt-ai-prompt-dialog textarea[name="promptText"] {
	font-family: "SFMono-Regular", Consolas, "Liberation Mono", Menlo, monospace;
	font-size: 12px;
	height: 300px;
	line-height: 1.55;
}
```

预期结果：长提示词可读、可滚动，弹窗本身不会超出 90vh。

## Task 5: 验证和证据

**Files:**
- Create: `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/`

- [x] **Step 1: 运行 Java 8 编译验证**

Run:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn -s settings.local.xml -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

- [x] **Step 2: 浏览器验证登录后首页 zTree**

打开 `http://127.0.0.1:18080/`，登录后检查：

- 左侧仍为 zTree 结构，有折叠开关、文件夹/叶子图标、节点缩进。
- hover 和当前选中态无高度跳动。
- 节点文字过长时省略，不挤压右侧内容。

保存截图：

```text
docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/01-home-ztree.png
```

- [x] **Step 3: 浏览器验证第三方系统列表**

打开第三方系统管理页，检查：

- 表头包含操作、系统标识、系统名称、Client ID、状态、首页地址、微信登录、创建时间、更新时间、说明。
- 长 Client ID、首页地址、说明以省略展示，title 可查看完整值。
- 编辑、启停、AI 接入提示词按钮仍可点击。

保存截图：

```text
docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/02-thirdpart-list.png
```

- [x] **Step 4: 浏览器验证 AI 接入提示词弹窗**

点击任意一行的 AI 接入提示词，检查：

- 弹窗顶部有系统标识、Client ID、OAuth 回调地址三项元信息。
- 修改 Client Secret、API App Key、API App Secret 后，提示词文本同步更新。
- 点击复制提示词后显示成功或手动复制提示。

保存截图：

```text
docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/03-ai-prompt-dialog.png
```

- [x] **Step 5: 记录验证结论**

在验证目录新增 `README.md`：

```markdown
# Configuration Workbench Lite Verification - 2026-06-13

## Commands

- `mvn -s settings.local.xml -DskipTests compile`: BUILD SUCCESS

## Browser Checks

- 首页 zTree：通过，保留 sprite / 折叠 / 层级视觉信号，仅做降噪。
- 第三方系统列表：通过，字段未被简化，长文本不会撑破表格。
- AI 接入提示词弹窗：通过，已展示系统标识、Client ID、OAuth 回调地址，复制反馈保留。

## Residual Risk

- 本切片不覆盖 H5 页面。
- zTree 仍依赖历史 sprite 资源；本次只做 CSS 降噪，不替换图标系统。
```

## Completion Criteria

- `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp` 只新增弹窗元信息和表格长文本展示，不改变 Action 调用和 OAuth 逻辑。
- `platform/src/main/webapp/css/bpmt-modern.css` 只新增/调整 zTree、第三方系统表格、AI 弹窗样式。
- Maven 编译通过。
- 浏览器截图覆盖首页 zTree、第三方系统列表、AI 接入提示词弹窗。
- `design/prototype-review.md` 的 B1/B2/B3 在实现层面均有对应落地点。

## Rollback

如需回滚本切片，只还原以下文件的本次 diff：

- `platform/src/main/webapp/xhtml/thirdpart/ThirdpartAction/list.jsp`
- `platform/src/main/webapp/css/bpmt-modern.css`
- `docs/v2.0.0/configuration-workbench-lite-verification-2026-06-13/`

不需要数据库回滚、Docker 数据卷回滚或后端配置回滚。
