package com.riversoft.api.modules.report_views;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReportViewPermissionServiceTest {
    @Test
    public void generatesStablePermissionKeysForAllManagedReportSections() {
        ReportViewSnapshot snapshot = snapshotWithManagedPermissions("SALES_REPORT");

        ReportViewResponse.WritePlan plan = new ReportViewPermissionService().apply("SALES_REPORT", null, snapshot);

        assertEquals("report.SALES_REPORT.column.ORDER_NO.view",
                snapshot.getColumns().getShow().get(0).getPermissions().getView().get(0));
        assertEquals("report.SALES_REPORT.line.TOTAL_LINE.view",
                snapshot.getColumns().getLines().get(0).getPermissions().getView().get(0));
        assertEquals("report.SALES_REPORT.limit.SELF_DEPT.view",
                snapshot.getLimits().get(0).getPermissions().getView().get(0));
        assertEquals("report.SALES_REPORT.subview.DETAILS.view",
                snapshot.getSubviews().getViewTabs().get(0).getPermissions().getView().get(0));
        assertEquals("report.SALES_REPORT.button.system.export.view",
                snapshot.getButtons().getSystem().get(0).getPermissions().getView().get(0));
        assertEquals("report.SALES_REPORT.button.item.approve.view",
                snapshot.getButtons().getItem().get(0).getPermissions().getView().get(0));
        assertEquals("report.SALES_REPORT.button.summary.refresh.view",
                snapshot.getButtons().getSummary().get(0).getPermissions().getView().get(0));
        assertEquals("report.SALES_REPORT.weixin.view",
                snapshot.getWeixin().getPermissions().getView().get(0));
        assertTrue(plan.getPermissionDeletes().isEmpty());
    }

    @Test
    public void preservesExistingPermissionAndPlansOldOnlyDeletes() {
        ReportViewSnapshot oldSnapshot = snapshotWithManagedPermissions("SALES_REPORT");
        new ReportViewPermissionService().apply("SALES_REPORT", null, oldSnapshot);
        ReportViewSnapshot target = snapshotWithManagedPermissions("SALES_REPORT");
        target.getColumns().getShow().get(0).setPermissions(permission("custom.order.view"));
        target.getLimits().clear();

        ReportViewResponse.WritePlan plan = new ReportViewPermissionService()
                .apply("SALES_REPORT", oldSnapshot, target);

        assertEquals("custom.order.view",
                target.getColumns().getShow().get(0).getPermissions().getView().get(0));
        assertTrue(plan.getPermissionDeletes().contains("report.SALES_REPORT.column.ORDER_NO.view"));
        assertTrue(plan.getPermissionDeletes().contains("report.SALES_REPORT.limit.SELF_DEPT.view"));
        assertTrue(!plan.getPermissionDeletes().contains("custom.order.view"));
    }

    @Test
    public void copiesOldColumnPermissionByStableKeyWhenTargetPermissionIsMissing() {
        ReportViewSnapshot oldSnapshot = snapshotWithManagedPermissions("SALES_REPORT");
        oldSnapshot.getColumns().getShow().get(0).setPermissions(permission("custom.old.order.view"));
        ReportViewSnapshot target = snapshotWithManagedPermissions("SALES_REPORT");
        target.getColumns().getShow().get(0).setPermissions(null);

        ReportViewResponse.WritePlan plan = new ReportViewPermissionService()
                .apply("SALES_REPORT", oldSnapshot, target);

        assertEquals("custom.old.order.view",
                target.getColumns().getShow().get(0).getPermissions().getView().get(0));
        assertTrue(!plan.getPermissionDeletes().contains("custom.old.order.view"));
    }

    @Test
    public void copiesOldSystemButtonPermissionByNameWhenTargetPermissionIsMissing() {
        ReportViewSnapshot oldSnapshot = snapshotWithManagedPermissions("SALES_REPORT");
        oldSnapshot.getButtons().getSystem().get(0).setPermissions(permission("custom.old.export.view"));
        ReportViewSnapshot target = snapshotWithManagedPermissions("SALES_REPORT");
        target.getButtons().getSystem().get(0).setPermissions(null);

        ReportViewResponse.WritePlan plan = new ReportViewPermissionService()
                .apply("SALES_REPORT", oldSnapshot, target);

        assertEquals("custom.old.export.view",
                target.getButtons().getSystem().get(0).getPermissions().getView().get(0));
        assertTrue(!plan.getPermissionDeletes().contains("custom.old.export.view"));
    }

    @Test
    public void copiesOldColumnPermissionByOrderWhenStableKeyNoLongerMatches() {
        ReportViewSnapshot oldSnapshot = snapshotWithManagedPermissions("SALES_REPORT");
        oldSnapshot.getColumns().getShow().get(0).setStableKey("OLD_ORDER_NO");
        oldSnapshot.getColumns().getShow().get(0).setPermissions(permission("custom.old.order.view"));
        ReportViewSnapshot target = snapshotWithManagedPermissions("SALES_REPORT");
        target.getColumns().getShow().get(0).setStableKey("NEW_ORDER_NO");
        target.getColumns().getShow().get(0).setPermissions(null);

        ReportViewResponse.WritePlan plan = new ReportViewPermissionService()
                .apply("SALES_REPORT", oldSnapshot, target);

        assertEquals("custom.old.order.view",
                target.getColumns().getShow().get(0).getPermissions().getView().get(0));
        assertTrue(!plan.getPermissionDeletes().contains("custom.old.order.view"));
    }

    @Test
    public void copiesOldSystemButtonPermissionByOrderWhenNameNoLongerMatches() {
        ReportViewSnapshot oldSnapshot = snapshotWithManagedPermissions("SALES_REPORT");
        oldSnapshot.getButtons().getSystem().get(0).setName("oldExport");
        oldSnapshot.getButtons().getSystem().get(0).setPermissions(permission("custom.old.export.view"));
        ReportViewSnapshot target = snapshotWithManagedPermissions("SALES_REPORT");
        target.getButtons().getSystem().get(0).setName("newExport");
        target.getButtons().getSystem().get(0).setPermissions(null);

        ReportViewResponse.WritePlan plan = new ReportViewPermissionService()
                .apply("SALES_REPORT", oldSnapshot, target);

        assertEquals("custom.old.export.view",
                target.getButtons().getSystem().get(0).getPermissions().getView().get(0));
        assertTrue(!plan.getPermissionDeletes().contains("custom.old.export.view"));
    }

    @Test
    public void deletionPlanCollectsAllManagedOldKeys() {
        ReportViewSnapshot oldSnapshot = snapshotWithManagedPermissions("SALES_REPORT");
        new ReportViewPermissionService().apply("SALES_REPORT", null, oldSnapshot);

        ReportViewResponse.WritePlan plan = new ReportViewPermissionService()
                .apply("SALES_REPORT", oldSnapshot, null);

        assertTrue(plan.getPermissionDeletes().contains("report.SALES_REPORT.column.ORDER_NO.view"));
        assertTrue(plan.getPermissionDeletes().contains("report.SALES_REPORT.weixin.view"));
    }

    @Test
    public void removedCustomPermissionIsNotPlannedForDelete() {
        ReportViewSnapshot oldSnapshot = snapshotWithManagedPermissions("SALES_REPORT");
        oldSnapshot.getLimits().get(0).setPermissions(permission("custom.shared.permission"));
        ReportViewSnapshot target = snapshotWithManagedPermissions("SALES_REPORT");
        target.getLimits().clear();

        ReportViewResponse.WritePlan plan = new ReportViewPermissionService()
                .apply("SALES_REPORT", oldSnapshot, target);

        assertTrue(!plan.getPermissionDeletes().contains("custom.shared.permission"));
    }

    @Test
    public void removedGeneratedPermissionIsStillPlannedForDelete() {
        ReportViewSnapshot oldSnapshot = snapshotWithManagedPermissions("SALES_REPORT");
        new ReportViewPermissionService().apply("SALES_REPORT", null, oldSnapshot);
        ReportViewSnapshot target = snapshotWithManagedPermissions("SALES_REPORT");
        target.getLimits().clear();

        ReportViewResponse.WritePlan plan = new ReportViewPermissionService()
                .apply("SALES_REPORT", oldSnapshot, target);

        assertTrue(plan.getPermissionDeletes().contains("report.SALES_REPORT.limit.SELF_DEPT.view"));
    }

    private static ReportViewSnapshot snapshotWithManagedPermissions(String viewKey) {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot(viewKey);

        ReportViewSnapshot.LineColumn line = new ReportViewSnapshot.LineColumn();
        line.setStableKey("TOTAL_LINE");
        line.setDisplayName("合计分割线");
        snapshot.getColumns().getLines().add(line);

        ReportViewSnapshot.Limit limit = new ReportViewSnapshot.Limit();
        limit.setStableKey("SELF_DEPT");
        limit.setDescription("仅看本部门");
        limit.setSql(Fixtures.script("and DEPT_ID = session.user.deptId"));
        snapshot.getLimits().add(limit);

        ReportViewSnapshot.ViewTab tab = new ReportViewSnapshot.ViewTab();
        tab.setStableKey("DETAILS");
        tab.setDisplayName("明细");
        tab.setAction("/report/detail.view");
        snapshot.getSubviews().getViewTabs().add(tab);

        ReportViewSnapshot.SystemButton systemButton = new ReportViewSnapshot.SystemButton();
        systemButton.setName("export");
        systemButton.setDisplayName("导出");
        systemButton.setType(Integer.valueOf(1));
        snapshot.getButtons().getSystem().add(systemButton);

        ReportViewSnapshot.CustomButton itemButton = new ReportViewSnapshot.CustomButton();
        itemButton.setStableKey("approve");
        itemButton.setDisplayName("审批");
        itemButton.setAction("approve()");
        snapshot.getButtons().getItem().add(itemButton);

        ReportViewSnapshot.CustomButton summaryButton = new ReportViewSnapshot.CustomButton();
        summaryButton.setStableKey("refresh");
        summaryButton.setDisplayName("刷新汇总");
        summaryButton.setAction("refreshSummary()");
        snapshot.getButtons().getSummary().add(summaryButton);

        ReportViewSnapshot.Weixin weixin = new ReportViewSnapshot.Weixin();
        weixin.setListMode(Integer.valueOf(0));
        weixin.setUrlMode(Integer.valueOf(0));
        snapshot.setWeixin(weixin);
        return snapshot;
    }

    private static ReportViewSnapshot.PermissionSet permission(String key) {
        ReportViewSnapshot.PermissionSet permissions = new ReportViewSnapshot.PermissionSet();
        permissions.setView(Collections.singletonList(key));
        return permissions;
    }
}
