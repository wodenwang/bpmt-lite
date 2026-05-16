package com.riversoft.module.oauth.wechat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.riversoft.core.context.SessionContext;
import com.riversoft.module.thirdpart.ThirdpartService;
import com.riversoft.platform.SessionManager.SessionAttributeKey;
import com.riversoft.platform.po.UsUser;

public class OAuthWechatLoginServiceTest {

    @Test
    public void skipsWhenRequestIsNotFromWechat() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = request();

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.SKIP, result.getStatus());
        assertEquals(0, provider.authorizationCalls);
        assertEquals(0, provider.loginCalls);
    }

    @Test
    public void skipsWhenWechatLoginIsDisabled() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        Map<String, Object> thirdpart = agentThirdpart();
        thirdpart.put("wechatLoginEnabled", Integer.valueOf(0));

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), thirdpart);

        assertEquals(OAuthWechatLoginStatus.SKIP, result.getStatus());
        assertEquals(0, provider.authorizationCalls);
        assertEquals(0, provider.loginCalls);
    }

    @Test
    public void redirectsAgentWhenCodeIsMissing() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        provider.authorizationUrl = "https://wechat.example/oauth";
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        request.setServerName("127.0.0.1");
        request.setServerPort(18080);
        request.setQueryString("client_id=client-a");

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.REDIRECT, result.getStatus());
        assertEquals("https://wechat.example/oauth", result.getRedirectUrl());
        assertEquals("agent", provider.wechatType);
        assertEquals("corp-agent", provider.wechatKey);
        assertNull(provider.wechatScope);
        assertEquals("http://127.0.0.1:18080/oauth/authorize?client_id=client-a", provider.callbackUrl);
    }

    @Test
    public void logsInAgentWhenCodeIsPresent() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        provider.userId = "admin";
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        request.setParameter("code", "secret-code");

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.LOGGED_IN, result.getStatus());
        assertEquals("admin", result.getUserId());
        assertEquals(0, provider.authorizationCalls);
        assertEquals(1, provider.loginCalls);
        assertEquals("secret-code", provider.code);
    }

    @Test
    public void loginRefreshesCurrentSessionContext() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        provider.userId = "admin";
        provider.sessionAttributeName = "USER";
        provider.sessionAttributeValue = "admin";
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        request.setParameter("code", "secret-code");
        SessionContext.init(request.getSession(), new HashMap<String, Object>());

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.LOGGED_IN, result.getStatus());
        assertEquals("admin", SessionContext.getCurrent().get("USER"));
    }

    @Test
    public void returnsErrorForInvalidConfig() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        Map<String, Object> thirdpart = agentThirdpart();
        thirdpart.put("wechatType", "");

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), thirdpart);

        assertEquals(OAuthWechatLoginStatus.ERROR, result.getStatus());
        assertEquals("wechat_config_invalid", result.getReason());
        assertEquals(0, provider.authorizationCalls);
        assertEquals(0, provider.loginCalls);
    }

    @Test
    public void returnsConfigErrorWhenProviderCannotBuildAuthorizationUrl() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        provider.authorizationFailure = new OAuthWechatConfigException("WxMp配置不存在.");
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.ERROR, result.getStatus());
        assertEquals("wechat_config_invalid", result.getReason());
        assertEquals(1, provider.authorizationCalls);
        assertEquals(0, provider.loginCalls);
    }

    @Test
    public void returnsConfigErrorWhenProviderCannotLoginByCode() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        provider.loginFailure = new OAuthWechatConfigException("WxMp配置不完整.");
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        request.setParameter("code", "secret-code");

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.ERROR, result.getStatus());
        assertEquals("wechat_config_invalid", result.getReason());
        assertEquals(0, provider.authorizationCalls);
        assertEquals(1, provider.loginCalls);
    }

    @Test
    public void rejectsIncompleteMpConfigBeforeWechatSdkCall() {
        Map<String, Object> mpConfig = new HashMap<String, Object>();
        mpConfig.put("mpKey", "service-mp");
        mpConfig.put("appId", "wx-app");
        mpConfig.put("visitorTable", "WX_VISITOR");

        try {
            RealWechatOAuthProvider.validateMpConfig(mpConfig);
            fail("expected OAuthWechatConfigException");
        } catch (OAuthWechatConfigException e) {
            assertEquals("WxMp配置不完整: appSecret不能为空.", e.getMessage());
        }
    }

    @Test
    public void rejectsAgentConfigWithoutAgentIdBeforeWechatSdkCall() {
        Map<String, Object> agentConfig = agentConfig();
        agentConfig.remove("agentId");

        try {
            RealWechatOAuthProvider.validateAgentConfig(agentConfig, "corp-id", "corp-secret");
            fail("expected OAuthWechatConfigException");
        } catch (OAuthWechatConfigException e) {
            assertEquals("WxAgent配置不完整: agentId不能为空.", e.getMessage());
        }
    }

    @Test
    public void rejectsAgentConfigWithoutCorpIdBeforeWechatSdkCall() {
        Map<String, Object> agentConfig = agentConfig();

        try {
            RealWechatOAuthProvider.validateAgentConfig(agentConfig, " ", "corp-secret");
            fail("expected OAuthWechatConfigException");
        } catch (OAuthWechatConfigException e) {
            assertEquals("WxAgent配置不完整: wx.qy.corpId不能为空.", e.getMessage());
        }
    }

    @Test
    public void rejectsAgentConfigWithoutAgentSecretOrFallbackBeforeWechatSdkCall() {
        Map<String, Object> agentConfig = agentConfig();
        agentConfig.remove("agentSecret");

        try {
            RealWechatOAuthProvider.validateAgentConfig(agentConfig, "corp-id", " ");
            fail("expected OAuthWechatConfigException");
        } catch (OAuthWechatConfigException e) {
            assertEquals("WxAgent配置不完整: agentSecret不能为空.", e.getMessage());
        }
    }

    @Test
    public void acceptsAgentConfigWithFallbackSecretBeforeWechatSdkCall() {
        Map<String, Object> agentConfig = agentConfig();
        agentConfig.remove("agentSecret");

        RealWechatOAuthProvider.AgentOAuthConfig validated = RealWechatOAuthProvider.validateAgentConfig(agentConfig,
                "corp-id", "corp-secret");

        assertEquals("corp-agent", validated.getAgentKey());
        assertEquals("100001", validated.getAgentId());
        assertEquals("corp-id", validated.getCorpId());
        assertEquals("corp-secret", validated.getSecret());
    }

    @Test
    public void realProviderReadsMpLoggedInUserFromHttpSession() {
        MockHttpServletRequest request = wechatRequest();
        UsUser user = new UsUser();
        user.setUid("mp-user");
        request.getSession().setAttribute(SessionAttributeKey.USER.toString(), user);
        SessionContext.init(request.getSession(), new HashMap<String, Object>());

        assertEquals("mp-user", RealWechatOAuthProvider.userFromSession(request).getUid());
    }

    @Test
    public void returnsErrorWhenProviderCannotLogin() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        provider.loginFailure = new RuntimeException("provider down");
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        request.setParameter("code", "secret-code");

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.ERROR, result.getStatus());
        assertEquals("wechat_login_failed", result.getReason());
        assertEquals(1, provider.loginCalls);
    }

    @Test
    public void returnsErrorWhenProviderReturnsBlankUserId() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        provider.userId = " ";
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        request.setParameter("code", "secret-code");

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.ERROR, result.getStatus());
        assertEquals("wechat_login_failed", result.getReason());
        assertEquals(1, provider.loginCalls);
    }

    @Test
    public void fakeProviderAppendsDefaultCodeToCallbackWithExistingQuery() {
        FakeWechatOAuthProvider provider = new FakeWechatOAuthProvider();

        String redirectUrl = provider.buildAuthorizationUrl("agent", "fake-agent", null,
                "http://127.0.0.1/oauth/authorize?client_id=wechat-smoke-client");

        assertEquals("http://127.0.0.1/oauth/authorize?client_id=wechat-smoke-client&code=fake-admin",
                redirectUrl);
    }

    @Test
    public void fakeProviderLogsInAdminByControlledCode() {
        final String[] loggedInUserId = new String[1];
        FakeWechatOAuthProvider provider = new FakeWechatOAuthProvider(new FakeWechatOAuthProvider.LoginSessionWriter() {
            public void doUserLogin(javax.servlet.http.HttpServletRequest request, String userId) {
                loggedInUserId[0] = userId;
            }
        });
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        request.setParameter("code", "fake-admin");

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.LOGGED_IN, result.getStatus());
        assertEquals("admin", result.getUserId());
        assertEquals("admin", loggedInUserId[0]);
    }

    @Test
    public void fakeProviderInvalidCodeReturnsWechatLoginFailed() {
        FakeWechatOAuthProvider provider = new FakeWechatOAuthProvider();
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        request.setParameter("code", "fake-invalid");

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.ERROR, result.getStatus());
        assertEquals("wechat_login_failed", result.getReason());
    }

    @Test
    public void classifiesBpmtPauseDuringWechatLogin() {
        OAuthWechatLoginException failure = OAuthWechatLoginFailureClassifier.classify("woden",
                new RuntimeException("系统维护中,暂停用户登陆."));

        assertEquals("bpmt_login_paused", failure.getReason());
        assertEquals("woden", failure.getUserId());
        assertEquals("微信授权已成功，但 BPMT 当前处于维护/暂停模式，用户[woden]无法建立登录态。请管理员检查 safe.role 或 safe.admin 配置。",
                failure.getSafeMessage());
    }

    @Test
    public void classifiesBpmtUserNotFoundDuringWechatLogin() {
        OAuthWechatLoginException failure = OAuthWechatLoginFailureClassifier.classify("woden",
                new RuntimeException("找不到用户[woden]."));

        assertEquals("bpmt_user_not_found", failure.getReason());
        assertEquals("微信授权已成功，但 BPMT 中找不到用户[woden]。请管理员检查企业微信 UserId 与 BPMT 用户账号映射。",
                failure.getSafeMessage());
    }

    @Test
    public void classifiesBpmtUserDisabledDuringWechatLogin() {
        OAuthWechatLoginException failure = OAuthWechatLoginFailureClassifier.classify("woden",
                new RuntimeException("用户[woden]账号已失效."));

        assertEquals("bpmt_user_disabled", failure.getReason());
        assertEquals("微信授权已成功，但 BPMT 用户[woden]账号已失效。请管理员启用用户或更换绑定账号。",
                failure.getSafeMessage());
    }

    @Test
    public void classifiesBpmtIpDeniedDuringWechatLogin() {
        OAuthWechatLoginException failure = OAuthWechatLoginFailureClassifier.classify("woden",
                new RuntimeException("用户[woden]当前网络环境不安全,请更换网络环境登陆."));

        assertEquals("bpmt_user_ip_denied", failure.getReason());
        assertEquals("微信授权已成功，但 BPMT 拒绝了用户[woden]当前网络环境。请管理员检查用户 IP 白名单或上游代理地址。",
                failure.getSafeMessage());
    }

    @Test
    public void classifiesBpmtRelationshipInvalidDuringWechatLogin() {
        OAuthWechatLoginException failure = OAuthWechatLoginFailureClassifier.classify("woden",
                new RuntimeException("无法找到用户[woden]归属的组织与角色,无法登陆系统."));

        assertEquals("bpmt_user_relationship_invalid", failure.getReason());
        assertEquals("微信授权已成功，但 BPMT 用户[woden]的组织或角色关系不可用。请管理员检查用户所属组织、角色和权限组。",
                failure.getSafeMessage());
    }

    @Test
    public void classifiesUnknownBpmtLoginFailureDuringWechatLogin() {
        OAuthWechatLoginException failure = OAuthWechatLoginFailureClassifier.classify("woden",
                new RuntimeException("unexpected local login failure"));

        assertEquals("bpmt_login_failed", failure.getReason());
        assertEquals("微信授权已成功，但 BPMT 本地登录态建立失败。请联系管理员并提供 Request ID。", failure.getSafeMessage());
    }

    @Test
    public void returnsClassifiedBpmtLoginFailureWhenProviderReportsLocalLoginFailure() {
        TestWechatOAuthProvider provider = new TestWechatOAuthProvider();
        provider.loginFailure = OAuthWechatLoginFailureClassifier.classify("woden",
                new RuntimeException("系统维护中,暂停用户登陆."));
        OAuthWechatLoginService service = new OAuthWechatLoginService(provider);
        MockHttpServletRequest request = wechatRequest();
        request.setParameter("code", "secret-code");

        OAuthWechatLoginResult result = service.handle(request, new MockHttpServletResponse(), agentThirdpart());

        assertEquals(OAuthWechatLoginStatus.ERROR, result.getStatus());
        assertEquals("bpmt_login_paused", result.getReason());
        assertEquals("微信授权已成功，但 BPMT 当前处于维护/暂停模式，用户[woden]无法建立登录态。请管理员检查 safe.role 或 safe.admin 配置。",
                result.getMessage());
        assertEquals(1, provider.loginCalls);
    }

    @Test
    public void fakeProviderSwitchAcceptsOnlyExplicitTrueValues() {
        assertEquals(false, OAuthWechatLoginService.isFakeProviderEnabled("false", null));
        assertEquals(false, OAuthWechatLoginService.isFakeProviderEnabled(null, "0"));
        assertEquals(true, OAuthWechatLoginService.isFakeProviderEnabled("true", null));
        assertEquals(true, OAuthWechatLoginService.isFakeProviderEnabled(null, "1"));
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/oauth/authorize");
        request.setRequestURI("/oauth/authorize");
        request.setServerName("localhost");
        request.setServerPort(80);
        return request;
    }

    private MockHttpServletRequest wechatRequest() {
        MockHttpServletRequest request = request();
        request.addHeader("user-agent", "Mozilla/5.0 MicroMessenger");
        return request;
    }

    private Map<String, Object> agentThirdpart() {
        Map<String, Object> thirdpart = new HashMap<String, Object>();
        thirdpart.put("thirdpartKey", "demo-app");
        thirdpart.put("clientId", "client-a");
        thirdpart.put("wechatLoginEnabled", Integer.valueOf(1));
        thirdpart.put("wechatType", ThirdpartService.WECHAT_TYPE_AGENT);
        thirdpart.put("wechatKey", "corp-agent");
        thirdpart.put("wechatScope", ThirdpartService.WECHAT_SCOPE_USERINFO);
        return thirdpart;
    }

    private Map<String, Object> agentConfig() {
        Map<String, Object> agentConfig = new HashMap<String, Object>();
        agentConfig.put("agentKey", "corp-agent");
        agentConfig.put("agentId", Integer.valueOf(100001));
        agentConfig.put("agentSecret", "agent-secret");
        return agentConfig;
    }

    private static class TestWechatOAuthProvider implements WechatOAuthProvider {
        private String authorizationUrl = "https://wechat.example/oauth";
        private String userId = "admin";
        private RuntimeException authorizationFailure;
        private RuntimeException loginFailure;
        private int authorizationCalls;
        private int loginCalls;
        private String wechatType;
        private String wechatKey;
        private String wechatScope;
        private String callbackUrl;
        private String code;
        private String sessionAttributeName;
        private Object sessionAttributeValue;

        public String buildAuthorizationUrl(String wechatType, String wechatKey, String wechatScope,
                String callbackUrl) {
            authorizationCalls++;
            this.wechatType = wechatType;
            this.wechatKey = wechatKey;
            this.wechatScope = wechatScope;
            this.callbackUrl = callbackUrl;
            if (authorizationFailure != null) {
                throw authorizationFailure;
            }
            return authorizationUrl;
        }

        public String loginByCode(javax.servlet.http.HttpServletRequest request, String wechatType, String wechatKey,
                String wechatScope, String code) {
            loginCalls++;
            this.wechatType = wechatType;
            this.wechatKey = wechatKey;
            this.wechatScope = wechatScope;
            this.code = code;
            if (loginFailure != null) {
                throw loginFailure;
            }
            if (sessionAttributeName != null) {
                request.getSession().setAttribute(sessionAttributeName, sessionAttributeValue);
            }
            return userId;
        }
    }
}
