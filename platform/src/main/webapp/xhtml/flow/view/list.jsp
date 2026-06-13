<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');
		
		var $queryForm = null;
		if ('${_form}' != '') {
			$queryForm = $('#${_form}');
		} else {
			$queryForm = $('form[query=true]', $main);
		}

		//回调
		var invokeCallback = function() {
			if ($.isFunction(Core.fn($zone, 'callback'))) {
				Core.fn($zone, 'callback')();
			}
		}

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

		//删除草稿
		Core.fn($zone, 'removeOrder', function(ordId) {
			Ui.confirm('${wpf:lan("#:zh[是否确认删除订单]:en[Whether confirm delete the order]#")}[' + ordId + ']?', function() {
				Ajax.json('${_acp}/removeOrder.shtml', function(json) {
					Ui.msg($msg, json.msg);
					invokeCallback();
				}, {
					errorZone : $msg.attr('id'),
					data : {
						_main : '${param._main}',
						_ORD_ID : ordId
					}
				});
			});
		});

		//处理任务
		Core.fn($zone, 'invokeTask', function(ordId) {
			var $tab;
			if ($tabs != null) {
				$tab = Ajax.tab($tabs, '${_acp}/form.shtml', {
					title : '${wpf:lan("#:zh[任务订单]:en[Task order]#")}',
					minWidth : 1024,
					data : {
						_main : '${param._main}',
						_params : $('#${_zone}_params').val(),
						_ORD_ID : ordId,
						ordFlag : 1
					}
				});
			} else {
				$tab = Ajax.win('${_acp}/form.shtml', {
					title : '${wpf:lan("#:zh[任务订单]:en[Task order]#")}',
					minWidth : 1024,
					data : {
						_main : '${param._main}',
						_params : $('#${_zone}_params').val(),
						_ORD_ID : ordId,
						ordFlag : 1
					}
				});
			}
			Core.fn($tab, 'callback', function() {
				invokeCallback();
			});
		});

		//查看任务
		Core.fn($zone, 'showTask', function(ordId) {
			var $tab;
			if ($tabs != null) {
				$tab = Ajax.tab($tabs, '${_acp}/detail.shtml', {
					title : '${wpf:lan("#:zh[查看订单]:en[View order]#")}',
					minWidth : 1024,
					data : {
						_main : '${param._main}',
						_params : $('#${_zone}_params').val(),
						_ORD_ID : ordId,
						ordFlag : 1
					}
				});
			} else {
				$tab = Ajax.win('${_acp}/detail.shtml', {
					title : '${wpf:lan("#:zh[查看订单]:en[View order]#")}',
					minWidth : 1024,
					data : {
						_main : '${param._main}',
						_params : $('#${_zone}_params').val(),
						_ORD_ID : ordId,
						ordFlag : 1
					}
				});
			}
			Core.fn($tab, 'callback', function() {
				invokeCallback();
			});
		});

		//创建订单
		Core.fn($zone, 'createOrder', function() {
			var $tab;
			if ($tabs != null) {
				$tab = Ajax.tab($tabs, '${_acp}/form.shtml', {
					title : '${wpf:lan("#:zh[创建订单]:en[Create order]#")}',
					minWidth : 1024,
					data : {
						_main : '${param._main}',
						_params : $('#${_zone}_params').val(),
						ordFlag : 1
					}
				});
			} else {
				$tab = Ajax.win('${_acp}/form.shtml', {
					title : '${wpf:lan("#:zh[创建订单]:en[Create order]#")}',
					minWidth : 1024,
					data : {
						_main : '${param._main}',
						_params : $('#${_zone}_params').val(),
						ordFlag : 1
					}
				});
			}
			Core.fn($tab, 'callback', function() {
				invokeCallback();
			});
		});

		//查看
		$('button[name=showTask]', $zone).click(function() {
			var ordId = $(this).val();
			Core.fn($zone, 'showTask')(ordId);
		});

		//处理
		$('button[name=invokeTask]', $zone).click(function() {
			var ordId = $(this).val();
			Core.fn($zone, 'invokeTask')(ordId);
		});

		//删除
		$('button[name=removeOrder]', $zone).click(function() {
			var ordId = $(this).val();
			Core.fn($zone, 'removeOrder')(ordId);
		});

		//新建
		$('button[name=createOrder]', $zone).click(function() {
			Core.fn($zone, 'createOrder')();
		});
		
		//导出
		Core.fn($zone, 'download', function() {

			Ajax.win('${_acp}/downloadSettingZone.shtml', {
				title : '${wpf:lan(title)}[${wpf:lan("#:zh[导出]:en[Export]#")}]',
				minHeight : 400,
				minWidth : 500,
				data : {
					_params : $('#${_zone}_params').val()
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
							type : $('input[name=type]:radio:checked:first', $form).val()
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
							Ajax.download($form.attr('action') + '?' , {
								data : json
							});
							$this.dialog("close");
							Ajax.loadingWin(random);
						});
					}
				} ]
			});
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
            
			var keys = new Array();
			$.each($('#${_zone}_edit_form input:checked[name=_keys]'), function() {
				var val = $(this).val();
				keys.push(val);
			});
			
			if (openType == 1) {//tab
				var $page;
				try {
					$page = Ajax.tab(Ui.getTab(), url, {
						title : title,
						data : {
							_main : '${param._main}',
							_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
							_keys : keys
						}
					});
				} catch (e) {
					//无法创建tab则弹出窗口
					$page = Ajax.win(url, {
						minWidth : 1024,
						title : title,
						data : {
							_main : '${param._main}',
							_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
							_keys : keys
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
						_main : '${param._main}',
						_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
						_keys : keys
					}
				});
				//设置回调
				Core.fn($page, 'callback', function() {
					Core.fn($zone, 'callback')();
				});
			} else if (openType == 3) {//msg
				Ajax.post($msg, url, {
					data : {
						_main : '${param._main}',
						_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
						_keys : keys
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
						_main : '${param._main}',
						_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
						_random : random ,
						_keys : keys
					}
				});
				Ajax.loadingWin(random);
			} else if (openType == 5) {//print
				Ajax.win(url, {
					outFlag : true,
					minWidth : 1024,
					title : title,
					data : {
						_main : '${param._main}',
						_params : $('textarea[paramsFor="' + id + '"]', $zone).val(),
						_keys : keys
					},
					buttons : [ {
						text : '取消',
						icons : {
							primary : "ui-icon-cancel"
						},
						click : function() {
							$(this).dialog("close");
						}
					}, {
						text : '打印',
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

<form id="${_zone}_edit_form">
<textarea style="display: none;" name="_params" id="${_zone}_params">${param._params}</textarea>
<div class="ws-scroll">
	<%-- 数据表格 --%>
	<table class="ws-table" form="${_form}">
		<tr>
			<%--pkScript空时没有外链按钮 --%>
			<th check="true"></th>
			<th style="width: 50px; min-width: 50px;">${wpf:lan("#:zh[操作]:en[Operation]#")}</th>
			<c:set var="fieldPriCache" value="${null}"/>
			<c:forEach items="${config.listFields}" var="field">
				<c:if test="${wpf:checkExt(field.pri,wcm:map(null,'mode',1))}">
					<c:set var="fieldPriCache" value="${wcm:map(fieldPriCache,field.pri.priKey,true)}"/>
					<th field="${field.sortField}" style="${wcm:widget('style[min-height;text-align;background;color;font-weight;min-width(60)]',field.style)}">${wpf:lan(field.busiName)}</th>
				</c:if>
			</c:forEach>
		</tr>

		<c:forEach items="${dp.list}" var="vo" varStatus="status">
			<c:set var="fo" value="${wflow:fo(vo)}" />
			<%-- 数据准备 --%>
			<c:set var="context" value="${wcm:map(wcm:map(null,'mode',1),'vo',vo)}" />
			<c:set var="context" value="${wcm:map(context,'fo',fo)}" />
			<!-- 数据展示准备处理器 -->
			
			<c:forEach items="${config.table.prepareExecs}" var="exec">
				<c:set var="context" value="${wcm:map(context,exec.var,(wpf:script(exec.execType,exec.execScript,context)))}" />
			</c:forEach>
			<tr>
			    <td check="true" value="${fo.ordId}"></td>
				<td class="ws-group"><c:forEach items="${config.itemBtns}" var="btn">
						<c:choose>
							<c:when test="${btn.name!=null}">
								<c:choose>
									<%-- 处理 --%>
									<c:when test="${btn.name=='invokeTask'&&wflow:checkTask(vo)}">
										<button type="button" icon="${btn.icon}" text="false" name="${btn.name}" value="${fo.ordId}">${wpf:lan(btn.busiName)}</button>
									</c:when>
									<%-- 删除 --%>
									<c:when test="${btn.name=='removeOrder'&&wflow:checkRemove(vo)}">
										<button type="button" icon="${btn.icon}" text="false" name="${btn.name}" value="${fo.ordId}">${wpf:lan(btn.busiName)}</button>
									</c:when>
									<%-- 查看 --%>
									<c:when test="${btn.name=='showTask'}">
										<button type="button" icon="${btn.icon}" text="false" name="${btn.name}" value="${fo.ordId}">${wpf:lan(btn.busiName)}</button>
									</c:when>
								</c:choose>
							</c:when>
							<c:otherwise>
								<c:if test="${wpf:checkExt(btn.pri,context)}">
									<button id="${_zone}_itembtn_${btn.id}_${status.index}" name="configBtn" type="button" title="${btn.busiName}" icon="${btn.icon}" text="false" confirmMsg="${btn.confirmMsg}"
										openType="${btn.openType}" value="${btn.action}">${wpf:lan(btn.busiName)}</button>
									<textarea style="display: none;" paramsFor="${_zone}_itembtn_${btn.id}_${status.index}"><wpf:script script="${btn. paramScript}" type="${btn.paramType}" context="${context}" /></textarea>
								</c:if>
							</c:otherwise>
						</c:choose>
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
							<strong>${wpf:lan("#:zh[没有匹配的流程数据]:en[No matching workflow records]#")}</strong>
							<span>${wpf:lan("#:zh[当前查询条件没有返回流程记录。请调整筛选条件，或点击“重置条件”后重新查询。]:en[The current query returned no workflow records. Adjust filters, or reset the query and search again.]#")}</span>
						</div>
					</td>
				</tr>
			</c:if>

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

<wcm:page dp="${dp}" form="${_form}" defLimit="${pageLimit}" params="${_zone}_params" />
</form>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
