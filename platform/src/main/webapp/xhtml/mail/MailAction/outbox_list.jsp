<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $("#${_zone}");

		$('button[name=del]', $zone).click(function() {
			Core.fn($zone, 'delMail')();
		});

		$('button[name=edit]', $zone).click(function() {
			var id = $(this).val();
			Core.fn($zone, 'sendMailForm')(4, id);
		});

		$('button[name=fw]', $zone).click(function() {
			var id = $(this).val();
			Core.fn($zone, 'sendMailForm')(5, id);
		});
	});
</script>

<table class="ws-table" form="${_form}">
	<tr>
		<th check="true"></th>
		<th field="STATE" style="width: 20px;">状态</th>
		<th style="width: 20px;">操作</th>
		<th field="SUBJECT">标题</th>
		<th field="TO_ADDRS">收件人</th>
		<th field="CREATE_DATE">创建时间</th>
		<th field="SENT_DATE">发送时间</th>
	</tr>
	<c:forEach items="${dp.list}" var="vo">
		<tr>
			<td check="true" value="${vo.ID}"></td>
			<td class="center"><c:choose>
					<c:when test="${vo.STATE==0}">
						<span title="未发送" tip="true"><img src="${_cp}/css/icon/control_pause.png" alt="未发送" /></span>
					</c:when>
					<c:otherwise>
						<span title="已发送" tip="true"><img src="${_cp}/css/icon/accept.png" alt="已发送" /></span>
					</c:otherwise>
				</c:choose></td>
			<td class="center"><span class="ws-group"> <c:if test="${vo.STATE==0}">
						<button text="false" icon="pencil" type="button" name="edit" value="${vo.ID}">编辑</button>
					</c:if> <c:if test="${vo.STATE==1}">
						<button text="false" icon="arrowthick-1-w" type="button" name="fw" value="${vo.ID}">转发</button>
					</c:if>
			</span></td>
			<td>${vo.SUBJECT}<c:if test="${vo.ATTACHMENT!=null}">
					<img alt="附件" src="${_cp}/css/icon/attach.png" />
				</c:if></td>
			<td class="center">${vo.TO_ADDRS}</td>
			<td class="center"><f:formatDate value="${vo.CREATE_DATE}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
			<td class="center"><f:formatDate value="${vo.SENT_DATE}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
		</tr>
	</c:forEach>
	<c:if test="${fn:length(dp.list)<1}">
		<tr class="bpmt-empty-table-row">
			<td colspan="99">
				<div class="bpmt-state bpmt-state-empty">
					<strong>没有匹配的发件邮件</strong>
					<span>当前查询条件下没有发件邮件。请调整筛选条件，或点击“重置查询”后重新查询。</span>
				</div>
			</td>
		</tr>
	</c:if>
	<tr>
		<th class="ws-bar left"><button icon="trash" type="button" name="del">删除</button></th>
	</tr>
</table>

<wcm:page dp="${dp}" form="${_form}" />

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
