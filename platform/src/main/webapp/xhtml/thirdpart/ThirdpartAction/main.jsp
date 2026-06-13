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
			if (activeFlag == '1') {
				Ui.confirm('确认启用第三方系统[' + key + ']?', function() {
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
			} else {
				Ui.confirm('确认停用第三方系统[' + key + ']?', function() {
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
			}
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

		$('button[name=createThirdpart]', $zone).click(function() {
			Core.fn('${_zone}_list', 'create')();
		});

		$('button[name=resetThirdpartQuery]', $zone).click(function() {
			var $form = $('#${_zone}_list_form');
			$('input[type=text]', $form).val('');
			$('select', $form).val('').trigger('liszt:updated').trigger('chosen:updated');
			$form.submit();
		});

		$('#${_zone}_list_form').submit();
	});
</script>

<div tabs="true" max="10" id="${_zone}_tabs" main="true">
	<div title="第三方系统">
		<div class="bpmt-page-head bpmt-thirdpart-head">
			<div class="bpmt-page-head-main">
				<h2>第三方系统</h2>
				<p>配置外部系统 OAuth 登录、微信登录和 AI 接入提示词。新增外部系统后，可在列表中管理启停状态并复制接入说明。</p>
			</div>
			<div class="bpmt-page-head-actions">
				<button type="button" icon="plus" text="true" name="createThirdpart">新增外部系统</button>
			</div>
		</div>

		<form zone="${_zone}_list" action="${_acp}/list.shtml" query="true" id="${_zone}_list_form" method="get" class="bpmt-query-panel">
			<input type="hidden" name="_field" value="thirdpartKey" />
			<input type="hidden" name="_dir" value="asc" />
			<table class="ws-table bpmt-query-table">
				<tr>
					<th>系统标识 <span class="bpmt-field-hint">模糊</span></th>
					<td><wcm:widget name="_sl_thirdpartKey" cmd="text" /></td>
					<th>系统名称 <span class="bpmt-field-hint">模糊</span></th>
					<td><wcm:widget name="_sl_thirdpartName" cmd="text" /></td>
				</tr>
				<tr>
					<th>Client ID <span class="bpmt-field-hint">模糊</span></th>
					<td><wcm:widget name="_sl_clientId" cmd="text" /></td>
					<th>是否启用</th>
					<td><wcm:widget name="_ne_activeFlag" cmd="select[YES_NO(全部)]" /></td>
				</tr>
				<tr>
					<th class="ws-bar">
						<div class="ws-group right">
							<button type="button" icon="arrowreturnthick-1-w" text="true" name="resetThirdpartQuery">重置查询</button>
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
