<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');
		var $tree = $("#${_zone}_tree", $zone);
		var treeSetting = {
			view : {
				fontCss : function(treeId, node) {
					return node.font ? eval('(' + node.font + ')') : {};
				},
				nameIsHTML : true
			},
			data : {
				key : {
					name : "name",
					title : "title"
				},
				simpleData : {
					enable : true,
					idKey : "id",
					pIdKey : "parentId"
				}
			},
			check : {
				enable : true
			},
			callback : {
				onClick : function(event, treeId, treeNode) {
					if (treeNode.pri == undefined) {
						return;
					}
					var $priZone = $(document.getElementById('${_zone}_pri_' + treeNode.pri.priKey));
					$('button', $priZone).click();
				}
			}
		};

		var strData = $('textarea', $tree).html();
		var datas = eval("(" + strData + ")");
		var zTree = $.fn.zTree.init($tree, treeSetting, datas);
		$tree.addClass("ztree");
		zTree.expandAll(true);
		function priZone(treeNode) {
			return $(document.getElementById('${_zone}_pri_' + treeNode.pri.priKey));
		}

		//处理表单
		$('form', $zone).submit(function(event) {
			var $this = $(this);
			event.preventDefault();
			$(':checkbox', $this).prop("checked", false);
			//提交之前把树勾选的项都勾上
			var array = zTree.getCheckedNodes(true);
			$.each(array, function(i, treeNode) {
				var $priZone = priZone(treeNode);
				$(':checkbox', $priZone).prop("checked", true);
			});

			Ajax.form('${_zone}_pris_msg', $this, {
				confirmMsg : '确认保存设置?'
			});
		});

	});
</script>

<ul id="${_zone}_tree">
	<textarea>${menu}</textarea>
</ul>

<form action="${_acp}/submitPris.shtml" sync="true">
	<div id="${_zone}_pris_msg"></div>
	<input type="hidden" name="groupId" value="${groupId}" />
	<c:forEach items="${pris}" var="pri">
		<div id="${_zone}_pri_${pri.priKey}" style="display: none;">
			<c:if test="${!pri.scriptOnly}">
				<wcm:widget name="relate" cmd="prigroup[${groupId};${pri.priKey}]"></wcm:widget>
			</c:if>
			<input type="checkbox" value="${pri.priKey}" name="priKey" />
		</div>
	</c:forEach>

	<div class="ws-bar" style="margin-top: 10px;">
		<div class="center">
			<button icon="disk" type="submit">保存</button>
		</div>
	</div>
</form>
<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
