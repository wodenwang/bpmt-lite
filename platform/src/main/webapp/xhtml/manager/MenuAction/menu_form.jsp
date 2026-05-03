<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');
		var toggleMenuActionFields = function() {
			var openType = $(':input[name=openType]:checked', $zone).val();
			if (openType == undefined || openType == '') {
				openType = $(':input[name=openType]', $zone).val();
			}
			$('[data-menu-open-type]', $zone).each(function() {
				var $row = $(this);
				var active = $row.attr('data-menu-open-type') == openType;
				$row.toggle(active);
				$(':input', $row).prop('disabled', !active);
			});
		};
		$zone.on('change click ifChecked', ':input[name=openType]', toggleMenuActionFields);
		toggleMenuActionFields();

		//表单动作
		$('form', $zone).submit(function() {
			var $form = $(this);
			Ajax.form('${_zone}_result', $form, {
				confirmMsg : '是否保存菜单?',
				callback : function(flag) {
					if (flag) {
						Core.fn($zone, 'refresh')();
					}
				}
			});
			return false;
		});
	});
</script>

<c:set var="isCreate" value="${vo==null}" />
<div tabs="true">
	<div title="${isCreate?'新增菜单':'编辑菜单'}">
		<div id="${_zone}_msg"></div>

		<div id="${_zone}_result">
			<form action="${_acp}/submitMenuForm.shtml" method="post" sync="true">
				<input type="hidden" name="parentId" value="${isCreate?'':vo.parentId}" /><input type="hidden" name="sort" value="${isCreate?999:vo.sort}" /><input type="hidden" name="isCreate"
					value="${isCreate?1:0}" />
				<table class="ws-table">
					<tr>
						<th>菜单主键</th>
						<td><c:choose>
								<c:when test="${isCreate}">
									<wcm:widget name="id" cmd="key[MENU]{required:true}"></wcm:widget>
								</c:when>
								<c:otherwise>
									<span style="color: red; font-weight: bold;">${vo.id}</span>
									<input name="id" value="${vo.id}" type="hidden" />
								</c:otherwise>
							</c:choose></td>
					</tr>
					<tr>
						<th>所属子系统</th>
						<td><wcm:widget name="domainKey" cmd="select[$CmDomain(请选择);domainKey;busiName;1=1 order by sort asc]{required:true}" value="${vo.domainKey}" state="${isCreate?'normal':'readonly'}"></wcm:widget></td>
					</tr>
					<tr>
						<th>菜单名</th>
						<td><wcm:widget name="name" cmd="textarea{required:true}" value="${vo.name}"></wcm:widget></td>
					</tr>
					<tr>
						<th>图标</th>
						<td><wcm:widget name="icon" cmd="icon[sys]" value="${vo.icon}"></wcm:widget></td>
					</tr>
					<c:choose>
						<c:when test="${isCreate||vo.sysFlag==0}">
							<tr>
								<th>打开类型</th>
								<td><wcm:widget name="openType" cmd="radio[@com.riversoft.module.frame.MenuOpenType]{required:true}" value="${isCreate?1:vo.openType}"></wcm:widget></td>
							</tr>
							<tr data-menu-open-type="1">
								<th>打开网址</th>
								<td><wcm:widget name="action" cmd="view[MENU]" value="${vo.action}" /></td>
							</tr>
							<tr data-menu-open-type="2">
								<th>打开第三方网址</th>
								<td><textarea name="thirdpartUrl" rows="4" style="width: 90%;"><c:out value='${vo.action}' /></textarea> <font color="red" tip="true" title="打开类型为第三方网页时使用该地址，可填写 http://、https:// 或 / 开头的地址。">(提示)</font></td>
							</tr>
						</c:when>
						<c:otherwise>
							<tr>
								<th>打开类型</th>
								<td><span style="color: red;">(系统内置,无法修改)</span> <input type="hidden" name="openType" value="${vo.openType}" /></td>
							</tr>
							<tr>
								<th>目标地址</th>
								<td><span style="color: red;">(系统内置,无法修改)</span> <input type="hidden" name="action" value="${vo.action}" /></td>
							</tr>
						</c:otherwise>
					</c:choose>
					<tr>
						<th>动态入参(脚本类型)</th>
						<td><wcm:widget name="paramType" cmd="select[@com.riversoft.platform.script.ScriptTypes]" value="${vo.paramType}"></wcm:widget></td>
					</tr>
					<tr>
						<th>动态入参(脚本)<br /> <font color="red" tip="true" title="在视图模块中在request中通过[_params]命令字获取.">(提示)</font></th>
						<td><wcm:widget name="paramScript" cmd="codemirror[groovy]" value="${vo.paramScript}"></wcm:widget></td>
					</tr>
					<tr>
						<th>功能点</th>
						<td><wcm:widget name="pri" cmd="pri{required:true}" value="${vo.pri}"></wcm:widget></td>
					</tr>
				</table>

				<div class="ws-bar">
					<button icon="disk" type="submit">保存</button>
				</div>
			</form>
		</div>
	</div>
</div>
<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
