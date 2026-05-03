package com.riversoft.module.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.riversoft.core.db.DataPO;
import com.riversoft.core.exception.ExceptionType;
import com.riversoft.core.exception.SystemRuntimeException;

public class MenuActionThirdpartTest {

    @Test
    public void submitMenuFormUsesThirdpartUrlWhenOpenTypeIsExternalIframe() {
        TestMenuAction action = new TestMenuAction();
        MockHttpServletRequest request = filledRequest();
        request.setParameter("openType", "2");
        request.setParameter("action", "view[MENU]-should-not-win");
        request.setParameter("thirdpartUrl", "https://example.local/app");

        action.submitMenuForm(request, new MockHttpServletResponse());

        assertEquals("save", action.writeMode);
        assertEquals("https://example.local/app", action.writtenMenu.get("action"));
        assertEquals(Integer.valueOf(2), action.writtenMenu.get("openType"));
        assertTrue(action.redirectMessage.indexOf("编辑菜单[第三方菜单]成功") >= 0);
    }

    @Test
    public void submitMenuFormRejectsInvalidThirdpartUrl() {
        TestMenuAction action = new TestMenuAction();
        MockHttpServletRequest request = filledRequest();
        request.setParameter("openType", "2");
        request.setParameter("thirdpartUrl", "javascript:alert(1)");

        assertRejectsThirdpartUrl(action, request);
    }

    @Test
    public void submitMenuFormRejectsThirdpartUrlWithAttributeBreakingCharacters() {
        TestMenuAction action = new TestMenuAction();
        MockHttpServletRequest request = filledRequest();
        request.setParameter("openType", "2");
        request.setParameter("thirdpartUrl", "https://x\" autofocus onfocus=\"alert(1)");

        assertRejectsThirdpartUrl(action, request);
    }

    @Test
    public void submitMenuFormRejectsProtocolRelativeThirdpartUrl() {
        TestMenuAction action = new TestMenuAction();
        MockHttpServletRequest request = filledRequest();
        request.setParameter("openType", "2");
        request.setParameter("thirdpartUrl", "//evil.example/path");

        assertRejectsThirdpartUrl(action, request);
    }

    @Test
    public void submitMenuFormAcceptsSiteLocalThirdpartPath() {
        TestMenuAction action = new TestMenuAction();
        MockHttpServletRequest request = filledRequest();
        request.setParameter("openType", "2");
        request.setParameter("thirdpartUrl", "/thirdpart/app");

        action.submitMenuForm(request, new MockHttpServletResponse());

        assertEquals("/thirdpart/app", action.writtenMenu.get("action"));
    }

    private void assertRejectsThirdpartUrl(TestMenuAction action, MockHttpServletRequest request) {
        try {
            action.submitMenuForm(request, new MockHttpServletResponse());
        } catch (SystemRuntimeException e) {
            assertEquals(ExceptionType.BUSINESS, e.getType());
            assertTrue(e.getExtMessage().indexOf("第三方网页地址格式不正确") >= 0);
            return;
        }
        throw new AssertionError("Expected SystemRuntimeException");
    }

    @Test
    public void submitMenuFormKeepsViewActionWhenOpenTypeIsNotExternalIframe() {
        TestMenuAction action = new TestMenuAction();
        MockHttpServletRequest request = filledRequest();
        request.setParameter("openType", "1");
        request.setParameter("action", "dyn/DemoAction/list.shtml");
        request.setParameter("thirdpartUrl", "https://example.local/app");

        action.submitMenuForm(request, new MockHttpServletResponse());

        assertEquals("dyn/DemoAction/list.shtml", action.writtenMenu.get("action"));
        assertEquals(Integer.valueOf(1), action.writtenMenu.get("openType"));
    }

    @Test
    public void frameMenusOpenExternalIframeWithDomApi() throws IOException {
        assertFrameMenuHandlesExternalIframe("src/main/webapp/xhtml/frame/menu.jsp");
        assertFrameMenuHandlesExternalIframe("src/main/webapp/xhtml/frame_new/menu.jsp");
    }

    private void assertFrameMenuHandlesExternalIframe(String path) throws IOException {
        String jsp = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        assertTrue(jsp.indexOf("case 2://第三方网页") >= 0);
        assertTrue(jsp.indexOf("document.createElement(\"iframe\")") >= 0);
        assertTrue(jsp.indexOf("openThirdpartFrame(action)") >= 0);
    }

    private static MockHttpServletRequest filledRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/manager/MenuAction/submitMenuForm.shtml");
        request.setParameter("isCreate", "1");
        request.setParameter("id", "thirdpart-menu");
        request.setParameter("name", "第三方菜单");
        request.setParameter("icon", "world.png");
        request.setParameter("action", "view[MENU]-default");
        request.setParameter("domainKey", "sys");
        request.setParameter("parentId", "");
        request.setParameter("sort", "10");
        request.setParameter("paramType", "0");
        request.setParameter("paramScript", "");
        return request;
    }

    private static class TestMenuAction extends MenuAction {
        private final MenuService service = new TestMenuService(this);
        private String writeMode;
        private Map<String, Object> writtenMenu;
        private String redirectMessage;

        @Override
        protected DataPO newMenuPO() {
            return new DataPO("CmMenu");
        }

        @Override
        protected MenuService menuService() {
            return service;
        }

        @Override
        protected void redirectInfoPage(javax.servlet.http.HttpServletRequest request,
                javax.servlet.http.HttpServletResponse response, String message) {
            redirectMessage = message;
        }
    }

    private static class TestMenuService extends MenuService {
        private final TestMenuAction action;

        TestMenuService(TestMenuAction action) {
            this.action = action;
        }

        @Override
        public void save(Map<String, Object> po) {
            action.writeMode = "save";
            action.writtenMenu = po;
        }

        @Override
        public void update(Map<String, Object> po) {
            action.writeMode = "update";
            action.writtenMenu = po;
        }
    }
}
