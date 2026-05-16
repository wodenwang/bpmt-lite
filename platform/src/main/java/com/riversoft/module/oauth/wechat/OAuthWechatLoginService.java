package com.riversoft.module.oauth.wechat;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.riversoft.core.context.SessionContext;
import com.riversoft.core.web.Actions;
import com.riversoft.module.thirdpart.ThirdpartService;

public class OAuthWechatLoginService {
    private static final Logger logger = LoggerFactory.getLogger(OAuthWechatLoginService.class);

    private final WechatOAuthProvider provider;

    public OAuthWechatLoginService() {
        this(createDefaultProvider());
    }

    public OAuthWechatLoginService(WechatOAuthProvider provider) {
        this.provider = provider;
    }

    public OAuthWechatLoginResult handle(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> thirdpart) {
        if (thirdpart == null || !Actions.Util.fromWx(request)) {
            return OAuthWechatLoginResult.skip();
        }
        if (!isWechatLoginEnabled(thirdpart.get("wechatLoginEnabled"))) {
            return OAuthWechatLoginResult.skip();
        }

        Map<String, Object> wechat;
        try {
            wechat = ThirdpartService.normalizeWechatLogin(thirdpart);
        } catch (RuntimeException e) {
            return error(thirdpart, stringValue(thirdpart.get("wechatType")), stringValue(thirdpart.get("wechatKey")),
                    null, "wechat_config_invalid", e.getMessage());
        }

        String wechatType = stringValue(wechat.get("wechatType"));
        String wechatKey = stringValue(wechat.get("wechatKey"));
        String wechatScope = stringValue(wechat.get("wechatScope"));
        String code = request.getParameter("code");
        if (StringUtils.isBlank(code)) {
            try {
                String redirectUrl = provider.buildAuthorizationUrl(wechatType, wechatKey, wechatScope,
                        Actions.Util.getFullURL(request));
                logger.info(
                        "wechat oauth login redirect clientId={} thirdpartKey={} wechatType={} wechatKey={} userId={} result=REDIRECT reason={}",
                        stringValue(thirdpart.get("clientId")), stringValue(thirdpart.get("thirdpartKey")),
                        wechatType, wechatKey, null, "wechat_code_missing");
                return OAuthWechatLoginResult.redirect(redirectUrl);
            } catch (OAuthWechatConfigException e) {
                return error(thirdpart, wechatType, wechatKey, null, "wechat_config_invalid", e.getMessage());
            } catch (RuntimeException e) {
                return error(thirdpart, wechatType, wechatKey, null, "wechat_login_failed", "微信授权地址生成失败.");
            }
        }

        try {
            String userId = provider.loginByCode(request, wechatType, wechatKey, wechatScope, code);
            if (StringUtils.isBlank(userId)) {
                return error(thirdpart, wechatType, wechatKey, null, "wechat_login_failed", "userId is blank");
            }
            refreshSessionContext(request);
            logger.info(
                    "wechat oauth login success clientId={} thirdpartKey={} wechatType={} wechatKey={} userId={} result=LOGGED_IN reason={}",
                    stringValue(thirdpart.get("clientId")), stringValue(thirdpart.get("thirdpartKey")), wechatType,
                    wechatKey, userId, "wechat_code_accepted");
            return OAuthWechatLoginResult.loggedIn(userId);
        } catch (OAuthWechatConfigException e) {
            return error(thirdpart, wechatType, wechatKey, null, "wechat_config_invalid", e.getMessage());
        } catch (OAuthWechatLoginException e) {
            return error(thirdpart, wechatType, wechatKey, e.getUserId(), e.getReason(), e.getSafeMessage());
        } catch (RuntimeException e) {
            return error(thirdpart, wechatType, wechatKey, null, "wechat_login_failed", "微信登录失败.");
        }
    }

    private OAuthWechatLoginResult error(Map<String, Object> thirdpart, String wechatType, String wechatKey,
            String userId, String reason, String message) {
        logger.info(
                "wechat oauth login error clientId={} thirdpartKey={} wechatType={} wechatKey={} userId={} result=ERROR reason={} message={}",
                stringValue(thirdpart.get("clientId")), stringValue(thirdpart.get("thirdpartKey")), wechatType,
                wechatKey, userId, reason, message);
        return OAuthWechatLoginResult.error(reason, message);
    }

    private void refreshSessionContext(HttpServletRequest request) {
        Enumeration<String> names = request.getSession().getAttributeNames();
        Map<String, Object> attributes = new HashMap<String, Object>();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            attributes.put(name, request.getSession().getAttribute(name));
        }
        SessionContext.init(request.getSession(), attributes);
    }

    private static boolean isWechatLoginEnabled(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }
        return "1".equals(StringUtils.trimToEmpty(String.valueOf(value)));
    }

    private static String stringValue(Object value) {
        return value == null ? null : StringUtils.trimToNull(String.valueOf(value));
    }

    private static WechatOAuthProvider createDefaultProvider() {
        if (isFakeProviderEnabled(System.getenv("BPMT_OAUTH_WECHAT_FAKE_PROVIDER"),
                System.getProperty("bpmt.oauth.wechat.fake.provider"))) {
            return new FakeWechatOAuthProvider();
        }
        return new RealWechatOAuthProvider();
    }

    static boolean isFakeProviderEnabled(String envValue, String propertyValue) {
        return isTrueValue(envValue) || isTrueValue(propertyValue);
    }

    private static boolean isTrueValue(String value) {
        String normalized = StringUtils.trimToEmpty(value);
        return "true".equalsIgnoreCase(normalized) || "1".equals(normalized);
    }
}
