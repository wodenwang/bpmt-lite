package com.riversoft.api.modules.report_views;

import com.riversoft.platform.po.CmPri;
import com.riversoft.platform.po.VwUrl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ReportViewMapper {
    ReportViewSnapshot toSnapshot(VwUrl url, Map<String, Object> report) {
        if (report == null || report.isEmpty()) {
            throw ReportViewErrors.notFound(url == null ? null : url.getViewKey());
        }

        ReportViewSnapshot snapshot = new ReportViewSnapshot();
        snapshot.setViewKey(firstString(urlMap(url), report, "viewKey"));
        snapshot.setDescription(url == null ? stringValue(report, "description") : url.getDescription());
        snapshot.setLoginRequired(url == null || url.getLoginType() == null || url.getLoginType().intValue() == 1);
        snapshot.setBase(base(report));
        snapshot.setColumns(columns(report));
        snapshot.setQueries(queries(report));
        snapshot.setLimits(limits(report));
        snapshot.setVariables(variables(report));
        snapshot.setSubviews(subviews(report));
        snapshot.setButtons(buttons(report));
        snapshot.setWeixin(weixin(asMap(report.get("weixin"))));
        snapshot.setScripts(scripts(report));
        return snapshot;
    }

    Map<String, Object> toReportMap(ReportViewSnapshot snapshot) {
        ReportViewSnapshot.Base base = snapshot.getBase();
        ReportViewSnapshot.Scripts scripts = snapshot.getScripts();
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("viewKey", snapshot.getViewKey());
        map.put("busiName", base == null ? null : base.getDisplayName());
        map.put("sort", Integer.valueOf(0));
        map.put("dbKey", base == null ? null : base.getDbKey());
        putScript(map, "mainSqlType", "mainSqlScript", base == null ? null : base.getMainSql());
        putPrimaryKey(map, base == null ? null : base.getPrimaryKey());
        map.put("orderBy", base == null ? null : base.getOrderBySql());
        map.put("col", base == null ? null : base.getLayoutColumns());
        map.put("initQuery", flag(base == null ? null : base.getInitQuery()));
        map.put("pageFlag", flag(base == null || base.getPagination() == null ? null : base.getPagination().getEnabled()));
        map.put("pageLimit", base == null || base.getPagination() == null ? null : base.getPagination().getPageLimit());
        map.put("summaryFlag", flag(base == null ? null : base.getSummaryEnabled()));
        map.put("description", snapshot.getDescription());
        putScript(map, "listJsType", "listJsScript", scripts == null ? null : scripts.getList());
        map.put("showColumns", showColumns(snapshot));
        map.put("lineColumns", lineColumns(snapshot));
        map.put("querys", queries(snapshot));
        map.put("limits", limits(snapshot));
        map.put("prepareExecs", prepareExecs(snapshot));
        map.put("viewSubs", viewSubs(snapshot));
        map.put("sysBtns", sysBtns(snapshot));
        map.put("itemBtns", itemBtns(snapshot));
        map.put("summaryBtns", summaryBtns(snapshot));
        map.put("weixin", weixin(snapshot));
        return map;
    }

    private ReportViewSnapshot.Base base(Map<String, Object> report) {
        ReportViewSnapshot.Base base = new ReportViewSnapshot.Base();
        base.setDisplayName(stringValue(report, "busiName"));
        base.setDbKey(stringValue(report, "dbKey"));
        base.setMainSql(script(report, "mainSqlType", "mainSqlScript"));
        base.setPrimaryKey(primaryKey(report));
        base.setLayoutColumns(intValue(report, "col"));
        base.setInitQuery(Boolean.valueOf(intFlag(report.get("initQuery"))));
        base.getPagination().setEnabled(Boolean.valueOf(intFlag(report.get("pageFlag"))));
        base.getPagination().setPageLimit(intValue(report, "pageLimit"));
        base.setSummaryEnabled(Boolean.valueOf(intFlag(report.get("summaryFlag"))));
        base.setOrderBySql(stringValue(report, "orderBy"));
        return base;
    }

    private ReportViewSnapshot.PrimaryKey primaryKey(Map<String, Object> report) {
        ReportViewSnapshot.ScriptValue value = script(report, "pkType", "pkScript");
        ReportViewSnapshot.ScriptValue sql = script(report, "pkSqlType", "pkSqlScript");
        if (value == null && sql == null) {
            return null;
        }
        ReportViewSnapshot.PrimaryKey primaryKey = new ReportViewSnapshot.PrimaryKey();
        primaryKey.setValue(value);
        primaryKey.setSql(sql);
        return primaryKey;
    }

    private ReportViewSnapshot.Columns columns(Map<String, Object> report) {
        ReportViewSnapshot.Columns columns = new ReportViewSnapshot.Columns();
        columns.setShow(showColumns(collectionValue(report, "showColumns")));
        columns.setLines(lineColumns(collectionValue(report, "lineColumns")));
        columns.setListOrder(listOrder(collectionValue(report, "showColumns")));
        return columns;
    }

    private List<ReportViewSnapshot.ShowColumn> showColumns(List<Map<String, Object>> rows) {
        List<ReportViewSnapshot.ShowColumn> result = new ArrayList<ReportViewSnapshot.ShowColumn>();
        for (Map<String, Object> row : sortByInt(rows, "sort")) {
            ReportViewSnapshot.ShowColumn column = new ReportViewSnapshot.ShowColumn();
            column.setStableKey(firstString(row, null, "key", "stableKey", "sortField", "id"));
            column.setDisplayName(stringValue(row, "busiName"));
            column.setStyle(stringValue(row, "style"));
            column.setWholeLine(Boolean.valueOf(intFlag(row.get("whole"))));
            column.setSortField(stringValue(row, "sortField"));
            column.setContent(script(row, "contentType", "contentScript"));
            column.setSummaryContent(script(row, "summaryContentType", "summaryContentScript"));
            column.setPermissions(permissions(row.get("pri")));
            result.add(column);
        }
        return result;
    }

    private List<ReportViewSnapshot.LineColumn> lineColumns(List<Map<String, Object>> rows) {
        List<ReportViewSnapshot.LineColumn> result = new ArrayList<ReportViewSnapshot.LineColumn>();
        for (Map<String, Object> row : sortByInt(rows, "sort")) {
            ReportViewSnapshot.LineColumn line = new ReportViewSnapshot.LineColumn();
            line.setStableKey(firstString(row, null, "key", "stableKey", "id"));
            line.setDisplayName(stringValue(row, "busiName"));
            line.setTip(script(row, "tipType", "tipScript"));
            line.setExpanded(Boolean.valueOf(intFlag(row.get("expandFlag"))));
            line.setPermissions(permissions(row.get("pri")));
            result.add(line);
        }
        return result;
    }

    private List<ReportViewSnapshot.Query> queries(Map<String, Object> report) {
        List<ReportViewSnapshot.Query> result = new ArrayList<ReportViewSnapshot.Query>();
        for (Map<String, Object> row : sortByInt(collectionValue(report, "querys"), "sort")) {
            ReportViewSnapshot.Query query = new ReportViewSnapshot.Query();
            query.setName(stringValue(row, "name"));
            query.setDisplayName(stringValue(row, "busiName"));
            query.setWidget(stringValue(row, "widget"));
            query.setWidgetParam(script(row, "widgetParamType", "widgetParamScript"));
            query.setDefaultValue(stringValue(row, "defVal"));
            query.setSql(script(row, "sqlType", "sqlScript"));
            query.setDescription(stringValue(row, "description"));
            query.setPermissions(permissions(row.get("pri")));
            result.add(query);
        }
        return result;
    }

    private List<ReportViewSnapshot.Limit> limits(Map<String, Object> report) {
        List<ReportViewSnapshot.Limit> result = new ArrayList<ReportViewSnapshot.Limit>();
        for (Map<String, Object> row : sortByInt(collectionValue(report, "limits"), "sort")) {
            ReportViewSnapshot.Limit limit = new ReportViewSnapshot.Limit();
            limit.setStableKey(firstString(row, null, "key", "stableKey", "id"));
            limit.setDescription(stringValue(row, "description"));
            limit.setSql(script(row, "sqlType", "sqlScript"));
            limit.setPermissions(permissions(row.get("pri")));
            result.add(limit);
        }
        return result;
    }

    private ReportViewSnapshot.Variables variables(Map<String, Object> report) {
        ReportViewSnapshot.Variables variables = new ReportViewSnapshot.Variables();
        List<ReportViewSnapshot.PreparedVariable> result = new ArrayList<ReportViewSnapshot.PreparedVariable>();
        for (Map<String, Object> row : sortByInt(collectionValue(report, "prepareExecs"), "sort")) {
            ReportViewSnapshot.PreparedVariable variable = new ReportViewSnapshot.PreparedVariable();
            variable.setVar(stringValue(row, "var"));
            variable.setDescription(stringValue(row, "description"));
            variable.setExec(script(row, "execType", "execScript"));
            variable.setPermissions(permissions(row.get("pri")));
            result.add(variable);
        }
        variables.setPrepared(result);
        return variables;
    }

    private ReportViewSnapshot.Subviews subviews(Map<String, Object> report) {
        ReportViewSnapshot.Subviews subviews = new ReportViewSnapshot.Subviews();
        List<ReportViewSnapshot.ViewTab> result = new ArrayList<ReportViewSnapshot.ViewTab>();
        for (Map<String, Object> row : sortByInt(collectionValue(report, "viewSubs"), "sort")) {
            ReportViewSnapshot.ViewTab tab = new ReportViewSnapshot.ViewTab();
            tab.setStableKey(firstString(row, null, "subKey", "key"));
            tab.setDisplayName(stringValue(row, "busiName"));
            tab.setStyle(stringValue(row, "style"));
            tab.setAction(stringValue(row, "action"));
            tab.setParam(script(row, "paramType", "paramScript"));
            tab.setPermissions(permissions(row.get("pri")));
            result.add(tab);
        }
        subviews.setViewTabs(result);
        return subviews;
    }

    private ReportViewSnapshot.Buttons buttons(Map<String, Object> report) {
        ReportViewSnapshot.Buttons buttons = new ReportViewSnapshot.Buttons();
        buttons.setSystem(systemButtons(collectionValue(report, "sysBtns")));
        buttons.setItem(customButtons(collectionValue(report, "itemBtns")));
        buttons.setSummary(customButtons(collectionValue(report, "summaryBtns")));
        return buttons;
    }

    private List<ReportViewSnapshot.SystemButton> systemButtons(List<Map<String, Object>> rows) {
        List<ReportViewSnapshot.SystemButton> result = new ArrayList<ReportViewSnapshot.SystemButton>();
        for (Map<String, Object> row : sortByInt(rows, "sort")) {
            ReportViewSnapshot.SystemButton button = new ReportViewSnapshot.SystemButton();
            button.setName(stringValue(row, "name"));
            button.setType(intValue(row, "type"));
            button.setDisplayName(stringValue(row, "busiName"));
            button.setIcon(stringValue(row, "icon"));
            button.setStyleClass(stringValue(row, "styleClass"));
            button.setDescription(stringValue(row, "description"));
            button.setPermissions(permissions(row.get("pri")));
            result.add(button);
        }
        return result;
    }

    private List<ReportViewSnapshot.CustomButton> customButtons(List<Map<String, Object>> rows) {
        List<ReportViewSnapshot.CustomButton> result = new ArrayList<ReportViewSnapshot.CustomButton>();
        for (Map<String, Object> row : sortByInt(rows, "sort")) {
            ReportViewSnapshot.CustomButton button = new ReportViewSnapshot.CustomButton();
            button.setStableKey(firstString(row, null, "key", "stableKey", "id"));
            button.setDisplayName(stringValue(row, "busiName"));
            button.setIcon(stringValue(row, "icon"));
            button.setStyleClass(stringValue(row, "styleClass"));
            button.setAction(stringValue(row, "action"));
            button.setOpenType(intValue(row, "openType"));
            button.setParam(script(row, "paramType", "paramScript"));
            button.setConfirmMessage(stringValue(row, "confirmMsg"));
            button.setDescription(stringValue(row, "description"));
            button.setPermissions(permissions(row.get("pri")));
            result.add(button);
        }
        return result;
    }

    private ReportViewSnapshot.Weixin weixin(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        ReportViewSnapshot.Weixin weixin = new ReportViewSnapshot.Weixin();
        weixin.setListMode(intValue(row, "listMode"));
        weixin.setUrlMode(intValue(row, "urlMode"));
        weixin.setTitle(script(row, "titleType", "titleScript"));
        weixin.setImage(script(row, "imgType", "imgScript"));
        weixin.setDescription(script(row, "desType", "desScript"));
        weixin.setDate(script(row, "dateType", "dateScript"));
        weixin.setPermissions(permissions(row.get("pri")));
        return weixin;
    }

    private ReportViewSnapshot.Scripts scripts(Map<String, Object> report) {
        ReportViewSnapshot.Scripts scripts = new ReportViewSnapshot.Scripts();
        scripts.setList(script(report, "listJsType", "listJsScript"));
        return scripts;
    }

    private Set<Map<String, Object>> showColumns(ReportViewSnapshot snapshot) {
        Set<Map<String, Object>> result = new LinkedHashSet<Map<String, Object>>();
        ReportViewSnapshot.Columns columns = snapshot.getColumns();
        if (columns == null || columns.getShow() == null) {
            return result;
        }
        List<String> listOrder = columns.getListOrder();
        for (int i = 0; i < columns.getShow().size(); i++) {
            ReportViewSnapshot.ShowColumn column = columns.getShow().get(i);
            if (column == null) {
                continue;
            }
            Map<String, Object> row = typed("VwReportColumnShow");
            row.put("viewKey", snapshot.getViewKey());
            row.put("busiName", column.getDisplayName());
            row.put("style", column.getStyle());
            row.put("whole", flag(column.getWholeLine()));
            row.put("sortField", column.getSortField());
            putScript(row, "contentType", "contentScript", column.getContent());
            putScript(row, "summaryContentType", "summaryContentScript", column.getSummaryContent());
            row.put("sort", Integer.valueOf(i));
            row.put("listSort", Integer.valueOf(listSort(listOrder, column.getStableKey())));
            row.put("pri", pri(permission(column.getPermissions()), row, "报表展示列", "查看"));
            result.add(row);
        }
        return result;
    }

    private Set<Map<String, Object>> lineColumns(ReportViewSnapshot snapshot) {
        Set<Map<String, Object>> result = new LinkedHashSet<Map<String, Object>>();
        ReportViewSnapshot.Columns columns = snapshot.getColumns();
        if (columns == null || columns.getLines() == null) {
            return result;
        }
        for (int i = 0; i < columns.getLines().size(); i++) {
            ReportViewSnapshot.LineColumn line = columns.getLines().get(i);
            if (line == null) {
                continue;
            }
            Map<String, Object> row = typed("VwReportColumnLine");
            row.put("viewKey", snapshot.getViewKey());
            row.put("busiName", line.getDisplayName());
            putScript(row, "tipType", "tipScript", line.getTip());
            row.put("expandFlag", flag(line.getExpanded()));
            row.put("sort", Integer.valueOf(i));
            row.put("pri", pri(permission(line.getPermissions()), row, "报表分割线", "查看"));
            result.add(row);
        }
        return result;
    }

    private Set<Map<String, Object>> queries(ReportViewSnapshot snapshot) {
        Set<Map<String, Object>> result = new LinkedHashSet<Map<String, Object>>();
        if (snapshot.getQueries() == null) {
            return result;
        }
        for (int i = 0; i < snapshot.getQueries().size(); i++) {
            ReportViewSnapshot.Query query = snapshot.getQueries().get(i);
            if (query == null) {
                continue;
            }
            Map<String, Object> row = typed("VwReportQuery");
            row.put("viewKey", snapshot.getViewKey());
            row.put("busiName", query.getDisplayName());
            row.put("name", query.getName());
            row.put("widget", query.getWidget());
            putScript(row, "widgetParamType", "widgetParamScript", query.getWidgetParam());
            row.put("defVal", query.getDefaultValue());
            row.put("sort", Integer.valueOf(i));
            putScript(row, "sqlType", "sqlScript", query.getSql());
            row.put("description", query.getDescription());
            result.add(row);
        }
        return result;
    }

    private Set<Map<String, Object>> limits(ReportViewSnapshot snapshot) {
        Set<Map<String, Object>> result = new LinkedHashSet<Map<String, Object>>();
        if (snapshot.getLimits() == null) {
            return result;
        }
        for (int i = 0; i < snapshot.getLimits().size(); i++) {
            ReportViewSnapshot.Limit limit = snapshot.getLimits().get(i);
            if (limit == null) {
                continue;
            }
            Map<String, Object> row = typed("VwReportLimit");
            row.put("viewKey", snapshot.getViewKey());
            row.put("description", limit.getDescription());
            putScript(row, "sqlType", "sqlScript", limit.getSql());
            row.put("sort", Integer.valueOf(i));
            row.put("pri", pri(permission(limit.getPermissions()), row, "报表数据约束", "查看"));
            result.add(row);
        }
        return result;
    }

    private Set<Map<String, Object>> prepareExecs(ReportViewSnapshot snapshot) {
        Set<Map<String, Object>> result = new LinkedHashSet<Map<String, Object>>();
        ReportViewSnapshot.Variables variables = snapshot.getVariables();
        if (variables == null || variables.getPrepared() == null) {
            return result;
        }
        for (int i = 0; i < variables.getPrepared().size(); i++) {
            ReportViewSnapshot.PreparedVariable variable = variables.getPrepared().get(i);
            if (variable == null) {
                continue;
            }
            Map<String, Object> row = typed("VwReportExecPrepare");
            row.put("viewKey", snapshot.getViewKey());
            putScript(row, "execType", "execScript", variable.getExec());
            row.put("description", variable.getDescription());
            row.put("var", variable.getVar());
            row.put("sort", Integer.valueOf(i));
            result.add(row);
        }
        return result;
    }

    private Set<Map<String, Object>> viewSubs(ReportViewSnapshot snapshot) {
        Set<Map<String, Object>> result = new LinkedHashSet<Map<String, Object>>();
        ReportViewSnapshot.Subviews subviews = snapshot.getSubviews();
        if (subviews == null || subviews.getViewTabs() == null) {
            return result;
        }
        for (int i = 0; i < subviews.getViewTabs().size(); i++) {
            ReportViewSnapshot.ViewTab tab = subviews.getViewTabs().get(i);
            if (tab == null) {
                continue;
            }
            Map<String, Object> row = typed("VwReportSubView");
            row.put("subKey", tab.getStableKey());
            row.put("viewKey", snapshot.getViewKey());
            row.put("busiName", tab.getDisplayName());
            row.put("style", tab.getStyle());
            row.put("sort", Integer.valueOf(i));
            row.put("action", tab.getAction());
            putScript(row, "paramType", "paramScript", tab.getParam());
            row.put("pri", pri(permission(tab.getPermissions()), row, "报表子视图", "查看"));
            result.add(row);
        }
        return result;
    }

    private Set<Map<String, Object>> sysBtns(ReportViewSnapshot snapshot) {
        Set<Map<String, Object>> result = new LinkedHashSet<Map<String, Object>>();
        ReportViewSnapshot.Buttons buttons = snapshot.getButtons();
        if (buttons == null || buttons.getSystem() == null) {
            return result;
        }
        for (int i = 0; i < buttons.getSystem().size(); i++) {
            ReportViewSnapshot.SystemButton button = buttons.getSystem().get(i);
            if (button == null) {
                continue;
            }
            Map<String, Object> row = typed("VwReportBtnSys");
            row.put("viewKey", snapshot.getViewKey());
            row.put("type", button.getType());
            row.put("name", button.getName());
            row.put("busiName", button.getDisplayName());
            row.put("icon", button.getIcon());
            row.put("styleClass", button.getStyleClass());
            row.put("sort", Integer.valueOf(i));
            row.put("description", button.getDescription());
            row.put("pri", pri(permission(button.getPermissions()), row, "报表系统按钮", "查看"));
            result.add(row);
        }
        return result;
    }

    private Set<Map<String, Object>> itemBtns(ReportViewSnapshot snapshot) {
        ReportViewSnapshot.Buttons buttons = snapshot.getButtons();
        return customButtonMaps(snapshot.getViewKey(), buttons == null ? null : buttons.getItem(), "VwReportBtnItem");
    }

    private Set<Map<String, Object>> summaryBtns(ReportViewSnapshot snapshot) {
        ReportViewSnapshot.Buttons buttons = snapshot.getButtons();
        return customButtonMaps(snapshot.getViewKey(), buttons == null ? null : buttons.getSummary(), "VwReportBtnSummary");
    }

    private Set<Map<String, Object>> customButtonMaps(String viewKey,
                                                      List<ReportViewSnapshot.CustomButton> buttons,
                                                      String type) {
        Set<Map<String, Object>> result = new LinkedHashSet<Map<String, Object>>();
        if (buttons == null) {
            return result;
        }
        for (int i = 0; i < buttons.size(); i++) {
            ReportViewSnapshot.CustomButton button = buttons.get(i);
            if (button == null) {
                continue;
            }
            Map<String, Object> row = typed(type);
            row.put("viewKey", viewKey);
            row.put("busiName", button.getDisplayName());
            row.put("styleClass", button.getStyleClass());
            row.put("icon", button.getIcon());
            row.put("action", button.getAction());
            row.put("openType", button.getOpenType());
            row.put("description", button.getDescription());
            row.put("sort", Integer.valueOf(i));
            putScript(row, "paramType", "paramScript", button.getParam());
            row.put("confirmMsg", button.getConfirmMessage());
            row.put("pri", pri(permission(button.getPermissions()), row, "报表自定义按钮", "查看"));
            result.add(row);
        }
        return result;
    }

    private Map<String, Object> weixin(ReportViewSnapshot snapshot) {
        ReportViewSnapshot.Weixin weixin = snapshot.getWeixin();
        if (weixin == null) {
            return null;
        }
        Map<String, Object> row = typed("VwReportWeixin");
        row.put("viewKey", snapshot.getViewKey());
        row.put("listMode", weixin.getListMode());
        row.put("urlMode", weixin.getUrlMode());
        putScript(row, "titleType", "titleScript", weixin.getTitle());
        putScript(row, "imgType", "imgScript", weixin.getImage());
        putScript(row, "desType", "desScript", weixin.getDescription());
        putScript(row, "dateType", "dateScript", weixin.getDate());
        row.put("pri", pri(permission(weixin.getPermissions()), row, "报表微信配置", "查看"));
        return row;
    }

    private void putPrimaryKey(Map<String, Object> map, ReportViewSnapshot.PrimaryKey primaryKey) {
        putScript(map, "pkType", "pkScript", primaryKey == null ? null : primaryKey.getValue());
        putScript(map, "pkSqlType", "pkSqlScript", primaryKey == null ? null : primaryKey.getSql());
    }

    private void putScript(Map<String, Object> map, String typeKey, String scriptKey, ReportViewSnapshot.ScriptValue value) {
        map.put(typeKey, scriptType(value));
        map.put(scriptKey, scriptText(value));
    }

    private Map<String, Object> typed(String type) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("$type$", type);
        return row;
    }

    private int listSort(List<String> listOrder, String stableKey) {
        if (stableKey == null || listOrder == null) {
            return -1;
        }
        int index = listOrder.indexOf(stableKey);
        return index < 0 ? -1 : index;
    }

    private List<String> listOrder(List<Map<String, Object>> rows) {
        List<String> result = new ArrayList<String>();
        for (Map<String, Object> row : sortByInt(rows, "listSort")) {
            Integer listSort = intValue(row, "listSort");
            if (listSort == null || listSort.intValue() < 0) {
                continue;
            }
            String stableKey = firstString(row, null, "key", "stableKey", "sortField", "id");
            if (stableKey != null) {
                result.add(stableKey);
            }
        }
        return result;
    }

    private Integer flag(Boolean value) {
        return Boolean.TRUE.equals(value) ? Integer.valueOf(1) : Integer.valueOf(0);
    }

    private boolean intFlag(Object value) {
        Integer number = intValue(value);
        return number != null && number.intValue() == 1;
    }

    private Integer scriptType(ReportViewSnapshot.ScriptValue value) {
        return value == null ? null : value.getType();
    }

    private String scriptText(ReportViewSnapshot.ScriptValue value) {
        return value == null ? null : value.getScript();
    }

    private ReportViewSnapshot.ScriptValue script(Map<String, Object> map, String typeKey, String scriptKey) {
        Integer type = intValue(map, typeKey);
        String text = stringValue(map, scriptKey);
        if (type == null && text == null) {
            return null;
        }
        ReportViewSnapshot.ScriptValue value = new ReportViewSnapshot.ScriptValue();
        value.setType(type);
        value.setScript(text);
        return value;
    }

    private String permission(ReportViewSnapshot.PermissionSet permissions) {
        if (permissions == null || permissions.getView() == null || permissions.getView().isEmpty()) {
            return null;
        }
        return permissions.getView().get(0);
    }

    private ReportViewSnapshot.PermissionSet permissions(Object value) {
        ReportViewSnapshot.PermissionSet permissions = new ReportViewSnapshot.PermissionSet();
        String priKey = permissionValue(value);
        if (priKey != null) {
            List<String> view = new ArrayList<String>();
            view.add(priKey);
            permissions.setView(view);
        }
        return permissions;
    }

    private CmPri pri(String priKey, Map<String, Object> owner, String... labels) {
        if (priKey == null || priKey.trim().length() == 0) {
            return null;
        }
        CmPri pri = new CmPri();
        pri.setPriKey(priKey);
        pri.setType(Integer.valueOf(1));
        pri.setCheckType(Integer.valueOf(2));
        pri.setCheckScript("${true}");
        pri.setDevelopmentInfo(owner, labels);
        if (pri.getCatelogType() == null) {
            pri.setCatelogType((Integer) CmPri.Catelog.VIEW.getCode());
        }
        if (pri.getCatelogKey() == null || pri.getCatelogKey().trim().length() == 0) {
            pri.setCatelogKey(stringValue(owner, "viewKey"));
        }
        if (pri.getBusiName() == null || pri.getBusiName().trim().length() == 0) {
            pri.setBusiName(priKey);
        }
        return pri;
    }

    private Map<String, Object> urlMap(VwUrl url) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if (url != null) {
            map.put("viewKey", url.getViewKey());
        }
        return map;
    }

    private String firstString(Map<String, Object> first, Map<String, Object> second, String... keys) {
        for (String key : keys) {
            String value = stringValue(first, key);
            if (value != null) {
                return value;
            }
            value = stringValue(second, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String stringValue(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Integer intValue(Map<String, Object> map, String key) {
        return intValue(map == null ? null : map.get(key));
    }

    private Integer intValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }
        if (value == null || String.valueOf(value).trim().length() == 0) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> collectionValue(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (value == null) {
            return result;
        }
        if (value instanceof Collection) {
            for (Object item : (Collection<Object>) value) {
                Map<String, Object> itemMap = asMap(item);
                if (itemMap != null) {
                    result.add(itemMap);
                }
            }
            return result;
        }
        Map<String, Object> itemMap = asMap(value);
        if (itemMap != null) {
            result.add(itemMap);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    private List<Map<String, Object>> sortByInt(List<Map<String, Object>> rows, final String key) {
        List<Map<String, Object>> sorted = new ArrayList<Map<String, Object>>(rows);
        Collections.sort(sorted, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> left, Map<String, Object> right) {
                Integer leftValue = intValue(left, key);
                Integer rightValue = intValue(right, key);
                if (leftValue == null && rightValue == null) {
                    return 0;
                }
                if (leftValue == null) {
                    return 1;
                }
                if (rightValue == null) {
                    return -1;
                }
                return leftValue.compareTo(rightValue);
            }
        });
        return sorted;
    }

    private String permissionValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof CmPri) {
            return ((CmPri) value).getPriKey();
        }
        Map<String, Object> map = asMap(value);
        if (map != null) {
            String priKey = firstString(map, null, "priKey", "id", "key", "name");
            return priKey == null ? String.valueOf(value) : priKey;
        }
        String reflected = reflectedValue(value, "getPriKey");
        if (reflected != null) {
            return reflected;
        }
        reflected = reflectedValue(value, "getId");
        return reflected == null ? String.valueOf(value) : reflected;
    }

    private String reflectedValue(Object value, String methodName) {
        try {
            Method method = value.getClass().getMethod(methodName);
            Object result = method.invoke(value);
            return result == null ? null : String.valueOf(result);
        } catch (Exception e) {
            return null;
        }
    }
}
