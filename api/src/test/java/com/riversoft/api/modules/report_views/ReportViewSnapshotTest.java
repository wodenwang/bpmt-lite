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
                + "\"content\":{\"type\":1,\"script\":\"return vo.ORDER_NO;\"}}],"
                + "\"lines\":[{\"stableKey\":\"line1\",\"displayName\":\"分组\",\"tip\":{\"type\":1,\"script\":\"return vo.TIP;\"}}],"
                + "\"listOrder\":[\"ORDER_NO\"]},"
                + "\"queries\":[{\"name\":\"keyword\",\"displayName\":\"关键字\",\"widget\":\"textfield\","
                + "\"widgetParam\":{\"type\":1,\"script\":\"{placeholder:'订单号'}\"},"
                + "\"sql\":{\"type\":1,\"script\":\"return ' and ORDER_NO like ?';\"}}],"
                + "\"limits\":[],\"variables\":{\"prepared\":[]},"
                + "\"subviews\":{\"viewTabs\":[{\"stableKey\":\"detail\",\"displayName\":\"明细\","
                + "\"param\":{\"type\":1,\"script\":\"return 'id=' + vo.ID;\"}}]},"
                + "\"buttons\":{\"system\":[],\"item\":[{\"stableKey\":\"open\",\"displayName\":\"打开\","
                + "\"openType\":2,"
                + "\"param\":{\"type\":1,\"script\":\"return vo.ID;\"}}],\"summary\":[]},"
                + "\"weixin\":{\"listMode\":1,\"urlMode\":2,"
                + "\"title\":{\"type\":1,\"script\":\"return vo.TITLE;\"},"
                + "\"image\":{\"type\":1,\"script\":\"return vo.IMAGE;\"},"
                + "\"description\":{\"type\":1,\"script\":\"return vo.DESCRIPTION;\"},"
                + "\"date\":{\"type\":1,\"script\":\"return vo.CREATE_DATE;\"}},"
                + "\"scripts\":{\"list\":{\"type\":1,\"script\":\"console.log('loaded');\"}}}";

        ReportViewSnapshot snapshot = ApiJson.fromJson(new ByteArrayInputStream(json.getBytes("UTF-8")),
                ReportViewSnapshot.class);

        assertEquals("SALES_REPORT", snapshot.getViewKey());
        assertTrue(snapshot.isLoginRequired());
        assertEquals("销售报表", snapshot.getBase().getDisplayName());
        assertEquals("CREATE_DATE desc", snapshot.getBase().getOrderBySql());
        assertEquals("ORDER_NO", snapshot.getColumns().getShow().get(0).getStableKey());
        assertEquals("return vo.TIP;", snapshot.getColumns().getLines().get(0).getTip().getScript());
        assertEquals("{placeholder:'订单号'}", snapshot.getQueries().get(0).getWidgetParam().getScript());
        assertEquals("return 'id=' + vo.ID;", snapshot.getSubviews().getViewTabs().get(0).getParam().getScript());
        assertEquals(Integer.valueOf(2), snapshot.getButtons().getItem().get(0).getOpenType());
        assertEquals("return vo.ID;", snapshot.getButtons().getItem().get(0).getParam().getScript());
        assertEquals(Integer.valueOf(1), snapshot.getWeixin().getListMode());
        assertEquals(Integer.valueOf(2), snapshot.getWeixin().getUrlMode());
        assertEquals("return vo.TITLE;", snapshot.getWeixin().getTitle().getScript());
        assertEquals("return vo.IMAGE;", snapshot.getWeixin().getImage().getScript());
        assertEquals("return vo.DESCRIPTION;", snapshot.getWeixin().getDescription().getScript());
        assertEquals("return vo.CREATE_DATE;", snapshot.getWeixin().getDate().getScript());
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

    @Test
    public void defaultsDoNotFillRequiredBaseScalars() {
        ReportViewSnapshot snapshot = new ReportViewSnapshot();
        snapshot.setBase(new ReportViewSnapshot.Base());

        ReportViewSnapshot normalized = new ReportViewDefaults().normalizeForCreate(snapshot);

        assertTrue(normalized.getViewKey().startsWith("REPORT_"));
        assertEquals(null, normalized.getBase().getLayoutColumns());
        assertEquals(null, normalized.getBase().getInitQuery());
        assertEquals(null, normalized.getBase().getPagination().getEnabled());
    }
}
