package com.riversoft.api.modules.report_views;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class Fixtures {
    private Fixtures() {
    }

    static ReportViewSnapshot reportSnapshot(String viewKey) {
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
        snapshot.getBase().setOrderBySql("CREATE_DATE desc");
        ReportViewSnapshot.ShowColumn column = new ReportViewSnapshot.ShowColumn();
        column.setStableKey("ORDER_NO");
        column.setDisplayName("订单号");
        column.setContent(script("return vo.ORDER_NO;"));
        snapshot.getColumns().getShow().add(column);
        snapshot.getColumns().getListOrder().add("ORDER_NO");
        return snapshot;
    }

    static ReportViewSnapshot.ScriptValue script(String script) {
        ReportViewSnapshot.ScriptValue value = new ReportViewSnapshot.ScriptValue();
        value.setType(Integer.valueOf(1));
        value.setScript(script);
        return value;
    }

    static Set<String> warningCodes(List<ReportViewResponse.Warning> warnings) {
        Set<String> codes = new LinkedHashSet<String>();
        for (ReportViewResponse.Warning warning : warnings) {
            codes.add(warning.getCode());
        }
        return codes;
    }

    static Set<String> errorCodes(ReportViewValidationResult result) {
        Set<String> codes = new LinkedHashSet<String>();
        for (ReportViewResponse.ValidationItem error : result.getErrors()) {
            codes.add(error.getCode());
        }
        return codes;
    }
}
