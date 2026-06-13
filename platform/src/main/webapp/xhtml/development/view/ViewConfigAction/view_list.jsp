<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<%--数据表格 --%>
<table class="ws-table" form="${_zone}_form">
	<thead>
		<tr>
			<th style="width: 150px;">操作</th>
			<th field="viewKey">主键(唯一标识)</th>
			<th field="description">描述</th>
			<th field="viewClass">绑定模块</th>
			<th field="loginType">需要登录</th>
			<th field="createDate">创建时间</th>
			<th field="updateDate">更新时间</th>
			<th field="createUid">创建人</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${dp.list}" var="vo">
			<tr>
				<td class="center ws-group">
					<button icon="calculator" text="false" type="button" onclick="Ajax.jump('${_cp}/${vo.viewKey}.view',true);">视图预览</button> <c:if test="${vo.lockFlag!=1}">
						<button icon="wrench" text="false" type="button" onclick="Ui.openTab('编辑[${vo.description}]视图','${_acp}/updateZone.shtml?viewKey=${vo.viewKey}');">修改</button>
						<button icon="trash" text="false" type="button" onclick="Core.fn($('#${_zone}').parents('div[tabs=true]:first').parent(),'del')('${vo.viewKey}');">删除</button>
						<button icon="key" text="false" type="button" onclick="Ui.openTab('权限设置[${vo.description}]视图','${_acp}/priSetZone.shtml?viewKey=${vo.viewKey}');">权限设置</button>
					</c:if> <c:choose>
						<c:when test="${vo.lockFlag==1}">
							<button icon="unlocked" text="false" tip="true" title="解锁之后可以编辑表结构." onclick="Core.fn($('#${_zone}').parents('div[tabs=true]:first').parent(),'unlock')('${vo.viewKey}');">解锁</button>
						</c:when>
						<c:otherwise>
							<button icon="locked" text="false" tip="true" title="锁定之后无法编辑表结构." onclick="Core.fn($('#${_zone}').parents('div[tabs=true]:first').parent(),'lock')('${vo.viewKey}');">锁定</button>
						</c:otherwise>
					</c:choose>
				</td>
				<td class="center">${vo.viewKey}</td>
				<td class="left">${vo.description}</td>
				<td class="center">${moduleMap[vo.viewClass].description}</td>
				<td class="center">${wcm:widget('select[YES_NO]',vo.loginType)}</td>
				<td class="right">${wcm:widget('date[datetime]',vo.createDate)}</td>
				<td class="right">${wcm:widget('date[datetime]',vo.updateDate)}</td>
				<td class="center">${wcm:widget('select[$com.riversoft.platform.po.UsUser;uid;busiName]',vo.createUid)}</td>
			</tr>
		</c:forEach>
		<c:if test="${fn:length(dp.list)<1}">
			<tr class="bpmt-empty-table-row">
				<td colspan="99">
					<div class="bpmt-state bpmt-state-empty">
						<strong>没有匹配的视图配置</strong>
						<span>当前查询条件下没有视图配置。请调整筛选条件，或点击“重置查询”后重新查询。</span>
					</div>
				</td>
			</tr>
		</c:if>
	</tbody>
	<tr>
		<th class="ws-bar">
			<div class="ws-group right" style="float: right;">
				<button type="button" icon="plus" text="true" onclick="Ui.openTab('新建视图','${_acp}/createZone.shtml');">新建视图</button>
			</div>
		</th>
	</tr>
</table>

<%-- 分页  --%>
<wcm:page dp="${dp}" form="${_zone}_form" />

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
