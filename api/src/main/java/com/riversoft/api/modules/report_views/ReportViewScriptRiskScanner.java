package com.riversoft.api.modules.report_views;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ReportViewScriptRiskScanner {
    private static final String MEDIUM = "MEDIUM";

    public List<ReportViewResponse.Warning> scan(ReportViewSnapshot snapshot) {
        List<ReportViewResponse.Warning> warnings = new ArrayList<ReportViewResponse.Warning>();
        Set<String> codes = new LinkedHashSet<String>();
        if (snapshot == null) {
            return warnings;
        }
        ReportViewSnapshot normalized = new ReportViewDefaults().normalize(snapshot);
        scanBase(normalized.getBase(), warnings, codes);
        scanColumns(normalized.getColumns(), warnings, codes);
        scanQueries(normalized.getQueries(), warnings, codes);
        scanLimits(normalized.getLimits(), warnings, codes);
        scanVariables(normalized.getVariables(), warnings, codes);
        scanSubviews(normalized.getSubviews(), warnings, codes);
        scanButtons(normalized.getButtons(), warnings, codes);
        scanWeixin(normalized.getWeixin(), warnings, codes);
        scanScripts(normalized.getScripts(), warnings, codes);
        return warnings;
    }

    private void scanBase(ReportViewSnapshot.Base base, List<ReportViewResponse.Warning> warnings, Set<String> codes) {
        if (base == null) {
            return;
        }
        if (StringUtils.isNotBlank(base.getDbKey())) {
            add(warnings, codes, "base.dbKey", "EXTERNAL_DB_KEY_PRESENT", "报表视图使用了外部数据源 key。");
        }
        scanSqlScript(base.getMainSql(), "base.mainSql", warnings, codes);
        if (base.getPrimaryKey() != null) {
            scanSqlScript(base.getPrimaryKey().getSql(), "base.primaryKey.sql", warnings, codes);
            if (hasScript(base.getPrimaryKey().getValue())) {
                add(warnings, codes, "base.primaryKey.value", "SQL_SCRIPT_PRESENT", "报表视图包含服务端脚本或 SQL 配置。");
            }
        }
        if (StringUtils.isNotBlank(base.getOrderBySql())) {
            add(warnings, codes, "base.orderBySql", "UNEXECUTED_SQL_SEMANTICS", "API 不执行 SQL，也不验证 SQL 语义或字段正确性。");
        }
    }

    private void scanColumns(ReportViewSnapshot.Columns columns,
                             List<ReportViewResponse.Warning> warnings,
                             Set<String> codes) {
        if (columns == null) {
            return;
        }
        for (int i = 0; i < columns.getShow().size(); i++) {
            ReportViewSnapshot.ShowColumn column = columns.getShow().get(i);
            if (column == null) {
                continue;
            }
            scanClientScript(column.getContent(), "columns.show[" + i + "].content", warnings, codes);
            scanClientScript(column.getSummaryContent(), "columns.show[" + i + "].summaryContent", warnings, codes);
        }
        for (int i = 0; i < columns.getLines().size(); i++) {
            ReportViewSnapshot.LineColumn line = columns.getLines().get(i);
            if (line != null) {
                scanClientScript(line.getTip(), "columns.lines[" + i + "].tip", warnings, codes);
            }
        }
    }

    private void scanQueries(List<ReportViewSnapshot.Query> queries,
                             List<ReportViewResponse.Warning> warnings,
                             Set<String> codes) {
        for (int i = 0; i < queries.size(); i++) {
            ReportViewSnapshot.Query query = queries.get(i);
            if (query != null) {
                scanSqlScript(query.getSql(), "queries[" + i + "].sql", warnings, codes);
            }
        }
    }

    private void scanLimits(List<ReportViewSnapshot.Limit> limits,
                            List<ReportViewResponse.Warning> warnings,
                            Set<String> codes) {
        for (int i = 0; i < limits.size(); i++) {
            ReportViewSnapshot.Limit limit = limits.get(i);
            if (limit != null) {
                scanSqlScript(limit.getSql(), "limits[" + i + "].sql", warnings, codes);
            }
        }
    }

    private void scanVariables(ReportViewSnapshot.Variables variables,
                               List<ReportViewResponse.Warning> warnings,
                               Set<String> codes) {
        if (variables == null) {
            return;
        }
        for (int i = 0; i < variables.getPrepared().size(); i++) {
            ReportViewSnapshot.PreparedVariable variable = variables.getPrepared().get(i);
            if (variable != null && hasScript(variable.getExec())) {
                add(warnings, codes, "variables.prepared[" + i + "].exec", "SQL_SCRIPT_PRESENT", "报表视图包含服务端脚本或 SQL 配置。");
            }
        }
    }

    private void scanSubviews(ReportViewSnapshot.Subviews subviews,
                              List<ReportViewResponse.Warning> warnings,
                              Set<String> codes) {
        if (subviews == null) {
            return;
        }
        for (int i = 0; i < subviews.getViewTabs().size(); i++) {
            ReportViewSnapshot.ViewTab tab = subviews.getViewTabs().get(i);
            if (tab != null) {
                scanClientScript(tab.getParam(), "subviews.viewTabs[" + i + "].param", warnings, codes);
            }
        }
    }

    private void scanButtons(ReportViewSnapshot.Buttons buttons,
                             List<ReportViewResponse.Warning> warnings,
                             Set<String> codes) {
        if (buttons == null) {
            return;
        }
        scanCustomButtons(buttons.getItem(), "buttons.item", warnings, codes);
        scanCustomButtons(buttons.getSummary(), "buttons.summary", warnings, codes);
    }

    private void scanCustomButtons(List<ReportViewSnapshot.CustomButton> buttons,
                                   String path,
                                   List<ReportViewResponse.Warning> warnings,
                                   Set<String> codes) {
        for (int i = 0; i < buttons.size(); i++) {
            ReportViewSnapshot.CustomButton button = buttons.get(i);
            if (button != null && StringUtils.isNotBlank(button.getAction())) {
                add(warnings, codes, path + "[" + i + "].action", "BUTTON_ACTION_PRESENT", "报表视图包含自定义按钮动作。");
            }
            if (button != null) {
                scanClientScript(button.getParam(), path + "[" + i + "].param", warnings, codes);
            }
        }
    }

    private void scanScripts(ReportViewSnapshot.Scripts scripts,
                             List<ReportViewResponse.Warning> warnings,
                             Set<String> codes) {
        if (scripts != null && hasScript(scripts.getList())) {
            scanClientScript(scripts.getList(), "scripts.list", warnings, codes);
        }
    }

    private void scanWeixin(ReportViewSnapshot.Weixin weixin,
                            List<ReportViewResponse.Warning> warnings,
                            Set<String> codes) {
        if (weixin == null) {
            return;
        }
        scanClientScript(weixin.getTitle(), "weixin.title", warnings, codes);
        scanClientScript(weixin.getImage(), "weixin.image", warnings, codes);
        scanClientScript(weixin.getDescription(), "weixin.description", warnings, codes);
        scanClientScript(weixin.getDate(), "weixin.date", warnings, codes);
    }

    private void scanSqlScript(ReportViewSnapshot.ScriptValue value,
                               String path,
                               List<ReportViewResponse.Warning> warnings,
                               Set<String> codes) {
        if (!hasScript(value)) {
            return;
        }
        add(warnings, codes, path, "SQL_SCRIPT_PRESENT", "报表视图包含服务端脚本或 SQL 配置。");
        add(warnings, codes, path, "UNEXECUTED_SQL_SEMANTICS", "API 不执行 SQL，也不验证 SQL 语义或字段正确性。");
    }

    private boolean hasScript(ReportViewSnapshot.ScriptValue value) {
        return value != null && StringUtils.isNotBlank(value.getScript());
    }

    private void scanClientScript(ReportViewSnapshot.ScriptValue value,
                                  String path,
                                  List<ReportViewResponse.Warning> warnings,
                                  Set<String> codes) {
        if (hasScript(value)) {
            add(warnings, codes, path, "CLIENT_SCRIPT_PRESENT", "报表视图包含客户端脚本。");
        }
    }

    private void add(List<ReportViewResponse.Warning> warnings,
                     Set<String> codes,
                     String path,
                     String code,
                     String message) {
        if (codes.add(code)) {
            warnings.add(new ReportViewResponse.Warning(MEDIUM, path, code, message));
        }
    }
}
