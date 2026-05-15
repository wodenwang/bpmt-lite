package com.riversoft.api.modules.report_views;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class ReportViewScriptRiskScannerTest {
    @Test
    public void warnsForSqlClientScriptButtonActionAndExternalDb() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        snapshot.getBase().setDbKey("legacy-db");
        snapshot.getScripts().setList(Fixtures.script("console.log('loaded');"));
        ReportViewSnapshot.CustomButton button = new ReportViewSnapshot.CustomButton();
        button.setStableKey("OPEN_ORDER");
        button.setDisplayName("查看订单");
        button.setAction("/order.view");
        snapshot.getButtons().getItem().add(button);

        List<ReportViewResponse.Warning> warnings = new ReportViewScriptRiskScanner().scan(snapshot);

        assertTrue(Fixtures.warningCodes(warnings).contains("SQL_SCRIPT_PRESENT"));
        assertTrue(Fixtures.warningCodes(warnings).contains("CLIENT_SCRIPT_PRESENT"));
        assertTrue(Fixtures.warningCodes(warnings).contains("BUTTON_ACTION_PRESENT"));
        assertTrue(Fixtures.warningCodes(warnings).contains("EXTERNAL_DB_KEY_PRESENT"));
        assertTrue(Fixtures.warningCodes(warnings).contains("UNEXECUTED_SQL_SEMANTICS"));
    }

    @Test
    public void warnsForLineSubviewAndButtonParamClientScripts() {
        ReportViewSnapshot lineSnapshot = Fixtures.reportSnapshot("LINE_REPORT");
        lineSnapshot.getColumns().getShow().get(0).setContent(null);
        ReportViewSnapshot.LineColumn line = new ReportViewSnapshot.LineColumn();
        line.setStableKey("line1");
        line.setTip(Fixtures.script("return vo.TIP;"));
        lineSnapshot.getColumns().getLines().add(line);
        assertTrue(Fixtures.warningCodes(new ReportViewScriptRiskScanner().scan(lineSnapshot))
                .contains("CLIENT_SCRIPT_PRESENT"));

        ReportViewSnapshot tabSnapshot = Fixtures.reportSnapshot("TAB_REPORT");
        tabSnapshot.getColumns().getShow().get(0).setContent(null);
        ReportViewSnapshot.ViewTab tab = new ReportViewSnapshot.ViewTab();
        tab.setStableKey("detail");
        tab.setParam(Fixtures.script("return 'id=' + vo.ID;"));
        tabSnapshot.getSubviews().getViewTabs().add(tab);
        assertTrue(Fixtures.warningCodes(new ReportViewScriptRiskScanner().scan(tabSnapshot))
                .contains("CLIENT_SCRIPT_PRESENT"));

        ReportViewSnapshot buttonSnapshot = Fixtures.reportSnapshot("BUTTON_REPORT");
        buttonSnapshot.getColumns().getShow().get(0).setContent(null);
        ReportViewSnapshot.CustomButton button = new ReportViewSnapshot.CustomButton();
        button.setStableKey("open");
        button.setParam(Fixtures.script("return vo.ID;"));
        buttonSnapshot.getButtons().getSummary().add(button);
        assertTrue(Fixtures.warningCodes(new ReportViewScriptRiskScanner().scan(buttonSnapshot))
                .contains("CLIENT_SCRIPT_PRESENT"));
    }
}
