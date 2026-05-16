package com.riversoft.module.oauth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.riversoft.core.web.Actions;
import com.riversoft.core.web.annotation.ActionAccess;
import com.riversoft.module.oauth.wechat.OAuthWechatLoginResult;
import com.riversoft.module.oauth.wechat.OAuthWechatLoginService;
import com.riversoft.module.oauth.wechat.OAuthWechatLoginStatus;
import com.riversoft.platform.SessionManager;
import com.riversoft.platform.po.UsGroup;
import com.riversoft.platform.po.UsRole;
import com.riversoft.platform.po.UsUser;
import com.riversoft.platform.script.function.UserHelper;

public class OAuthAction {
    private static final Logger logger = LoggerFactory.getLogger(OAuthAction.class);

    @ActionAccess(login = false)
    public void authorize(HttpServletRequest request, HttpServletResponse response) {
        String responseType = request.getParameter("response_type");
        String clientId = request.getParameter("client_id");
        String redirectUri = request.getParameter("redirect_uri");
        String state = request.getParameter("state");

        OAuthService service = getOAuthService();
        Map<String, Object> validation = service.validateAuthorize(responseType, clientId, redirectUri);
        String requestId = stringValue(validation.get("requestId"));
        if (hasError(validation)) {
            logger.info("OAuth authorize rejected before redirect. requestId={} clientId={} result={} reason={}",
                    requestId, clientId, "deny", validation.get("error"));
            showBrowserError(request, response, "OAuth 登录请求无效", stringValue(validation.get("error_description")),
                    requestId);
            return;
        }

        Map<String, Object> thirdpart = thirdpart(validation);
        if (!isLoggedIn(request)) {
            OAuthWechatLoginResult wechat = getWechatLoginService().handle(request, response, thirdpart);
            if (wechat != null && OAuthWechatLoginStatus.REDIRECT.equals(wechat.getStatus())) {
                logger.info("OAuth authorize redirects to WeChat login. requestId={} clientId={} result={} reason={}",
                        requestId, clientId, "pending", "wechat_redirect");
                redirectExternal(response, wechat.getRedirectUrl());
                return;
            }
            if (wechat != null && OAuthWechatLoginStatus.ERROR.equals(wechat.getStatus())) {
                logger.info("OAuth authorize WeChat login failed. requestId={} clientId={} result={} reason={}",
                        requestId, clientId, "deny", wechat.getReason());
                showBrowserError(request, response, wechatErrorTitle(wechat), wechat.getMessage(), requestId);
                return;
            }
            if (wechat != null && OAuthWechatLoginStatus.LOGGED_IN.equals(wechat.getStatus())) {
                logger.info("OAuth authorize WeChat login success. requestId={} clientId={} userId={} result={} reason={}",
                        requestId, clientId, wechat.getUserId(), "pending", "wechat_logged_in");
            } else {
                storeReturnUrl(request);
                logger.info("OAuth authorize requires BPMT login. requestId={} clientId={} result={} reason={}",
                        requestId, clientId, "pending", "login_required");
                jumpToLogin(request, response);
                return;
            }
        }

        String userId = currentUserId(request);
        if (!service.currentUserCanAccess(thirdpart)) {
            storeAccessDeniedContext(request, requestId, clientId, thirdpart, userId, redirectUri, state);
            logger.info("OAuth authorize access denied prompt. requestId={} clientId={} userId={} result={} reason={}",
                    requestId, clientId, userId, "deny", "permission_denied_prompt");
            forwardAccessDeniedPage(request, response);
            return;
        }

        Map<String, Object> codeResult = service.createAuthorizationCodeResult(thirdpart, userId, redirectUri, state);
        if (hasError(codeResult)) {
            logger.info("OAuth authorize rejected before code. requestId={} clientId={} userId={} result={} reason={}",
                    requestId, clientId, userId, "deny", codeResult.get("error"));
            redirectExternal(response, appendErrorQuery(redirectUri, stringValue(codeResult.get("error")),
                    stringValue(codeResult.get("error_description")), validStateForRedirect(state)));
            return;
        }
        String code = stringValue(codeResult.get("code"));
        logger.info("OAuth authorize issued code. requestId={} clientId={} userId={} result={} reason={}",
                requestId, clientId, userId, "allow", "code_issued");
        redirectExternal(response, appendQuery(redirectUri, "code", code, state));
    }

    @ActionAccess(login = false)
    public void token(HttpServletRequest request, HttpServletResponse response) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            logger.info("OAuth token rejected. clientId={} result={} reason={}", request.getParameter("client_id"),
                    "deny", "method_not_allowed");
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.setHeader("Allow", "POST");
            writeJson(request, response, OAuthJson.error("invalid_request", "token endpoint requires POST."));
            return;
        }

        String grantType = request.getParameter("grant_type");
        String clientId = request.getParameter("client_id");
        if (!"authorization_code".equals(grantType)) {
            logger.info("OAuth token rejected. clientId={} result={} reason={}", clientId, "deny",
                    "unsupported_grant_type");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(request, response,
                    OAuthJson.error("unsupported_grant_type", "grant_type must be authorization_code."));
            return;
        }

        Map<String, Object> result = getOAuthService().exchangeCode(clientId, request.getParameter("client_secret"),
                request.getParameter("code"), request.getParameter("redirect_uri"));
        if (hasError(result)) {
            logger.info("OAuth token rejected. clientId={} result={} reason={}", clientId, "deny",
                    result.get("error"));
            response.setStatus("invalid_client".equals(result.get("error")) ? HttpServletResponse.SC_UNAUTHORIZED
                    : HttpServletResponse.SC_BAD_REQUEST);
            writeJson(request, response,
                    OAuthJson.error(stringValue(result.get("error")), stringValue(result.get("error_description"))));
            return;
        }

        logger.info("OAuth token success. clientId={} userId={} result={} reason={}", clientId, result.get("userid"),
                "allow", "token_issued");
        writeJson(request, response, OAuthJson.token(stringValue(result.get("access_token")),
                NumberUtils.toInt(stringValue(result.get("expires_in"))), stringValue(result.get("userid"))));
    }

    @ActionAccess(login = false)
    public void userinfo(HttpServletRequest request, HttpServletResponse response) {
        String token = bearerToken(request);
        if (StringUtils.isBlank(token)) {
            logger.info("OAuth userinfo rejected. result={} reason={}", "deny", "missing_bearer");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(request, response, OAuthJson.error("invalid_token", "Bearer token is required."));
            return;
        }

        Map<String, Object> tokenInfo = getOAuthService().loadUserInfo(token);
        if (hasError(tokenInfo)) {
            logger.info("OAuth userinfo rejected. result={} reason={}", "deny", tokenInfo.get("error"));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(request, response,
                    OAuthJson.error(stringValue(tokenInfo.get("error")), stringValue(tokenInfo.get("error_description"))));
            return;
        }

        String userId = stringValue(tokenInfo.get("userid"));
        UsUser user = loadUser(userId);
        if (user == null) {
            logger.info("OAuth userinfo rejected. clientId={} userId={} result={} reason={}", tokenInfo.get("client_id"),
                    userId, "deny", "user_not_found");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(request, response, OAuthJson.error("invalid_token", "user is inactive or not found."));
            return;
        }
        logger.info("OAuth userinfo success. clientId={} userId={} result={} reason={}", tokenInfo.get("client_id"),
                userId, "allow", "userinfo");
        writeJson(request, response, OAuthJson.userinfo(user, loadDefaultGroup(userId), loadDefaultRole(userId)));
    }

    @ActionAccess(login = false)
    public void switchAccount(HttpServletRequest request, HttpServletResponse response) {
        if (!requiresPost(request, response, "switchAccount")) {
            return;
        }

        Map<String, Object> context = loadAccessDeniedContext(request);
        if (context == null) {
            showBrowserError(request, response, "OAuth 登录请求已失效", "当前 OAuth 登录请求已失效，请从第三方系统重新发起登录。", null);
            return;
        }
        String requestId = stringValue(context.get("requestId"));
        logger.info("OAuth access denied switch account. requestId={} clientId={} userId={} result={} reason={}",
                requestId, context.get("clientId"), context.get("userId"), "pending", "switch_account_requested");
        clearAccessDeniedContext(request);
        logoutCurrentUser(request);
        request.getSession().setAttribute(OAuthSessionKeys.RETURN_URL, stringValue(context.get("returnUrl")));
        jumpToLogin(request, response);
    }

    @ActionAccess(login = false)
    public void cancelAccessDenied(HttpServletRequest request, HttpServletResponse response) {
        if (!requiresPost(request, response, "cancelAccessDenied")) {
            return;
        }

        Map<String, Object> context = loadAccessDeniedContext(request);
        if (context == null) {
            showBrowserError(request, response, "OAuth 登录请求已失效", "当前 OAuth 登录请求已失效，请从第三方系统重新发起登录。", null);
            return;
        }
        String requestId = stringValue(context.get("requestId"));
        String redirectUri = stringValue(context.get("redirectUri"));
        String state = stringValue(context.get("state"));
        String description = accessDeniedDescription(context);
        logger.info("OAuth access denied cancelled. requestId={} clientId={} userId={} result={} reason={}",
                requestId, context.get("clientId"), context.get("userId"), "deny", "access_denied_cancelled");
        clearAccessDeniedContext(request);
        redirectExternal(response,
                appendErrorQuery(redirectUri, "access_denied", description, validStateForRedirect(state)));
    }

    protected OAuthService getOAuthService() {
        return new OAuthService();
    }

    protected OAuthWechatLoginService getWechatLoginService() {
        return new OAuthWechatLoginService();
    }

    protected boolean isLoggedIn(HttpServletRequest request) {
        return SessionManager.checkUserLogin();
    }

    protected String currentUserId(HttpServletRequest request) {
        UsUser user = SessionManager.getUser();
        return user == null ? null : user.getUid();
    }

    protected void jumpToLogin(HttpServletRequest request, HttpServletResponse response) {
        Actions.jump(request, response, "/login.jsp");
    }

    protected void forwardErrorPage(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.getRequestDispatcher("/xhtml/oauth/error.jsp").forward(request, response);
        } catch (IOException e) {
            throw new IllegalStateException("OAuth error page forward failed.", e);
        } catch (ServletException e) {
            throw new IllegalStateException("OAuth error page forward failed.", e);
        }
    }

    protected void forwardAccessDeniedPage(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.getRequestDispatcher("/xhtml/oauth/access_denied.jsp").forward(request, response);
        } catch (IOException e) {
            throw new IllegalStateException("OAuth access denied page forward failed.", e);
        } catch (ServletException e) {
            throw new IllegalStateException("OAuth access denied page forward failed.", e);
        }
    }

    protected void logoutCurrentUser(HttpServletRequest request) {
        SessionManager.doLogout(request);
    }

    protected UsUser loadUser(String userId) {
        return UserHelper.findUser(userId);
    }

    protected UsGroup loadDefaultGroup(String userId) {
        return UserHelper.getGroupByUser(userId);
    }

    protected UsRole loadDefaultRole(String userId) {
        return UserHelper.getRoleByUser(userId);
    }

    protected void redirectExternal(HttpServletResponse response, String url) {
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            throw new IllegalStateException("OAuth redirect failed.", e);
        }
    }

    protected void writeJson(HttpServletRequest request, HttpServletResponse response, Map<String, Object> body) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        Actions.showJson(request, response, body);
    }

    private boolean requiresPost(HttpServletRequest request, HttpServletResponse response, String actionName) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        logger.info("OAuth access denied action rejected. action={} result={} reason={}", actionName, "deny",
                "method_not_allowed");
        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        response.setHeader("Allow", "POST");
        writeJson(request, response,
                OAuthJson.error("invalid_request", "OAuth access denied action requires POST."));
        return false;
    }

    private void storeReturnUrl(HttpServletRequest request) {
        request.getSession().setAttribute(OAuthSessionKeys.RETURN_URL, Actions.Util.getFullURL(request));
    }

    private void storeAccessDeniedContext(HttpServletRequest request, String requestId, String clientId,
            Map<String, Object> thirdpart, String userId, String redirectUri, String state) {
        Map<String, Object> context = new LinkedHashMap<String, Object>();
        context.put("requestId", requestId);
        context.put("clientId", clientId);
        context.put("thirdpartKey", stringValue(thirdpart.get("thirdpartKey")));
        context.put("thirdpartName", stringValue(thirdpart.get("thirdpartName")));
        context.put("userId", userId);
        context.put("message", accessDeniedUserMessage(userId));
        context.put("redirectUri", redirectUri);
        context.put("state", validStateForRedirect(state));
        context.put("returnUrl", Actions.Util.getFullURL(request));
        request.setAttribute("oauthAccessDenied", context);
        request.getSession().setAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT, context);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadAccessDeniedContext(HttpServletRequest request) {
        Object value = request.getSession().getAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT);
        if (!(value instanceof Map)) {
            return null;
        }
        Map<String, Object> context = (Map<String, Object>) value;
        if (StringUtils.isBlank(stringValue(context.get("returnUrl")))
                || StringUtils.isBlank(stringValue(context.get("redirectUri")))) {
            return null;
        }
        return context;
    }

    private void clearAccessDeniedContext(HttpServletRequest request) {
        request.getSession().removeAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT);
    }

    protected void showBrowserError(HttpServletRequest request, HttpServletResponse response, String title, String message,
            String requestId) {
        request.setAttribute("oauthErrorTitle", title);
        request.setAttribute("oauthErrorMessage", message);
        request.setAttribute("requestId", requestId);
        forwardErrorPage(request, response);
    }

    private String wechatErrorTitle(OAuthWechatLoginResult wechat) {
        String reason = wechat == null ? null : wechat.getReason();
        if (StringUtils.startsWith(reason, "bpmt_")) {
            return "OAuth BPMT 登录失败";
        }
        return "OAuth 微信登录失败";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> thirdpart(Map<String, Object> validation) {
        Object value = validation.get("thirdpart");
        return value instanceof Map ? (Map<String, Object>) value : new HashMap<String, Object>();
    }

    private String appendQuery(String redirectUri, String key, String value, String state) {
        StringBuilder url = new StringBuilder(redirectUri);
        url.append(redirectUri.indexOf('?') >= 0 ? '&' : '?');
        url.append(urlEncode(key)).append('=').append(urlEncode(value));
        if (StringUtils.isNotEmpty(state)) {
            url.append('&').append("state=").append(urlEncode(state));
        }
        return url.toString();
    }

    private String appendErrorQuery(String redirectUri, String error, String description, String state) {
        StringBuilder url = new StringBuilder(redirectUri);
        url.append(redirectUri.indexOf('?') >= 0 ? '&' : '?');
        url.append("error=").append(urlEncode(error));
        if (StringUtils.isNotEmpty(description)) {
            url.append('&').append("error_description=").append(urlEncode(description));
        }
        if (StringUtils.isNotEmpty(state)) {
            url.append('&').append("state=").append(urlEncode(state));
        }
        return url.toString();
    }

    private String accessDeniedDescription(Map<String, Object> context) {
        if (context == null) {
            return "用户[]不具备访问本应用权限。";
        }
        return accessDeniedUserMessage(stringValue(context.get("userId")));
    }

    private String accessDeniedUserMessage(String userId) {
        return "用户[" + stringValue(userId) + "]不具备访问本应用权限。";
    }

    private String bearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.isBlank(header) || !StringUtils.startsWithIgnoreCase(header, "Bearer ")) {
            return null;
        }
        return StringUtils.trim(StringUtils.substring(header, "Bearer ".length()));
    }

    private boolean hasError(Map<String, Object> result) {
        return result != null && result.containsKey("error");
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(StringUtils.defaultString(value), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 is unavailable.", e);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String validStateForRedirect(String state) {
        return state != null && state.length() <= 500 ? state : null;
    }
}
