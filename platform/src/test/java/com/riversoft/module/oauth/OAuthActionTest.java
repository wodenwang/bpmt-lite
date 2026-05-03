package com.riversoft.module.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.riversoft.platform.po.UsGroup;
import com.riversoft.platform.po.UsRole;
import com.riversoft.platform.po.UsUser;

public class OAuthActionTest {

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
    public void directFilterForwardsAuthorizePath() throws Exception {
        OAuthDirectFilter filter = new OAuthDirectFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/authorize");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals("/oauth/OAuthAction/authorize.shtml", response.getForwardedUrl());
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

    private static class TestOAuthAction extends OAuthAction {
        private final TestOAuthService service = new TestOAuthService();
        private boolean loggedIn;
        private boolean canAccess;
        private String currentUserId;
        private String code = "code-a";
        private String loginTarget;
        private final Map<String, Object> tokenResult = new HashMap<String, Object>();

        @Override
        protected OAuthService getOAuthService() {
            service.action = this;
            return service;
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
        protected UsUser loadUser(String userId) {
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
