<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');

		//表单动作
		$('form', $zone).submit(function() {
			var $form = $(this);
			Ajax.form('${_zone}_result', $form, {
				errorZone : '${_zone}_msg',
				confirmMsg : '是否保存?',
				callback : function(flag) {
					if (flag) {
						Core.fn($zone, 'refresh')();
					}
				}
			});
			return false;
		});
	});
</script>

<c:set var="isCreate" value="${vo==null}" />

<c:choose>
	<c:when test="${isCreate&&param.leafFlag==1}">
		<c:set var="title" value="新增权限组" />
	</c:when>
	<c:when test="${isCreate&&param.leafFlag==0}">
		<c:set var="title" value="新增文件夹" />
	</c:when>
	<c:otherwise>
		<c:set var="title" value="[${vo.name}]编辑" />
	</c:otherwise>
</c:choose>

<div tabs="true">
	<div title="${title}">
		<div id="${_zone}_msg"></div>
		<div id="${_zone}_result">
			<form action="${_acp}/submitForm.shtml" method="post" sync="true">
				<input type="hidden" name="leafFlag" value="${isCreate?param.leafFlag:vo.leafFlag}" /> <input type="hidden" name="parentId" value="${isCreate?'':vo.parentId}" /><input type="hidden" name="sort"
					value="${isCreate?999:vo.sort}" /><input type="hidden" name="isCreate" value="${isCreate?1:0}" /> <input name="groupId" value="${vo.groupId}" type="hidden" />

				<table class="ws-table">
					<tr>
						<th>展示名</th>
						<td><wcm:widget name="name" cmd="text{required:true}" value="${vo.name}"></wcm:widget></td>
					</tr>
					<tr>
						<th>描述</th>
						<td><wcm:widget name="description" cmd="textarea" value="${vo.description}"></wcm:widget></td>
					</tr>
				</table>

				<div class="ws-bar">
					<button icon="arrowreturnthick-1-w" type="reset">重置</button>
					<button icon="disk" type="submit">保存</button>
				</div>
			</form>
		</div>
	</div>

	<c:if test="${!isCreate&&vo.leafFlag==1}">
		<div title="菜单权限" init="${_acp}/menuPri.shtml?groupId=${vo.groupId}"></div>
		<div title="视图权限" init="${_acp}/viewPri.shtml?groupId=${vo.groupId}"></div>
		<div title="控件权限" init="${_acp}/widgetPri.shtml?groupId=${vo.groupId}"></div>
		<div title="第三方系统权限" init="${_acp}/thirdpartPri.shtml?groupId=${vo.groupId}"></div>
		<div title="角色归属" init="${_acp}/roleList.shtml?groupId=${vo.groupId}"></div>
	</c:if>

</div>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
