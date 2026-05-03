<%@ page language="java" pageEncoding="UTF-8"%>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/common.jsp"%>
<%@ include file="/include/html_head.jsp"%>

<!--[if lt IE 8]> 
	<script type="text/javascript">
		//判断浏览器版本
		var browserMsg = '${browserMsg}';
		var browserUrl = '${browserUrl}';
		alert(browserMsg);
		if (browserUrl != '') {
			Ajax.jump(browserUrl);
		}
	</script>
<![endif]-->

<script src="http://res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js"></script>
<script type="text/javascript">
	$(function() {
		var $zone = $('#${_zone}');

		setTimeout(function() {//居中
			$('div.frame.login').fadeIn(100).position({
				of : $(document),
				my : "center middle",
				at : "center middle"
			});
		}, 500);

		var errorCount = Number('${errorCount}');
		var allowErrorCount = Number('${allowErrorCount}');
		var initRandom = function() {
			if (errorCount >= allowErrorCount) {
				$('.random_code', $zone).show();
			}
			$('#${_zone}_random_code').click();
		};

		$('#${_zone}_random_code').click(
				function() {
					var now = new Date();
					var $img = $(this).find('img');
					$img.attr('src', '${_acp}/randomImg.shtml?_tmp=' + now.getFullYear() + now.getMonth() + now.getDay() + now.getHours() + now.getMinutes() + now.getSeconds() + now.getMilliseconds()
							+ Math.random());
				});

		$('button[name=login]', $zone).bind('click', function() {
			var $form = $('form', $zone);
			var $btn = $(this);
			Ajax.form('${_zone}_msg', $form, {
				dataType : 'json',
				successFn : function(json) {
					if (json.flag) {
						if (json.redirectUrl) {
							window.location.href = json.redirectUrl;
						} else {
							window.location.reload();
						}
					} else {
						Ui.msg('${_zone}_msg', json.msg, 'error');
						errorCount = json.errorCount;
						initRandom();
					}
				},
				callback : function(flag) {
					if (!flag) {
						//刷新验证码
						initRandom();
					}
				},
				btn : $btn
			});
		});

		$('input', $zone).bind('keydown', function(e) {
			var key = e.which;
			if (key == 13) {
				e.preventDefault();
				$('button[name=login]', $zone).click();
			}
		});

		//微信登录触发
		$('button[name=wx]', $zone).click(function() {
			wxChecking = true;
			var $win = $("#${_zone}_wx");
			try {
				$win.dialog("open");
			} catch (e) {
				$win.dialog({
					modal : true,
					draggable : false,
					title : '微信扫一扫登录',
					resizable : false,
					minWidth : 500,
					minHeight : 400
				});
				new WxLogin({
					id : '${_zone}_wx_qrcode',
					appid : '${wxWebAppId}',
					scope : 'snsapi_login',
					redirect_uri : 'http://${wxDomain}/frame/LoginAction/wxLogin.shtml',
					state : new Date().getTime()
				});
			}
			$('span.ui-dialog-title', $win.prev()).addClass('ext');
		});

		//初始化验证码
		initRandom();
	});
</script>

<div id="${_zone}_wx" style="display: none; text-align: center;" class="wx_win">
	<div id="${_zone}_wx_qrcode"></div>
</div>

<%-- 顶部bar  --%>
<div class="frame topbar ui-widget-header">
	<div class="left">
		<span style="color: red; font-weight: bold;">${wpf:lan("#:zh[请先登录.]:en[Please login.]#")}</span>
	</div>
</div>

<div class="frame login" style="display: none;">
	<div panel="${wpf:lan(_title)}">
		<div style="text-align: center; margin-bottom: 20px; margin-top: 10px;">
			<c:if test="${logoUrl!=null&&logoUrl!=''}">
				<img src="${logoUrl}" />
			</c:if>
		</div>
		<div id="${_zone}_msg"></div>

		<%--表单 --%>
		<form action="${_cp}/frame/LoginAction/login.shtml" method="post" sync="true">
			<table class="ws-table">
				<tr>
					<th>${wpf:lan("#:zh[用户名]:en[User name]#")}</th>
					<td ><input type="text" name="username" style="width: 200px;" /></td>
				</tr>
				<tr class="last-child">
					<th>${wpf:lan("#:zh[密码]:en[Password]#")}</th>
					<td ><input type="password" name="password" style="width: 200px;" /></td>
				</tr>
				<tr class="random_code" style="display: none;">
					<th>${wpf:lan("#:zh[验证码]:en[Codes]#")}</th>
					<td ><div style="float: left;">
							<input type="text" name="randomcode" style="width: 110px;" />
						</div>
						<div style="margin-left: 120px;">
							<a style="cursor: pointer;" id="${_zone}_random_code" href="javascript:void(0);" alt="点击刷新"> <img alt="点击刷新" style="border-width: 1px; border-style: solid; width: 80px; height: 22px;" /></a>
						</div></td>
				</tr>
			</table>
			<div class="ws-bar">
				<div class="center">
					<button type="button" icon="user.png" text="true" name="login">${wpf:lan("#:zh[登录]:en[login]#")}</button>
				</div>
				<c:if test="${wxQRcode && wxOpenFlag}">
					<div class="right">
						<button type="button" icon="wx.png" text="false" name="wx">微信登录</button>
					</div>
				</c:if>
			</div>
		</form>
	</div>

	<c:if test="${tips!=''}">
		<div style="margin-top: 20px; margin-left: 5px; margin-right: 5px;">${tips}</div>
	</c:if>
</div>

<%-- 底部  --%>
<div class="frame footer ui-widget-header">
	<p>${copyRight}</p>
</div>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
