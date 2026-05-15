package com.riversoft.api.modules.report_views;

import com.riversoft.platform.po.VwUrl;

import java.util.List;
import java.util.Map;

interface ReportViewRepository {
    List<VwUrl> listReportUrls(int start, int limit);

    int countReportUrls();

    VwUrl findUrl(String viewKey);

    Map<String, Object> findReport(String viewKey);

    VwUrl saveUrl(VwUrl url);

    void updateUrl(VwUrl url);

    void createViewConfig(VwUrl url, Map<String, Object> reportMap, ReportViewResponse.WritePlan plan);

    void replaceViewConfig(VwUrl url, Map<String, Object> reportMap, ReportViewResponse.WritePlan plan);

    void patchViewConfig(VwUrl url, ReportViewSection section, Map<String, Object> reportMap,
                         ReportViewResponse.WritePlan plan);

    void removeViewConfig(String viewKey, ReportViewResponse.WritePlan plan);

    void flushAndClearViewCache(String viewKey);
}
