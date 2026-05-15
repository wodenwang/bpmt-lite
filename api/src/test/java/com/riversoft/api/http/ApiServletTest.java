package com.riversoft.api.http;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.riversoft.api.ApiServlet;
import com.riversoft.api.modules.database_operations.DatabaseOperationController;
import com.riversoft.api.modules.dynamic_tables.DynamicTableController;
import com.riversoft.api.modules.dynamic_table_views.DynamicTableViewController;
import com.riversoft.api.modules.report_views.ReportViewController;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ApiServletTest {

    @Test
    public void successResponseSerializesSuccessTrue() {
        String json = ApiJson.toJson(ApiResponse.success(Collections.singletonMap("name", "RV_TEST")));

        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"name\":\"RV_TEST\""));
    }

    @Test
    public void errorResponseSerializesStableCode() {
        ApiError error = new ApiError(
                "DYNAMIC_TABLE_ALREADY_EXISTS",
                "表已存在",
                Collections.<String, Object>emptyMap(),
                "req-1");

        String json = ApiJson.toJson(ApiResponse.error(error));

        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("\"code\":\"DYNAMIC_TABLE_ALREADY_EXISTS\""));
        assertTrue(json.contains("\"requestId\":\"req-1\""));
    }

    @Test
    public void unknownRouteReturnsJsonError() throws Exception {
        ApiServlet servlet = new ApiServlet();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/not-found");
        request.setPathInfo("/not-found");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service((javax.servlet.ServletRequest) request, (javax.servlet.ServletResponse) response);

        assertTrue(response.getContentAsString().contains("\"success\":false"));
        assertTrue(response.getContentAsString().contains("\"code\":\"API_ROUTE_NOT_FOUND\""));
        assertTrue(response.getStatus() == 404);
    }

    @Test
    public void unsupportedMethodReturnsJsonError() throws Exception {
        ApiServlet servlet = new ApiServlet();
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/v1/dynamic-tables");
        request.setPathInfo("/dynamic-tables");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service((javax.servlet.ServletRequest) request, (javax.servlet.ServletResponse) response);

        assertTrue(response.getContentAsString().contains("\"code\":\"API_METHOD_NOT_ALLOWED\""));
        assertTrue(response.getStatus() == 405);
    }

    @Test
    public void templatesRouteShouldNotBeCapturedAsDynamicTableName() throws Exception {
        DynamicTableController dynamicTableController = new DynamicTableController() {
            @Override
            public Map<String, Object> templates() {
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("items", Collections.singletonList("tpl-ok"));
                return result;
            }
        };
        ApiServlet servlet = new ApiServlet(dynamicTableController, new DatabaseOperationController());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/dynamic-tables/templates");
        request.setPathInfo("/dynamic-tables/templates");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service((javax.servlet.ServletRequest) request, (javax.servlet.ServletResponse) response);

        assertTrue(response.getStatus() == 200);
        assertTrue(response.getContentAsString().contains("\"tpl-ok\""));
    }

    @Test
    public void dynamicTableViewValidateRouteIsNotCapturedAsViewKey() throws Exception {
        ApiServlet servlet = servletWithDynamicTableViewRouteProbe();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/dynamic-table-views:validate");
        request.setPathInfo("/dynamic-table-views:validate");
        request.setContentType("application/json");
        request.setContent("{}".getBytes("UTF-8"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service((javax.servlet.ServletRequest) request, (javax.servlet.ServletResponse) response);

        assertTrue(response.getStatus() == 200);
        assertTrue(response.getContentAsString().contains("\"route\":\"validate\""));
    }

    @Test
    public void dynamicTableViewDetailDecodesViewKey() throws Exception {
        ApiServlet servlet = servletWithDynamicTableViewRouteProbe();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/dynamic-table-views/CRM%2DCUSTOMER");
        request.setPathInfo("/dynamic-table-views/CRM%2DCUSTOMER");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service((javax.servlet.ServletRequest) request, (javax.servlet.ServletResponse) response);

        assertTrue(response.getStatus() == 200);
        assertTrue(response.getContentAsString().contains("\"viewKey\":\"CRM-CUSTOMER\""));
    }

    @Test
    public void reportViewValidateRouteIsNotCapturedAsViewKey() throws Exception {
        ApiServlet servlet = servletWithReportViewRouteProbe();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/report-views:validate");
        request.setPathInfo("/report-views:validate");
        request.setContentType("application/json");
        request.setContent("{}".getBytes("UTF-8"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service((javax.servlet.ServletRequest) request, (javax.servlet.ServletResponse) response);

        assertTrue(response.getStatus() == 200);
        assertTrue(response.getContentAsString().contains("\"route\":\"report-validate\""));
    }

    @Test
    public void reportViewListRouteDispatchesToController() throws Exception {
        ApiServlet servlet = servletWithReportViewRouteProbe();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/report-views");
        request.setPathInfo("/report-views");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service((javax.servlet.ServletRequest) request, (javax.servlet.ServletResponse) response);

        assertTrue(response.getStatus() == 200);
        assertTrue(response.getContentAsString().contains("\"route\":\"report-list\""));
    }

    @Test
    public void reportViewCreateRouteDispatchesToController() throws Exception {
        ApiServlet servlet = servletWithReportViewRouteProbe();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/report-views");
        request.setPathInfo("/report-views");
        request.setContentType("application/json");
        request.setContent("{}".getBytes("UTF-8"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service((javax.servlet.ServletRequest) request, (javax.servlet.ServletResponse) response);

        assertTrue(response.getStatus() == 200);
        assertTrue(response.getContentAsString().contains("\"route\":\"report-create\""));
    }

    @Test
    public void reportViewDetailDecodesViewKey() throws Exception {
        ApiServlet servlet = servletWithReportViewRouteProbe();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/report-views/SALES%2DREPORT");
        request.setPathInfo("/report-views/SALES%2DREPORT");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service((javax.servlet.ServletRequest) request, (javax.servlet.ServletResponse) response);

        assertTrue(response.getStatus() == 200);
        assertTrue(response.getContentAsString().contains("\"viewKey\":\"SALES-REPORT\""));
    }

    @Test
    public void reportViewPatchRouteDecodesViewKeyAndSection() throws Exception {
        ApiServlet servlet = servletWithReportViewRouteProbe();
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/v1/report-views/SALES%2DREPORT/base");
        request.setPathInfo("/report-views/SALES%2DREPORT/base");
        request.setContentType("application/json");
        request.setContent("{}".getBytes("UTF-8"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service((javax.servlet.ServletRequest) request, (javax.servlet.ServletResponse) response);

        assertTrue(response.getStatus() == 200);
        assertTrue(response.getContentAsString().contains("\"viewKey\":\"SALES-REPORT\""));
        assertTrue(response.getContentAsString().contains("\"section\":\"base\""));
    }

    private ApiServlet servletWithDynamicTableViewRouteProbe() {
        DynamicTableViewController viewController = new DynamicTableViewController() {
            @Override
            public Map<String, Object> validate(ApiRequest request) {
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("route", "validate");
                return result;
            }

            @Override
            public Map<String, Object> detail(String viewKey) {
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("viewKey", viewKey);
                return result;
            }
        };
        return new ApiServlet(new DynamicTableController(), new DatabaseOperationController(), viewController);
    }

    private ApiServlet servletWithReportViewRouteProbe() {
        ReportViewController reportController = new ReportViewController() {
            @Override
            public Map<String, Object> list(ApiRequest request) {
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("route", "report-list");
                return result;
            }

            @Override
            public Map<String, Object> create(ApiRequest request) {
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("route", "report-create");
                return result;
            }

            @Override
            public Map<String, Object> validate(ApiRequest request) {
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("route", "report-validate");
                return result;
            }

            @Override
            public Map<String, Object> detail(String viewKey) {
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("viewKey", viewKey);
                return result;
            }

            @Override
            public Map<String, Object> patch(String viewKey, String section, ApiRequest request) {
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                result.put("viewKey", viewKey);
                result.put("section", section);
                return result;
            }
        };
        return new ApiServlet(new DynamicTableController(), new DatabaseOperationController(),
                new DynamicTableViewController(), reportController);
    }
}
