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
	});
</script>

<table class="ws-table" form="${_zone}_form">
	<thead>
		<tr>
			<th style="width: 95px;">操作</th>
			<th field="thirdpartKey">系统标识</th>
			<th field="thirdpartName">系统名称</th>
			<th field="clientId">Client ID</th>
			<th field="activeFlag">状态</th>
			<th field="homeUrl">首页地址</th>
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
				</td>
				<td class="center">${vo.thirdpartKey}</td>
				<td class="left">${vo.thirdpartName}</td>
				<td class="left">${vo.clientId}</td>
				<td class="center"><c:choose><c:when test="${vo.activeFlag == 1}">启用</c:when><c:otherwise>停用</c:otherwise></c:choose></td>
				<td class="left">${vo.homeUrl}</td>
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

<wcm:page dp="${dp}" form="${_zone}_form" />

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
