package com.riversoft.api;

import com.riversoft.api.modules.dynamic_tables.DynamicTableController;
import com.riversoft.api.modules.dynamic_table_views.DynamicTableViewController;
import com.riversoft.api.modules.database_operations.DatabaseOperationController;
import com.riversoft.api.modules.report_views.ReportViewController;
import com.riversoft.api.http.ApiError;
import com.riversoft.api.http.ApiException;
import com.riversoft.api.http.ApiJson;
import com.riversoft.api.http.ApiRequest;
import com.riversoft.api.http.ApiResponse;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.UUID;

public class ApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final DynamicTableController dynamicTableController;
    private final DatabaseOperationController databaseOperationController;
    private final DynamicTableViewController dynamicTableViewController;
    private final ReportViewController reportViewController;

    public ApiServlet() {
        this(new DynamicTableController(), new DatabaseOperationController(), new DynamicTableViewController(),
                new ReportViewController());
    }

    public ApiServlet(DynamicTableController dynamicTableController,
                      DatabaseOperationController databaseOperationController) {
        this(dynamicTableController, databaseOperationController, new DynamicTableViewController(),
                new ReportViewController());
    }

    public ApiServlet(DynamicTableController dynamicTableController,
                      DatabaseOperationController databaseOperationController,
                      DynamicTableViewController dynamicTableViewController) {
        this(dynamicTableController, databaseOperationController, dynamicTableViewController,
                new ReportViewController());
    }

    public ApiServlet(DynamicTableController dynamicTableController,
                      DatabaseOperationController databaseOperationController,
                      DynamicTableViewController dynamicTableViewController,
                      ReportViewController reportViewController) {
        this.dynamicTableController = dynamicTableController;
        this.databaseOperationController = databaseOperationController;
        this.dynamicTableViewController = dynamicTableViewController;
        this.reportViewController = reportViewController;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestId = UUID.randomUUID().toString();
        try {
            Object data = dispatch(new ApiRequest(request));
            writeJson(response, HttpServletResponse.SC_OK, ApiResponse.success(data));
        } catch (ApiException e) {
            writeError(response, e, requestId);
        } catch (Exception e) {
            writeError(response, new ApiException(500, "INTERNAL_ERROR", "API 请求处理失败。"), requestId);
        }
    }

    private Object dispatch(ApiRequest request) {
        String method = StringUtils.upperCase(request.getMethod());
        String path = normalizePath(request.getPathInfo());

        if ("/report-views".equals(path)) {
            if ("GET".equals(method)) {
                return reportViewController.list(request);
            }
            if ("POST".equals(method)) {
                return reportViewController.create(request);
            }
            throw methodNotAllowed();
        }

        if ("/report-views:validate".equals(path)) {
            if ("POST".equals(method)) {
                return reportViewController.validate(request);
            }
            throw methodNotAllowed();
        }

        if (path.startsWith("/report-views/")) {
            String tail = path.substring("/report-views/".length());
            String[] parts = tail.split("/");
            if (parts.length == 1 && StringUtils.isNotBlank(parts[0])) {
                String viewKey = decode(parts[0]);
                if ("GET".equals(method)) {
                    return reportViewController.detail(viewKey);
                }
                if ("PUT".equals(method)) {
                    return reportViewController.replace(viewKey, request);
                }
                if ("DELETE".equals(method)) {
                    return reportViewController.delete(viewKey, request);
                }
                throw methodNotAllowed();
            }
            if (parts.length == 2 && StringUtils.isNotBlank(parts[0]) && StringUtils.isNotBlank(parts[1])) {
                if ("PATCH".equals(method)) {
                    return reportViewController.patch(decode(parts[0]), decode(parts[1]), request);
                }
                throw methodNotAllowed();
            }
        }

        if ("/dynamic-table-views".equals(path)) {
            if ("GET".equals(method)) {
                return dynamicTableViewController.list(request);
            }
            if ("POST".equals(method)) {
                return dynamicTableViewController.create(request);
            }
            throw methodNotAllowed();
        }

        if ("/dynamic-table-views:validate".equals(path)) {
            if ("POST".equals(method)) {
                return dynamicTableViewController.validate(request);
            }
            throw methodNotAllowed();
        }

        if (path.startsWith("/dynamic-table-views/")) {
            String tail = path.substring("/dynamic-table-views/".length());
            String[] parts = tail.split("/");
            if (parts.length == 1 && StringUtils.isNotBlank(parts[0])) {
                String viewKey = decode(parts[0]);
                if ("GET".equals(method)) {
                    return dynamicTableViewController.detail(viewKey);
                }
                if ("PUT".equals(method)) {
                    return dynamicTableViewController.replace(viewKey, request);
                }
                if ("DELETE".equals(method)) {
                    return dynamicTableViewController.delete(viewKey, request);
                }
                throw methodNotAllowed();
            }
            if (parts.length == 2 && StringUtils.isNotBlank(parts[0]) && StringUtils.isNotBlank(parts[1])) {
                if ("PATCH".equals(method)) {
                    return dynamicTableViewController.patch(decode(parts[0]), decode(parts[1]), request);
                }
                throw methodNotAllowed();
            }
        }

        if ("/dynamic-tables".equals(path)) {
            if ("GET".equals(method)) {
                return dynamicTableController.list(request);
            }
            if ("POST".equals(method)) {
                return dynamicTableController.create(request);
            }
            throw methodNotAllowed();
        }

        if ("/dynamic-tables/templates".equals(path)) {
            if ("GET".equals(method)) {
                return dynamicTableController.templates();
            }
            throw methodNotAllowed();
        }

        if (path.startsWith("/dynamic-tables/templates/")) {
            String tail = path.substring("/dynamic-tables/templates/".length());
            if (StringUtils.isBlank(tail)) {
                throw new ApiException(404, "API_ROUTE_NOT_FOUND", "API 路由不存在。");
            }
            if (tail.endsWith(":create-table")) {
                if ("POST".equals(method)) {
                    String templateCode = decode(tail.substring(0, tail.length() - ":create-table".length()));
                    return dynamicTableController.createFromTemplate(templateCode, request);
                }
                throw methodNotAllowed();
            }
            if ("GET".equals(method)) {
                return dynamicTableController.templateDetail(decode(tail));
            }
            throw methodNotAllowed();
        }

        if (path.startsWith("/dynamic-tables/")) {
            String tail = path.substring("/dynamic-tables/".length());
            String[] parts = tail.split("/");
            if (parts.length == 1) {
                String name = decode(parts[0]);
                if ("GET".equals(method)) {
                    return dynamicTableController.detail(name);
                }
                if ("PUT".equals(method)) {
                    return dynamicTableController.update(name, request);
                }
                throw methodNotAllowed();
            }
            if (parts.length == 2 && "ddl:sync".equals(parts[1])) {
                if ("POST".equals(method)) {
                    return dynamicTableController.syncDdl(decode(parts[0]));
                }
                throw methodNotAllowed();
            }
        }

        if ("/database-operations/query".equals(path)) {
            if ("POST".equals(method)) {
                return databaseOperationController.query(request);
            }
            throw methodNotAllowed();
        }

        if ("/database-operations/find".equals(path)) {
            if ("POST".equals(method)) {
                return databaseOperationController.find(request);
            }
            throw methodNotAllowed();
        }

        if ("/database-operations/save".equals(path)) {
            if ("POST".equals(method)) {
                return databaseOperationController.save(request);
            }
            throw methodNotAllowed();
        }

        if ("/database-operations/exec".equals(path)) {
            if ("POST".equals(method)) {
                return databaseOperationController.exec(request);
            }
            throw methodNotAllowed();
        }

        throw new ApiException(404, "API_ROUTE_NOT_FOUND", "API 路由不存在。");
    }

    private String normalizePath(String pathInfo) {
        if (StringUtils.isBlank(pathInfo) || "/".equals(pathInfo)) {
            return "/";
        }
        String path = pathInfo.trim();
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ApiException(400, "API_INVALID_PATH", "API 路径无法解析。");
        }
    }

    private ApiException methodNotAllowed() {
        return new ApiException(405, "API_METHOD_NOT_ALLOWED", "当前 API 路由不支持该 HTTP 方法。");
    }

    private void writeError(HttpServletResponse response, ApiException exception, String requestId) throws IOException {
        ApiError error = new ApiError(
                exception.getCode(),
                exception.getMessage(),
                exception.getDetails() == null ? Collections.<String, Object>emptyMap() : exception.getDetails(),
                requestId);
        writeJson(response, exception.getStatus(), ApiResponse.error(error));
    }

    private void writeJson(HttpServletResponse response, int status, ApiResponse payload) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(ApiJson.toJson(payload));
    }
}
