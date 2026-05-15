package com.riversoft.api.modules.report_views;

import java.util.LinkedHashMap;
import java.util.Map;

class ReportViewService {
    Map<String, Object> list(String start, String limit) {
        return new LinkedHashMap<String, Object>();
    }

    Map<String, Object> create(ReportViewSnapshot snapshot, boolean dryRun) {
        return new LinkedHashMap<String, Object>();
    }

    Map<String, Object> export(String viewKey) {
        return new LinkedHashMap<String, Object>();
    }

    Map<String, Object> replace(String viewKey, ReportViewSnapshot snapshot, boolean dryRun) {
        return new LinkedHashMap<String, Object>();
    }

    Map<String, Object> patch(String viewKey, ReportViewSection section, Object body, boolean dryRun) {
        return new LinkedHashMap<String, Object>();
    }

    Map<String, Object> delete(String viewKey, String confirmViewKey) {
        return new LinkedHashMap<String, Object>();
    }

    Map<String, Object> validate(ReportViewSnapshot snapshot) {
        return new LinkedHashMap<String, Object>();
    }
}
