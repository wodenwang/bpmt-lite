package com.riversoft.api.modules.report_views;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReportViewValidatorTest {
    @Test
    public void rejectsMissingMainSql() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        snapshot.getBase().setMainSql(null);

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).getMessage().contains("base.mainSql"));
    }

    @Test
    public void rejectsPermissionsOnQuery() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        ReportViewSnapshot.Query query = new ReportViewSnapshot.Query();
        query.setName("customerName");
        query.setDisplayName("客户名称");
        query.setWidget("text");
        query.setSql(Fixtures.script("and CUSTOMER_NAME = :customerName"));
        query.setPermissions(new ReportViewSnapshot.PermissionSet());
        query.getPermissions().setView(Collections.singletonList("report.SALES_REPORT.query.customerName.view"));
        snapshot.getQueries().add(query);

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertEquals("REPORT_VIEW_UNSUPPORTED_PERMISSION", result.getErrors().get(0).getCode());
    }

    @Test
    public void acceptsEmptyPermissionsOnQueryAndPreparedVariable() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        ReportViewSnapshot.Query query = new ReportViewSnapshot.Query();
        query.setName("customerName");
        query.setDisplayName("客户名称");
        query.setWidget("text");
        query.setSql(Fixtures.script("and CUSTOMER_NAME = :customerName"));
        query.setPermissions(new ReportViewSnapshot.PermissionSet());
        snapshot.getQueries().add(query);
        ReportViewSnapshot.PreparedVariable variable = new ReportViewSnapshot.PreparedVariable();
        variable.setVar("currentUser");
        variable.setExec(Fixtures.script("return SessionManager.getUser().getUid();"));
        variable.setPermissions(new ReportViewSnapshot.PermissionSet());
        snapshot.getVariables().getPrepared().add(variable);

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertTrue(result.isValid());
    }

    @Test
    public void rejectsDuplicateQueryNamesAndInvalidPagination() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        ReportViewSnapshot.Query first = new ReportViewSnapshot.Query();
        first.setName("customerName");
        first.setSql(Fixtures.script("and CUSTOMER_NAME = :customerName"));
        ReportViewSnapshot.Query second = new ReportViewSnapshot.Query();
        second.setName("customerName");
        second.setSql(Fixtures.script("and CUSTOMER_NAME like :customerName"));
        snapshot.getQueries().add(first);
        snapshot.getQueries().add(second);
        snapshot.getBase().getPagination().setPageLimit(Integer.valueOf(0));

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertTrue(Fixtures.errorCodes(result).contains("REPORT_VIEW_INVALID_SNAPSHOT"));
    }

    @Test
    public void rejectsMissingRequiredBaseScalars() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        snapshot.getBase().setLayoutColumns(null);
        snapshot.getBase().setInitQuery(null);
        snapshot.getBase().getPagination().setEnabled(null);

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertTrue(Fixtures.errorPaths(result).contains("base.layoutColumns"));
        assertTrue(Fixtures.errorPaths(result).contains("base.initQuery"));
        assertTrue(Fixtures.errorPaths(result).contains("base.pagination.enabled"));
    }

    @Test
    public void rejectsMissingColumnContentScript() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        snapshot.getColumns().getShow().get(0).setContent(null);

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertTrue(Fixtures.errorCodes(result).contains("REPORT_VIEW_INVALID_SCRIPT_CONFIG"));
        assertTrue(Fixtures.errorPaths(result).contains("columns.show[0].content"));
    }

    @Test
    public void rejectsInvalidOptionalClientScriptConfig() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        ReportViewSnapshot.LineColumn line = new ReportViewSnapshot.LineColumn();
        line.setStableKey("line1");
        line.setTip(new ReportViewSnapshot.ScriptValue());
        snapshot.getColumns().getLines().add(line);

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertTrue(Fixtures.errorCodes(result).contains("REPORT_VIEW_INVALID_SCRIPT_CONFIG"));
        assertTrue(Fixtures.errorPaths(result).contains("columns.lines[0].tip"));
    }

    @Test
    public void rejectsInvalidSummaryContentScriptConfig() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        snapshot.getColumns().getShow().get(0).setSummaryContent(new ReportViewSnapshot.ScriptValue());

        assertInvalidScriptPath(snapshot, "columns.show[0].summaryContent");
    }

    @Test
    public void rejectsInvalidSubviewParamScriptConfig() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        ReportViewSnapshot.ViewTab tab = new ReportViewSnapshot.ViewTab();
        tab.setStableKey("detail");
        tab.setParam(new ReportViewSnapshot.ScriptValue());
        snapshot.getSubviews().getViewTabs().add(tab);

        assertInvalidScriptPath(snapshot, "subviews.viewTabs[0].param");
    }

    @Test
    public void rejectsInvalidItemButtonParamScriptConfig() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        ReportViewSnapshot.CustomButton button = new ReportViewSnapshot.CustomButton();
        button.setStableKey("open");
        button.setParam(new ReportViewSnapshot.ScriptValue());
        snapshot.getButtons().getItem().add(button);

        assertInvalidScriptPath(snapshot, "buttons.item[0].param");
    }

    @Test
    public void rejectsInvalidSummaryButtonParamScriptConfig() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        ReportViewSnapshot.CustomButton button = new ReportViewSnapshot.CustomButton();
        button.setStableKey("summary");
        button.setParam(new ReportViewSnapshot.ScriptValue());
        snapshot.getButtons().getSummary().add(button);

        assertInvalidScriptPath(snapshot, "buttons.summary[0].param");
    }

    @Test
    public void rejectsInvalidScriptsListConfig() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        snapshot.getScripts().setList(new ReportViewSnapshot.ScriptValue());

        assertInvalidScriptPath(snapshot, "scripts.list");
    }

    @Test
    public void rejectsInvalidWeixinScriptConfig() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        ReportViewSnapshot.Weixin weixin = new ReportViewSnapshot.Weixin();
        weixin.setTitle(new ReportViewSnapshot.ScriptValue());
        weixin.setImage(new ReportViewSnapshot.ScriptValue());
        weixin.setDescription(new ReportViewSnapshot.ScriptValue());
        weixin.setDate(new ReportViewSnapshot.ScriptValue());
        snapshot.setWeixin(weixin);

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertTrue(Fixtures.errorCodes(result).contains("REPORT_VIEW_INVALID_SCRIPT_CONFIG"));
        assertTrue(Fixtures.errorPaths(result).contains("weixin.title"));
        assertTrue(Fixtures.errorPaths(result).contains("weixin.image"));
        assertTrue(Fixtures.errorPaths(result).contains("weixin.description"));
        assertTrue(Fixtures.errorPaths(result).contains("weixin.date"));
    }

    @Test
    public void rejectsMissingQueryNameAfterDefaultsNormalize() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        ReportViewSnapshot.Query query = new ReportViewSnapshot.Query();
        query.setSql(Fixtures.script("and CUSTOMER_NAME = :customerName"));
        snapshot.getQueries().add(query);

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertTrue(Fixtures.errorPaths(result).contains("queries[0].name"));
        assertEquals(null, result.getNormalizedSnapshot().getQueries().get(0).getName());
    }

    @Test
    public void rejectsMissingPreparedVariableVarAfterDefaultsNormalize() {
        ReportViewSnapshot snapshot = Fixtures.reportSnapshot("SALES_REPORT");
        ReportViewSnapshot.PreparedVariable variable = new ReportViewSnapshot.PreparedVariable();
        variable.setExec(Fixtures.script("return vo.ID;"));
        snapshot.getVariables().getPrepared().add(variable);

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertTrue(Fixtures.errorPaths(result).contains("variables.prepared[0].var"));
        assertEquals(null, result.getNormalizedSnapshot().getVariables().getPrepared().get(0).getVar());
    }

    private void assertInvalidScriptPath(ReportViewSnapshot snapshot, String path) {
        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertTrue(Fixtures.errorCodes(result).contains("REPORT_VIEW_INVALID_SCRIPT_CONFIG"));
        assertTrue(Fixtures.errorPaths(result).contains(path));
    }
}
