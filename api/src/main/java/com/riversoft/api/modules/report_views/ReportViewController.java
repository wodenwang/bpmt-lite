package com.riversoft.api.modules.report_views;

import com.riversoft.api.http.ApiRequest;

import java.util.Map;

public class ReportViewController {
    private final ReportViewService service;

    public ReportViewController() {
        this(new ReportViewService());
    }

    public ReportViewController(ReportViewService service) {
        this.service = service;
    }

    public Map<String, Object> list(ApiRequest request) {
        return service.list(request.getParameter("start"), request.getParameter("limit"));
    }

    public Map<String, Object> create(ApiRequest request) {
        return service.create(request.readJson(ReportViewSnapshot.class), dryRun(request));
    }

    public Map<String, Object> detail(String viewKey) {
        return service.export(viewKey);
    }

    public Map<String, Object> replace(String viewKey, ApiRequest request) {
        return service.replace(viewKey, request.readJson(ReportViewSnapshot.class), dryRun(request));
    }

    public Map<String, Object> patch(String viewKey, String section, ApiRequest request) {
        Object body = request.readJson(Object.class);
        return service.patch(viewKey, ReportViewSection.parse(section), body, dryRun(request));
    }

    public Map<String, Object> delete(String viewKey, ApiRequest request) {
        return service.delete(viewKey, request.getParameter("confirmViewKey"));
    }

    public Map<String, Object> validate(ApiRequest request) {
        return service.validate(request.readJson(ReportViewSnapshot.class));
    }

    private boolean dryRun(ApiRequest request) {
        String value = request.getParameter("dryRun");
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }
}
