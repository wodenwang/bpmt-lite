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
}
