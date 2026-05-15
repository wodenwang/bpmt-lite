package com.riversoft.api.modules.report_views;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReportViewValidator {
    private static final String INVALID = "REPORT_VIEW_INVALID_SNAPSHOT";
    private static final String INVALID_SQL_CONFIG = "REPORT_VIEW_INVALID_SQL_CONFIG";
    private static final String UNSUPPORTED_PERMISSION = "REPORT_VIEW_UNSUPPORTED_PERMISSION";

    private final ReportViewDefaults defaults = new ReportViewDefaults();
    private final ReportViewScriptRiskScanner scanner = new ReportViewScriptRiskScanner();

    public ReportViewValidationResult validate(ReportViewSnapshot snapshot) {
        ReportViewValidationResult result = new ReportViewValidationResult();
        ReportViewSnapshot normalized = defaults.normalize(snapshot);
        result.setNormalizedSnapshot(normalized);
        validateBase(normalized.getBase(), result);
        validateQueries(normalized.getQueries(), result);
        validateLimits(normalized.getLimits(), result);
        validateVariables(normalized.getVariables(), result);
        validateUnsupportedPermissions(normalized, result);
        result.setWarnings(scanner.scan(normalized));
        return result;
    }

    private void validateBase(ReportViewSnapshot.Base base, ReportViewValidationResult result) {
        if (base == null) {
            result.addError("base", INVALID, "base 不能为空。");
            return;
        }
        if (StringUtils.isBlank(base.getDisplayName())) {
            result.addError("base.displayName", INVALID, "base.displayName 不能为空。");
        }
        requireScript(base.getMainSql(), "base.mainSql", result);
        if (base.getPrimaryKey() != null) {
            if (base.getPrimaryKey().getValue() != null) {
                requireScript(base.getPrimaryKey().getValue(), "base.primaryKey.value", result);
            }
            if (base.getPrimaryKey().getSql() != null) {
                requireScript(base.getPrimaryKey().getSql(), "base.primaryKey.sql", result);
            }
        }
        if (base.getLayoutColumns() == null
                || base.getLayoutColumns().intValue() < 1
                || base.getLayoutColumns().intValue() > 5) {
            result.addError("base.layoutColumns", INVALID, "base.layoutColumns 必须在 1 到 5 之间。");
        }
        if (base.getPagination() != null && Boolean.TRUE.equals(base.getPagination().getEnabled())
                && (base.getPagination().getPageLimit() == null
                || base.getPagination().getPageLimit().intValue() <= 0)) {
            result.addError("base.pagination.pageLimit", INVALID, "启用分页时 base.pagination.pageLimit 必须大于 0。");
        }
    }

    private void validateQueries(List<ReportViewSnapshot.Query> queries, ReportViewValidationResult result) {
        Set<String> names = new HashSet<String>();
        for (int i = 0; i < queries.size(); i++) {
            ReportViewSnapshot.Query query = queries.get(i);
            if (query == null) {
                continue;
            }
            String name = StringUtils.trimToNull(query.getName());
            if (name == null) {
                result.addError("queries[" + i + "].name", INVALID, "queries[" + i + "].name 不能为空。");
            } else if (!names.add(name)) {
                result.addError("queries[" + i + "].name", INVALID, "queries.name 不能重复：" + name);
            }
            requireScript(query.getSql(), "queries[" + i + "].sql", result);
        }
    }

    private void validateLimits(List<ReportViewSnapshot.Limit> limits, ReportViewValidationResult result) {
        for (int i = 0; i < limits.size(); i++) {
            ReportViewSnapshot.Limit limit = limits.get(i);
            if (limit != null) {
                requireScript(limit.getSql(), "limits[" + i + "].sql", result);
            }
        }
    }

    private void validateVariables(ReportViewSnapshot.Variables variables, ReportViewValidationResult result) {
        if (variables == null) {
            return;
        }
        for (int i = 0; i < variables.getPrepared().size(); i++) {
            ReportViewSnapshot.PreparedVariable variable = variables.getPrepared().get(i);
            if (variable == null) {
                continue;
            }
            if (StringUtils.isBlank(variable.getVar())) {
                result.addError("variables.prepared[" + i + "].var", INVALID, "variables.prepared[" + i + "].var 不能为空。");
            }
            requireScript(variable.getExec(), "variables.prepared[" + i + "].exec", result);
        }
    }

    private void validateUnsupportedPermissions(ReportViewSnapshot snapshot, ReportViewValidationResult result) {
        List<ReportViewSnapshot.Query> queries = snapshot.getQueries();
        for (int i = 0; i < queries.size(); i++) {
            ReportViewSnapshot.Query query = queries.get(i);
            if (query != null && query.getPermissions() != null) {
                addUnsupportedPermissionError("queries[" + i + "].permissions", result);
            }
        }
        ReportViewSnapshot.Variables variables = snapshot.getVariables();
        if (variables == null) {
            return;
        }
        for (int i = 0; i < variables.getPrepared().size(); i++) {
            ReportViewSnapshot.PreparedVariable variable = variables.getPrepared().get(i);
            if (variable != null && variable.getPermissions() != null) {
                addUnsupportedPermissionError("variables.prepared[" + i + "].permissions", result);
            }
        }
    }

    private void addUnsupportedPermissionError(String path, ReportViewValidationResult result) {
        result.addError(path, UNSUPPORTED_PERMISSION, path + " 暂不支持权限配置。");
    }

    private void requireScript(ReportViewSnapshot.ScriptValue value, String path, ReportViewValidationResult result) {
        if (value == null || value.getType() == null || StringUtils.isBlank(value.getScript())) {
            result.addError(path, INVALID_SQL_CONFIG, path + " 必须包含 type 和 script。");
        }
    }
}
