<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $("#${_zone}");
		$('tr[state=0]', $zone).css('font-weight', 'bold');

		//查看
		$('a[name=detailWin]', $zone).click(function(event) {
			event.preventDefault();
			var $this = $(this);
			var url = $this.attr('href');

			var $win = Ajax.win(url, {
				title : $this.text(),
				minWidth : 1024,
				callback : function(flag) {
					if (flag) {
						//标记为已读
						$('img[name=state]', $this).attr('src', '${_cp}/css/icon/bullet_white.png');
						$this.parents('tr:first').css('font-weight', '');
					}
				}
			});
			Core.fn($win, 'sendMailForm', function(type, id) {
				Core.fn($zone, 'sendMailForm')(type, id);
			});

		});

		$('button[name=del]', $zone).click(function() {
			Core.fn($zone, 'delMail')();
		});

		$('button[name=reAll]', $zone).click(function() {
			var id = $(this).val();
			Core.fn($zone, 'sendMailForm')(1, id);
		});

		$('button[name=re]', $zone).click(function() {
			var id = $(this).val();
			Core.fn($zone, 'sendMailForm')(2, id);
		});

		$('button[name=fw]', $zone).click(function() {
			var id = $(this).val();
			Core.fn($zone, 'sendMailForm')(3, id);
		});
	});
</script>

<table class="ws-table" form="${_form}">
	<tr>
		<th check="true"></th>
		<th style="width: 20px;">操作</th>
		<th field="SUBJECT">标题</th>
		<th field="FROM_ADDR">发件人</th>
		<th field="RECEIVE_DATE">接收时间</th>
	</tr>
	<c:forEach items="${dp.list}" var="vo">
		<tr state="${vo.STATE}">
			<td check="true" value="${vo.ID}"></td>
			<td class="center"><span class="ws-group">
					<button text="false" icon="arrowreturnthick-1-w" type="button" name="reAll" value="${vo.ID}">回复全部</button>
					<button text="false" icon="arrowreturn-1-w" type="button" name="re" value="${vo.ID}">回复</button>
					<button text="false" icon="arrowthick-1-w" type="button" name="fw" value="${vo.ID}">转发</button>
			</span></td>
			<td><a href="${_acp}/showReceiveMail.shtml?id=${vo.ID}" name="detailWin"><c:choose>
						<c:when test="${vo.STATE==0}">
							<img alt="未读" src="${_cp}/css/icon/bullet_blue.png" name="state" />
						</c:when>
						<c:otherwise>
							<img alt="已读" src="${_cp}/css/icon/bullet_white.png" name="state" />
						</c:otherwise>
					</c:choose>${vo.SUBJECT}<c:if test="${vo.ATTACHMENT!=null}">
						<img alt="附件" src="${_cp}/css/icon/attach.png" />
					</c:if> </a></td>
			<td class="center">${vo.FROM_ADDR}</td>
			<td class="center"><f:formatDate value="${vo.RECEIVE_DATE}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
		</tr>
	</c:forEach>
	<c:if test="${fn:length(dp.list)<1}">
		<tr class="bpmt-empty-table-row">
			<td colspan="99">
				<div class="bpmt-state bpmt-state-empty">
					<strong>没有匹配的收件邮件</strong>
					<span>当前查询条件下没有收件邮件。请调整筛选条件，或点击“重置查询”后重新查询。</span>
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
