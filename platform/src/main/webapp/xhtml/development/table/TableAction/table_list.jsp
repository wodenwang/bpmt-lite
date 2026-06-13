<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');

		//预览
		$('button[name=preview]').click(function() {
			var val = $(this).val();
			Core.fn($zone, 'preview')(val);
		});

		//查看表结构
		$('button[name=show]').click(function() {
			var val = $(this).val();
			Core.fn($zone, 'show')(val);
		});

		//修改
		$('button[name=edit]').click(function() {
			var val = $(this).val();
			Core.fn($zone, 'edit')(val);
		});

		//修改
		$('button[name=index]').click(function() {
			var val = $(this).val();
			Core.fn($zone, 'index')(val);
		});

		//删除
		$('button[name=del]').click(function() {
			var val = $(this).val();
			Core.fn($zone, 'del')(val);
		});

		//锁定
		$('button[name=lock]').click(function() {
			var val = $(this).val();
			Core.fn($zone, 'lock')(val);
		});

		//解锁
		$('button[name=unlock]').click(function() {
			var val = $(this).val();
			Core.fn($zone, 'unlock')(val);
		});

		//新建
		$('button[name=create]').click(function() {
			Core.fn($zone, 'create')();
		});

		//关联游离表
		$('button[name=link]').click(function() {
			Core.fn($zone, 'link')();
		});

		//同步结构
		$('button[name=syncType]').click(function() {
			Core.fn($zone, 'syncType')();
		});

		//导出结构
		$('button[name=exportType]').click(function() {
			Core.fn($zone, 'exportType')();
		});

		//导出数据
		$('button[name=exportData]').click(function() {
			Core.fn($zone, 'exportData')();
		});

		//导出数据
		$('button[name=exportDataExt]').click(function() {
			Core.fn($zone, 'exportDataExt')();
		});

		//导入结构
		$('button[name=batchZone]').click(function() {
			Core.fn($zone, 'batchZone')();
		});

		//导入数据
		$('button[name=importDataZone]').click(function() {
			Core.fn($zone, 'importDataZone')();
		});
	});
</script>

<div class="ws-bar">
	<div class="left ws-group">
		<button type="button" icon="arrowthickstop-1-s" name="exportType">导出结构</button>
		<button icon="arrowthickstop-1-s" type="button" name="exportData">导出数据</button>
		<button icon="arrowthickstop-1-s" type="button" name="exportDataExt">导出数据(高级)</button>
	</div>
	<div class="right ws-group">
		<button type="button" icon="arrowthickstop-1-n" name="batchZone">导入结构</button>
		<button icon="arrowthickstop-1-n" type="button" name="importDataZone">导入数据</button>
	</div>
</div>

<%--数据表格 --%>
<table class="ws-table" form="${_zone}_form">
	<thead>
		<tr>
			<th check="true"></th>
			<th style="width: 150px;">操作</th>
			<th field="name">表名</th>
			<th field="description">展示名</th>
			<th field="createDate">创建时间</th>
			<th field="updateDate">更新时间</th>
			<th field="createUid">建表人</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${dp.list}" var="vo">
			<tr>
				<td check="true" value="${vo.name}"></td>
				<td class="center ws-group">
					<button icon="calculator" text="false" type="button" name="preview" value="${vo.name}">数据预览</button>
					<button icon="zoomin" text="false" type="button" name="show" value="${vo.name}">查看结构</button> <c:if test="${vo.lockFlag!=1}">
						<button icon="wrench" text="false" type="button" name="edit" value="${vo.name}">修改</button>
						<button icon="tag" text="false" type="button" name="index" value="${vo.name}">索引管理</button>
						<button icon="trash" text="false" type="button" name="del" value="${vo.name}">删除</button>
					</c:if> <c:choose>
						<c:when test="${vo.lockFlag==1}">
							<button icon="unlocked" text="false" tip="true" type="button" title="解锁之后可以编辑表结构." name="unlock" value="${vo.name}">解锁</button>
						</c:when>
						<c:otherwise>
							<button icon="locked" text="false" tip="true" type="button" title="锁定之后无法编辑表结构." name="lock" value="${vo.name}">锁定</button>
						</c:otherwise>
					</c:choose>
				</td>
				<td class="left">${vo.name}</td>
				<td class="left">${vo.description}</td>
				<td class="right">${wcm:widget('date[datetime]',vo.createDate)}</td>
				<td class="right">${wcm:widget('date[datetime]',vo.updateDate)}</td>
				<td class="center">${wcm:widget('select[$com.riversoft.platform.po.UsUser;uid;busiName]',vo.createUid)}</td>
			</tr>
		</c:forEach>
		<c:if test="${fn:length(dp.list)<1}">
			<tr class="bpmt-empty-table-row">
				<td colspan="99">
					<div class="bpmt-state bpmt-state-empty">
						<strong>没有匹配的数据表</strong>
						<span>当前查询条件下没有数据表。请调整筛选条件，或点击“重置查询”后重新查询。</span>
					</div>
				</td>
			</tr>
		</c:if>
	</tbody>
	<tr>
		<th class="ws-bar">
			<div class="left">
				<span class="ws-group"><button icon="transferthick-e-w" type="button" name="syncType">同步结构</button></span>
			</div>
			<div class="right">
				<span class="ws-group">
					<button type="button" icon="link" text="true" name="link">关联表</button>
					<button type="button" icon="plus" text="true" name="create">新建表</button>
				</span>
			</div>
		</th>
	</tr>
</table>

<%-- 分页  --%>
<wcm:page dp="${dp}" form="${_zone}_form" />

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
