<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');

		//新建
		$('button[name=create]').click(function() {
			Core.fn($zone, 'create')();
		});

		//删除
		$('button[name=del]').click(function() {
			var agentKey = $(this).val();
			Core.fn($zone, 'remove')(agentKey);
		});

		//设置
		$('button[name=edit]').click(function() {
			var agentKey = $(this).val();
			Core.fn($zone, 'edit')(agentKey);
		});

		//同步所选
		$('button[name=sync]').click(function() {
			var $checkbox = $('input:checked[name=agentKey]', $zone);
			if ($checkbox.size() < 1) {
				Ui.alert("请选择至少一项。");
				return;
			}

			Core.fn($zone, 'sync')();
		});

		//资源管理
		$('button[name=resource]').click(function() {
			var agentKey = $(this).val();
			Core.fn($zone, 'resource')(agentKey);
		});
	});
</script>

<%--数据表格 --%>
<table class="ws-table" form="${_form}">
	<thead>
		<tr>
			<th check="true"></th>
			<th style="width: 80px;">操作</th>
			<th field="agentKey">逻辑主键</th>
			<th field="agentId">Agent ID</th>
			<th field="title">标题</th>
			<th field="description">描述</th>
			<th field="status">状态</th>
			<th>进入时报告</th>
			<th>上报位置</th>
			<th field="updateDate">更新时间</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${dp.list}" var="vo">
			<tr>
				<td check="true" value="${vo.agentKey}" checkname="agentKey"></td>
				<td class="center ws-group">
					<button icon="image" text="false" type="button" name="resource" value="${vo.agentKey}">资源管理</button>

					<button icon="wrench" text="false" type="button" name="edit" value="${vo.agentKey}">设置</button>
					<button icon="trash" text="false" type="button" name="del" value="${vo.agentKey}">删除</button>
				</td>
				<td class="left">${vo.agentKey}</td>
				<td class="center">${vo.agentId}</td>
				<td class="left"><span tip="true" selector=".logo"><img src="${vo.logoUrl}" style="width: 30px; margin-bottom: -8px;" />${vo.title}<div class="logo">
							<img src="${vo.logoUrl}" style="width: 100px;" />
						</div></span></td>
				<td class="left">${vo.description}</td>
				<td class="center"><c:choose>
						<c:when test="${vo.closeFlag==1}">
							<span style="color: red;">被禁用</span>
						</c:when>
						<c:when test="${vo.status==0}">
							<span style="color: gray;">未对接</span>
						</c:when>
						<c:when test="${vo.status==1}">
							<span style="color: green;">已对接</span>
						</c:when>
						<c:when test="${vo.status==2}">
							<span style="color: red;">无法对接(应用不存在)</span>
						</c:when>
					</c:choose></td>
				<td class="center">${wcm:widget('select[YES_NO]',vo.reportUserEnter)}</td>
				<td class="center">${wcm:widget('select[@com.riversoft.platform.translate.WxReportLocationFlag]',vo.reportLocationFlag)}</td>
				<td class="right">${wcm:widget('date[datetime]',vo.updateDate)}</td>
			</tr>
		</c:forEach>
		<c:if test="${fn:length(dp.list)<1}">
			<tr class="bpmt-empty-table-row">
				<td colspan="99">
					<div class="bpmt-state bpmt-state-empty">
						<strong>没有匹配的企业微信应用</strong>
						<span>当前查询条件下没有企业微信应用。请调整筛选条件，或点击“重置查询”后重新查询。</span>
					</div>
				</td>
			</tr>
		</c:if>
	</tbody>
	<tr>
		<th class="ws-bar">
			<div class="left">
				<span class="ws-group"><button icon="transferthick-e-w" type="button" name="sync">发布应用(同步)</button></span>
			</div>
			<div class="right">
				<span class="ws-group">
					<button type="button" icon="plus" text="true" name="create">新建应用</button>
				</span>
			</div>
		</th>
	</tr>
</table>

<%-- 分页  --%>
<wcm:page dp="${dp}" form="${_form}" />

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
