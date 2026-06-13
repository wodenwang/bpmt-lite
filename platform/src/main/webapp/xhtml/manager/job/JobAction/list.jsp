<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');

		//修改
		$('button[name=edit]').click(function() {
			var val = $(this).val();
			Core.fn($zone, 'edit')(val);
		});

		//删除
		$('button[name=del]').click(function() {
			var val = $(this).val();
			Core.fn($zone, 'del')(val);
		});

		$('button[name=active]').click(function() {
			var val = $(this).val();
			Ui.confirm('确认启用任务[' + val + ']?', function() {
				Core.fn($zone, 'active')(val, true);
			});
		});

		$('button[name=pause]').click(function() {
			var val = $(this).val();
			Ui.confirm('确认暂停任务[' + val + ']?', function() {
				Core.fn($zone, 'active')(val, false);
			});
		});

		//新建
		$('button[name=create]').click(function() {
			Core.fn($zone, 'create')();
		});

		$('a[name=logTablePreview]').click(function() {
			var name = $(this).attr("tableName");
			Ajax.win('${_cp}/development/table/TableAction/preview.shtml?name=' + name, {
				title : '表[' + name + ']数据预览',
				minWidth : 800,
				minHeight : 500
			});
		});

	});
</script>

<%--数据表格 --%>
<table class="ws-table" form="${_zone}_form">
	<thead>
		<tr>
			<th style="width: 120px;">操作</th>
			<th field="jobKey">主键</th>
			<th field="description">描述</th>
			<th field="activeFlag">状态</th>
			<th field="isTransaction">是否起事务</th>
			<th>CRON表达式</th>
			<th>日志表</th>
			<th field="createUid">创建人</th>
			<th field="createDate">创建时间</th>
			<th field="updateDate">更新时间</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${dp.list}" var="vo">
			<tr>
				<td class="center ws-group">
					<button icon="trash" text="false" type="button" name="del"
						value="${vo.jobKey}">删除</button>
					<button icon="wrench" text="false" type="button" name="edit"
						value="${vo.jobKey}">修改</button> <c:choose>
						<c:when test="${vo.activeFlag==1}">
							<button icon="pause" text="false" type="button" name="pause"
								value="${vo.jobKey}">暂停</button>
						</c:when>
						<c:otherwise>
							<button icon="play" text="false" type="button" name="active"
								value="${vo.jobKey}">启用</button>
						</c:otherwise>
					</c:choose>
				</td>
				<td class="center">${vo.jobKey}</td>
				<td class="left">${vo.description}</td>
				<td class="center"><c:choose>
						<c:when test="${vo.activeFlag==1}">
							<font color="green">活跃中</font>
						</c:when>
						<c:otherwise>
							<font color="red">暂停</font>
						</c:otherwise>
					</c:choose></td>
				<td class="center">${wcm:widget('select[YES_NO]',vo.isTransaction)}</td>
				<td class="center">${vo.cronExpression}</td>
				<td class="center"><c:choose>
						<c:when test="${vo.logTableName!=null&&vo.logTableName!=''}">
							<a tableName="${vo.logTableName}" href="#" name="logTablePreview"
								style="text-decoration: underline;">${vo.logTableName}</a>
						</c:when>
						<c:otherwise>
							<span style="font-style: italic;">(无配置)</span>
						</c:otherwise>
					</c:choose></td>
				<td class="center">${wcm:widget('select[$com.riversoft.platform.po.UsUser;uid;busiName]',vo.createUid)}</td>
				<td class="center">${wcm:widget('date',vo.createDate)}</td>
				<td class="center">${wcm:widget('date',vo.updateDate)}</td>
			</tr>
		</c:forEach>
		<c:if test="${fn:length(dp.list)<1}">
			<tr class="bpmt-empty-table-row">
				<td colspan="99">
					<div class="bpmt-state bpmt-state-empty">
						<strong>没有匹配的定时任务</strong>
						<span>当前查询条件下没有定时任务。请调整筛选条件，或点击“重置查询”后重新查询。</span>
					</div>
				</td>
			</tr>
		</c:if>
	</tbody>
	<tr>
		<th class="ws-bar">
			<div class="ws-group right">
				<button type="button" icon="plus" text="true" name="create">新建任务</button>
			</div>
		</th>
	</tr>
</table>

<%-- 分页  --%>
<wcm:page dp="${dp}" form="${_zone}_form" />

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
