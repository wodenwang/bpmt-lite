<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');

		var $main, $tabs;
		if ('${param._main}' != '') {
			$main = $('#${param._main}');
			$tabs = $('div[tabs=true]:first', $main);
		} else {
			$main = $zone;
			$tabs = null;
		}
		var $msg = $('div[name=mainMsgZone]:first', $main);

		//回调
		var invokeCallback = function() {
			//判断该区域是否有回调函数

			if ($.isFunction(Core.fn($zone, 'callback'))) {
				Core.fn($zone, 'callback')();
			} else {
				var formId = $('div.ws-bar.page', $zone).attr('form');
				if (formId != undefined && formId != '') {
					var $form = $('#' + formId);
					$form.submit();
				}
			}
		};

		//查看
		$('button[name=showTask]', $zone).click(function() {
			var taskId = $(this).val();

			var $tab;
			if ($tabs != null) {
				$tab = Ajax.tab($tabs, '${_acp}/detail.shtml', {
					title : '${wpf:lan("#:zh[查看订单]:en[View Order]#")}',
					minWidth : 1024,
					data : {
						_main : '${param._main}',
						_params : $('#${_zone}_params').val(),
						_TASK_ID : taskId
					}
				});
			} else {
				$tab = Ajax.win('${_acp}/detail.shtml', {
					title : '${wpf:lan("#:zh[查看订单]:en[View Order]#")}',
					minWidth : 1024,
					data : {
						_main : '${param._main}',
						_params : $('#${_zone}_params').val(),
						_TASK_ID : taskId
					}
				});
			}
			Core.fn($tab, 'callback', function() {
				invokeCallback();
			});
		});

		//处理
		$('button[name=invokeTask]', $zone).click(function() {
			var taskId = $(this).val();
			var $tab;
			if ($tabs != null) {
				$tab = Ajax.tab($tabs, '${_acp}/form.shtml', {
					title : '${wpf:lan("#:zh[任务处理]:en[Task processing]#")}',
					minWidth : 1024,
					data : {
						_main : '${param._main}',
						_params : $('#${_zone}_params').val(),
						_TASK_ID : taskId
					}
				});
			} else {
				$tab = Ajax.win('${_acp}/form.shtml', {
					title : '${wpf:lan("#:zh[任务处理]:en[Task processing]#")}',
					minWidth : 1024,
					data : {
						_main : '${param._main}',
						_params : $('#${_zone}_params').val(),
						_TASK_ID : taskId
					}
				});
			}
			Core.fn($tab, 'callback', function() {
				invokeCallback();
			});
		});

		//领取任务
		$('button[name=claimTask]', $zone).click(function() {
			var taskId = $(this).val();
			Ui.confirm('${wpf:lan("#:zh[是否领取群组任务?领取后该任务将成为您的个人任务,其他候选人将无法处理.]:en[Get the group task?]#")}', function() {
				Ajax.json('${_acp}/claimTask.shtml', function(json) {
					Ui.msg($msg, json.msg);
					invokeCallback();
				}, {
					data : {
						_main : '${param._main}',
						_TASK_ID : taskId
					},
					errorZone : $msg.attr('id')
				});
			});
		});
	});
</script>

<textarea id="${_zone}_params" style="display: none;">${param._params}</textarea>
<div name="mainMsgZone" id="${_zone}_msg"></div>
<table class="ws-table" form="${_form}" params="${_zone}_params">
	<tr>
		<th style="width: 50px;">${wpf:lan("#:zh[操作]:en[Operation]#")}</th>
		<th>${wpf:lan("#:zh[单号]:en[Order no.]#")}</th>
		<th>${wpf:lan("#:zh[摘要]:en[Abstract]#")}</th>
		<th>${wpf:lan("#:zh[处理人]:en[Handler]#")}</th>
		<th>${wpf:lan("#:zh[当前节点]:en[Current node]#")}</th>
		<th>${wpf:lan("#:zh[开始时间]:en[Start time]#")}</th>
		<th>${wpf:lan("#:zh[等待时长]:en[Waiting time]#")}</th>
	</tr>
	<c:forEach items="${list}" var="vo">
		<tr>
			<td class="ws-group center"><button name="showTask" type="button" text="false" icon="circle-zoomin" value="${vo.task.id}">${wpf:lan("#:zh[查看]:en[View]#")}</button>
				<button name="invokeTask" type="button" text="false" icon="circle-triangle-e" value="${vo.task.id}">${wpf:lan("#:zh[处理]:en[Handle]#")}</button> <c:if test="${param.type=='share'}">
					<%-- 共享任务,展示"领取"按钮 --%>
					<button name="claimTask" type="button" text="false" icon="star" value="${vo.task.id}">${wpf:lan("#:zh[领取任务]:en[Get the task]#")}</button>
				</c:if></td>
			<td class="center">${vo.ordId}</td>
			<td class="center">${vo.order.REMARK}</td>
			<td class="center">${vo.assignee}</td>
			<td class="center">${vo.activity}</td>
			<td class="center">${wcm:widget('date[datetime]',vo.task.createTime)}</td>
			<td class="center">${wpf:formatDuring(wpf:compareDate(_now,vo.task.createTime,'s'))}</td>
		</tr>
	</c:forEach>
	<c:if test="${fn:length(list) < 1}">
		<tr class="bpmt-empty-table-row">
			<td colspan="7">
				<div class="bpmt-state bpmt-state-empty">
					<strong>${wpf:lan("#:zh[没有流程任务]:en[No workflow tasks]#")}</strong>
					<span>${wpf:lan("#:zh[当前查询条件下没有流程任务。请调整筛选条件，或点击“重置查询”后重新查询。]:en[There are no workflow tasks for the current query. Adjust filters, or reset the query and search again.]#")}</span>
				</div>
			</td>
		</tr>
	</c:if>
</table>

<wcm:page dp="${dp}" form="${_form}" params="${_zone}_params" />

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
