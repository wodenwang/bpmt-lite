package com.riversoft.api.modules.report_views;

import com.riversoft.api.http.ApiJson;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReportViewSnapshotTest {
    @Test
    public void parsesCompleteReportViewSnapshot() throws Exception {
        String json = "{\"viewKey\":\"SALES_REPORT\",\"description\":\"销售报表\",\"loginRequired\":true,"
                + "\"base\":{\"displayName\":\"销售报表\",\"mainSql\":{\"type\":1,\"script\":\"select * from SALE_ORDER\"},"
                + "\"layoutColumns\":2,\"initQuery\":true,\"pagination\":{\"enabled\":true,\"pageLimit\":20},"
                + "\"summaryEnabled\":false,\"orderBySql\":\"CREATE_DATE desc\"},"
                + "\"columns\":{\"show\":[{\"stableKey\":\"ORDER_NO\",\"displayName\":\"订单号\","
                + "\"content\":{\"type\":1,\"script\":\"return vo.ORDER_NO;\"}}],\"lines\":[],\"listOrder\":[\"ORDER_NO\"]},"
                + "\"queries\":[],\"limits\":[],\"variables\":{\"prepared\":[]},"
                + "\"subviews\":{\"viewTabs\":[]},\"buttons\":{\"system\":[],\"item\":[],\"summary\":[]},"
                + "\"scripts\":{\"list\":{\"type\":1,\"script\":\"console.log('loaded');\"}}}";

        ReportViewSnapshot snapshot = ApiJson.fromJson(new ByteArrayInputStream(json.getBytes("UTF-8")),
                ReportViewSnapshot.class);

        assertEquals("SALES_REPORT", snapshot.getViewKey());
        assertTrue(snapshot.isLoginRequired());
        assertEquals("销售报表", snapshot.getBase().getDisplayName());
        assertEquals("CREATE_DATE desc", snapshot.getBase().getOrderBySql());
        assertEquals("ORDER_NO", snapshot.getColumns().getShow().get(0).getStableKey());
    }

    @Test
    public void normalizeForCreateFillsDefaultsAndViewKey() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot(null);
        snapshot.setColumns(null);
        snapshot.setVariables(null);

        ReportViewSnapshot normalized = new ReportViewDefaults().normalizeForCreate(snapshot);

        assertTrue(normalized.getViewKey().startsWith("REPORT_"));
        assertEquals(Integer.valueOf(2), normalized.getBase().getLayoutColumns());
        assertTrue(normalized.getColumns().getShow().isEmpty());
        assertTrue(normalized.getVariables().getPrepared().isEmpty());
    }
}
