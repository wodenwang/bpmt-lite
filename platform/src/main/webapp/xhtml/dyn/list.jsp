<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');

		var $main, $msg, $tabs;
		if ('${param._main}' != '') {
			$main = $('#${param._main}');
			$msg = $('[name=mainMsgZone]', $main);
			$tabs = $('div[tabs=true]:first', $main);
		} else {
			$main = $zone;
			$msg = $('#${_zone}_msg');
			$tabs = null;
		}
		var $queryForm = null;
		if ('${_form}' != '') {
			$queryForm = $('#${_form}');
		} else {
			$queryForm = $('form[query=true]', $main);
		}

		//设置回调
		if (!$.isFunction(Core.fn($zone, 'callback'))) {
			Core.fn($zone, 'callback', function() {
				var formId = $('.ws-table', $zone).attr('form');
				if (formId != undefined && formId != '') {
					var $form = $('#' + formId);
					$form.submit();
				}
			});
		}

		/**
		 * 提交删除
		 */
		Core.fn($zone, 'del', function(pk) {
			Ui.confirm("${wpf:lan('#:zh[确认删除所选项？]:en[Confirm the delete option?]#')}", function() {
				var form = $('#${_zone}_delete_form');
				Ajax.post($msg, form.attr("action"), {
					callback : function(flag) {
						if (flag) {//调用成功
							Core.fn($zone, 'callback')();
						}
					},
					btn : $('button', form),
					data : {
						_params : $('#${_zone}_params').val(),
						_main : '${param._main}',
						_keys : pk
					}
				});
			});
		});

		/**
		 * 提交删除
		 */
		Core.fn($zone, 'delAll', function() {
			var $checkbox = $('#${_zone}_delete_form input:checked[name=_keys]');
			if ($checkbox.size() < 1) {
				Ui.alert("${wpf:lan('#:zh[请选择至少一项。]:en[Please select at least one.]#')}");
				return;
			}

			Ui.confirm("${wpf:lan('#:zh[确认删除所选项？]:en[Confirm the delete option?]#')}", function() {
				var form = $('#${_zone}_delete_form');
				//滚动到提示区域
				Ajax.form($msg, form, {
					callback : function(flag) {
						if (flag) {//调用成功
							Core.fn($zone, 'callback')();
						}
					},
					btn : $('button', form)
				});
			});
		});

		$('button[name=del]', $zone).click(function() {
			Core.fn($zone, 'del')($(this).val());
		});

		$('button[name=delAll]', $zone).click(function() {
			Core.fn($zone, 'delAll')();
		});

		/**
		 *编辑
		 */
		Core.fn($zone, 'edit', function(pk) {
			var $tab;
			if ($tabs != null) {
				$tab = Ajax.tab($tabs, '${_acp}/updateZone.shtml', {
					title : '${wpf:lan(title)}[${wpf:lan("#:zh[编辑]:en[Edit]#")}]',
					data : {
						_params : $('#${_zone}_params').val(),
						_main : '${param._main}',
						_list : '${_zone}',
						_key : pk
					}
				});
			} else {
				$tab = Ajax.win('${_acp}/updateZone.shtml', {
					title : '${wpf:lan(title)}[${wpf:lan("#:zh[编辑]:en[Edit]#")}]',
					minWidth : 1024,
					data : {
						_params : $('#${_zone}_params').val(),
						_main : '${param._main}',
						_list : '${_zone}',
						_key : pk
					}
				});
			}
			Core.fn($tab, 'callback', function() {
				Core.fn($zone, 'callback')();
			});
		});

		/**
		 * 打开创建表单
		 */
		Core.fn($zone, 'create', function() {
			var $tab;
			if ($tabs != null) {
				$tab = Ajax.tab($tabs, '${_acp}/createZone.shtml', {
					title : '${wpf:lan(title)}[${wpf:lan("#:zh[新增]:en[Add]#")}]',
					data : {
						_params : $('#${_zone}_params').val(),
						_main : '${param._main}',
						_list : '${_zone}',
					}
				});
			} else {
				$tab = Ajax.win('${_acp}/createZone.shtml', {
					title : '${wpf:lan(title)}[${wpf:lan("#:zh[新增]:en[Add]#")}]',
					minWidth : 1024,
					data : {
						_params : $('#${_zone}_params').val(),
						_main : '${param._main}',
						_list : '${_zone}',
					}
				});
			}
			Core.fn($tab, 'callback', function() {
				Core.fn($zone, 'callback')();
			});
		});

		$('button[name=edit]', $zone).click(function() {
			Core.fn($zone, 'edit')($(this).val());
		});

		$('button[name=create]', $zone).click(function() {
			Core.fn($zone, 'create')();
		});

		/**
		 * 展示
		 */
		Core.fn($zone, 'show', function(pk) {
			var $tab;
			if ($tabs != null) {
				$tab = Ajax.tab($tabs, '${_acp}/detail.shtml', {
					title : '${wpf:lan(title)}[${wpf:lan("#:zh[查看]:en[View]#")}]',
					data : {
						_params : $('#${_zone}_params').val(),
						_main : '${param._main}',
						_list : '${_zone}',
						_key : pk
					}
				});
			} else {
				$tab = Ajax.win('${_acp}/detail.shtml', {
					title : '${wpf:lan(title)}[${wpf:lan("#:zh[查看]:en[View]#")}]',
					minWidth : 1024,
					data : {
						_params : $('#${_zone}_params').val(),
						_main : '${param._main}',
						_list : '${_zone}',
						_key : pk
					}
				});
			}
			Core.fn($tab, 'callback', function() {
				Core.fn($zone, 'callback')();
			});
		});

		$('button[name=show]', $zone).click(function() {
			Core.fn($zone, 'show')($(this).val());
		});

		//批量导入
		Core.fn($zone, 'upload', function() {
			Ajax.win('${_acp}/uploadSettingZone.shtml', {
				title : '${wpf:lan(title)}[${wpf:lan("#:zh[导入]:en[Import]#")}]',
				minWidth : 600,
				data : {
					_params : $('#${_zone}_params').val()
				},
				buttons : [ {
					text : '${wpf:lan("#:zh[关闭]:en[Close]#")}',
					click : function() {
						$(this).dialog("close");
					}
				}, {
					text : '${wpf:lan("#:zh[导入]:en[Import]#")}',
					click : function() {
						var $this = $(this);
						var $form = $('form', $this);
						var option = eval('(' + $form.attr("option") + ')');
						option = $.extend({}, {
							callback : function(flag) {
								if (flag) {//调用成功
									//关闭tab
									$this.dialog("close");
									Core.fn($zone, 'callback')();
								}
							}
						}, option);
						Ajax.form($msg, $form, option);
					}
				} ]
			});
		});

		//导出
		Core.fn($zone, 'download', function() {
			//勾选的项
			var keys = new Array();
			$.each($('#${_zone}_delete_form input:checked[name=_keys]'), function() {
				var val = $(this).val();
				keys.push(eval("(" + val + ")"));
			});

			Ajax.win('${_acp}/downloadSettingZone.shtml', {
				title : '${wpf:lan(title)}[${wpf:lan("#:zh[导出]:en[Export]#")}]',
				minHeight : 400,
				minWidth : 500,
				data : {
					_params : $('#${_zone}_params').val(),
					_keys : JSON.stringify(keys)
				},
				buttons : [ {
					text : '${wpf:lan("#:zh[关闭]:en[Close]#")}',
					click : function() {
						$(this).dialog("close");
					}
				}, {
					text : '${wpf:lan("#:zh[导出]:en[Export]#")}',
					click : function() {
						var $this = $(this);
						var $form = $('form:visible', $this);
						var random = Math.random();
						var json = {
							_random : random,
							type : $('input[name=type]:radio:checked:first', $form).val(),
							_keys : JSON.stringify(keys)
						};
						var indexArray = [];
						$('[name=selectKey]:checkbox:checked', $form).each(function() {
							indexArray.push($(this).val());
						});
						json._index = indexArray;
						
						//为了解决传参问题
						var serializeObj={};
				        var array=$queryForm.serializeArray();
				        $(array).each(function(){
				            if(json[this.name]){
				                if($.isArray(json[this.name])){
				                	json[this.name].push(this.value);
				                }else{
				                	json[this.name]=[json[this.name],this.value];
				                }
				            }else{
				            	json[this.name]=this.value;
				            }
				        });

						Ui.confirm('${wpf:lan("#:zh[确认导出?]:en[Confirm the export?]#")}', function() {
							Ajax.download($form.attr('action') + '?', {
								data : json
							});
							$this.dialog("close");
							Ajax.loadingWin(random);
						});
					}
				} ]
			});
		});

		$('button[name=upload]', $zone).click(function() {
			Core.fn($zone, 'upload')();
		});

		$('button[name=download]', $zone).click(function() {
			Core.fn($zone, 'download')();
		});

		//按钮调用
		var _btnInvoke = function($btn) {
			var action = $btn.val();
			var url = _cp + action;
			var title = $btn.attr('title');
			var openType = $btn.attr('openType');//1:tab,2:win,3:msg,4:download
			var id = $btn.attr('id');
			
			var keys = new Array(); //向每个按钮传递主键
			$.each($('#${_zone}_delete_form input:checked[name=_keys]'), function() {
				var val = $(this).val();
				keys.push(eval("(" + val + ")"));
			});
			
			if (openType == 1) {//tab
				var $page;
				try {
					$page = Ajax.tab(Ui.getTab(), url, {
						title : title,
						data : {
							_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
							_keys : JSON.stringify(keys)
						}
					});
				} catch (e) {
					//无法创建tab则弹出窗口
					$page = Ajax.win(url, {
						minWidth : 1024,
						title : title,
						data : {
							_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
							_keys : JSON.stringify(keys)
						}
					});
				}
				//设置回调
				Core.fn($page, 'callback', function() {
					Core.fn($zone, 'callback')();
				});
			} else if (openType == 2) {//win
				var $page = Ajax.win(url, {
					minWidth : 1024,
					title : title,
					data : {
						_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
						_keys : JSON.stringify(keys)
					}
				});
				//设置回调
				Core.fn($page, 'callback', function() {
					Core.fn($zone, 'callback')();
				});
			} else if (openType == 3) {//msg
				Ajax.post($msg, url, {
					data : {
						_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
						_keys : JSON.stringify(keys)
					},
					callback : function(flag) {
						if (flag) {
							Core.fn($zone, 'callback')();
						}
					}
				});
			} else if (openType == 4) {//download
				var random = Math.random();
				Ajax.download(url, {
					data : {
						_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
						_random : random,
						_keys : JSON.stringify(keys)
					}
				});
				Ajax.loadingWin(random);
			} else if (openType == 5) {//print
				Ajax.win(url, {
					outFlag : true,
					minWidth : 1024,
					title : title,
					data : {
						_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
						_keys : JSON.stringify(keys)
					},
					buttons : [ {
						text : '${wpf:lan("#:zh[取消]:en[Cancel]#")}',
						icons : {
							primary : "ui-icon-cancel"
						},
						click : function() {
							$(this).dialog("close");
						}
					}, {
						text : '${wpf:lan("#:zh[打印]:en[Print]#")}',
						icons : {
							primary : "ui-icon-print"
						},
						click : function() {
							var $this = $(this);
							var iframeId = $('iframe', $this).attr('id');
							document.getElementById(iframeId).contentWindow.print();
						}
					} ]
				});
			}
		};

		//自定义按钮
		$('button[name=configBtn]', $zone).click(function() {
			var $btn = $(this);
			var confirmMsg = $btn.attr("confirmMsg");
			if (confirmMsg != undefined && confirmMsg != '') {
				Ui.confirm(confirmMsg, function() {
					_btnInvoke($btn);
				});
			} else {
				_btnInvoke($btn);
			}
		});

	});
</script>

<div id="${_zone}_msg"></div>

<form action="${_acp}/delete.shtml" method="post" id="${_zone}_delete_form">
	<textarea style="display: none;" name="_params" id="${_zone}_params">${param._params}</textarea>
	<textarea style="display: none;" name="_main">${param._main}</textarea>

	<div class="ws-scroll">
		<%--数据表格 --%>
		<table class="ws-table" form="${_form}" params="${_zone}_params">
			<thead>
				<tr>
					<th check="true"></th>
					<th style="width: 50px; min-width: 50px;">${wpf:lan("#:zh[操作]:en[Operation]#")}</th>
					<c:set var="fieldPriCache" value="${null}"/>
					<c:forEach items="${config.listFields}" var="field">
						<c:if test="${wpf:checkExt(field.pri,wcm:map(null,'mode',1))}">
							<c:set var="fieldPriCache" value="${wcm:map(fieldPriCache,field.pri.priKey,true)}"/>
							<th field="${field.name!=null?field.name:field.sortField}" style="${wcm:widget('style[min-height;text-align;background;color;font-weight;min-width(60)]',field.style)}">${wpf:lan(field.busiName)}</th>
						</c:if>
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${dp.list}" var="vo" varStatus="status">
					<%-- 数据准备 --%>
					<c:set var="context" value="${wcm:map(wcm:map(null,'mode',1),'vo',vo)}" />
					<%-- 获取left join绑定上下文 --%>
					<c:forEach items="${config.table.parents}" var="parent">
						<c:set var="context" value="${wcm:map(context,parent.var,wpf:pixelVO(parent.var,vo))}" />
					</c:forEach>
					<!-- 数据展示准备处理器 -->
					<c:forEach items="${config.table.prepareExecs}" var="exec">
						<c:set var="context" value="${wcm:map(context,exec.var,(wpf:script(exec.execType,exec.execScript,context)))}" />
					</c:forEach>
					<tr>
						<td check="true" value="${fn:replace(wcm:jsonKey(vo,config.keysArray),'\\\\"','')}"></td>
						<td class="ws-group"><c:forEach items="${config.itemBtns}" var="btn">
								<c:if test="${wpf:checkExt(btn.pri,context)}">
									<c:choose>
										<c:when test="${btn.name!=null}">
											<button type="button" icon="${btn.icon}" text="false" name="${btn.name}" value="${fn:replace(wcm:jsonKey(vo,config.keysArray),'\\\\"','')}">${wpf:lan(btn.busiName)}</button>
										</c:when>
										<c:otherwise>
											<button id="${_zone}_itembtn_${btn.id}_${status.index}" name="configBtn" type="button" title="${wpf:lan(btn.busiName)}" icon="${btn.icon}" text="false" confirmMsg="${btn.confirmMsg}"
												openType="${btn.openType}" value="${btn.action}">${wpf:lan(btn.busiName)}</button>
											<textarea style="display: none;" paramsFor="${_zone}_itembtn_${btn.id}_${status.index}"><wpf:script script="${btn. paramScript}" type="${btn.paramType}" context="${context}" /></textarea>
										</c:otherwise>
									</c:choose>
								</c:if>
							</c:forEach></td>
						<c:forEach items="${config.listFields}" var="field">
							<c:if test="${fieldPriCache[field.pri.priKey]==true}">
								<td class="center" style="${wcm:widget('style[min-height;min-width(60)]',field.style)}"><wpf:script script="${field.contentScript}" type="${field.contentType}" context="${context}" /></td>
							</c:if>
						</c:forEach>
					</tr>
				</c:forEach>
				<c:if test="${fn:length(dp.list) < 1}">
					<tr class="bpmt-empty-table-row">
						<td colspan="99">
							<div class="bpmt-state bpmt-state-empty">
								<strong>${wpf:lan("#:zh[没有匹配的数据]:en[No matching records]#")}</strong>
								<span>${wpf:lan("#:zh[当前查询条件没有返回记录。请调整筛选条件，或点击“重置条件”后重新查询。]:en[The current query returned no records. Adjust filters, or reset the query and search again.]#")}</span>
							</div>
						</td>
					</tr>
				</c:if>
			</tbody>
			<tr class="bpmt-summary-button-row">
				<th class="ws-bar" colspan="99"><c:forEach items="${fn:split('left,center,right',',')}" var="styleClass">
						<div class="ws-group ${styleClass}">
							<c:forEach items="${config.summaryBtns}" var="btn">
								<c:if test="${btn.styleClass==styleClass}">
									<c:if test="${wpf:check(btn.pri)}">
										<c:choose>
											<c:when test="${btn.name!=null}">
												<button type="button" icon="${btn.icon}" text="true" name="${btn.name}">${wpf:lan(btn.busiName)}</button>
											</c:when>
											<c:otherwise>
												<button id="${_zone}_summarybtn_${btn.id}" name="configBtn" type="button" icon="${btn.icon}" title="${btn.busiName}" text="true" confirmMsg="${btn.confirmMsg}" openType="${btn.openType}"
													value="${btn.action}">${wpf:lan(btn.busiName)}</button>
												<textarea style="display: none;" paramsFor="${_zone}_summarybtn_${btn.id}"><wpf:script script="${btn. paramScript}" type="${btn.paramType}" /></textarea>
											</c:otherwise>
										</c:choose>
									</c:if>
								</c:if>
							</c:forEach>
						</div>
					</c:forEach></th>
			</tr>
		</table>
	</div>

	<%-- 分页  --%>
	<wcm:page dp="${dp}" form="${_form}" params="${_zone}_params" defLimit="${pageLimit}" />

</form>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
