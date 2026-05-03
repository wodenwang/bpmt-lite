package com.riversoft.module.thirdpart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.riversoft.core.db.DataPackage;
import com.riversoft.core.web.annotation.ActionAccess;
import com.riversoft.core.web.annotation.ActionAccess.SafeLevel;
import com.riversoft.platform.po.CmPri;

public class ThirdpartActionTest {

    @Test
    public void accessAnnotationsUseDevReadAndDevWrite() throws Exception {
        ActionAccess classAccess = ThirdpartAction.class.getAnnotation(ActionAccess.class);
        assertNotNull(classAccess);
        assertEquals(SafeLevel.DEV_R, classAccess.level());
        assertEquals(SafeLevel.DEV_W,
                ThirdpartAction.class.getMethod("submitForm", javax.servlet.http.HttpServletRequest.class,
                        javax.servlet.http.HttpServletResponse.class).getAnnotation(ActionAccess.class).level());
        assertEquals(SafeLevel.DEV_W,
                ThirdpartAction.class.getMethod("toggleActive", javax.servlet.http.HttpServletRequest.class,
                        javax.servlet.http.HttpServletResponse.class).getAnnotation(ActionAccess.class).level());
    }

    @Test
    public void indexIncludesMainPage() {
        TestThirdpartAction action = new TestThirdpartAction();

        action.index(request(), new MockHttpServletResponse());

        assertEquals("main.jsp", action.includedPage);
    }

    @Test
    public void listQueriesCmThirdpartAndIncludesListPage() {
        TestThirdpartAction action = new TestThirdpartAction();
        action.queryResult = packageOf(thirdpart("app-a", 1));
        MockHttpServletRequest request = request();

        action.list(request, new MockHttpServletResponse());

        assertEquals("CmThirdpart", action.queryEntityName);
        assertEquals(action.queryResult, request.getAttribute("dp"));
        assertEquals("list.jsp", action.includedPage);
    }

    @Test
    public void createZoneIncludesFormPage() {
        TestThirdpartAction action = new TestThirdpartAction();

        action.createZone(request(), new MockHttpServletResponse());

        assertEquals("form.jsp", action.includedPage);
    }

    @Test
    public void editZoneLoadsThirdpartByKey() {
        TestThirdpartAction action = new TestThirdpartAction();
        Map<String, Object> vo = thirdpart("app-a", 1);
        action.entities.put("app-a", vo);
        MockHttpServletRequest request = request();
        request.setParameter("thirdpartKey", "app-a");

        action.editZone(request, new MockHttpServletResponse());

        assertEquals("app-a", action.findKey);
        assertEquals(vo, request.getAttribute("vo"));
        assertEquals("form.jsp", action.includedPage);
    }

    @Test
    public void submitFormCreatesThirdpartAndShowsOneTimeSecret() {
        TestThirdpartAction action = new TestThirdpartAction();
        action.createdSecret = "plain-secret";
        MockHttpServletRequest request = filledRequest();

        action.submitForm(request, new MockHttpServletResponse());

        assertEquals("create", action.writeMode);
        assertEquals("app-a", action.input.get("thirdpartKey"));
        assertEquals("外部系统 A", action.input.get("thirdpartName"));
        assertEquals("client-a", action.input.get("clientId"));
        assertEquals("http://client.example/callback", action.input.get("redirectUris"));
        assertEquals("1", action.input.get("activeFlag"));
        assertEquals(action.pri, action.writtenPri);
        assertTrue(action.redirectMessage.indexOf("plain-secret") >= 0);
    }

    @Test
    public void submitFormUpdatesThirdpartAndAcceptsOptionalSecret() {
        TestThirdpartAction action = new TestThirdpartAction();
        action.entities.put("app-a", thirdpart("app-a", 1));
        MockHttpServletRequest request = filledRequest();
        request.setParameter("clientSecret", "new-secret");

        action.submitForm(request, new MockHttpServletResponse());

        assertEquals("update", action.writeMode);
        assertEquals("app-a", action.updatedKey);
        assertEquals("new-secret", action.input.get("clientSecret"));
        assertTrue(action.redirectMessage.indexOf("保存成功") >= 0);
        assertTrue(action.redirectMessage.indexOf("new-secret") < 0);
    }

    @Test
    public void toggleActiveUsesRequestedFlag() {
        TestThirdpartAction action = new TestThirdpartAction();
        action.entities.put("app-a", thirdpart("app-a", 1));
        MockHttpServletRequest request = request();
        request.setParameter("thirdpartKey", "app-a");
        request.setParameter("activeFlag", "0");

        action.toggleActive(request, new MockHttpServletResponse());

        assertEquals(Integer.valueOf(0), action.input.get("activeFlag"));
        assertEquals("update", action.writeMode);
        assertTrue(action.redirectMessage.indexOf("停用") >= 0);
    }

    @Test
    public void toggleActiveFlipsCurrentFlagWhenRequestFlagMissing() {
        TestThirdpartAction action = new TestThirdpartAction();
        action.entities.put("app-a", thirdpart("app-a", 1));
        MockHttpServletRequest request = request();
        request.setParameter("thirdpartKey", "app-a");

        action.toggleActive(request, new MockHttpServletResponse());

        assertEquals(Integer.valueOf(0), action.input.get("activeFlag"));
    }

    private static MockHttpServletRequest filledRequest() {
        MockHttpServletRequest request = request();
        request.setParameter("thirdpartKey", "app-a");
        request.setParameter("thirdpartName", "外部系统 A");
        request.setParameter("clientId", "client-a");
        request.setParameter("redirectUris", "http://client.example/callback");
        request.setParameter("homeUrl", "http://client.example/");
        request.setParameter("activeFlag", "1");
        request.setParameter("description", "demo");
        return request;
    }

    private static MockHttpServletRequest request() {
        return new MockHttpServletRequest("GET", "/thirdpart/ThirdpartAction/index.shtml");
    }

    private static DataPackage packageOf(Map<String, Object> entity) {
        DataPackage dp = new DataPackage();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list.add(entity);
        dp.setList(list);
        dp.setStart(0);
        dp.setLimit(20);
        dp.setTotalRecord(1);
        return dp;
    }

    private static Map<String, Object> thirdpart(String thirdpartKey, int activeFlag) {
        Map<String, Object> entity = new HashMap<String, Object>();
        entity.put("thirdpartKey", thirdpartKey);
        entity.put("thirdpartName", "外部系统 A");
        entity.put("clientId", "client-a");
        entity.put("redirectUris", "http://client.example/callback");
        entity.put("activeFlag", Integer.valueOf(activeFlag));
        return entity;
    }

    private static class TestThirdpartAction extends ThirdpartAction {
        private final ThirdpartService service = new TestThirdpartService(this);
        private final Map<String, Map<String, Object>> entities = new HashMap<String, Map<String, Object>>();
        private final CmPri pri = new CmPri();
        private DataPackage queryResult = new DataPackage();
        private String includedPage;
        private String redirectMessage;
        private String queryEntityName;
        private String findKey;
        private String writeMode;
        private String updatedKey;
        private String createdSecret = "generated-secret";
        private Map<String, Object> input;
        private CmPri writtenPri;

        @Override
        protected ThirdpartService service() {
            return service;
        }

        @Override
        protected DataPackage queryAll(javax.servlet.http.HttpServletRequest request) {
            queryEntityName = "CmThirdpart";
            return queryResult;
        }

        @Override
        protected Map<String, Object> findByPk(String thirdpartKey) {
            findKey = thirdpartKey;
            return entities.get(thirdpartKey);
        }

        @Override
        protected CmPri readPri(javax.servlet.http.HttpServletRequest request) {
            return pri;
        }

        @Override
        protected void includePage(javax.servlet.http.HttpServletRequest request,
                javax.servlet.http.HttpServletResponse response, String page) {
            includedPage = page;
        }

        @Override
        protected void redirectInfoPage(javax.servlet.http.HttpServletRequest request,
                javax.servlet.http.HttpServletResponse response, String message) {
            redirectMessage = message;
        }
    }

    private static class TestThirdpartService extends ThirdpartService {
        private final TestThirdpartAction action;

        TestThirdpartService(TestThirdpartAction action) {
            this.action = action;
        }

        @Override
        public String createThirdpart(Map<String, Object> input, CmPri pri) {
            action.writeMode = "create";
            action.input = input;
            action.writtenPri = pri;
            return action.createdSecret;
        }

        @Override
        public void updateThirdpart(String thirdpartKey, Map<String, Object> input, CmPri pri) {
            action.writeMode = "update";
            action.updatedKey = thirdpartKey;
            action.input = input;
            action.writtenPri = pri;
        }
    }
}
