<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var current = '${currentKey}';//当前菜单
		var domainKey = '${domainKey}';
		var $main = $('[main-zone]');//主刷新区域

		//触发事件
		var openThirdpartFrame = function(src) {
			$main.empty();
			var iframe = document.createElement("iframe");
			iframe.setAttribute("src", src);
			iframe.setAttribute("frameborder", "0");
			iframe.setAttribute("width", "100%");
			iframe.setAttribute("height", "100%");
			iframe.setAttribute("style", "width:100%;height:100%;border:0;");
			$main.append(iframe);
		};

		var clickFunc = function(treeNode, params) {
			if (params == undefined || params == null) {
				params = "";
			}

			var data = {
				_frame_type : 1
			};//来源于菜单
			if (treeNode.params != undefined && treeNode.params != '') {
				data._params = treeNode.params;
			}

			var openType = treeNode.openType;
			var action = treeNode.action;
			if (action != null && action != '') {
				switch (openType) {
				case 0://无操作
					return;
				case 1://ajax刷新
					Ajax.post($main, _cp + action + params, {
						data : data
					});
					break;
				case 2://第三方网页
					openThirdpartFrame(action);
					break;
				default:
					break;
				}
			}
		};

		var $frameMenu = $("#frame-menu");
		var frameMenuSetting = {
			data : {
				simpleData : {
					enable : true,
					idKey : "id",
					pIdKey : "parentId"
				}
			},
			edit : {
				enable : true,
				drag : {
					autoExpandTrigger : true,
					isCopy : false,
					isMove : false,
					inner : false,
					next : false,
					prev : false
				},
				showRemoveBtn : false,
				showRenameBtn : function(treeId, treeNode) {
					var openType = treeNode.openType;
					var action = treeNode.action;
					if (action != null && action != '' && openType == 1) {
						return true;
					}
					return false;
				},
				renameTitle : '${wpf:lan("#:zh[新窗口打开]:en[Open a new window]#")}'
			},
			callback : {
				beforeEditName : function(treeId, treeNode) {
					var openType = treeNode.openType;
					var action = treeNode.action;
					if (action != null && action != '') {
						switch (openType) {
						case 0://无操作
							break;
						case 1://新窗口打开
							Ajax.jump(_cp + '/' + domainKey + '/' + treeNode.id + '.xhtml', true);
							break;
						default:
							break;
						}
					}
					return false;
				},
				onClick : function(event, treeId, treeNode, clickFlagNumber) {
					clickFunc(treeNode);
				}
			}
		};

		var strData = $('textarea', $frameMenu).html();
		var datas = eval("(" + strData + ")");
		var zTree = $.fn.zTree.init($frameMenu, frameMenuSetting, datas);
		$frameMenu.addClass("ztree");
		zTree.expandAll(true);

		var currentNode;
		if (current != '' && (currentNode = zTree.getNodeByParam('id', current))) {//初始化当前菜单
			zTree.selectNode(currentNode);
			clickFunc(currentNode, window.location.search);
		} else {
			Ajax.post($main, '${_acp}/panel.shtml?domain=' + domainKey);
		}
	});
</script>

<ul id="frame-menu">
	<textarea>${wcm:json(menus)}</textarea>
</ul>


<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
