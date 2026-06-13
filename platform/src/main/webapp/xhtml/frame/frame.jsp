<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<style type="text/css">
/*重写edit按钮的图标样式*/
#frame-menu-panel .ztree li span.button.edit {
	margin-right: 2px;
	background-image: url("${_cp}/css/icon/monitor_add.png");
	background-position: 0px 0px;
	vertical-align: top;
	*vertical-align: middle;
}
</style>

<!--[if lt IE 8]> 
	<script type="text/javascript">
		//判断浏览器版本
		var browserMsg = '${browserMsg}';
		var browserUrl = '${browserUrl}';
		if (browserMsg != '') {
			alert(browserMsg);
		}
		if (browserUrl != '') {
			Ajax.jump(browserUrl);
		}
	</script>
<![endif]-->

<script type="text/javascript">
	$(function() {
		//新的safari回退功能很好用,不需要额外提示
		if (!(/Safari/.test(navigator.userAgent) && /Apple Computer/.test(navigator.vendor))) {
			window.onbeforeunload = function() {
				return "${wpf:lan('#:zh[是否离开当前域?]:en[Whether to leave the current domain?]#')}";
			};
		}

		var $zone = $('#${_zone}');

		$('.top-menu', $zone).find('li').hover(function() {
			$(this).addClass("ui-state-highlight");
		}, function() {
			$(this).removeClass("ui-state-highlight");
		});

		var switchScreen = function(isFullScreen) {
			if (isFullScreen == undefined) {
				isFullScreen = $('.frame.main.fullScreen').size() <= 0;
			}

			if (!isFullScreen) {
				//$('.frame.topbar').show();
				$('.frame.header').show();
				$('.frame.menu').show();
				//$('.frame.footer').show();
				$('.frame.main').removeClass('fullScreen');
			} else {
				//$('.frame.topbar').hide();
				$('.frame.header').hide();
				$('.frame.menu').hide();
				//$('.frame.footer').hide();
				$('.frame.main').addClass('fullScreen');
			}
			try {
				$(window).resize();
			} catch (e) {
				console.log("${wpf:lan('#:zh[无法触发全屏resize函数.]:en[To trigger the full screen resize function.]#')}");
			}

		};

		//全屏
		$('a[name=swidthFullscreen]', $('.top-menu', $zone)).click(function() {
			switchScreen();
		});

		$(document).keyup(function(e) {
			// ESCAPE key pressed
			if (e.keyCode == 27) {
				switchScreen(false);
			} else if (e.keyCode == 120) {
				switchScreen();
			}
		});

		$('a[name=userSetting]', $('.top-menu', $zone)).click(function() {
			Ajax.win('${_acp}/userSetting.shtml', {
				title : "${wpf:lan('#:zh[用户设置]:en[User settings]#')}",
				minWidth : 1024,
				minHeight : 200,
				buttons : [ {
					text : "${wpf:lan('#:zh[关闭]:en[Close]#')}",
					click : function() {
						$(this).dialog("close");
					}
				} ]
			});
		});

		$('a[name=changeGroup]', $('.top-menu', $zone)).click(function() {
			Ajax.win('${_acp}/changeGroup.shtml', {
				title : "${wpf:lan('#:zh[切换组织]:en[Switching organization]#')}",
				minWidth : 400,
				buttons : [ {
					text : "${wpf:lan('#:zh[关闭]:en[Close]#')}",
					click : function() {
						$(this).dialog("close");
					}
				} ]
			});
		});

		$('a[name=logout]', $('.top-menu', $zone)).click(function() {
			Ui.confirm('${wpf:lan("#:zh[确认退出?]:en[Confirm exit?]#")}', function() {
				Ajax.jump('${_cp}/frame/LoginAction/logout.shtml')
			});
		});

		$('a[name=taskPanel]', $('.top-menu', $zone)).click(function() {
			Ajax.win('${_cp}/flow/CommonFlowAction/taskPanel.shtml', {
				title : "${wpf:lan('#:zh[待办事项(快捷处理)]:en[Todo(fast processing)]#')}",
				minWidth : 1024,
				data : {
					_params : "{quickMode:true}"
				},
				buttons : [ {
					text : '${wpf:lan("#:zh[关闭]:en[Close]#")}',
					click : function() {
						$(this).dialog("close");
					}
				} ]
			});
		});
		//定时刷新待办任务数
		var showTaskCount = function() {
			if ($('a[name=taskPanel]', $('.top-menu', $zone)).size() < 1) {
				return;
			}
			Ajax.json('${_cp}/flow/CommonFlowAction/getTaskCount.shtml', function(result) {
				if (result.flag) {
					var userCount = result.userCount;
					var shareCount = result.shareCount;
					var count;
					if (userCount > 0) {
						count = userCount;
					} else {
						count = '*';
					}
					$('a[name=taskPanel]', $('.top-menu', $zone)).find('span[name=taskCount]').remove();
					var $taskCount = $('<span name="taskCount" class="ui-state-highlight" style="color:red;">[' + count + ']</span>');
					$('a[name=taskPanel]', $('.top-menu', $zone)).append($taskCount);
				} else {
					$('a[name=taskPanel]', $('.top-menu', $zone)).find('span[name=taskCount]').remove();
				}
			});

			setTimeout(function() {
				showTaskCount();
			}, 1000 * 60);//60秒执行一次
		};
		showTaskCount();

		//返回顶部
		$('body').UItoTop({
			easingType : 'easeOutQuart'
		});

		//菜单钉
		var $window = $(window);
		var $menu = $('.frame.menu', $zone);
		var $menuTree = $("#${_zone}_menu", $zone);
		var onScrollMenu = function() {
			var windowY = $window.scrollTop();
			var windowH = $window.height();
			var topbarH = $('.frame.topbar', $zone).height();
			var headerH = $('.frame.header', $zone).height();
			var footerH = $('.frame.footer', $zone).height();

			if (windowY >= topbarH + headerH + 5) {
				$menu.css({
					position : 'fixed',
					top : topbarH + 5 + 'px',
					bottom : ''
				});
			} else {
				$menu.css({
					position : 'absolute',
					top : '',
					bottom : ''
				});
			}

			var lineH;
			if (windowY < headerH) {
				lineH = 80 - windowY;
			} else {
				lineH = 80 - headerH - 10;
			}
			$menuTree.css({
				height : windowH - topbarH - headerH - footerH - lineH + 'px',
				overflow : 'auto'
			});
		};
		$window.scroll(onScrollMenu);
		$window.resize(onScrollMenu);
		onScrollMenu();

	});
</script>

<%-- 语言切换菜单 --%>
<c:if test="${languages!=null}">
	<script type="text/javascript">
		$(function() {
			var $zone = $("#${_zone}");
			$("#${_zone}_lan_menu").hide().menu();
			$("#${_zone}_lan_btn").click(function() {
				var $menu = $("#${_zone}_lan_menu").show().position({
					my : "left top",
					at : "left bottom",
					of : this
				});
				$(document).one("click", function() {
					$menu.hide();
				});
				return false;
			});

			$("li[lan]", $zone).click(function() {
				var lan = $(this).attr('lan');
				//AJAX处理切换,成功之后刷新页面
				if (confirm('${wpf:lan("#:en[确认切换语言?]:zh[Confirm switch language?]#")}')) {
					Ajax.json('${_acp}/changeLanguage.shtml', function(json) {
						if (json.flag) {
							Ajax.jump(window.location.href);
						}
					}, {
						data : {
							lanKey : lan
						}
					});
				}
			});
		});
	</script>
	<ul id="${_zone}_lan_menu" style="display: none; width: 120px; position: absolute; z-index: 99999;">
		<c:forEach items="${languages}" var="o">
			<li lan="${o}"><span class="ui-icon ui-icon-disk" style="background-position: 0 0; background-image: url('/css/icon/${o}.png');"></span>${wcm:widget('select[@com.riversoft.platform.translate.Language]',o)}</li>
		</c:forEach>
	</ul>
</c:if>

<%-- 公众号切换菜单 --%>
<c:if test="${!sessionScope.USER.entity && fn:length(visitors)>1}">
	<script type="text/javascript">
		$(function() {
			var $zone = $("#${_zone}");
			$("#${_zone}_visitor_menu").hide().menu();
			$("#${_zone}_visitor_btn").click(function() {
				var $menu = $("#${_zone}_visitor_menu").show().position({
					my : "left top",
					at : "left bottom",
					of : this
				});
				$(document).one("click", function() {
					$menu.hide();
				});
				return false;
			});

			$("li[mpKey]", $zone).click(function() {
				var mpKey = $(this).attr('mpKey');
				var text = $(this).text();
				//AJAX处理切换,成功之后刷新页面
				Ui.confirm('切换到公众号[' + text + ']?', function() {
					Ajax.post('${_zone}', '${_acp}/submitChangeMp.shtml', {
						data : {
							mpKey : mpKey
						},
						callback : function(flag) {
							if (flag) {
								Ajax.jump('${_cp}');
							}
						}
					});
				});
			});
		});
	</script>
	<ul id="${_zone}_visitor_menu" style="display: none; width: 120px; position: absolute; z-index: 99999;">
		<c:forEach items="${visitors}" var="o">
			<li mpKey="${o.MP_KEY}"><span style="float: left;"><img src="${o.HEAD_IMG_URL}" height="16" width="16" border="0" /></span>${wcm:widget('select[$WxMp;mpKey;title]',o.MP_KEY)}</li>
		</c:forEach>
	</ul>
</c:if>

<%-- 顶部bar  --%>
<div class="frame topbar ui-widget-header">
	<div class="left bpmt-session-summary" aria-label="${wpf:lan('#:zh[当前登录信息]:en[Current session]#')}">
		<span class="bpmt-session-item">
			<span class="bpmt-session-label">${wpf:lan("#:zh[用户]:en[User]#")}</span>
			<span class="bpmt-session-value">${sessionScope.USER.busiName}</span>
		</span>
		<span class="bpmt-session-item">
			<span class="bpmt-session-label">${wpf:lan("#:zh[组织]:en[Organization]#")}</span>
			<span class="bpmt-session-value">${sessionScope.GROUP.busiName}</span>
		</span>
		<span class="bpmt-session-item">
			<span class="bpmt-session-label">${wpf:lan("#:zh[角色]:en[Role]#")}</span>
			<span class="bpmt-session-value">${sessionScope.ROLE.busiName}</span>
		</span>
		<span class="bpmt-session-time">${wpf:lan("#:zh[登录时间]:en[Signed in]#")} ${wcm:widget('date[datetime]',sessionScope.DATE)}</span>
	</div>
	<div class="right">
		<ul class="top-menu">

			<c:if test="${languages!=null}">
				<li style="border: 0px;"><a href="javascript:;;" id="${_zone}_lan_btn"><span class="ui-icon ui-icon-disk"
						style="float: left; background-position: 0 0; background-image: url('/css/icon/${currentLanguage}.png');"></span>${wpf:lan("#:zh[Language]:en[语言]#")}</a></li>
			</c:if>

			<c:if test="${taskPanel}">
				<li style="border: 0px;"><a href="javascript:;;" name="taskPanel"><span class="ui-icon ui-icon-star" style="float: left;"></span>${wpf:lan("#:zh[待办]:en[To-do]#")}</a></li>
			</c:if>
			<li style="border: 0px;"><a href="javascript:;;" name="swidthFullscreen" title="${wpf:lan('#:zh[按下F9切换全屏]:en[Press F9 toggles full screen]#')}." tip="true"><span
					class="ui-icon ui-icon-arrowthick-2-ne-sw" style="float: left;"></span>${wpf:lan("#:zh[全屏]:en[Full screen]#")}</a></li>


			<%-- 实体用户才可以设置 --%>
			<c:if test="${sessionScope.USER.entity}">
				<li style="border: 0px;"><a href="javascript:;;" name="changeGroup"><span class="ui-icon ui-icon-person" style="float: left;"></span>${wpf:lan("#:zh[切换]:en[Switch]#")}</a></li>
				<li style="border: 0px;"><a href="javascript:;;" name="userSetting"><span class="ui-icon ui-icon-wrench" style="float: left;"></span>${wpf:lan("#:zh[设置]:en[Option]#")}</a></li>
			</c:if>

			<%-- 非实体才需要切换--%>
			<c:if test="${!sessionScope.USER.entity && fn:length(visitors)>1}">
				<c:choose>
					<c:when test="${sessionScope.USER.wxAvatar!=null}">
						<li style="border: 0px;"><a href="javascript:;;" id="${_zone}_visitor_btn"><span style="float: left;"><img src="${sessionScope.USER.wxAvatar}" height="16" width="16" border="0" /></span>${wcm:widget('select[$WxMp;mpKey;title]',sessionScope.USER.mpKey)}</a></li>
					</c:when>
					<c:otherwise>
						<li style="border: 0px;"><a href="javascript:;;" id="${_zone}_visitor_btn"><span class="ui-icon ui-icon-persion" style="float: left;"></span>${wcm:widget('select[$WxMp;mpKey;title]',sessionScope.USER.mpKey)}</a></li>
					</c:otherwise>
				</c:choose>
			</c:if>
			<li style="border: 0px;"><a href="javascript:;;" name="logout"><span class="ui-icon ui-icon-power" style="float: left;"></span>${wpf:lan("#:zh[退出]:en[Exit]#")}</a></li>
		</ul>
	</div>
</div>

<%-- 头部 --%>
<div class="frame header">
	<div class="left" style="text-align: center;">
		<c:if test="${logoUrl!=null&&logoUrl!=''}">
			<a href="javascript:void(0);" onclick="Ajax.jump('${_cp}');"><img src="${logoUrl}" border="0" /></a>
		</c:if>
	</div>

	<div class="right">
		<c:choose>
			<c:when test="${domains!=null && fn:length(domains) > 0}">
				<div class="ws-group">
					<c:forEach items="${domains}" var="vo">
						<c:set var="icon" value="${vo.icon==null||vo.icon==''?'home':vo.icon}" />
						<c:choose>
							<c:when test="${vo.domainKey == domain.domainKey}">
								<button type="button" icon="${icon}" text="true" class="ui-state-highlight" onclick="Ajax.jump('${_cp}/${vo.domainKey}.xhtml')">${wpf:lan(vo.busiName)}</button>
							</c:when>
							<c:otherwise>
								<button type="button" text="true" icon="${icon}" onclick="Ajax.jump('${_cp}/${vo.domainKey}.xhtml')">${wpf:lan(vo.busiName)}</button>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</div>
			</c:when>
			<c:otherwise>
				<div class="ws-group">
					<button type="button" text="true" icon="home" disabled="disabled">${wpf:lan("#:zh[无可用域]:en[No use domain]#")}</button>
				</div>
			</c:otherwise>
		</c:choose>
	</div>
</div>

<%-- 中间 --%>
<div class="frame center">
	<%-- 左侧菜单栏目 --%>
	<div class="frame menu">
		<div id="frame-menu-panel" class="ui-widget-content ui-corner-all ws-panel">
			<h3 class="ui-widget-header ui-corner-all" style="padding-left: 5px !important;">
				<span class="ui-icon ui-icon-home" style="float: left; margin-right: 5px;"></span>${wpf:lan("#:zh[菜单]:en[Menu]#")}
			</h3>
			<div id="${_zone}_menu" init="${_acp}/menu.shtml?domain=${domain.domainKey}&menu=${menuKey}"></div>
		</div>
	</div>

	<%-- 主体区域 --%>
	<div class="frame main" main-zone></div>
</div>

<%-- 底部  --%>
<div class="frame footer ui-widget-header">
	<p>${copyRight}</p>
</div>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
