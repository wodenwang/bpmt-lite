package com.riversoft.api.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ApiDocsContractTest {

    @Test
    public void openApiJsonPublishesDynamicTableRoutes() throws Exception {
        JsonNode root = new ObjectMapper().readTree(apiFile("src/main/webapp/openapi.json"));

        assertTrue(root.path("paths").has("/v1/dynamic-tables"));
        assertTrue(root.path("paths").has("/v1/dynamic-tables/{name}"));
        assertTrue(root.path("paths").has("/v1/dynamic-tables/{name}/ddl:sync"));
        assertTrue(root.path("paths").has("/v1/dynamic-tables/templates"));
        assertTrue(root.path("paths").has("/v1/database-operations/query"));
        assertTrue(root.path("paths").has("/v1/database-operations/find"));
        assertTrue(root.path("paths").has("/v1/database-operations/save"));
        assertTrue(root.path("paths").has("/v1/database-operations/exec"));
        assertTrue(root.path("components").path("securitySchemes").has("signatureHeader"));
        assertTrue(root.path("paths").path("/v1/dynamic-tables").path("post").has("x-bpmt-writes-metadata"));
        assertTrue(root.path("paths").path("/v1/dynamic-tables").path("post").has("x-bpmt-executes-ddl"));
        assertTrue(root.path("paths").path("/v1/dynamic-tables").path("post").has("x-bpmt-risk-level"));
        assertQueryParameter(root, "/v1/dynamic-tables", "get", "sort");
        assertQueryParameter(root, "/v1/dynamic-tables", "get", "order");
    }

    @Test
    public void openApiJsonPublishesDynamicTableViewRouteMatrix() throws Exception {
        JsonNode root = new ObjectMapper().readTree(apiFile("src/main/webapp/openapi.json"));

        assertOnlyMethods(root, "/v1/dynamic-table-views", "get", "post");
        assertOnlyMethods(root, "/v1/dynamic-table-views:validate", "post");
        assertOnlyMethods(root, "/v1/dynamic-table-views/{viewKey}", "get", "put", "delete");
        assertOnlyMethods(root, "/v1/dynamic-table-views/{viewKey}/{section}", "patch");

        assertRisk(root, "/v1/dynamic-table-views", "get", false, false, "read");
        assertRisk(root, "/v1/dynamic-table-views", "post", true, false, "high");
        assertRisk(root, "/v1/dynamic-table-views:validate", "post", false, false, "read");
        assertRisk(root, "/v1/dynamic-table-views/{viewKey}", "get", false, false, "read");
        assertRisk(root, "/v1/dynamic-table-views/{viewKey}", "put", true, false, "high");
        assertRisk(root, "/v1/dynamic-table-views/{viewKey}", "delete", true, false, "high");
        assertRisk(root, "/v1/dynamic-table-views/{viewKey}/{section}", "patch", true, false, "high");

        assertQueryParameter(root, "/v1/dynamic-table-views", "post", "dryRun");
        assertQueryParameter(root, "/v1/dynamic-table-views/{viewKey}", "put", "dryRun");
        assertQueryParameter(root, "/v1/dynamic-table-views/{viewKey}/{section}", "patch", "dryRun");
        assertQueryParameter(root, "/v1/dynamic-table-views/{viewKey}", "delete", "confirmViewKey");

        JsonNode codes = root.path("components").path("schemas").path("DynamicTableViewErrorCode").path("enum");
        assertArrayContains(codes, "DYNAMIC_TABLE_VIEW_CONFIRM_REQUIRED");

        assertUnsupportedPermissionsDescription(root.path("paths").path("/v1/dynamic-table-views:validate").path("post").path("description"),
                "查询", "变量", "处理器");
        assertUnsupportedPermissionsDescription(root.path("components").path("schemas").path("DynamicTableViewQueries").path("description"),
                "查询");
        assertUnsupportedPermissionsDescription(root.path("components").path("schemas").path("DynamicTableViewVariables").path("description"),
                "变量");
        assertUnsupportedPermissionsDescription(root.path("components").path("schemas").path("DynamicTableViewProcessors").path("description"),
                "处理器");
    }

    @Test
    public void openApiJsonPublishesReportViewRouteMatrix() throws Exception {
        JsonNode root = new ObjectMapper().readTree(apiFile("src/main/webapp/openapi.json"));

        assertOnlyMethods(root, "/v1/report-views", "get", "post");
        assertOnlyMethods(root, "/v1/report-views:validate", "post");
        assertOnlyMethods(root, "/v1/report-views/{viewKey}", "get", "put", "delete");
        assertOnlyMethods(root, "/v1/report-views/{viewKey}/{section}", "patch");

        assertRisk(root, "/v1/report-views", "get", false, false, "read");
        assertRisk(root, "/v1/report-views", "post", true, false, "high");
        assertRisk(root, "/v1/report-views:validate", "post", false, false, "read");
        assertRisk(root, "/v1/report-views/{viewKey}", "get", false, false, "read");
        assertRisk(root, "/v1/report-views/{viewKey}", "put", true, false, "high");
        assertRisk(root, "/v1/report-views/{viewKey}", "delete", true, false, "high");
        assertRisk(root, "/v1/report-views/{viewKey}/{section}", "patch", true, false, "high");

        assertQueryParameter(root, "/v1/report-views", "post", "dryRun");
        assertQueryParameter(root, "/v1/report-views/{viewKey}", "put", "dryRun");
        assertQueryParameter(root, "/v1/report-views/{viewKey}/{section}", "patch", "dryRun");
        assertQueryParameter(root, "/v1/report-views/{viewKey}", "delete", "confirmViewKey");

        JsonNode codes = root.path("components").path("schemas").path("ReportViewErrorCode").path("enum");
        assertArrayContains(codes, "REPORT_VIEW_NOT_REP_LIST");
        assertArrayContains(codes, "REPORT_VIEW_UNSUPPORTED_PERMISSION");
        assertArrayContains(codes, "REPORT_VIEW_INVALID_SQL_CONFIG");
        assertArrayContains(codes, "REPORT_VIEW_INVALID_SCRIPT_CONFIG");

        JsonNode warnings = root.path("components").path("schemas").path("ReportViewWarningCode").path("enum");
        assertArrayContains(warnings, "SQL_SCRIPT_PRESENT");
        assertArrayContains(warnings, "CLIENT_SCRIPT_PRESENT");
        assertArrayContains(warnings, "BUTTON_ACTION_PRESENT");
        assertArrayContains(warnings, "EXTERNAL_DB_KEY_PRESENT");
        assertArrayContains(warnings, "UNEXECUTED_SQL_SEMANTICS");

        assertUnsupportedPermissionsDescription(root.path("paths").path("/v1/report-views:validate").path("post").path("description"),
                "查询", "变量", "SQL");
        assertTrue(root.path("components").path("schemas").has("ReportViewSnapshot"));
        assertTrue(root.path("components").path("schemas").has("ReportViewWritePlan"));
        assertTrue(root.path("components").path("schemas").has("ReportViewValidationResult"));
        assertArrayDoesNotContain(root.path("components").path("schemas").path("ReportViewSnapshot").path("required"),
                "viewKey");
        assertTrue(root.path("components").path("schemas").path("ReportViewSnapshot")
                .path("properties").path("viewKey").path("description").asText().contains("可选"));
    }

    @Test
    public void docsIndexDescribesReportViewApi() throws Exception {
        String html = read(apiFile("src/main/webapp/docs/index.html"));

        assertTrue(html.contains("报表视图 API"));
        assertTrue(html.contains("/api/v1/report-views"));
        assertTrue(html.contains("/api/v1/report-views:validate"));
        assertTrue(html.contains("/api/v1/report-views/SALES_REPORT/columns?dryRun=true"));
        assertTrue(html.contains("REPORT_VIEW_NOT_REP_LIST"));
        assertTrue(html.contains("REPORT_VIEW_UNSUPPORTED_PERMISSION"));
        assertTrue(html.contains("API 不执行 SQL"));
    }

    @Test
    public void openApiJsonDoesNotPublishInventedDynamicTableViewRoutes() throws Exception {
        JsonNode root = new ObjectMapper().readTree(apiFile("src/main/webapp/openapi.json"));
        JsonNode paths = root.path("paths");

        assertFalse(paths.has("/v1/dynamic-table-views/{viewKey}/dry-run"));
        assertFalse(paths.has("/v1/dynamic-table-views/{viewKey}/sections/{section}"));
        assertFalse(paths.has("/v1/dynamic-table-views/{viewKey}/sections/{section}/dry-run"));

        Iterator<String> names = paths.fieldNames();
        while (names.hasNext()) {
            String path = names.next();
            if (path.startsWith("/v1/dynamic-table-views")) {
                assertFalse(path.contains("/dry-run"));
                assertFalse(path.contains("/sections"));
            }
        }
    }

    @Test
    public void docsIndexLinksOpenApiJson() throws Exception {
        String html = read(apiFile("src/main/webapp/docs/index.html"));

        assertTrue(html.contains("../openapi.json"));
        assertTrue(html.contains("X-BPMT-Signature"));
        assertTrue(html.contains("动态表视图 API"));
        assertTrue(html.contains("只管理 dyn 动态表视图"));
        assertTrue(html.contains("删除视图不会删除动态表和业务数据"));
        assertTrue(html.contains("查询、变量、处理器"));
        assertTrue(html.contains("会被校验拒绝"));
        assertTrue(html.contains("每个 curl 示例都需要按当前 METHOD、PATH、QUERY 和 BODY 重新计算 X-BPMT-Signature"));
    }

    @Test
    public void versionedOpenApiSnapshotRemainsValidJson() throws Exception {
        JsonNode versioned = new ObjectMapper().readTree(rootFile("docs/v1.7.0/openapi.json"));

        assertTrue(versioned.path("paths").has("/v1/dynamic-table-views"));
        assertFalse(versioned.path("paths").has("/v1/report-views"));
    }

    @Test
    public void openApiInternalRefsResolve() throws Exception {
        JsonNode root = new ObjectMapper().readTree(apiFile("src/main/webapp/openapi.json"));
        List<String> refs = new ArrayList<String>();
        collectRefs(root, refs);

        for (String ref : refs) {
            if (!ref.startsWith("#/")) {
                continue;
            }
            assertResolvableRef(root, ref);
        }
    }

    private void assertOnlyMethods(JsonNode root, String path, String... methods) {
        JsonNode node = root.path("paths").path(path);
        assertTrue("Missing path " + path, node.isObject());

        int count = 0;
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String method = fieldNames.next();
            assertArrayContains(methods, method);
            count++;
        }
        assertEquals("Unexpected method count for " + path, methods.length, count);
    }

    private void assertRisk(JsonNode root, String path, String method, boolean writes, boolean ddl, String risk) {
        JsonNode operation = root.path("paths").path(path).path(method);
        assertTrue("missing x-bpmt-writes-metadata " + method + " " + path,
                operation.has("x-bpmt-writes-metadata"));
        assertTrue("missing x-bpmt-executes-ddl " + method + " " + path,
                operation.has("x-bpmt-executes-ddl"));
        assertTrue("missing x-bpmt-risk-level " + method + " " + path,
                operation.has("x-bpmt-risk-level"));
        assertEquals("writes metadata " + method + " " + path, writes, operation.path("x-bpmt-writes-metadata").asBoolean());
        assertEquals("executes ddl " + method + " " + path, ddl, operation.path("x-bpmt-executes-ddl").asBoolean());
        assertEquals("risk level " + method + " " + path, risk, operation.path("x-bpmt-risk-level").asText());
    }

    private void assertQueryParameter(JsonNode root, String path, String method, String parameterName) {
        JsonNode parameters = root.path("paths").path(path).path(method).path("parameters");
        assertTrue("Missing parameters for " + method + " " + path, parameters.isArray());
        for (JsonNode parameter : parameters) {
            if (parameterName.equals(parameter.path("name").asText())) {
                assertEquals("query", parameter.path("in").asText());
                return;
            }
        }
        fail("Missing query parameter " + parameterName + " on " + method + " " + path);
    }

    private void assertUnsupportedPermissionsDescription(JsonNode description, String... sections) {
        String text = description.asText();
        for (String section : sections) {
            assertTrue(text, text.contains(section));
        }
        assertTrue(text, text.contains("permissions"));
        assertTrue(text, text.contains("拒绝"));
    }

    private void assertArrayContains(JsonNode array, String value) {
        assertTrue("Expected array for " + value, array.isArray());
        for (JsonNode item : array) {
            if (value.equals(item.asText())) {
                return;
            }
        }
        fail("Missing array value " + value);
    }

    private void assertArrayDoesNotContain(JsonNode array, String value) {
        assertTrue("Expected array for " + value, array.isArray());
        for (JsonNode item : array) {
            if (value.equals(item.asText())) {
                fail("Unexpected array value " + value);
            }
        }
    }

    private void assertArrayContains(String[] array, String value) {
        for (String item : array) {
            if (item.equals(value)) {
                return;
            }
        }
        fail("Unexpected array value " + value);
    }

    private void collectRefs(JsonNode node, List<String> refs) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if ("$ref".equals(field.getKey())) {
                    refs.add(field.getValue().asText());
                }
                collectRefs(field.getValue(), refs);
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectRefs(child, refs);
            }
        }
    }

    private void assertResolvableRef(JsonNode root, String ref) {
        JsonNode current = root;
        String[] parts = ref.substring(2).split("/");
        for (String part : parts) {
            current = current.path(unescapeJsonPointer(part));
            if (current.isMissingNode()) {
                fail("OpenAPI $ref does not resolve: " + ref);
            }
        }
    }

    private String unescapeJsonPointer(String value) {
        return value.replace("~1", "/").replace("~0", "~");
    }

    private String read(File file) throws Exception {
        return new String(Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));
    }

    private File apiFile(String relativeToApi) throws Exception {
        File base = basedir();
        File direct = new File(base, relativeToApi);
        if (direct.isFile()) {
            return direct;
        }
        return new File(new File(base, "api"), relativeToApi);
    }

    private File rootFile(String relativeToRoot) throws Exception {
        File base = basedir();
        File direct = new File(base, relativeToRoot);
        if (direct.isFile()) {
            return direct;
        }
        return new File(base.getParentFile(), relativeToRoot);
    }

    private File basedir() throws Exception {
        return new File(System.getProperty("basedir", ".")).getCanonicalFile();
    }
}
