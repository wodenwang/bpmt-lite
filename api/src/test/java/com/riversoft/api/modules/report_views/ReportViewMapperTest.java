package com.riversoft.api.modules.report_views;

import com.riversoft.platform.po.CmPri;
import com.riversoft.platform.po.VwUrl;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReportViewMapperTest {
    @Test
    public void mapsSnapshotToReportMap() {
        ReportViewSnapshot snapshot = fullSnapshot("SALES_REPORT");

        Map<String, Object> map = new ReportViewMapper().toReportMap(snapshot);

        assertEquals("SALES_REPORT", map.get("viewKey"));
        assertEquals("销售报表", map.get("busiName"));
        assertEquals(Integer.valueOf(1), map.get("mainSqlType"));
        assertEquals("select * from SALE_ORDER where 1=1", map.get("mainSqlScript"));
        assertEquals(Integer.valueOf(1), map.get("pkType"));
        assertEquals("return vo.ID;", map.get("pkScript"));
        assertEquals(Integer.valueOf(1), map.get("pkSqlType"));
        assertEquals("select * from SALE_ORDER where ID = ?", map.get("pkSqlScript"));
        assertEquals("CREATE_DATE desc", map.get("orderBy"));
        assertEquals(Integer.valueOf(1), map.get("pageFlag"));
        assertEquals(Integer.valueOf(1), map.get("listJsType"));
        assertEquals("console.log('list');", map.get("listJsScript"));

        Map<String, Object> showColumn = firstMap(map.get("showColumns"));
        assertEquals("VwReportColumnShow", showColumn.get("$type$"));
        assertEquals("SALES_REPORT", showColumn.get("viewKey"));
        assertEquals(Integer.valueOf(0), showColumn.get("sort"));
        assertEquals(Integer.valueOf(0), showColumn.get("listSort"));
        assertCompletePri((CmPri) showColumn.get("pri"), "report.sales.ORDER_NO.view", "SALES_REPORT");

        Map<String, Object> query = firstMap(map.get("querys"));
        assertEquals("VwReportQuery", query.get("$type$"));
        assertEquals("keyword", query.get("name"));
        assertEquals("return ' and ORDER_NO like ?';", query.get("sqlScript"));

        Map<String, Object> limit = firstMap(map.get("limits"));
        assertEquals("VwReportLimit", limit.get("$type$"));
        assertEquals("report.sales.limit.view", ((CmPri) limit.get("pri")).getPriKey());

        Map<String, Object> itemButton = firstMap(map.get("itemBtns"));
        assertEquals("VwReportBtnItem", itemButton.get("$type$"));
        assertEquals(Integer.valueOf(2), itemButton.get("openType"));
        assertEquals("return {id: vo.ID};", itemButton.get("paramScript"));

        Map<String, Object> weixin = castMap(map.get("weixin"));
        assertEquals("VwReportWeixin", weixin.get("$type$"));
        assertEquals("SALES_REPORT", weixin.get("viewKey"));
        assertEquals("return vo.ORDER_NO;", weixin.get("titleScript"));
        assertCompletePri((CmPri) weixin.get("pri"), "report.sales.weixin.view", "SALES_REPORT");
    }

    @Test
    public void mapsReportMapToSnapshot() {
        VwUrl url = new VwUrl();
        url.setViewKey("SALES_REPORT");
        url.setViewClass("rep_list");
        url.setDescription("销售报表");
        url.setLoginType(Integer.valueOf(1));
        Map<String, Object> report = fullReportMap("SALES_REPORT");

        ReportViewSnapshot snapshot = new ReportViewMapper().toSnapshot(url, report);

        assertEquals("SALES_REPORT", snapshot.getViewKey());
        assertEquals("销售报表", snapshot.getBase().getDisplayName());
        assertTrue(snapshot.getBase().getPagination().getEnabled().booleanValue());
        assertEquals("CREATE_DATE desc", snapshot.getBase().getOrderBySql());
        assertEquals("return vo.ID;", snapshot.getBase().getPrimaryKey().getValue().getScript());
        assertEquals("console.log('list');", snapshot.getScripts().getList().getScript());

        assertEquals("ORDER_NO", snapshot.getColumns().getShow().get(0).getStableKey());
        assertEquals("ORDER_NO", snapshot.getColumns().getListOrder().get(0));
        assertEquals("AMOUNT", snapshot.getColumns().getListOrder().get(1));
        assertEquals("订单号", snapshot.getColumns().getShow().get(0).getDisplayName());
        assertEquals("report.sales.ORDER_NO.view",
                snapshot.getColumns().getShow().get(0).getPermissions().getView().get(0));

        assertEquals("BASE", snapshot.getColumns().getLines().get(0).getStableKey());
        assertEquals("基础信息", snapshot.getColumns().getLines().get(0).getDisplayName());
        assertEquals("line tip", snapshot.getColumns().getLines().get(0).getTip().getScript());

        assertEquals("keyword", snapshot.getQueries().get(0).getName());
        assertEquals("return ' and ORDER_NO like ?';", snapshot.getQueries().get(0).getSql().getScript());

        assertEquals("limit-tenant", snapshot.getLimits().get(0).getStableKey());
        assertEquals("return ' and TENANT_ID = ?';", snapshot.getLimits().get(0).getSql().getScript());

        assertEquals("detail", snapshot.getButtons().getItem().get(0).getStableKey());
        assertEquals("2", snapshot.getButtons().getItem().get(0).getOpenType());
        assertEquals("return {id: vo.ID};", snapshot.getButtons().getItem().get(0).getParam().getScript());

        assertNotNull(snapshot.getWeixin());
        assertEquals(Integer.valueOf(1), snapshot.getWeixin().getListMode());
        assertEquals("return vo.ORDER_NO;", snapshot.getWeixin().getTitle().getScript());
    }

    @Test
    public void sortsChildrenBySortAndListOrderByListSort() {
        VwUrl url = new VwUrl();
        url.setViewKey("SALES_REPORT");

        Map<String, Object> second = showColumn("SECOND", "第二字段", 2, 20);
        Map<String, Object> first = showColumn("FIRST", "第一字段", 1, 10);
        Map<String, Object> report = baseReport("SALES_REPORT");
        report.put("showColumns", Arrays.asList(second, first));

        ReportViewSnapshot snapshot = new ReportViewMapper().toSnapshot(url, report);

        assertEquals("FIRST", snapshot.getColumns().getShow().get(0).getStableKey());
        assertEquals("SECOND", snapshot.getColumns().getShow().get(1).getStableKey());
        assertEquals("FIRST", snapshot.getColumns().getListOrder().get(0));
        assertEquals("SECOND", snapshot.getColumns().getListOrder().get(1));
    }

    private ReportViewSnapshot fullSnapshot(String viewKey) {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot(viewKey);
        ReportViewSnapshot.PrimaryKey primaryKey = new ReportViewSnapshot.PrimaryKey();
        primaryKey.setValue(Fixtures.script("return vo.ID;"));
        primaryKey.setSql(Fixtures.script("select * from SALE_ORDER where ID = ?"));
        snapshot.getBase().setPrimaryKey(primaryKey);
        snapshot.getScripts().setList(Fixtures.script("console.log('list');"));

        ReportViewSnapshot.ShowColumn amount = new ReportViewSnapshot.ShowColumn();
        amount.setStableKey("AMOUNT");
        amount.setDisplayName("金额");
        amount.setWholeLine(Boolean.TRUE);
        amount.setContent(Fixtures.script("return vo.AMOUNT;"));
        snapshot.getColumns().getShow().add(amount);
        snapshot.getColumns().setListOrder(Arrays.asList("ORDER_NO", "AMOUNT"));
        snapshot.getColumns().getShow().get(0).setSortField("ORDER_NO");
        snapshot.getColumns().getShow().get(0).setPermissions(permission("report.sales.ORDER_NO.view"));

        ReportViewSnapshot.LineColumn line = new ReportViewSnapshot.LineColumn();
        line.setStableKey("BASE");
        line.setDisplayName("基础信息");
        line.setTip(Fixtures.script("line tip"));
        line.setExpanded(Boolean.TRUE);
        line.setPermissions(permission("report.sales.line.view"));
        snapshot.getColumns().setLines(Collections.singletonList(line));

        ReportViewSnapshot.Query query = new ReportViewSnapshot.Query();
        query.setName("keyword");
        query.setDisplayName("关键字");
        query.setWidget("textfield");
        query.setWidgetParam("{placeholder:'订单号'}");
        query.setDefaultValue("");
        query.setSql(Fixtures.script("return ' and ORDER_NO like ?';"));
        query.setDescription("按订单号模糊查询");
        snapshot.setQueries(Collections.singletonList(query));

        ReportViewSnapshot.Limit limit = new ReportViewSnapshot.Limit();
        limit.setStableKey("limit-tenant");
        limit.setDescription("租户限制");
        limit.setSql(Fixtures.script("return ' and TENANT_ID = ?';"));
        limit.setPermissions(permission("report.sales.limit.view"));
        snapshot.setLimits(Collections.singletonList(limit));

        ReportViewSnapshot.CustomButton item = new ReportViewSnapshot.CustomButton();
        item.setStableKey("detail");
        item.setDisplayName("查看");
        item.setIcon("zoomin");
        item.setAction("/detail");
        item.setOpenType("2");
        item.setParam(Fixtures.script("return {id: vo.ID};"));
        item.setConfirmMessage("确认查看？");
        item.setDescription("查看明细");
        item.setPermissions(permission("report.sales.item.view"));
        snapshot.getButtons().setItem(Collections.singletonList(item));

        ReportViewSnapshot.Weixin weixin = new ReportViewSnapshot.Weixin();
        weixin.setListMode(Integer.valueOf(1));
        weixin.setUrlMode(Integer.valueOf(0));
        weixin.setTitle(Fixtures.script("return vo.ORDER_NO;"));
        weixin.setImage(Fixtures.script("return vo.IMG;"));
        weixin.setDescription(Fixtures.script("return vo.DES;"));
        weixin.setDate(Fixtures.script("return vo.CREATE_DATE;"));
        weixin.setPermissions(permission("report.sales.weixin.view"));
        snapshot.setWeixin(weixin);
        return snapshot;
    }

    private Map<String, Object> fullReportMap(String viewKey) {
        Map<String, Object> report = baseReport(viewKey);
        report.put("pkType", Integer.valueOf(1));
        report.put("pkScript", "return vo.ID;");
        report.put("pkSqlType", Integer.valueOf(1));
        report.put("pkSqlScript", "select * from SALE_ORDER where ID = ?");
        report.put("listJsType", Integer.valueOf(1));
        report.put("listJsScript", "console.log('list');");
        report.put("showColumns", Arrays.asList(showColumn("AMOUNT", "金额", 2, 20),
                showColumn("ORDER_NO", "订单号", 1, 10)));
        report.put("lineColumns", Collections.singleton(lineColumn("BASE", "基础信息")));
        report.put("querys", Collections.singleton(query()));
        report.put("limits", Collections.singleton(limit()));
        report.put("itemBtns", Collections.singleton(customButton("detail")));
        report.put("weixin", weixin(viewKey));
        return report;
    }

    private Map<String, Object> baseReport(String viewKey) {
        Map<String, Object> report = new LinkedHashMap<String, Object>();
        report.put("viewKey", viewKey);
        report.put("busiName", "销售报表");
        report.put("mainSqlType", Integer.valueOf(1));
        report.put("mainSqlScript", "select * from SALE_ORDER where 1=1");
        report.put("col", Integer.valueOf(2));
        report.put("initQuery", Integer.valueOf(1));
        report.put("pageFlag", Integer.valueOf(1));
        report.put("pageLimit", Integer.valueOf(20));
        report.put("summaryFlag", Integer.valueOf(0));
        report.put("orderBy", "CREATE_DATE desc");
        return report;
    }

    private Map<String, Object> showColumn(String key, String displayName, int sort, int listSort) {
        Map<String, Object> column = new LinkedHashMap<String, Object>();
        column.put("key", key);
        column.put("busiName", displayName);
        column.put("style", "width:120px");
        column.put("whole", Integer.valueOf(0));
        column.put("sortField", key);
        column.put("contentType", Integer.valueOf(1));
        column.put("contentScript", "return vo." + key + ";");
        column.put("summaryContentType", Integer.valueOf(1));
        column.put("summaryContentScript", "return summary." + key + ";");
        column.put("sort", Integer.valueOf(sort));
        column.put("listSort", Integer.valueOf(listSort));
        column.put("pri", pri("report.sales." + key + ".view"));
        return column;
    }

    private Map<String, Object> lineColumn(String key, String displayName) {
        Map<String, Object> line = new LinkedHashMap<String, Object>();
        line.put("key", key);
        line.put("busiName", displayName);
        line.put("tipType", Integer.valueOf(1));
        line.put("tipScript", "line tip");
        line.put("expandFlag", Integer.valueOf(1));
        line.put("sort", Integer.valueOf(1));
        line.put("pri", pri("report.sales.line.view"));
        return line;
    }

    private Map<String, Object> query() {
        Map<String, Object> query = new LinkedHashMap<String, Object>();
        query.put("busiName", "关键字");
        query.put("name", "keyword");
        query.put("widget", "textfield");
        query.put("widgetParamScript", "{placeholder:'订单号'}");
        query.put("defVal", "");
        query.put("sort", Integer.valueOf(1));
        query.put("sqlType", Integer.valueOf(1));
        query.put("sqlScript", "return ' and ORDER_NO like ?';");
        query.put("description", "按订单号模糊查询");
        return query;
    }

    private Map<String, Object> limit() {
        Map<String, Object> limit = new LinkedHashMap<String, Object>();
        limit.put("key", "limit-tenant");
        limit.put("description", "租户限制");
        limit.put("sqlType", Integer.valueOf(1));
        limit.put("sqlScript", "return ' and TENANT_ID = ?';");
        limit.put("sort", Integer.valueOf(1));
        limit.put("pri", pri("report.sales.limit.view"));
        return limit;
    }

    private Map<String, Object> customButton(String key) {
        Map<String, Object> button = new LinkedHashMap<String, Object>();
        button.put("key", key);
        button.put("busiName", "查看");
        button.put("icon", "zoomin");
        button.put("action", "/detail");
        button.put("openType", Integer.valueOf(2));
        button.put("description", "查看明细");
        button.put("sort", Integer.valueOf(1));
        button.put("paramType", Integer.valueOf(1));
        button.put("paramScript", "return {id: vo.ID};");
        button.put("confirmMsg", "确认查看？");
        button.put("pri", pri("report.sales.item.view"));
        return button;
    }

    private Map<String, Object> weixin(String viewKey) {
        Map<String, Object> weixin = new LinkedHashMap<String, Object>();
        weixin.put("viewKey", viewKey);
        weixin.put("listMode", Integer.valueOf(1));
        weixin.put("urlMode", Integer.valueOf(0));
        weixin.put("titleType", Integer.valueOf(1));
        weixin.put("titleScript", "return vo.ORDER_NO;");
        weixin.put("imgType", Integer.valueOf(1));
        weixin.put("imgScript", "return vo.IMG;");
        weixin.put("desType", Integer.valueOf(1));
        weixin.put("desScript", "return vo.DES;");
        weixin.put("dateType", Integer.valueOf(1));
        weixin.put("dateScript", "return vo.CREATE_DATE;");
        weixin.put("pri", pri("report.sales.weixin.view"));
        return weixin;
    }

    private ReportViewSnapshot.PermissionSet permission(String priKey) {
        ReportViewSnapshot.PermissionSet permissions = new ReportViewSnapshot.PermissionSet();
        permissions.setView(Collections.singletonList(priKey));
        return permissions;
    }

    private CmPri pri(String priKey) {
        CmPri pri = new CmPri();
        pri.setPriKey(priKey);
        return pri;
    }

    private void assertCompletePri(CmPri pri, String priKey, String viewKey) {
        assertNotNull(pri);
        assertEquals(priKey, pri.getPriKey());
        assertEquals(Integer.valueOf(2), pri.getCatelogType());
        assertEquals(viewKey, pri.getCatelogKey());
        assertEquals(Integer.valueOf(1), pri.getType());
        assertEquals(Integer.valueOf(2), pri.getCheckType());
        assertEquals("${true}", pri.getCheckScript());
        assertNotNull(pri.getBusiName());
        assertTrue(pri.getBusiName().length() > 0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> firstMap(Object value) {
        return (Map<String, Object>) ((Collection<Object>) value).iterator().next();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }
}
