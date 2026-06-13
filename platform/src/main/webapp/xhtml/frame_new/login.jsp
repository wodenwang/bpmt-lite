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

<c:set var="baseCp" value="${_cp}/xhtml/frame_new" />
<link rel="stylesheet" href="${baseCp}/css/main.css">

<script src="https://res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js"></script>
<script type="text/javascript">
	
	$(function(){
		var $zone = $('#${_zone}');
		
		$(window).resize(function(){
			var $login_container = $('#logo-container');
			$login_container.css({'height':$(window).height()});					
		}).trigger('resize');		
		
		var $login = $('#login'),
			$user = $('#user'),
			$password = $('#password'),
			$verification_group = $('#verification-group'),
			allowErrorCount = new Number('${allowErrorCount}'),
			errorCount = new Number('${errorCount}'),			
			sw = true;  //动画开关
			
		var initRandom = function() {
			if (errorCount >= allowErrorCount) {
				$verification_group.show();
				
			}else{
				$verification_group.hide(); 
			}
			$('#${_zone}_random_code').click();
		};
		initRandom();	

		//获焦事件 
		$('#user,#password').on('focus',function(){
			$(this).next().html('').css('display','none');
		})
		//失焦事件
		$user.on('blur',function(){
			if(checkNull($(this),'用户名不能为空')){}
		});
		$password.on('blur',function(){
			if(checkNull($(this),'密码不能为空')){}
		});

		//点击登录操作
		$login.on('click',function(event){
			event.preventDefault();
			if(sw){
				reload($(this));
			}else{
				return;
			}
		})
		$login.on('keydown',function(e){
			if(e.which == 13 || e.which == 32){
				e.preventDefault();
				$(this).trigger('click');
			}
		})

		//键盘回车登录操作
		$(document).on('keypress',function(e){
			if(e.which == 13){
				if(sw){
					reload();
				}else{
					return;
				}				
			}
		});

		//验证码改变事件
		$("#verification-img").on('click',function(){
			$(this).attr('src','${cp}/frame/LoginAction/randomImg.shtml?_tmp=' + new Date().getTime());	
		})
		
		//微信登录事件
		$('button[name=wx]', $zone).on('click',function() {
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
					redirect_uri : '${_cp}/frame/LoginAction/wxLogin.shtml',
					state : new Date().getTime()
				});
			}
			$('span.ui-dialog-title', $win.prev()).addClass('ext');
		});		

		//登录事件
		function reload($btn){
			var t = true;
				t = t && checkNull($user,'用户名不能为空');
				t = t && checkNull($password,'密码不能为空');
			if(t){
				var $form = $('form');
				Ajax.form('${_zone}_msg', $form, {
					dataType : 'json',
					successFn : function(d) {
						if (d.flag) {
							if (d.redirectUrl) {
								window.location.href = d.redirectUrl;
							} else {
								window.location.reload();
							}
						} else {
							if(d.errorCount >= allowErrorCount){

								$verification_group.show();

								//$('#verification-img').attr('src','${cp}/frame/LoginAction/randomImg.shtml?_tmp=' + new Date().getTime());
							}

							//避免快速点击动画错乱
							if(sw){
								sw = false;
								$('#content').html(d.msg);
								$('.top-tips').animate({'top':0}, 500,function(){
									setTimeout(function(){
										$('.top-tips').animate({'top':'-100%'}, 500,function(){
											sw = true;
										});
									},1000)
								});	
							}
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
			}		
		}
		//显示提示
		function showTips(element,msg){
			element.next().html(msg).css('display','inline-block');
		}
		//检查是否为空
		function checkNull(element,msg){
			if(element.val() == ''){
				showTips(element,msg)
				return false;
			}else{
				return true;
			}
		}		
	})
	
</script>

<div id="${_zone}_wx" style="display: none; text-align: center;" class="wx_win">
	<div id="${_zone}_wx_qrcode"></div>
</div>

<div class="tc-login-wrap tc-box-sizing" id="logo-container" >
	<div class="tc-login-box">
	
   		<div class="logo-box">
   			<div class="item">
    			<div class="item-box">
    				<img src="${logoUrl}" alt="">
    			</div>    				
   			</div>
   		</div>	
   		
   		<div class="slogan">
   			<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAM0AAAAiBAMAAAAOgEANAAAAMFBMVEUAAAD///////////////////////////////////////////////////////////87TQQwAAAAD3RSTlMAd7tEqhHMM+7dmSJmVYgWUmgDAAAD/klEQVRIx62Wy+8LURTHz6iqtJX2eoZEpi4RLLRyiQXiFSuLTkKEDQ2xsJB6hh3x2hZbiyEsJCSEjRVWtirs8RfUaNVM/TjmPjpzZn79qYZv8uvMnN+59zP3nu+9d+A/K3NqVPRMe4IuCskucnVEn3O+6qn59w35e+kLSclecfSNW5mAc+k7UFloVNPPRYR9YrNNOTn8ZFJfT8DZ3k2Oh+9kS33GmEO5TcoBVzd5EZBYpzyO821aaNbX+D6vOK76bQyb9NTlyRRpUyWcfWIDU1pCEt5++SNnls82vgY7wSkqTgYrM3DqaET6AbvP+WpE/BSHDvWfUubb62AP2Lv3IgqeVzPqOzNwOprC+UrKMewd+nE+13O0znQy14NXHoQ+eHscksougTSHluNnKt0O8Mp6xpY7xkspv5U8yPuOfRPe/oqabDLXP3J+ANWm8FWRpGTw6E5WH7CFQ87cn1BoPT3zGg70iDWUghXOHziJsue/Qr2S5NQgRKurkrUX4IK62xKVDDGe6xOn/oZj96B5dxqnc1NdjR7U0nYMFgjhHtu2rFUBaE0Rzkw2LrSuA5ancaqV8BpZDqfAYkwIakGZo/8u4t2RHJty8tguYJtyCuFDJnxPC4khjSdPmsDsvvSu5HwCyLW+jOds78kBUA7oCvO1MSfzGM5or5tSWGj6UQ2fNcZyCq0uZHxIcOhSpjorlkXWQP6ZNT3GzD46lnMAuwALkhz3Vujpo9yNOB/MKIgFk2tsPKeOXVMSkq9rU/pFOiAcfXObVw1nP+d7xnEsdLumGWNDe1Vvao5HOIfXcL7KiTg5+WLNhsrbjujptPtCCIdyiD+3aw7di+tfII8AR6YIpykTyhEHsKzH7UM4nqrhyJwK4TRiG9QM5yhvYjnevqu4FrEbcXZAmMApx92h15h6lxLh7CA1qaTPUgufktBH1PpOCoZtAMLRffbY4l+KQ+aN1plyLmkOEFmM1YN7rNOgHDy6lnJs1BpQDvVBmlP6STn06MgC5aTqk2NsodvnfOVfczzFeS+E2JL+YqCcW2wxtjPopPfJCTlSfvTx90h2QRvofbWWwZHnWGnwpJzgWFyqdWVtZEDK6ZFzfAwnt2v9cmF7Zr2UEO8+F6K5W2xgr0EFYl2mnGC5ICfxGUSPTlD9MthdYWFcxzwaqSNhDg4O6yezGObt4kqrmFz9hJPaMs8hDmp0YWt/QT4Yjmf+ahkw30jFoB0dsHdgtOYqTp9EzP4QLKKcg8rDc+j7nLUHIpqFrFDaDDMp743wdRGfP0R8RDgFsZUtkp9XSQtNoJrs10/GLB+yb2L/PRsaqfM9xZlUS1LjkT28vEZD03csmOPBv8oZHT6diBevyt/fdCthe23wZDsAAAAASUVORK5CYII=" alt="">
   		</div>  
   		
   		<div class="main">
   			<h1>${_title}</h1>
   			<form  action="${_cp}/frame/LoginAction/login.shtml" method="post" sync="true">
    			<div class="group">
					<label for="user" aria-label="用户名"><i class="icon icon-user"></i></label>
					<input type="text" name="username" placeholder="请输入用户名" id="user" autocomplete="username">
    				<i class="tips"></i>
    			</div>
    			<div class="group">
					<label for="password" aria-label="密码"><i class="icon icon-lock"></i></label>
					<input type="password" name="password" placeholder="请输入密码" id="password" autocomplete="current-password">
    				<i class="tips"></i>
    			</div>  
				<div class="group tc-hide" id="verification-group">
					<label for="verification" aria-label="验证码"><i class="icon icon-ver"></i></label>
					<input type="text" name="randomcode" placeholder="请输入验证码" id="verification" autocomplete="off">
					<div class="ver-pic-wrap" style="position:absolute;width:96px;height:auto;top:50%;margin-top: -13px;right: 10px;z-index: 10;line-height:0;">
						<img src="${cp}/frame/LoginAction/randomImg.shtml?_tmp=abd" alt="验证码，点击刷新" id="verification-img">
					</div>
					<i class="tips"></i>
				</div>       			 
   			</form>
				<a href="javascript:;" class="login-btn" id="login" role="button" tabindex="0" aria-label="登录">登录<div id="${_zone}_msg"></div></a>
   			<div>
   				<p>${tips}</p>
   			</div>
  	
			<div class="top-tips" style="width:100%;position:fixed;height: auto;left: 0;top:-100%;text-align:center;border-radius: 0 0 5px 5px;">
				<p id="content" style="padding:10px 15px;background-color:#fd9112;color:#fff;width:auto;display: inline-block;font-size: 13px;"></p>
			</div>	
			
			<c:if test="${wxQRcode && wxOpenFlag}">
				<button type="button" icon="wx.png" text="false" name="wx">微信登录</button>
			</c:if>										
   		</div> 			
	</div>
</div>


<%-- 底部  --%>
<div class="frame footer ui-widget-header">
	<p>${copyRight}</p>
</div>

<%-- 每个模块页面必须引入 --%>
<%@ include file="/include/html_bottom.jsp"%>
