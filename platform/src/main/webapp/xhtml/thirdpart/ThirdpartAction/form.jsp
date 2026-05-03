<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');

		$('button[name=submitForm]', $zone).click(function() {
			var $form = $('form', $zone);
			Core.fn($zone, 'submitForm')($form, $zone, {
				confirmMsg : '确认保存外部系统?',
				errorZone : '${_zone}_err_zone'
			});
		});
	});
</script>

<c:set var="editFlag" value="${vo!=null}" />
<div name="msgZone" id="${_zone}_err_zone"></div>
<form aync="true" action="${_acp}/submitForm.shtml" method="post">
	<table class="ws-table">
		<tr>
			<th>系统标识</th>
			<td>
				<c:choose>
					<c:when test="${editFlag}">
						<input type="hidden" name="thirdpartKey" value="${vo.thirdpartKey}" />
						<c:out value="${vo.thirdpartKey}" />
					</c:when>
					<c:otherwise>
						<wcm:widget name="thirdpartKey" cmd="key[THIRDPART]{required:true}" />
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<th>系统名称</th>
			<td><wcm:widget name="thirdpartName" cmd="text{required:true}" value="${vo.thirdpartName}" /></td>
		</tr>
		<tr>
			<th>Client ID</th>
			<td><wcm:widget name="clientId" cmd="text{required:true}" value="${vo.clientId}" /></td>
		</tr>
		<c:if test="${editFlag}">
			<tr>
				<th>重置密钥</th>
				<td><wcm:widget name="clientSecret" cmd="text" /></td>
			</tr>
		</c:if>
		<tr>
			<th>回调地址</th>
			<td><wcm:widget name="redirectUris" cmd="textarea{required:true}" value="${vo.redirectUris}" /></td>
		</tr>
		<tr>
			<th>首页地址</th>
			<td><wcm:widget name="homeUrl" cmd="textarea" value="${vo.homeUrl}" /></td>
		</tr>
		<tr>
			<th>状态</th>
			<td><wcm:widget name="activeFlag" cmd="radio[YES_NO]{required:true}" value="${editFlag?vo.activeFlag:1}" /></td>
		</tr>
		<tr>
			<th>可访问权限</th>
			<td><wcm:widget name="pri" cmd="pri{required:true}" value="${vo.pri}" /></td>
		</tr>
		<tr>
			<th>说明</th>
			<td><wcm:widget name="description" cmd="textarea" value="${vo.description}" /></td>
		</tr>
		<c:if test="${editFlag}">
			<tr>
				<th>创建时间</th>
				<td>${wcm:widget('date[datetime]',vo.createTime)}</td>
			</tr>
			<tr>
				<th>更新时间</th>
				<td>${wcm:widget('date[datetime]',vo.updateTime)}</td>
			</tr>
		</c:if>
	</table>
</form>

<div class="ws-bar">
	<div class="ws-group">
		<button type="button" icon="check" text="true" name="submitForm">保存</button>
	</div>
</div>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
