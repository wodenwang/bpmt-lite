<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');

		$('button[name=create]', $zone).click(function() {
			Core.fn($zone, 'create')();
		});

		$('button[name=edit]', $zone).click(function() {
			Core.fn($zone, 'edit')($(this).val());
		});

		$('button[name=toggleActive]', $zone).click(function() {
			Core.fn($zone, 'toggleActive')($(this).val(), $(this).attr('activeFlag'));
		});

		$('button[name=aiPrompt]', $zone).click(function() {
			var $row = $(this).closest('tr');
			var data = {
				thirdpartKey: $('.bpmt-ai-prompt-thirdpart-key', $row).val() || '',
				thirdpartName: $('.bpmt-ai-prompt-thirdpart-name', $row).val() || '',
				clientId: $('.bpmt-ai-prompt-client-id', $row).val() || '',
				redirectUris: $('.bpmt-ai-prompt-redirect-uris', $row).val() || '',
				homeUrl: $('.bpmt-ai-prompt-home-url', $row).val() || ''
			};
			var $dialog = $('#${_zone}_bpmt_ai_prompt_dialog');
			$dialog.data('bpmtAiPromptData', data);
			$('input[name=promptClientSecret]', $dialog).val('CLIENT_SECRET_PLACEHOLDER');
			$('input[name=promptApiAppKey]', $dialog).val('BPMT_API_APP_KEY');
			$('input[name=promptApiAppSecret]', $dialog).val('BPMT_API_APP_SECRET');
			renderAiPrompt($dialog);
			$dialog.dialog({ title: 'AI 接入提示词', width: 760, modal: true });
		});

		$('input[name=promptClientSecret], input[name=promptApiAppKey], input[name=promptApiAppSecret]', $('#${_zone}_bpmt_ai_prompt_dialog')).keyup(function() {
			renderAiPrompt($('#${_zone}_bpmt_ai_prompt_dialog'));
		}).change(function() {
			renderAiPrompt($('#${_zone}_bpmt_ai_prompt_dialog'));
		});

		$('button[name=copyAiPrompt]', $('#${_zone}_bpmt_ai_prompt_dialog')).click(function() {
			var $prompt = $('textarea[name=promptText]', $('#${_zone}_bpmt_ai_prompt_dialog'));
			var prompt = $prompt.val();
			if (navigator.clipboard && navigator.clipboard.writeText) {
				var copyResult = navigator.clipboard.writeText(prompt);
				if (copyResult && copyResult.then) {
					copyResult.then(null, function() {
						$prompt.focus().select();
					});
				}
			} else {
				$prompt.focus().select();
				document.execCommand('copy');
			}
		});

		function renderAiPrompt($dialog) {
			var data = $dialog.data('bpmtAiPromptData') || {};
			data.clientSecret = $('input[name=promptClientSecret]', $dialog).val() || 'CLIENT_SECRET_PLACEHOLDER';
			data.apiAppKey = $('input[name=promptApiAppKey]', $dialog).val() || 'BPMT_API_APP_KEY';
			data.apiAppSecret = $('input[name=promptApiAppSecret]', $dialog).val() || 'BPMT_API_APP_SECRET';
			$('textarea[name=promptText]', $dialog).val(buildAiPrompt(data));
		}

		function buildAiPrompt(data) {
			var thirdpartName = data.thirdpartName || data.thirdpartKey || '第三方系统';
			var homeUrl = data.homeUrl || '请填写第三方系统首页地址';
			var redirectUris = data.redirectUris || '请填写 BPMT 后台配置的 redirect_uri 白名单';
			return [
				'你正在初始化一个接入 BPMT 的第三方系统项目。请先检查项目根目录是否已有 AGENTS.md 或 CLAUDE.md：如果已有，优先更新现有文件；如果都不存在，Codex 项目创建 AGENTS.md，Claude Code 项目创建 CLAUDE.md。',
				'',
				'项目身份：' + thirdpartName,
				'BPMT 第三方系统标识 thirdpartKey：' + (data.thirdpartKey || '请填写 thirdpartKey'),
				'第三方系统首页 homeUrl：' + homeUrl,
				'',
				'必须遵守 BPMT OAuth Authorization Code 登录流程：',
				'- BPMT 基础地址由部署环境提供，例如 http://127.0.0.1/ 或生产域名。',
				'- 授权端点：GET /oauth/authorize',
				'- 换 token 端点：POST /oauth/token',
				'- 用户信息端点：GET /oauth/userinfo',
				'- client_id：' + (data.clientId || '请填写 client_id'),
				'- client_secret：' + data.clientSecret,
				'- redirect_uri 白名单：' + redirectUris,
				'- 发起 /oauth/authorize 时必须生成并保存不可预测的 state，回调时严格校验 state，校验失败不得建立登录态。',
				'- /oauth/token 使用 authorization_code、code、redirect_uri、client_id、client_secret 换取 access_token。',
				'- /oauth/userinfo 使用 Authorization: Bearer <access_token> 获取 BPMT 用户信息，并据此建立本系统登录态。',
				'- 组织、用户和登录统一走 BPMT OAuth；第三方系统不得维护独立 BPMT 用户密码，也不得绕过 BPMT 用户权限边界。',
				'',
				'如需调用 BPMT API，使用现有 HMAC-SHA256 规则：',
				'- API App Key：' + data.apiAppKey,
				'- API App Secret：' + data.apiAppSecret,
				'- 请求头必须包含 X-BPMT-App-Key、X-BPMT-Timestamp、X-BPMT-Nonce、X-BPMT-Signature。',
				'- canonical path 必须包含公开 context path，例如 /api/v1/dynamic-tables，不能只签 /v1/dynamic-tables。',
				'- canonical string 格式为 METHOD\\nPATH\\nNORMALIZED_QUERY\\nTIMESTAMP\\nNONCE\\nSHA256_HEX(BODY)。',
				'- JSON 响应使用 success/data/error 包装；OAuth token 和 userinfo 响应不使用该包装。',
				'',
				'请把以上规则写入项目治理文件，并在后续实现登录、会话、API client、测试和部署说明时持续遵守。'
			].join('\n');
		}
	});
</script>

<table class="ws-table" form="${_zone}_form">
	<thead>
		<tr>
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
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${dp.list}" var="vo">
			<tr>
				<td class="center ws-group">
					<button icon="wrench" text="false" type="button" name="edit" value="${vo.thirdpartKey}">编辑</button>
					<c:choose>
						<c:when test="${vo.activeFlag == 1}">
							<button icon="closethick" text="false" type="button" name="toggleActive" value="${vo.thirdpartKey}" activeFlag="0">停用</button>
						</c:when>
						<c:otherwise>
							<button icon="check" text="false" type="button" name="toggleActive" value="${vo.thirdpartKey}" activeFlag="1">启用</button>
						</c:otherwise>
					</c:choose>
					<button icon="clipboard" text="false" type="button" name="aiPrompt">AI 接入提示词</button>
					<textarea class="bpmt-ai-prompt-thirdpart-key" style="display:none;"><c:out value="${vo.thirdpartKey}" /></textarea>
					<textarea class="bpmt-ai-prompt-thirdpart-name" style="display:none;"><c:out value="${vo.thirdpartName}" /></textarea>
					<textarea class="bpmt-ai-prompt-client-id" style="display:none;"><c:out value="${vo.clientId}" /></textarea>
					<textarea class="bpmt-ai-prompt-redirect-uris" style="display:none;"><c:out value="${vo.redirectUris}" /></textarea>
					<textarea class="bpmt-ai-prompt-home-url" style="display:none;"><c:out value="${vo.homeUrl}" /></textarea>
				</td>
				<td class="center">${vo.thirdpartKey}</td>
				<td class="left">${vo.thirdpartName}</td>
				<td class="left">${vo.clientId}</td>
				<td class="center"><c:choose><c:when test="${vo.activeFlag == 1}">启用</c:when><c:otherwise>停用</c:otherwise></c:choose></td>
				<td class="left">${vo.homeUrl}</td>
				<td class="left"><c:choose><c:when test="${vo.wechatLoginEnabled == 1 && vo.wechatType == 'agent'}">企业号: ${vo.wechatKey}</c:when><c:when test="${vo.wechatLoginEnabled == 1 && vo.wechatType == 'mp'}">服务号: ${vo.wechatKey}</c:when><c:otherwise>关闭</c:otherwise></c:choose></td>
				<td class="right">${wcm:widget('date[datetime]',vo.createTime)}</td>
				<td class="right">${wcm:widget('date[datetime]',vo.updateTime)}</td>
				<td class="left">${vo.description}</td>
			</tr>
		</c:forEach>
	</tbody>
	<tr>
		<th class="ws-bar">
			<div class="ws-group right">
				<button type="button" icon="plus" text="true" name="create">新增外部系统</button>
			</div>
		</th>
	</tr>
</table>

<div id="${_zone}_bpmt_ai_prompt_dialog" class="bpmt-ai-prompt-dialog" style="display:none;">
	<table class="ws-table">
		<tr>
			<th>Client Secret</th>
			<td><input type="text" name="promptClientSecret" placeholder="新增时一次性展示或重置后自行保存的明文 clientSecret" style="width: 98%;" /></td>
		</tr>
		<tr>
			<th>API App Key</th>
			<td><input type="text" name="promptApiAppKey" placeholder="例如 BPMT_API_APP_KEY，默认开发值 bpmt-api" style="width: 98%;" /></td>
		</tr>
		<tr>
			<th>API App Secret</th>
			<td><input type="text" name="promptApiAppSecret" placeholder="正式部署必须填写实际 BPMT_API_APP_SECRET" style="width: 98%;" /></td>
		</tr>
		<tr>
			<th>提示词</th>
			<td><textarea name="promptText" style="width: 100%; height: 360px;"></textarea></td>
		</tr>
	</table>
	<div class="ws-bar">
		<div class="ws-group">
			<button type="button" icon="copy" text="true" name="copyAiPrompt">复制提示词</button>
		</div>
	</div>
</div>

<wcm:page dp="${dp}" form="${_zone}_form" />

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
