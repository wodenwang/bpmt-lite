<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');

		Core.fn('${_zone}_list', 'create', function() {
			var $tab = Ui.openTab('新增外部系统', '${_acp}/createZone.shtml');
			Core.fn($tab, 'submitForm', Core.fn($zone, 'submitForm'));
		});

		Core.fn('${_zone}_list', 'edit', function(key) {
			var $tab = Ui.openTab('编辑外部系统', '${_acp}/editZone.shtml?thirdpartKey=' + encodeURIComponent(key));
			Core.fn($tab, 'submitForm', Core.fn($zone, 'submitForm'));
		});

		Core.fn('${_zone}_list', 'toggleActive', function(key, activeFlag) {
			Ajax.post('${_zone}_msg', '${_acp}/toggleActive.shtml', {
				data : {
					thirdpartKey : key,
					activeFlag : activeFlag
				},
				callback : function(flag) {
					if (flag) {
						$('#${_zone}_list_form').submit();
					}
				}
			});
		});

		Core.fn($zone, 'submitForm', function($form, $tab, option) {
			option = $.extend({}, option, {
				callback : function(flag) {
					if (flag) {
						Ui.closeTab($tab);
						$('#${_zone}_list_form').submit();
					}
				}
			});
			Ajax.form('${_zone}_msg', $form, option);
		});

		$('#${_zone}_list_form').submit();
	});
</script>

<div tabs="true" max="10" id="${_zone}_tabs" main="true">
	<div title="外部系统管理">
		<form zone="${_zone}_list" action="${_acp}/list.shtml" query="true" id="${_zone}_list_form" method="get">
			<input type="hidden" name="_field" value="thirdpartKey" />
			<input type="hidden" name="_dir" value="asc" />
			<table class="ws-table">
				<tr>
					<th>系统标识(模糊)</th>
					<td><wcm:widget name="_sl_thirdpartKey" cmd="text" /></td>
					<th>系统名称(模糊)</th>
					<td><wcm:widget name="_sl_thirdpartName" cmd="text" /></td>
				</tr>
				<tr>
					<th>Client ID(模糊)</th>
					<td><wcm:widget name="_sl_clientId" cmd="text" /></td>
					<th>启用状态</th>
					<td>
						<select name="_ne_activeFlag">
							<option value="">全部</option>
							<option value="1">启用</option>
							<option value="0">停用</option>
						</select>
					</td>
				</tr>
				<tr>
					<th class="ws-bar">
						<div class="ws-group right">
							<button type="reset" icon="arrowreturnthick-1-w" text="true">重置查询</button>
							<button type="submit" icon="search" text="true">查询</button>
						</div>
					</th>
				</tr>
			</table>
		</form>

		<div id="${_zone}_msg"></div>
		<div id="${_zone}_list"></div>
	</div>
</div>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
