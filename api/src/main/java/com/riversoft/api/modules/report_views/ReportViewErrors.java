package com.riversoft.api.modules.report_views;

import com.riversoft.api.http.ApiException;

final class ReportViewErrors {
    private ReportViewErrors() {
    }

    static ApiException notFound(String viewKey) {
        return new ApiException(404, "REPORT_VIEW_NOT_FOUND", "报表视图不存在：" + viewKey);
    }

    static ApiException notRepList(String viewKey) {
        return new ApiException(409, "REPORT_VIEW_NOT_REP_LIST", "目标视图不是 rep_list 报表视图：" + viewKey);
    }

    static ApiException alreadyExists(String viewKey) {
        return new ApiException(409, "REPORT_VIEW_ALREADY_EXISTS", "报表视图已存在：" + viewKey);
    }

    static ApiException invalidSnapshot(String message) {
        return new ApiException(400, "REPORT_VIEW_INVALID_SNAPSHOT", message);
    }

    static ApiException invalidSqlConfig(String message) {
        return new ApiException(400, "REPORT_VIEW_INVALID_SQL_CONFIG", message);
    }

    static ApiException invalidScriptConfig(String message) {
        return new ApiException(400, "REPORT_VIEW_INVALID_SCRIPT_CONFIG", message);
    }

    static ApiException confirmRequired() {
        return new ApiException(400, "REPORT_VIEW_CONFIRM_REQUIRED", "删除报表视图必须传入 confirmViewKey 并与 viewKey 一致。");
    }

    static ApiException unsupportedPermission(String message) {
        return new ApiException(400, "REPORT_VIEW_UNSUPPORTED_PERMISSION", message);
    }

    static ApiException unsupportedSection(String section) {
        return new ApiException(400, "REPORT_VIEW_UNSUPPORTED_SECTION", "不支持的报表视图区块：" + section);
    }
}
