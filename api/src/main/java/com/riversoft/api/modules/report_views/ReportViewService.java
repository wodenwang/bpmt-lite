package com.riversoft.api.modules.report_views;

import com.riversoft.api.http.ApiException;

import java.util.Map;

class ReportViewService {
    Map<String, Object> list(String start, String limit) {
        throw notImplemented();
    }

    Map<String, Object> create(ReportViewSnapshot snapshot, boolean dryRun) {
        throw notImplemented();
    }

    Map<String, Object> export(String viewKey) {
        throw notImplemented();
    }

    Map<String, Object> replace(String viewKey, ReportViewSnapshot snapshot, boolean dryRun) {
        throw notImplemented();
    }

    Map<String, Object> patch(String viewKey, ReportViewSection section, Object body, boolean dryRun) {
        throw notImplemented();
    }

    Map<String, Object> delete(String viewKey, String confirmViewKey) {
        throw notImplemented();
    }

    Map<String, Object> validate(ReportViewSnapshot snapshot) {
        throw notImplemented();
    }

    private ApiException notImplemented() {
        return new ApiException(501, "REPORT_VIEW_NOT_IMPLEMENTED", "报表视图 API 尚未实现。");
    }
}
