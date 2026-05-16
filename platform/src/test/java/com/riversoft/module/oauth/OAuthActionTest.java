package com.riversoft.module.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.riversoft.core.web.Actions;
import com.riversoft.module.oauth.wechat.OAuthWechatLoginResult;
import com.riversoft.module.oauth.wechat.OAuthWechatLoginService;
import com.riversoft.platform.po.UsGroup;
import com.riversoft.platform.po.UsRole;
import com.riversoft.platform.po.UsUser;

public class OAuthActionTest {

    @Test
    public void accessDeniedJspExists() {
        assertTrue(accessDeniedJspFile().isFile());
    }

    @Test
    public void accessDeniedJspEscapesUserControlledValues() throws Exception {
        String jsp = readAccessDeniedJsp();

        assertTrue(jsp.contains("private String escapeHtml(Object value)"));
        assertTrue(jsp.contains("<%= escapedUserId %>"));
        assertTrue(jsp.contains("<%= escapedThirdpartName %>"));
        assertTrue(jsp.contains("<%= escapedRequestId %>"));
        assertFalse(jsp.contains("<%= userId %>"));
        assertFalse(jsp.contains("<%= thirdpartName %>"));
        assertFalse(jsp.contains("<%= requestId %>"));
        assertFalse(jsp.contains("<%= redirectUri %>"));
        assertFalse(jsp.contains("<%= returnUrl %>"));
        assertFalse(jsp.contains("<%= code %>"));
        assertFalse(jsp.contains("<%= token %>"));
        assertFalse(jsp.contains("<%= secret %>"));
        assertFalse(jsp.contains("<%= access_token %>"));
        assertFalse(jsp.contains("<%= client_secret %>"));
        assertFalse(jsp.contains("<%= clientSecret %>"));
    }

    @Test
    public void accessDeniedJspShowsClearUserAndClientContext() throws Exception {
        String jsp = readAccessDeniedJsp();

        assertTrue(jsp.contains("用户[<%= escapedUserId %>]不具备访问本应用权限。"));
        assertTrue(jsp.contains("目标应用"));
        assertTrue(jsp.contains("<%= escapedThirdpartName %>"));
        assertTrue(jsp.contains("Client ID"));
        assertTrue(jsp.contains("<%= escapedClientId %>"));
        assertTrue(jsp.contains("系统标识"));
        assertTrue(jsp.contains("<%= escapedThirdpartKey %>"));
        assertFalse(jsp.contains("<%= clientSecret %>"));
        assertFalse(jsp.contains("<%= accessToken %>"));
    }

    @Test
    public void authorizeWithoutLoginStoresReturnUrl() {
        TestOAuthAction action = new TestOAuthAction();
        action.loggedIn = false;
        MockHttpServletRequest request = authorizeRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.authorize(request, response);

        assertEquals("http://localhost/oauth/authorize?response_type=code&client_id=client-a&redirect_uri=http%3A%2F%2Fclient.example%2Fcallback&state=s-1",
                request.getSession().getAttribute(OAuthSessionKeys.RETURN_URL));
        assertEquals("/login.jsp", action.loginTarget);
    }

    @Test
    public void authorizeWechatWithoutLoginRedirectsToWechatInsteadOfLogin() {
        TestOAuthAction action = new TestOAuthAction();
        action.loggedIn = false;
        action.wechatResult = OAuthWechatLoginResult.redirect("https://wechat.example/oauth?state=w-1");
        MockHttpServletRequest request = authorizeRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.authorize(request, response);

        assertEquals("https://wechat.example/oauth?state=w-1", response.getRedirectedUrl());
        assertNull(action.loginTarget);
        assertNull(request.getSession().getAttribute(OAuthSessionKeys.RETURN_URL));
    }

    @Test
    public void authorizeWechatLoginSuccessContinuesToIssueCode() {
        TestOAuthAction action = new TestOAuthAction();
        action.loggedIn = false;
        action.currentUserId = "admin";
        action.canAccess = true;
        action.wechatResult = OAuthWechatLoginResult.loggedIn("admin");
        MockHttpServletRequest request = authorizeRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.authorize(request, response);

        assertNull(action.loginTarget);
        assertNull(request.getSession().getAttribute(OAuthSessionKeys.RETURN_URL));
        assertEquals("http://client.example/callback?code=code-a&state=s-1", response.getRedirectedUrl());
    }

    @Test
    public void authorizeWechatConfigErrorShowsOauthErrorPage() {
        TestOAuthAction action = new TestOAuthAction();
        action.loggedIn = false;
        action.wechatResult = OAuthWechatLoginResult.error("wechat_config_invalid", "微信登录配置缺失");
        MockHttpServletRequest request = authorizeRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.authorize(request, response);

        assertEquals("/xhtml/oauth/error.jsp", action.forwardedPage);
        assertEquals("OAuth 微信登录失败", request.getAttribute("oauthErrorTitle"));
        assertEquals("微信登录配置缺失", request.getAttribute("oauthErrorMessage"));
        assertEquals("request-a", request.getAttribute("requestId"));
        assertNull(action.loginTarget);
        assertNull(response.getRedirectedUrl());
        assertNull(request.getSession().getAttribute(OAuthSessionKeys.RETURN_URL));
    }

    @Test
    public void authorizeWechatBpmtLoginErrorShowsBpmtErrorPageTitle() {
        TestOAuthAction action = new TestOAuthAction();
        action.loggedIn = false;
        action.wechatResult = OAuthWechatLoginResult.error("bpmt_login_paused",
                "微信授权已成功，但 BPMT 当前处于维护/暂停模式，用户[woden]无法建立登录态。请管理员检查 safe.role 或 safe.admin 配置。");
        MockHttpServletRequest request = authorizeRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.authorize(request, response);

        assertEquals("/xhtml/oauth/error.jsp", action.forwardedPage);
        assertEquals("OAuth BPMT 登录失败", request.getAttribute("oauthErrorTitle"));
        assertEquals("微信授权已成功，但 BPMT 当前处于维护/暂停模式，用户[woden]无法建立登录态。请管理员检查 safe.role 或 safe.admin 配置。",
                request.getAttribute("oauthErrorMessage"));
        assertEquals("request-a", request.getAttribute("requestId"));
        assertNull(action.loginTarget);
        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void authorizeWechatSkipFallsBackToNormalLogin() {
        TestOAuthAction action = new TestOAuthAction();
        action.loggedIn = false;
        action.wechatResult = OAuthWechatLoginResult.skip();
        MockHttpServletRequest request = authorizeRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.authorize(request, response);

        assertEquals("/login.jsp", action.loginTarget);
        assertEquals("http://localhost/oauth/authorize?response_type=code&client_id=client-a&redirect_uri=http%3A%2F%2Fclient.example%2Fcallback&state=s-1",
                request.getSession().getAttribute(OAuthSessionKeys.RETURN_URL));
    }

    @Test
    public void authorizeWithLoginRedirectsToVerifiedRedirectUri() {
        TestOAuthAction action = new TestOAuthAction();
        action.loggedIn = true;
        action.currentUserId = "admin";
        action.canAccess = true;
        action.code = "code with space";
        MockHttpServletRequest request = authorizeRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.authorize(request, response);

        assertEquals("http://client.example/callback?code=code+with+space&state=s-1", response.getRedirectedUrl());
    }

    @Test
    public void authorizeWithExistingBpmtLoginDoesNotJumpToLogin() {
        TestOAuthAction action = new TestOAuthAction();
        action.loggedIn = true;
        action.currentUserId = "admin";
        action.canAccess = true;
        MockHttpServletRequest request = authorizeRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.authorize(request, response);

        assertNull(action.loginTarget);
        assertEquals("http://client.example/callback?code=code-a&state=s-1", response.getRedirectedUrl());
    }

    @Test
    public void authorizeWithoutThirdpartPermissionShowsBpmtPromptAndStoresContext() {
        TestOAuthAction action = new TestOAuthAction();
        action.loggedIn = true;
        action.currentUserId = "oauth_no_pri";
        action.canAccess = false;
        MockHttpServletRequest request = authorizeRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.authorize(request, response);

        assertEquals("/xhtml/oauth/access_denied.jsp", action.forwardedPage);
        assertNull(response.getRedirectedUrl());
        assertTrue(request.getSession().getAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT) instanceof Map);
        Map<?, ?> context = (Map<?, ?>) request.getSession().getAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT);
        assertEquals("request-a", context.get("requestId"));
        assertEquals("client-a", context.get("clientId"));
        assertEquals("app-a", context.get("thirdpartKey"));
        assertEquals("演示系统", context.get("thirdpartName"));
        assertEquals("oauth_no_pri", context.get("userId"));
        assertEquals("用户[oauth_no_pri]不具备访问本应用权限。", context.get("message"));
        assertEquals("http://client.example/callback", context.get("redirectUri"));
        assertEquals("s-1", context.get("state"));
        assertEquals(
                "http://localhost/oauth/authorize?response_type=code&client_id=client-a&redirect_uri=http%3A%2F%2Fclient.example%2Fcallback&state=s-1",
                context.get("returnUrl"));
    }

    @Test
    public void cancelAccessDeniedRedirectsTrustedUriAndKeepsLogin() {
        TestOAuthAction action = new TestOAuthAction();
        MockHttpServletRequest request = new MockHttpServletRequest("POST",
                "/oauth/OAuthAction/cancelAccessDenied.shtml");
        request.getSession().setAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT, accessDeniedContext());
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.cancelAccessDenied(request, response);

        assertEquals(
                "http://client.example/callback?error=access_denied&error_description=%E7%94%A8%E6%88%B7%5Boauth_no_pri%5D%E4%B8%8D%E5%85%B7%E5%A4%87%E8%AE%BF%E9%97%AE%E6%9C%AC%E5%BA%94%E7%94%A8%E6%9D%83%E9%99%90%E3%80%82&state=s-1",
                response.getRedirectedUrl());
        assertFalse(action.logoutCalled);
        assertNull(request.getSession().getAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT));
    }

    @Test
    public void getSwitchAccountRejectsMethodWithoutSideEffects() throws Exception {
        TestOAuthAction action = new TestOAuthAction();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/OAuthAction/switchAccount.shtml");
        Map<String, Object> context = accessDeniedContext();
        request.getSession().setAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT, context);
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.switchAccount(request, response);

        assertEquals(405, response.getStatus());
        assertEquals("POST", response.getHeader("Allow"));
        assertTrue(response.getContentAsString().contains("\"error\":\"invalid_request\""));
        assertFalse(action.logoutCalled);
        assertSame(context, request.getSession().getAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT));
        assertNull(action.loginTarget);
    }

    @Test
    public void getCancelAccessDeniedRejectsMethodWithoutSideEffects() throws Exception {
        TestOAuthAction action = new TestOAuthAction();
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
                "/oauth/OAuthAction/cancelAccessDenied.shtml");
        Map<String, Object> context = accessDeniedContext();
        request.getSession().setAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT, context);
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.cancelAccessDenied(request, response);

        assertEquals(405, response.getStatus());
        assertEquals("POST", response.getHeader("Allow"));
        assertTrue(response.getContentAsString().contains("\"error\":\"invalid_request\""));
        assertSame(context, request.getSession().getAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT));
        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void switchAccountLogsOutAndStoresOAuthReturnUrl() {
        TestOAuthAction action = new TestOAuthAction();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/oauth/OAuthAction/switchAccount.shtml");
        request.getSession().setAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT, accessDeniedContext());
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.switchAccount(request, response);

        assertTrue(action.logoutCalled);
        assertEquals("/login.jsp", action.loginTarget);
        assertEquals(
                "http://localhost/oauth/authorize?response_type=code&client_id=client-a&redirect_uri=http%3A%2F%2Fclient.example%2Fcallback&state=s-1",
                request.getSession().getAttribute(OAuthSessionKeys.RETURN_URL));
        assertNull(request.getSession().getAttribute(OAuthSessionKeys.ACCESS_DENIED_CONTEXT));
    }

    @Test
    public void cancelAccessDeniedWithoutContextShowsBrowserError() {
        TestOAuthAction action = new TestOAuthAction();
        MockHttpServletRequest request = new MockHttpServletRequest("POST",
                "/oauth/OAuthAction/cancelAccessDenied.shtml");
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.cancelAccessDenied(request, response);

        assertEquals("/xhtml/oauth/error.jsp", action.forwardedPage);
        assertNull(response.getRedirectedUrl());
    }

    @Test
    public void switchAccountWithoutContextShowsBrowserError() {
        TestOAuthAction action = new TestOAuthAction();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/oauth/OAuthAction/switchAccount.shtml");
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.switchAccount(request, response);

        assertEquals("/xhtml/oauth/error.jsp", action.forwardedPage);
        assertFalse(action.logoutCalled);
        assertNull(action.loginTarget);
    }

    @Test
    public void tokenReturnsOAuthJson() throws Exception {
        TestOAuthAction action = new TestOAuthAction();
        action.tokenResult.put("access_token", "token-a");
        action.tokenResult.put("token_type", "Bearer");
        action.tokenResult.put("expires_in", Long.valueOf(7200L));
        action.tokenResult.put("userid", "admin");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/oauth/token");
        request.setParameter("grant_type", "authorization_code");
        request.setParameter("client_id", "client-a");
        request.setParameter("client_secret", "secret-a");
        request.setParameter("code", "code-a");
        request.setParameter("redirect_uri", "http://client.example/callback");
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.token(request, response);

        String json = response.getContentAsString();
        assertTrue(json.contains("\"access_token\":\"token-a\""));
        assertTrue(json.contains("\"token_type\":\"Bearer\""));
        assertTrue(json.contains("\"expires_in\":7200"));
        assertTrue(json.contains("\"userid\":\"admin\""));
        assertEquals("no-store", response.getHeader("Cache-Control"));
        assertEquals("no-cache", response.getHeader("Pragma"));
    }

    @Test
    public void tokenRejectsNonPostMethod() throws Exception {
        TestOAuthAction action = new TestOAuthAction();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/token");
        request.setParameter("grant_type", "authorization_code");
        request.setParameter("client_id", "client-a");
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.token(request, response);

        assertEquals(405, response.getStatus());
        assertEquals("POST", response.getHeader("Allow"));
        assertTrue(response.getContentAsString().contains("\"error\":\"invalid_request\""));
    }

    @Test
    public void userinfoRejectsMissingBearerToken() throws Exception {
        TestOAuthAction action = new TestOAuthAction();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/userinfo");
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.userinfo(request, response);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"error\":\"invalid_token\""));
    }

    @Test
    public void userinfoRejectsTokenWhenUserNoLongerExists() throws Exception {
        TestOAuthAction action = new TestOAuthAction();
        action.userExists = false;
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/userinfo");
        request.addHeader("Authorization", "Bearer token-a");
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.userinfo(request, response);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"error\":\"invalid_token\""));
    }

    @Test
    public void directFilterForwardsAuthorizePath() throws Exception {
        OAuthDirectFilter filter = new OAuthDirectFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/authorize");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals("/oauth/OAuthAction/authorize.shtml?_full_url=http%3A%2F%2Flocalhost%2Foauth%2Fauthorize",
                response.getForwardedUrl());
    }

    @Test
    public void directFilterPassesPublicFullUrlToForwardedAction() throws Exception {
        OAuthDirectFilter filter = new OAuthDirectFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/authorize");
        request.setServerName("127.0.0.1");
        request.setServerPort(18080);
        request.setParameter("response_type", "code");
        request.setParameter("client_id", "client-a");
        request.setParameter("redirect_uri", "http://client.example/callback");
        request.setQueryString("response_type=code&client_id=client-a&redirect_uri=http%3A%2F%2Fclient.example%2Fcallback");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(
                "/oauth/OAuthAction/authorize.shtml?_full_url=http%3A%2F%2F127.0.0.1%3A18080%2Foauth%2Fauthorize%3Fresponse_type%3Dcode%26client_id%3Dclient-a%26redirect_uri%3Dhttp%253A%252F%252Fclient.example%252Fcallback",
                response.getForwardedUrl());
    }

    @Test
    public void directFilterUsesForwardedHttpsUrlForPublicFullUrl() throws Exception {
        OAuthDirectFilter filter = new OAuthDirectFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/authorize");
        request.setServerName("bpmt-web");
        request.setServerPort(8080);
        request.setRequestURI("/oauth/authorize");
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "127.0.0.1:18443");
        request.setQueryString("response_type=code&client_id=client-a");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(
                "/oauth/OAuthAction/authorize.shtml?_full_url=https%3A%2F%2F127.0.0.1%3A18443%2Foauth%2Fauthorize%3Fresponse_type%3Dcode%26client_id%3Dclient-a",
                response.getForwardedUrl());
    }

    @Test
    public void directFilterDoesNotTrustIncomingFullUrlParameter() throws Exception {
        OAuthDirectFilter filter = new OAuthDirectFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/authorize");
        request.setServerName("bpmt-web");
        request.setServerPort(8080);
        request.setRequestURI("/oauth/authorize");
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "127.0.0.1:18443");
        request.setParameter(Actions.Keys.FULL_URL.toString(), "http://evil.example/callback");
        request.setQueryString("_full_url=http%3A%2F%2Fevil.example%2Fcallback&client_id=client-a");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(
                "/oauth/OAuthAction/authorize.shtml?_full_url=https%3A%2F%2F127.0.0.1%3A18443%2Foauth%2Fauthorize%3F_full_url%3Dhttp%253A%252F%252Fevil.example%252Fcallback%26client_id%3Dclient-a",
                response.getForwardedUrl());
    }

    @Test
    public void directFilterAllowsSwitchAccountActionPathToChain() throws Exception {
        OAuthDirectFilter filter = new OAuthDirectFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/oauth/OAuthAction/switchAccount.shtml");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(request, chain.getRequest());
        assertNull(response.getErrorMessage());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void directFilterAllowsCancelAccessDeniedActionPathToChain() throws Exception {
        OAuthDirectFilter filter = new OAuthDirectFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST",
                "/oauth/OAuthAction/cancelAccessDenied.shtml");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(request, chain.getRequest());
        assertNull(response.getErrorMessage());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void directFilterRejectsUnknownOAuthPath() throws Exception {
        OAuthDirectFilter filter = new OAuthDirectFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/not-found");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(chain.getRequest());
        assertEquals(404, response.getStatus());
    }

    private MockHttpServletRequest authorizeRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/authorize");
        request.setServerName("localhost");
        request.setServerPort(80);
        request.setParameter("response_type", "code");
        request.setParameter("client_id", "client-a");
        request.setParameter("redirect_uri", "http://client.example/callback");
        request.setParameter("state", "s-1");
        request.setQueryString("response_type=code&client_id=client-a&redirect_uri=http%3A%2F%2Fclient.example%2Fcallback&state=s-1");
        return request;
    }

    private File accessDeniedJspFile() {
        File repoPath = new File("platform/src/main/webapp/xhtml/oauth/access_denied.jsp");
        if (repoPath.isFile()) {
            return repoPath;
        }
        return new File("src/main/webapp/xhtml/oauth/access_denied.jsp");
    }

    private String readAccessDeniedJsp() throws IOException {
        return new String(Files.readAllBytes(accessDeniedJspFile().toPath()), StandardCharsets.UTF_8);
    }

    private Map<String, Object> accessDeniedContext() {
        Map<String, Object> context = new LinkedHashMap<String, Object>();
        context.put("requestId", "request-a");
        context.put("clientId", "client-a");
        context.put("thirdpartKey", "app-a");
        context.put("thirdpartName", "演示系统");
        context.put("userId", "oauth_no_pri");
        context.put("redirectUri", "http://client.example/callback");
        context.put("state", "s-1");
        context.put("returnUrl",
                "http://localhost/oauth/authorize?response_type=code&client_id=client-a&redirect_uri=http%3A%2F%2Fclient.example%2Fcallback&state=s-1");
        return context;
    }

    private static class TestOAuthAction extends OAuthAction {
        private final TestOAuthService service = new TestOAuthService();
        private boolean loggedIn;
        private boolean canAccess;
        private String currentUserId;
        private String code = "code-a";
        private String loginTarget;
        private String forwardedPage;
        private boolean logoutCalled;
        private boolean userExists = true;
        private OAuthWechatLoginResult wechatResult = OAuthWechatLoginResult.skip();
        private final Map<String, Object> tokenResult = new HashMap<String, Object>();

        @Override
        protected OAuthService getOAuthService() {
            service.action = this;
            return service;
        }

        @Override
        protected OAuthWechatLoginService getWechatLoginService() {
            return new OAuthWechatLoginService() {
                @Override
                public OAuthWechatLoginResult handle(javax.servlet.http.HttpServletRequest request,
                        javax.servlet.http.HttpServletResponse response, Map<String, Object> thirdpart) {
                    return wechatResult;
                }
            };
        }

        @Override
        protected boolean isLoggedIn(javax.servlet.http.HttpServletRequest request) {
            return loggedIn;
        }

        @Override
        protected String currentUserId(javax.servlet.http.HttpServletRequest request) {
            return currentUserId;
        }

        @Override
        protected void jumpToLogin(javax.servlet.http.HttpServletRequest request,
                javax.servlet.http.HttpServletResponse response) {
            loginTarget = "/login.jsp";
        }

        @Override
        protected void forwardErrorPage(javax.servlet.http.HttpServletRequest request,
                javax.servlet.http.HttpServletResponse response) {
            forwardedPage = "/xhtml/oauth/error.jsp";
        }

        @Override
        protected void forwardAccessDeniedPage(javax.servlet.http.HttpServletRequest request,
                javax.servlet.http.HttpServletResponse response) {
            forwardedPage = "/xhtml/oauth/access_denied.jsp";
        }

        @Override
        protected void logoutCurrentUser(javax.servlet.http.HttpServletRequest request) {
            logoutCalled = true;
        }

        @Override
        protected UsUser loadUser(String userId) {
            if (!userExists) {
                return null;
            }
            UsUser user = new UsUser();
            user.setUid(userId);
            user.setBusiName("管理员");
            return user;
        }

        @Override
        protected UsGroup loadDefaultGroup(String userId) {
            UsGroup group = new UsGroup();
            group.setGroupKey("root");
            group.setBusiName("默认组织");
            return group;
        }

        @Override
        protected UsRole loadDefaultRole(String userId) {
            UsRole role = new UsRole();
            role.setRoleKey("admin");
            role.setBusiName("管理员");
            return role;
        }
    }

    private static class TestOAuthService extends OAuthService {
        private TestOAuthAction action;

        @Override
        public Map<String, Object> validateAuthorize(String responseType, String clientId, String redirectUri) {
            Map<String, Object> result = new HashMap<String, Object>();
            Map<String, Object> thirdpart = new HashMap<String, Object>();
            thirdpart.put("clientId", clientId);
            thirdpart.put("thirdpartKey", "app-a");
            thirdpart.put("thirdpartName", "演示系统");
            result.put("thirdpart", thirdpart);
            result.put("requestId", "request-a");
            return result;
        }

        @Override
        public boolean currentUserCanAccess(Map<String, Object> thirdpart) {
            return action.canAccess;
        }

        @Override
        public String createAuthorizationCode(Map<String, Object> thirdpart, String userId, String redirectUri,
                String state) {
            return action.code;
        }

        @Override
        public Map<String, Object> createAuthorizationCodeResult(Map<String, Object> thirdpart, String userId,
                String redirectUri, String state) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("code", action.code);
            return result;
        }

        @Override
        public Map<String, Object> exchangeCode(String clientId, String clientSecret, String code, String redirectUri) {
            return action.tokenResult;
        }

        @Override
        public Map<String, Object> loadUserInfo(String bearerToken) {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("userid", "admin");
            result.put("client_id", "client-a");
            result.put("thirdpart_key", "app-a");
            return result;
        }
    }
}
