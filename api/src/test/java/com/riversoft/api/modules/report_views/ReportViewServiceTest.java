package com.riversoft.api.modules.report_views;

import com.riversoft.api.http.ApiException;
import com.riversoft.platform.po.VwUrl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReportViewServiceTest {
    @Test
    public void listReturnsReportUrlsWithPaging() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedReport(reportSnapshot("SALES_REPORT"));
        ReportViewService service = new ReportViewService(repository);

        Map<String, Object> result = service.list("0", "10");

        assertEquals(Integer.valueOf(0), result.get("start"));
        assertEquals(Integer.valueOf(10), result.get("limit"));
        assertEquals(Integer.valueOf(1), result.get("totalRecord"));
        assertEquals(1, ((List<?>) result.get("items")).size());
    }

    @Test
    public void dryRunCreateDoesNotWriteRepository() {
        RecordingRepository repository = new RecordingRepository();
        ReportViewService service = new ReportViewService(repository);

        Map<String, Object> result = service.create(reportSnapshot("SALES_REPORT"), true);

        assertEquals(0, repository.createAttempts);
        assertEquals(0, repository.flushes);
        ReportViewResponse.WritePlan plan = (ReportViewResponse.WritePlan) result.get("plan");
        assertTrue(plan.isDryRun());
        assertTrue(plan.getCreates().contains("VW_URL"));
        assertTrue(plan.getCreates().contains("VW_REPORT"));
    }

    @Test
    public void createWritesUrlAndReportWhenNotDryRun() {
        RecordingRepository repository = new RecordingRepository();
        ReportViewService service = new ReportViewService(repository);

        service.create(reportSnapshot("SALES_REPORT"), false);

        assertEquals(1, repository.createAttempts);
        assertEquals("SALES_REPORT", repository.createdUrl.getViewKey());
        assertTrue(repository.reports.containsKey("SALES_REPORT"));
        assertEquals(1, repository.flushes);
    }

    @Test
    public void exportReturnsSnapshotForSeededReport() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedReport(reportSnapshot("SALES_REPORT"));
        ReportViewService service = new ReportViewService(repository);

        Map<String, Object> result = service.export("SALES_REPORT");

        ReportViewSnapshot exported = (ReportViewSnapshot) result.get("snapshot");
        assertEquals("SALES_REPORT", exported.getViewKey());
        assertEquals("销售报表", exported.getBase().getDisplayName());
        assertNotNull(exported.getBase().getMainSql());
    }

    @Test
    public void validateInvalidReturnsValidFalseWithoutRepositoryWrite() {
        RecordingRepository repository = new RecordingRepository();
        ReportViewService service = new ReportViewService(repository);
        ReportViewSnapshot invalid = reportSnapshot("SALES_REPORT");
        invalid.getBase().setDisplayName(null);

        Map<String, Object> result = service.validate(invalid);

        assertEquals(Boolean.FALSE, result.get("valid"));
        assertFalse(((List<?>) result.get("errors")).isEmpty());
        assertEquals(0, repository.createAttempts);
        assertEquals(0, repository.replaceAttempts);
        assertEquals(0, repository.patchAttempts);
        assertEquals(0, repository.removeAttempts);
    }

    @Test
    public void replaceRejectsNonReportView() {
        RecordingRepository repository = new RecordingRepository();
        repository.urls.put("DYN_VIEW", url("DYN_VIEW", "dyn"));
        ReportViewService service = new ReportViewService(repository);

        try {
            service.replace("DYN_VIEW", reportSnapshot("DYN_VIEW"), false);
            fail("Expected non report view error");
        } catch (ApiException e) {
            assertEquals("REPORT_VIEW_NOT_REP_LIST", e.getCode());
            assertEquals(409, e.getStatus());
        }
    }

    @Test
    public void replaceRealPathCallsRepositoryReplaceAndFlushes() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedReport(reportSnapshot("SALES_REPORT"));
        ReportViewService service = new ReportViewService(repository);

        service.replace("SALES_REPORT", reportSnapshot("SALES_REPORT"), false);

        assertEquals(1, repository.replaceAttempts);
        assertEquals("SALES_REPORT", repository.replacedUrl.getViewKey());
        assertTrue(repository.replacedReports.containsKey("SALES_REPORT"));
        assertEquals(1, repository.flushes);
    }

    @Test
    public void patchColumnsDryRunReturnsUpdatedSectionAndDoesNotWrite() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedReport(reportSnapshot("SALES_REPORT"));
        ReportViewService service = new ReportViewService(repository);
        ReportViewSnapshot.Columns columns = new ReportViewSnapshot.Columns();

        Map<String, Object> result = service.patch("SALES_REPORT", ReportViewSection.COLUMNS, columns, true);

        ReportViewResponse.WritePlan plan = (ReportViewResponse.WritePlan) result.get("plan");
        assertEquals(Collections.singletonList("columns"), plan.getUpdatedSections());
        assertEquals(0, repository.patchAttempts);
        assertEquals(0, repository.flushes);
    }

    @Test
    public void patchRealPathCallsRepositoryPatchAndFlushes() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedReport(reportSnapshot("SALES_REPORT"));
        ReportViewService service = new ReportViewService(repository);
        ReportViewSnapshot.Columns columns = new ReportViewSnapshot.Columns();

        service.patch("SALES_REPORT", ReportViewSection.COLUMNS, columns, false);

        assertEquals(1, repository.patchAttempts);
        assertEquals(ReportViewSection.COLUMNS, repository.patchedSections.get(0));
        assertTrue(repository.patchedReports.containsKey("SALES_REPORT"));
        assertEquals(1, repository.flushes);
    }

    @Test
    public void deleteRequiresConfirmViewKey() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedReport(reportSnapshot("SALES_REPORT"));
        ReportViewService service = new ReportViewService(repository);

        try {
            service.delete("SALES_REPORT", "OTHER");
            fail("Expected confirm error");
        } catch (ApiException e) {
            assertEquals("REPORT_VIEW_CONFIRM_REQUIRED", e.getCode());
        }
        assertEquals(0, repository.removeAttempts);
    }

    @Test
    public void deleteConfirmedCallsRemoveAndReturnsSafeFlags() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedReport(reportSnapshot("SALES_REPORT"));
        ReportViewService service = new ReportViewService(repository);

        Map<String, Object> result = service.delete("SALES_REPORT", "SALES_REPORT");

        assertEquals(1, repository.removeAttempts);
        assertEquals(Collections.singletonList("SALES_REPORT"), repository.removedViewKeys);
        assertEquals(Boolean.TRUE, result.get("deleted"));
        assertEquals(Boolean.FALSE, result.get("businessDataDeleted"));
        assertEquals(Boolean.FALSE, result.get("menuDeleted"));
        assertEquals(Boolean.FALSE, result.get("externalEntriesDeleted"));
        assertEquals(1, repository.flushes);
    }

    private static ReportViewSnapshot reportSnapshot(String viewKey) {
        ReportViewSnapshot snapshot = new ReportViewSnapshot();
        snapshot.setViewKey(viewKey);
        snapshot.setDescription("销售报表");
        snapshot.setLoginRequired(true);
        snapshot.getBase().setDisplayName("销售报表");
        snapshot.getBase().setMainSql(script("select * from SALE_ORDER where 1=1"));
        snapshot.getBase().setLayoutColumns(Integer.valueOf(2));
        snapshot.getBase().setInitQuery(Boolean.TRUE);
        snapshot.getBase().getPagination().setEnabled(Boolean.TRUE);
        snapshot.getBase().getPagination().setPageLimit(Integer.valueOf(20));
        snapshot.getBase().setSummaryEnabled(Boolean.FALSE);
        return snapshot;
    }

    private static ReportViewSnapshot.ScriptValue script(String value) {
        ReportViewSnapshot.ScriptValue script = new ReportViewSnapshot.ScriptValue();
        script.setType(Integer.valueOf(1));
        script.setScript(value);
        return script;
    }

    private static VwUrl url(String viewKey, String viewClass) {
        VwUrl url = new VwUrl();
        url.setViewKey(viewKey);
        url.setViewClass(viewClass);
        url.setDescription("销售报表");
        url.setLoginType(Integer.valueOf(1));
        url.setLockFlag(Integer.valueOf(0));
        url.setCreateUid("admin");
        return url;
    }

    private static class RecordingRepository implements ReportViewRepository {
        private final Map<String, VwUrl> urls = new LinkedHashMap<String, VwUrl>();
        private final Map<String, Map<String, Object>> reports = new LinkedHashMap<String, Map<String, Object>>();
        private final Map<String, Map<String, Object>> createdReports = new LinkedHashMap<String, Map<String, Object>>();
        private final Map<String, Map<String, Object>> replacedReports = new LinkedHashMap<String, Map<String, Object>>();
        private final Map<String, Map<String, Object>> patchedReports = new LinkedHashMap<String, Map<String, Object>>();
        private final List<ReportViewSection> patchedSections = new ArrayList<ReportViewSection>();
        private final List<String> removedViewKeys = new ArrayList<String>();
        private final List<ReportViewResponse.WritePlan> removePlans = new ArrayList<ReportViewResponse.WritePlan>();
        private VwUrl createdUrl;
        private VwUrl replacedUrl;
        private int createAttempts;
        private int replaceAttempts;
        private int patchAttempts;
        private int removeAttempts;
        private int flushes;

        public List<VwUrl> listReportUrls(int start, int limit) {
            return new ArrayList<VwUrl>(urls.values());
        }

        public int countReportUrls() {
            return urls.size();
        }

        public VwUrl findUrl(String viewKey) {
            return urls.get(viewKey);
        }

        public Map<String, Object> findReport(String viewKey) {
            return reports.get(viewKey);
        }

        public VwUrl saveUrl(VwUrl url) {
            urls.put(url.getViewKey(), url);
            return url;
        }

        public void updateUrl(VwUrl url) {
            urls.put(url.getViewKey(), url);
        }

        public void createViewConfig(VwUrl url, Map<String, Object> reportMap, ReportViewResponse.WritePlan plan) {
            createAttempts++;
            createdUrl = url;
            urls.put(url.getViewKey(), url);
            reports.put(url.getViewKey(), reportMap);
            createdReports.put(url.getViewKey(), reportMap);
        }

        public void replaceViewConfig(VwUrl url, Map<String, Object> reportMap, ReportViewResponse.WritePlan plan) {
            replaceAttempts++;
            replacedUrl = url;
            urls.put(url.getViewKey(), url);
            reports.put(url.getViewKey(), reportMap);
            replacedReports.put(url.getViewKey(), reportMap);
        }

        public void patchViewConfig(VwUrl url,
                                    ReportViewSection section,
                                    Map<String, Object> reportMap,
                                    ReportViewResponse.WritePlan plan) {
            patchAttempts++;
            patchedSections.add(section);
            urls.put(url.getViewKey(), url);
            reports.put(url.getViewKey(), reportMap);
            patchedReports.put(url.getViewKey(), reportMap);
        }

        public void removeViewConfig(String viewKey, ReportViewResponse.WritePlan plan) {
            removeAttempts++;
            removedViewKeys.add(viewKey);
            removePlans.add(plan);
            urls.remove(viewKey);
            reports.remove(viewKey);
        }

        public void flushAndClearViewCache(String viewKey) {
            flushes++;
        }

        private void seedReport(ReportViewSnapshot snapshot) {
            urls.put(snapshot.getViewKey(), url(snapshot.getViewKey(), "rep_list"));
            reports.put(snapshot.getViewKey(), new ReportViewMapper().toReportMap(snapshot));
        }
    }
}
