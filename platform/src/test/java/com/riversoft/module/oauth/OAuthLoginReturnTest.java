package com.riversoft.module.oauth;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.riversoft.core.exception.ExceptionType;
import com.riversoft.core.exception.SystemRuntimeException;
import com.riversoft.module.frame.LoginAction;

public class OAuthLoginReturnTest {

    @Test
    public void successfulLoginReturnsOAuthAuthorizeUrlAndClearsSessionAttribute() throws Exception {
        TestLoginAction action = new TestLoginAction();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/frame/LoginAction/login.shtml");
        request.getSession().setAttribute(OAuthSessionKeys.RETURN_URL,
                "http://localhost/oauth/authorize?client_id=client-a");
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.login(request, response);

        String json = response.getContentAsString();
        assertTrue(json.contains("\"flag\":true"));
        assertTrue(json.contains("\"redirectUrl\":\"http://localhost/oauth/authorize?client_id=client-a\""));
        assertNull(request.getSession().getAttribute(OAuthSessionKeys.RETURN_URL));
    }

    @Test
    public void successfulLoginWithoutOAuthReturnKeepsExistingSuccessJson() throws Exception {
        TestLoginAction action = new TestLoginAction();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/frame/LoginAction/login.shtml");
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.login(request, response);

        String json = response.getContentAsString();
        assertEquals("{\"flag\":true}", json.trim());
        assertFalse(json.contains("redirectUrl"));
    }

    @Test
    public void failedLoginDoesNotClearOAuthReturnUrl() throws Exception {
        TestLoginAction action = new TestLoginAction();
        action.failLogin = true;
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/frame/LoginAction/login.shtml");
        request.getSession().setAttribute(OAuthSessionKeys.RETURN_URL,
                "http://localhost/oauth/authorize?client_id=client-a");
        MockHttpServletResponse response = new MockHttpServletResponse();

        action.login(request, response);

        String json = response.getContentAsString();
        assertTrue(json.contains("\"flag\":false"));
        assertFalse(json.contains("redirectUrl"));
        assertTrue(json.contains("用户名或密码错误"));
        assertTrue(action.loginCalled);
        assertEquals("http://localhost/oauth/authorize?client_id=client-a",
                request.getSession().getAttribute(OAuthSessionKeys.RETURN_URL));
    }

    private static class TestLoginAction extends LoginAction {
        private boolean failLogin;
        private boolean loginCalled;

        @Override
        protected void doUserLogin(HttpServletRequest request) {
            loginCalled = true;
            if (failLogin) {
                throw new SystemRuntimeException(ExceptionType.INFO, "用户名或密码错误");
            }
        }
    }
}
