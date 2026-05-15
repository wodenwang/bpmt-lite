package com.riversoft.api.modules.report_views;

import org.junit.Test;

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
        snapshot.getQueries().add(query);

        ReportViewValidationResult result = new ReportViewValidator().validate(snapshot);

        assertFalse(result.isValid());
        assertEquals("REPORT_VIEW_UNSUPPORTED_PERMISSION", result.getErrors().get(0).getCode());
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
}
