/*
 * $HeadURL: $
 * $Id: $
 * Copyright (c) 2013 by Riversoft System, all rights reserved.
 */
package com.riversoft.module.frame;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.riversoft.core.Config;
import com.riversoft.core.db.DataCondition;
import com.riversoft.core.db.ORMService;
import com.riversoft.core.exception.SystemRuntimeException;
import com.riversoft.core.web.Actions;
import com.riversoft.core.web.Actions.Keys;
import com.riversoft.core.web.Actions.Util;
import com.riversoft.core.web.FreeMarkerUtils;
import com.riversoft.core.web.RequestUtils;
import com.riversoft.core.web.annotation.ActionAccess;
import com.riversoft.core.web.annotation.ActionMode;
import com.riversoft.core.web.annotation.ActionMode.Mode;
import com.riversoft.module.oauth.OAuthSessionKeys;
import com.riversoft.platform.SessionManager;
import com.riversoft.platform.SessionManager.SessionAttributeKey;
import com.riversoft.platform.db.ORMAdapterService;
import com.riversoft.platform.po.UsUser;
import com.riversoft.platform.web.WxUserManager;
import com.riversoft.weixin.common.oauth2.AccessToken;
import com.riversoft.weixin.common.oauth2.OpenUser;
import com.riversoft.weixin.open.base.AppSetting;
import com.riversoft.weixin.open.oauth2.OpenOAuth2s;
import com.riversoft.wx.mp.model.MpVisitorModelKeys;
import com.riversoft.wx.mp.model.OpenVisitorModelKeys;

/**
 * @author Woden
 * 
 */

public class LoginAction {

	private static Logger logger = LoggerFactory.getLogger(LoginAction.class);

	/**
	 * 登陆页面
	 * 
	 * @param request
	 * @param response
	 */
	@ActionAccess(login = false)
	@ActionMode(Mode.FIT)
	public void index(HttpServletRequest request, HttpServletResponse response) {

		// 校验到已登录则跳转到首页
		if (SessionManager.checkUserLogin()) {
			Actions.jump(request, response, "/");
			return;
		}

		// logo与copyright
		request.setAttribute("logoUrl", Config.get("page.logo.url", Actions.Util.getContextPath(request) + FrameStyleSwitcher.getLogoUrl()));
		request.setAttribute("copyRight", Config.getChinese("page.copyright", ""));

		// 浏览器提示
		request.setAttribute("browserMsg", Config.getChinese("page.browser.msg", ""));
		request.setAttribute("browserUrl", Config.get("page.browser.url", ""));

		// 是否验证码
		Integer allowErrorCount = NumberUtils.toInt(Config.get("page.randomcode"), 3);// 0表示必须验证码;默认不需要验证码.
		Integer errorCount = (Integer) request.getSession().getAttribute(SessionAttributeKey.LOGIN_ERROR_COUNT.toString());
		if (errorCount == null) {
			errorCount = 0;
		}
		request.setAttribute("allowErrorCount", allowErrorCount);
		request.setAttribute("errorCount", errorCount);

		// 提示
		request.setAttribute("tips", Config.getChinese("page.tips", ""));

		// 微信开放平台扫码登录
		request.setAttribute("wxQRcode", Boolean.valueOf(Config.get("wx.web.login.qrcode", "false")));
		request.setAttribute("wxOpenFlag", Boolean.valueOf(Config.get("wx.open.flag", "false")));
		request.setAttribute("wxDomain", Config.get("wx.net.domain", "gzriver.com"));
		request.setAttribute("wxWebAppId", Config.get("wx.open.appId"));
		request.setAttribute(Keys.H5_JS.toString(), "amaze");// 视图使用amaze ui

		String zoneId = RequestUtils.getStringValue(request, Actions.Keys.ZONE.toString());

		if (StringUtils.isNotEmpty(zoneId) && !"_body".equals(zoneId)) {// 小登录
			Actions.includePage(request, response, "/frame/zone_login.jsp");
		} else {// 大登录

			// 是否自定义登录模板
			String loginFtl = Config.get("page.frame.login");
			if (StringUtils.isNotEmpty(loginFtl)) {
				Map<String, Object> context = new HashMap<>();
				context.put("cp", Actions.Util.getContextPath(request));
				context.put("zone", zoneId);
				context.put("title", Config.getChinese("page.title", ""));
				context.put("icon", Config.get("page.ico.url", "/css/images/favicon.ico"));
				context.put("logoUrl", request.getAttribute("logoUrl"));
				context.put("tips", request.getAttribute("tips"));
				context.put("copyRight", request.getAttribute("copyRight"));
				context.put("allowErrorCount", request.getAttribute("allowErrorCount"));
				context.put("errorCount", request.getAttribute("errorCount"));
				String html;
				if (StringUtils.startsWith(loginFtl, "classpath:")) {
					html = FreeMarkerUtils.process(loginFtl, context);
				} else {
					try (InputStream is = new FileInputStream(loginFtl)) {
						html = FreeMarkerUtils.process(loginFtl, is, context);
					} catch (IOException e) {
						logger.warn("找不到登录模板[" + loginFtl + "]", e);
						Actions.includePage(request, response, Util.getPagePath(request, FrameStyleSwitcher.getLogin()));
						return;
					}
				}
				request.setAttribute(Keys.HEAD.toString(), false);
				Actions.showHtml(request, response, html);
			} else {
				Actions.includePage(request, response, Util.getPagePath(request, FrameStyleSwitcher.getLogin()));
			}
		}
	}

	/**
	 * 处理登陆
	 * 
	 * @param request
	 * @param response
	 */
	@ActionAccess(login = false)
	@ActionMode(Mode.FIT)
	public void login(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = new HashMap<>();
		result.put("flag", true);

		// 处理登陆
		try {
			doUserLogin(request);
			Object returnUrl = request.getSession().getAttribute(OAuthSessionKeys.RETURN_URL);
			if (returnUrl != null) {
				request.getSession().removeAttribute(OAuthSessionKeys.RETURN_URL);
				result.put("redirectUrl", returnUrl.toString());
			}
		} catch (SystemRuntimeException e) {
			result.put("flag", false);
			result.put("msg", e.getMessage());
			result.put("errorCount", request.getSession().getAttribute(SessionAttributeKey.LOGIN_ERROR_COUNT.toString()));
		}

		Actions.showJson(request, response, result);
	}

	protected void doUserLogin(HttpServletRequest request) {
		SessionManager.doUserLogin(request);
	}

	/**
	 * 处理微信开放平台登陆
	 *
	 * @param request
	 * @param response
	 */
	@ActionAccess(login = false)
	@ActionMode(Mode.FIT)
	public void wxLogin(HttpServletRequest request, HttpServletResponse response) {
		// 处理登陆
		String code = RequestUtils.getStringValue(request, "code");

		String webAppId = Config.get("wx.open.appId");
		String webAppSecret = Config.get("wx.open.appSecret");
		String openVisitorTable = Config.get("wx.open.table");

		OpenOAuth2s openOAuth2s = OpenOAuth2s.with(new AppSetting(webAppId, webAppSecret));
		AccessToken accessToken = openOAuth2s.getAccessToken(code);
		String openId = accessToken.getOpenId();
		String unionId = accessToken.getUnionId();

		UsUser openUser = null;
		List<UsUser> mpUsers = new ArrayList<>();

		// 开放平台登录
		{
			Map<String, Object> u = (Map<String, Object>) ORMAdapterService.getInstance().findByPk(openVisitorTable, openId);
			OpenUser ou = openOAuth2s.userInfo(accessToken.getAccessToken(), openId);

			if (u == null) {
				u = WxUserManager.buildOpenVisitor(openVisitorTable, ou);
			} else if (u.get(OpenVisitorModelKeys.NICK_NAME.name()) == null || u.get(OpenVisitorModelKeys.HEAD_IMG_URL.name()) == null) {
				u.put(OpenVisitorModelKeys.UPDATE_DATE.name(), new Date());
				u.put(OpenVisitorModelKeys.NICK_NAME.name(), ou.getNickName());
				u.put(OpenVisitorModelKeys.HEAD_IMG_URL.name(), ou.getHeadImgUrl());
				ORMAdapterService.getInstance().update(u);
			}

			openUser = WxUserManager.buildOpenUser(u);
			if (openUser.isEntity()) {
				SessionManager.doUserLogin(request, openUser);
				Actions.jump(request, response, "/");
				return;
			}
		}

		// 公众号登录
		String appIds = Config.get("wx.web.mp.appIds");
		if (StringUtils.isNotEmpty(appIds)) {
			for (String appId : StringUtils.split(appIds, ";")) {
				Map<String, Object> mpConfig = (Map<String, Object>) ORMService.getInstance().findByKey("WxMp", "appId", appId);
				if (mpConfig == null) {
					continue;
				}

				String visitorTable = (String) mpConfig.get("visitorTable");
				String visitorTagTable = (String) mpConfig.get("visitorTagTable");
				DataCondition condition = new DataCondition().setStringEqual(MpVisitorModelKeys.UNION_ID.getColumn().getName(), unionId).setNumberEqual(MpVisitorModelKeys.SUBSCRIBE.name(), "1");

				Map<String, Object> visitor = (Map<String, Object>) ORMAdapterService.getInstance().find(visitorTable, condition.toEntity());
				if (visitor != null) {
					UsUser user = WxUserManager.buildMpUser(visitor, (String) mpConfig.get("groupKey"), (String) mpConfig.get("roleKey"));
					if (user.isEntity()) {// 找到实体则立即登录
						SessionManager.doUserLogin(request, user);
						Actions.jump(request, response, "/");
						return;
					}
					mpUsers.add(user);
				}
			}
		}

		if (mpUsers.size() > 0) {
			SessionManager.doUserLogin(request, mpUsers.get(0));
		} else if (openUser != null) {
			SessionManager.doUserLogin(request, openUser);
		}
		Actions.jump(request, response, "/");

	}

	/**
	 * 登出
	 * 
	 * @param request
	 * @param response
	 */
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		// 处理登出
		SessionManager.doLogout(request);

		// 成功的话跳转到登陆页
		Actions.jump(request, response, "/login.jsp");
	}

	/**
	 * 随机验证码
	 * 
	 * @param request
	 * @param response
	 */
	@ActionAccess(login = false)
	public void randomImg(HttpServletRequest request, HttpServletResponse response) {
		Actions.showRandomImage(request, response, SessionManager.SessionAttributeKey.RANDOM_CODE.toString());
	}

}
