package com.riversoft.api.modules.report_views;

import java.util.ArrayList;
import java.util.List;

public class ReportViewSnapshot {
    private String viewKey;
    private String description;
    private boolean loginRequired = true;
    private Base base = new Base();
    private Columns columns = new Columns();
    private List<Query> queries = new ArrayList<Query>();
    private List<Limit> limits = new ArrayList<Limit>();
    private Variables variables = new Variables();
    private Subviews subviews = new Subviews();
    private Buttons buttons = new Buttons();
    private Weixin weixin;
    private Scripts scripts = new Scripts();
    private PermissionSet permissions;

    public String getViewKey() {
        return viewKey;
    }

    public void setViewKey(String viewKey) {
        this.viewKey = viewKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLoginRequired() {
        return loginRequired;
    }

    public void setLoginRequired(boolean loginRequired) {
        this.loginRequired = loginRequired;
    }

    public Base getBase() {
        return base;
    }

    public void setBase(Base base) {
        this.base = base;
    }

    public Columns getColumns() {
        return columns;
    }

    public void setColumns(Columns columns) {
        this.columns = columns;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(List<Query> queries) {
        this.queries = queries;
    }

    public List<Limit> getLimits() {
        return limits;
    }

    public void setLimits(List<Limit> limits) {
        this.limits = limits;
    }

    public Variables getVariables() {
        return variables;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    public Subviews getSubviews() {
        return subviews;
    }

    public void setSubviews(Subviews subviews) {
        this.subviews = subviews;
    }

    public Buttons getButtons() {
        return buttons;
    }

    public void setButtons(Buttons buttons) {
        this.buttons = buttons;
    }

    public Weixin getWeixin() {
        return weixin;
    }

    public void setWeixin(Weixin weixin) {
        this.weixin = weixin;
    }

    public Scripts getScripts() {
        return scripts;
    }

    public void setScripts(Scripts scripts) {
        this.scripts = scripts;
    }

    public PermissionSet getPermissions() {
        return permissions;
    }

    public void setPermissions(PermissionSet permissions) {
        this.permissions = permissions;
    }

    public static class Base {
        private String displayName;
        private String dbKey;
        private ScriptValue mainSql;
        private PrimaryKey primaryKey;
        private Integer layoutColumns;
        private Boolean initQuery;
        private Pagination pagination = new Pagination();
        private Boolean summaryEnabled;
        private String orderBySql;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDbKey() {
            return dbKey;
        }

        public void setDbKey(String dbKey) {
            this.dbKey = dbKey;
        }

        public ScriptValue getMainSql() {
            return mainSql;
        }

        public void setMainSql(ScriptValue mainSql) {
            this.mainSql = mainSql;
        }

        public PrimaryKey getPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(PrimaryKey primaryKey) {
            this.primaryKey = primaryKey;
        }

        public Integer getLayoutColumns() {
            return layoutColumns;
        }

        public void setLayoutColumns(Integer layoutColumns) {
            this.layoutColumns = layoutColumns;
        }

        public Boolean getInitQuery() {
            return initQuery;
        }

        public void setInitQuery(Boolean initQuery) {
            this.initQuery = initQuery;
        }

        public Pagination getPagination() {
            return pagination;
        }

        public void setPagination(Pagination pagination) {
            this.pagination = pagination;
        }

        public Boolean getSummaryEnabled() {
            return summaryEnabled;
        }

        public void setSummaryEnabled(Boolean summaryEnabled) {
            this.summaryEnabled = summaryEnabled;
        }

        public String getOrderBySql() {
            return orderBySql;
        }

        public void setOrderBySql(String orderBySql) {
            this.orderBySql = orderBySql;
        }
    }

    public static class ScriptValue {
        private Integer type;
        private String script;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }
    }

    public static class PrimaryKey {
        private ScriptValue value;
        private ScriptValue sql;

        public ScriptValue getValue() {
            return value;
        }

        public void setValue(ScriptValue value) {
            this.value = value;
        }

        public ScriptValue getSql() {
            return sql;
        }

        public void setSql(ScriptValue sql) {
            this.sql = sql;
        }
    }

    public static class Pagination {
        private Boolean enabled;
        private Integer pageLimit;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getPageLimit() {
            return pageLimit;
        }

        public void setPageLimit(Integer pageLimit) {
            this.pageLimit = pageLimit;
        }
    }

    public static class Columns {
        private List<ShowColumn> show = new ArrayList<ShowColumn>();
        private List<LineColumn> lines = new ArrayList<LineColumn>();
        private List<String> listOrder = new ArrayList<String>();

        public List<ShowColumn> getShow() {
            return show;
        }

        public void setShow(List<ShowColumn> show) {
            this.show = show;
        }

        public List<LineColumn> getLines() {
            return lines;
        }

        public void setLines(List<LineColumn> lines) {
            this.lines = lines;
        }

        public List<String> getListOrder() {
            return listOrder;
        }

        public void setListOrder(List<String> listOrder) {
            this.listOrder = listOrder;
        }
    }

    public static class ShowColumn {
        private String stableKey;
        private String displayName;
        private String style;
        private Boolean wholeLine;
        private String sortField;
        private ScriptValue content;
        private ScriptValue summaryContent;
        private PermissionSet permissions;

        public String getStableKey() {
            return stableKey;
        }

        public void setStableKey(String stableKey) {
            this.stableKey = stableKey;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public Boolean getWholeLine() {
            return wholeLine;
        }

        public void setWholeLine(Boolean wholeLine) {
            this.wholeLine = wholeLine;
        }

        public String getSortField() {
            return sortField;
        }

        public void setSortField(String sortField) {
            this.sortField = sortField;
        }

        public ScriptValue getContent() {
            return content;
        }

        public void setContent(ScriptValue content) {
            this.content = content;
        }

        public ScriptValue getSummaryContent() {
            return summaryContent;
        }

        public void setSummaryContent(ScriptValue summaryContent) {
            this.summaryContent = summaryContent;
        }

        public PermissionSet getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionSet permissions) {
            this.permissions = permissions;
        }
    }

    public static class LineColumn {
        private String stableKey;
        private String displayName;
        private String tip;
        private Boolean expanded;
        private PermissionSet permissions;

        public String getStableKey() {
            return stableKey;
        }

        public void setStableKey(String stableKey) {
            this.stableKey = stableKey;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getTip() {
            return tip;
        }

        public void setTip(String tip) {
            this.tip = tip;
        }

        public Boolean getExpanded() {
            return expanded;
        }

        public void setExpanded(Boolean expanded) {
            this.expanded = expanded;
        }

        public PermissionSet getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionSet permissions) {
            this.permissions = permissions;
        }
    }

    public static class Query {
        private String name;
        private String displayName;
        private String widget;
        private String widgetParam;
        private String defaultValue;
        private ScriptValue sql;
        private String description;
        private PermissionSet permissions;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getWidget() {
            return widget;
        }

        public void setWidget(String widget) {
            this.widget = widget;
        }

        public String getWidgetParam() {
            return widgetParam;
        }

        public void setWidgetParam(String widgetParam) {
            this.widgetParam = widgetParam;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public ScriptValue getSql() {
            return sql;
        }

        public void setSql(ScriptValue sql) {
            this.sql = sql;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public PermissionSet getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionSet permissions) {
            this.permissions = permissions;
        }
    }

    public static class Limit {
        private String stableKey;
        private String description;
        private ScriptValue sql;
        private PermissionSet permissions;

        public String getStableKey() {
            return stableKey;
        }

        public void setStableKey(String stableKey) {
            this.stableKey = stableKey;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ScriptValue getSql() {
            return sql;
        }

        public void setSql(ScriptValue sql) {
            this.sql = sql;
        }

        public PermissionSet getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionSet permissions) {
            this.permissions = permissions;
        }
    }

    public static class Variables {
        private List<PreparedVariable> prepared = new ArrayList<PreparedVariable>();

        public List<PreparedVariable> getPrepared() {
            return prepared;
        }

        public void setPrepared(List<PreparedVariable> prepared) {
            this.prepared = prepared;
        }
    }

    public static class PreparedVariable {
        private String var;
        private String description;
        private ScriptValue exec;
        private PermissionSet permissions;

        public String getVar() {
            return var;
        }

        public void setVar(String var) {
            this.var = var;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ScriptValue getExec() {
            return exec;
        }

        public void setExec(ScriptValue exec) {
            this.exec = exec;
        }

        public PermissionSet getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionSet permissions) {
            this.permissions = permissions;
        }
    }

    public static class Subviews {
        private List<ViewTab> viewTabs = new ArrayList<ViewTab>();

        public List<ViewTab> getViewTabs() {
            return viewTabs;
        }

        public void setViewTabs(List<ViewTab> viewTabs) {
            this.viewTabs = viewTabs;
        }
    }

    public static class ViewTab {
        private String stableKey;
        private String displayName;
        private String style;
        private String action;
        private String param;
        private PermissionSet permissions;

        public String getStableKey() {
            return stableKey;
        }

        public void setStableKey(String stableKey) {
            this.stableKey = stableKey;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }

        public PermissionSet getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionSet permissions) {
            this.permissions = permissions;
        }
    }

    public static class Buttons {
        private List<SystemButton> system = new ArrayList<SystemButton>();
        private List<CustomButton> item = new ArrayList<CustomButton>();
        private List<CustomButton> summary = new ArrayList<CustomButton>();

        public List<SystemButton> getSystem() {
            return system;
        }

        public void setSystem(List<SystemButton> system) {
            this.system = system;
        }

        public List<CustomButton> getItem() {
            return item;
        }

        public void setItem(List<CustomButton> item) {
            this.item = item;
        }

        public List<CustomButton> getSummary() {
            return summary;
        }

        public void setSummary(List<CustomButton> summary) {
            this.summary = summary;
        }
    }

    public static class SystemButton {
        private String name;
        private String displayName;
        private Integer type;
        private String icon;
        private String styleClass;
        private String description;
        private PermissionSet permissions;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getStyleClass() {
            return styleClass;
        }

        public void setStyleClass(String styleClass) {
            this.styleClass = styleClass;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public PermissionSet getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionSet permissions) {
            this.permissions = permissions;
        }
    }

    public static class CustomButton {
        private String stableKey;
        private String displayName;
        private String icon;
        private String styleClass;
        private String action;
        private String openType;
        private String param;
        private String confirmMessage;
        private String description;
        private PermissionSet permissions;

        public String getStableKey() {
            return stableKey;
        }

        public void setStableKey(String stableKey) {
            this.stableKey = stableKey;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getStyleClass() {
            return styleClass;
        }

        public void setStyleClass(String styleClass) {
            this.styleClass = styleClass;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getOpenType() {
            return openType;
        }

        public void setOpenType(String openType) {
            this.openType = openType;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }

        public String getConfirmMessage() {
            return confirmMessage;
        }

        public void setConfirmMessage(String confirmMessage) {
            this.confirmMessage = confirmMessage;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public PermissionSet getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionSet permissions) {
            this.permissions = permissions;
        }
    }

    public static class Weixin {
        private String listMode;
        private String urlMode;
        private String title;
        private String image;
        private String description;
        private String date;
        private PermissionSet permissions;

        public String getListMode() {
            return listMode;
        }

        public void setListMode(String listMode) {
            this.listMode = listMode;
        }

        public String getUrlMode() {
            return urlMode;
        }

        public void setUrlMode(String urlMode) {
            this.urlMode = urlMode;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public PermissionSet getPermissions() {
            return permissions;
        }

        public void setPermissions(PermissionSet permissions) {
            this.permissions = permissions;
        }
    }

    public static class Scripts {
        private ScriptValue list;

        public ScriptValue getList() {
            return list;
        }

        public void setList(ScriptValue list) {
            this.list = list;
        }
    }

    public static class PermissionSet {
        private List<String> view = new ArrayList<String>();

        public List<String> getView() {
            return view;
        }

        public void setView(List<String> view) {
            this.view = view;
        }
    }
}
