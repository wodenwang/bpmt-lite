package com.riversoft.api.modules.dynamic_table_views;

import com.riversoft.core.BeanFactory;
import com.riversoft.core.db.ORMService;
import com.riversoft.core.db.ORMService.QueryVO;
import com.riversoft.platform.po.CmPri;
import com.riversoft.platform.po.CmPriGroupRelate;
import com.riversoft.platform.po.TbColumn;
import com.riversoft.platform.po.TbTable;
import com.riversoft.platform.po.VwUrl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.Serializable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class OrmDynamicTableViewRepository implements DynamicTableViewRepository {
    private static final String DYN_VIEW_CLASS = "dyn";
    private static final Set<String> CHILD_KEYS = childKeys();

    @SuppressWarnings("unchecked")
    public List<VwUrl> listDynUrls(int start, int limit) {
        return (List<VwUrl>) ORMService.getInstance().queryHQLPage(
                "from " + VwUrl.class.getName() + " where viewClass = ? order by viewKey asc", start, limit,
                DYN_VIEW_CLASS);
    }

    public int countDynUrls() {
        Long count = (Long) ORMService.getInstance().findHQL(
                "select count(1) from " + VwUrl.class.getName() + " where viewClass = ?", DYN_VIEW_CLASS);
        return count == null ? 0 : count.intValue();
    }

    public VwUrl findUrl(String viewKey) {
        return (VwUrl) ORMService.getInstance().findByPk(VwUrl.class.getName(), viewKey);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> findTable(String viewKey) {
        return (Map<String, Object>) ORMService.getInstance().findByPk("VwDynTable", viewKey);
    }

    public Map<String, Object> findTableDefinition(String tableName) {
        TbTable table = (TbTable) ORMService.getInstance().findByPk(TbTable.class.getName(), tableName);
        if (table == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", table.getName());
        result.put("description", table.getDescription());
        result.put("cacheFlag", table.getCacheFlag());
        result.put("columns", toColumnDefinitions(table.getTbColumns()));
        addPrimaryKeyDefinition(result, table.getTbColumns());
        return result;
    }

    public Map<String, Object> findColumnDefinition(String tableName, String columnName) {
        TbTable table = (TbTable) ORMService.getInstance().findByPk(TbTable.class.getName(), tableName);
        if (table == null || table.getTbColumns() == null) {
            return null;
        }
        for (TbColumn column : table.getTbColumns()) {
            if (columnName != null && columnName.equals(column.getName())) {
                return toColumnDefinition(column);
            }
        }
        return null;
    }

    public VwUrl saveUrl(VwUrl url) {
        ORMService.getInstance().savePO(url);
        return url;
    }

    public void updateUrl(VwUrl url) {
        ORMService.getInstance().updatePO(url);
    }

    public void createViewConfig(final VwUrl url,
                                 final Map<String, Object> tableMap,
                                 final DynamicTableViewResponse.WritePlan plan) {
        inTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                saveUrl(url);
                saveViewConfig(url.getViewKey(), tableMap);
                removePermissions(plan);
            }
        });
    }

    public void saveViewConfig(String viewKey, Map<String, Object> tableMap) {
        saveDynamicEntity("VwDynTable", tableValues(tableMap));
        saveChildConfig(tableMap);
    }

    public void replaceViewConfig(final String viewKey, final Map<String, Object> tableMap) {
        throw new UnsupportedOperationException("replaceViewConfig requires VwUrl and WritePlan.");
    }

    public void replaceViewConfig(final VwUrl url,
                                  final Map<String, Object> tableMap,
                                  final DynamicTableViewResponse.WritePlan plan) {
        inTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                updateUrl(url);
                updateDynamicEntity("VwDynTable", tableValues(tableMap));
                removeAllChildConfig(url.getViewKey());
                removePermissions(plan);
                saveChildConfig(tableMap);
            }
        });
    }

    public void patchViewConfig(final VwUrl url,
                                final DynamicTableViewSection section,
                                final Map<String, Object> tableMap,
                                final DynamicTableViewResponse.WritePlan plan) {
        inTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                updateUrl(url);
                patchDynamicTableConfig(url.getViewKey(), section, tableMap);
                removePermissions(plan);
            }
        });
    }

    public void removeViewConfig(final String viewKey, final DynamicTableViewResponse.WritePlan plan) {
        inTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                removeDynamicTableConfig(viewKey);
                ORMService.getInstance().removeByPk(VwUrl.class.getName(), viewKey);
                removeViewPermissions(viewKey, plan);
            }
        });
    }

    protected void inTransaction(TransactionCallbackWithoutResult callback) {
        TransactionTemplate template = new TransactionTemplate(transactionManager());
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.execute(callback);
    }

    protected PlatformTransactionManager transactionManager() {
        return (PlatformTransactionManager) BeanFactory.getInstance().getBean("transactionManager");
    }

    public void saveDynamicEntity(String entityName, Map<String, Object> values) {
        values.put("$type$", entityName);
        ORMService.getInstance().save(values);
    }

    public void updateDynamicEntity(String entityName, Map<String, Object> values) {
        values.put("$type$", entityName);
        ORMService.getInstance().merge(values);
    }

    public void removeDynamicEntity(String entityName, Object id) {
        if (id instanceof Serializable) {
            ORMService.getInstance().removeByPk(entityName, (Serializable) id);
        }
    }

    public void removeDynamicTableConfig(String viewKey) {
        removeAllChildConfig(viewKey);
        removeDynamicEntity("VwDynTable", viewKey);
    }

    public void removeViewConfig(String viewKey) {
        removeDynamicTableConfig(viewKey);
        ORMService.getInstance().removeByPk(VwUrl.class.getName(), viewKey);
    }

    public void flushAndClearViewCache(String viewKey) {
        ORMService service = ormService();
        service.flush();
        service.clear();
        evictViewCacheRegions(service);
    }

    protected ORMService ormService() {
        return ORMService.getInstance();
    }

    protected void evictViewCacheRegions(ORMService service) {
        service.evictEntityRegions(
                VwUrl.class.getName(),
                "VwDynTable",
                "VwDynColumn",
                "VwDynColumnForm",
                "VwDynColumnShow",
                "VwDynColumnLine",
                "VwDynQuery",
                "VwDynQueryExt",
                "VwDynLimit",
                "VwDynExecBefore",
                "VwDynExecAfter",
                "VwDynExecPrepare",
                "VwDynParent",
                "VwDynParentForeign",
                "VwDynSubSys",
                "VwDynSubView",
                "VwDynBtnSys",
                "VwDynBtnItem",
                "VwDynBtnSummary",
                "VwDynWeixin");
        service.evictQueryRegions();
    }

    private List<Map<String, Object>> toColumnDefinitions(Set<TbColumn> columns) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        if (columns == null) {
            return result;
        }
        for (TbColumn column : columns) {
            result.add(toColumnDefinition(column));
        }
        return result;
    }

    private Map<String, Object> toColumnDefinition(TbColumn column) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", column.getName());
        result.put("description", column.getDescription());
        result.put("tableName", column.getTableName());
        int typeCode = column.getMappedTypeCode();
        String typeName = sqlTypeName(typeCode);
        result.put("typeCode", Integer.valueOf(typeCode));
        result.put("typeName", typeName);
        result.put("columnType", typeName);
        result.put("totalSize", Integer.valueOf(column.getTotalSize()));
        result.put("scale", Integer.valueOf(column.getScale()));
        result.put("primaryKey", Boolean.valueOf(column.isPrimaryKey()));
        result.put("autoIncrement", Boolean.valueOf(column.isAutoIncrement()));
        result.put("required", Boolean.valueOf(column.isRequired()));
        result.put("defaultValue", column.getDefaultValue());
        result.put("sort", column.getSort());
        result.put("memo", column.getMemo());
        return result;
    }

    private void addPrimaryKeyDefinition(Map<String, Object> result, Set<TbColumn> columns) {
        if (columns == null) {
            return;
        }
        for (TbColumn column : columns) {
            if (column != null && column.isPrimaryKey()) {
                int typeCode = column.getMappedTypeCode();
                result.put("primaryKeyName", column.getName());
                result.put("primaryKeyType", sqlTypeName(typeCode));
                result.put("primaryKeyTypeCode", Integer.valueOf(typeCode));
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void saveMappedChild(Object value) {
        if (!(value instanceof Map)) {
            return;
        }
        Map<String, Object> child = (Map<String, Object>) value;
        Object type = child.get("$type$");
        if (type == null) {
            return;
        }
        Object foreigns = child.get("foreigns");
        Map<String, Object> saveValues = new LinkedHashMap<String, Object>(child);
        saveValues.remove("foreigns");
        attachExistingPermissions(saveValues);
        saveDynamicEntity(String.valueOf(type), saveValues);
        if (foreigns instanceof Collection) {
            for (Object foreign : (Collection<Object>) foreigns) {
                saveMappedChild(foreign);
            }
        }
    }

    protected void attachExistingPermissions(Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (!(value instanceof CmPri)) {
                continue;
            }
            CmPri pri = (CmPri) value;
            if (pri.getPriKey() == null || pri.getPriKey().trim().length() == 0) {
                continue;
            }
            CmPri existing = findPermission(pri.getPriKey());
            if (existing != null) {
                entry.setValue(existing);
            }
        }
    }

    protected CmPri findPermission(String priKey) {
        return (CmPri) ORMService.getInstance().findByPk(CmPri.class.getName(), priKey);
    }

    protected void patchDynamicTableConfig(String viewKey,
                                           DynamicTableViewSection section,
                                           Map<String, Object> tableMap) {
        if (DynamicTableViewSection.BASE.equals(section) || DynamicTableViewSection.SCRIPTS.equals(section)) {
            updateDynamicEntity("VwDynTable", tableValues(tableMap));
            return;
        }
        removeSectionConfig(viewKey, section);
        for (String childKey : sectionChildKeys(section)) {
            saveMappedValue(tableMap.get(childKey));
        }
    }

    @SuppressWarnings("unchecked")
    protected void saveMappedValue(Object value) {
        if (value instanceof Collection) {
            for (Object item : (Collection<Object>) value) {
                saveMappedChild(item);
            }
        } else {
            saveMappedChild(value);
        }
    }

    protected Map<String, Object> tableValues(Map<String, Object> tableMap) {
        Map<String, Object> table = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, Object> entry : tableMap.entrySet()) {
            if (!CHILD_KEYS.contains(entry.getKey())) {
                table.put(entry.getKey(), entry.getValue());
            }
        }
        return table;
    }

    protected void saveChildConfig(Map<String, Object> tableMap) {
        for (String childKey : CHILD_KEYS) {
            saveMappedValue(tableMap.get(childKey));
        }
    }

    protected void removeAllChildConfig(String viewKey) {
        removeParentVariables(viewKey);
        removeEntitiesByViewKey(viewKey,
                "VwDynColumn",
                "VwDynColumnShow",
                "VwDynColumnForm",
                "VwDynColumnLine",
                "VwDynQuery",
                "VwDynQueryExt",
                "VwDynLimit",
                "VwDynExecPrepare",
                "VwDynParent",
                "VwDynExecBefore",
                "VwDynExecAfter",
                "VwDynSubSys",
                "VwDynSubView",
                "VwDynBtnSys",
                "VwDynBtnItem",
                "VwDynBtnSummary",
                "VwDynWeixin");
    }

    protected void removeSectionConfig(String viewKey, DynamicTableViewSection section) {
        if (DynamicTableViewSection.FIELDS.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwDynColumn", "VwDynColumnShow", "VwDynColumnForm", "VwDynColumnLine");
        } else if (DynamicTableViewSection.QUERIES.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwDynQuery", "VwDynQueryExt");
        } else if (DynamicTableViewSection.LIMITS.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwDynLimit");
        } else if (DynamicTableViewSection.PROCESSORS.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwDynExecBefore", "VwDynExecAfter");
        } else if (DynamicTableViewSection.VARIABLES.equals(section)) {
            removeParentVariables(viewKey);
            removeEntitiesByViewKey(viewKey, "VwDynExecPrepare", "VwDynParent");
        } else if (DynamicTableViewSection.SUBVIEWS.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwDynSubSys", "VwDynSubView");
        } else if (DynamicTableViewSection.BUTTONS.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwDynBtnSys", "VwDynBtnItem", "VwDynBtnSummary");
        } else if (DynamicTableViewSection.WEIXIN.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwDynWeixin");
        }
    }

    protected void removeParentVariables(String viewKey) {
        @SuppressWarnings("unchecked")
        List<String> parentKeys = (List<String>) ORMService.getInstance()
                .queryHQL("select parentKey from VwDynParent where viewKey = ?", viewKey);
        if (parentKeys != null && !parentKeys.isEmpty()) {
            ORMService.getInstance().executeHQL("delete from VwDynParentForeign where parentKey in (:list)",
                    new QueryVO("list", parentKeys));
        }
    }

    protected void removeEntitiesByViewKey(String viewKey, String... entityNames) {
        for (String entityName : entityNames) {
            ORMService.getInstance().executeHQL("delete from " + entityName + " where viewKey = ?", viewKey);
        }
    }

    private List<String> sectionChildKeys(DynamicTableViewSection section) {
        if (DynamicTableViewSection.FIELDS.equals(section)) {
            return keys("columns", "showColumns", "formColumns", "lineColumns");
        }
        if (DynamicTableViewSection.QUERIES.equals(section)) {
            return keys("querys", "extQuerys");
        }
        if (DynamicTableViewSection.LIMITS.equals(section)) {
            return keys("limits");
        }
        if (DynamicTableViewSection.PROCESSORS.equals(section)) {
            return keys("beforeExecs", "afterExecs");
        }
        if (DynamicTableViewSection.VARIABLES.equals(section)) {
            return keys("prepareExecs", "parents");
        }
        if (DynamicTableViewSection.SUBVIEWS.equals(section)) {
            return keys("sysSubs", "viewSubs");
        }
        if (DynamicTableViewSection.BUTTONS.equals(section)) {
            return keys("sysBtns", "itemBtns", "summaryBtns");
        }
        if (DynamicTableViewSection.WEIXIN.equals(section)) {
            return keys("weixin");
        }
        return Collections.emptyList();
    }

    private List<String> keys(String... keys) {
        List<String> values = new ArrayList<String>();
        Collections.addAll(values, keys);
        return values;
    }

    private void removePermissions(DynamicTableViewResponse.WritePlan plan) {
        if (plan == null || plan.getPermissionDeletes() == null || plan.getPermissionDeletes().isEmpty()) {
            return;
        }
        removePermissionKeys(plan.getPermissionDeletes(), plan);
    }

    private void removeViewPermissions(String viewKey, DynamicTableViewResponse.WritePlan plan) {
        Set<String> priKeys = new LinkedHashSet<String>();
        if (plan != null && plan.getPermissionDeletes() != null) {
            priKeys.addAll(plan.getPermissionDeletes());
        }
        @SuppressWarnings("unchecked")
        List<String> viewPriKeys = (List<String>) ORMService.getInstance().queryHQL("select priKey from "
                + CmPri.class.getName() + " where catelogType = ? and catelogKey = ?",
                CmPri.Catelog.VIEW.getCode(), viewKey);
        if (viewPriKeys != null) {
            priKeys.addAll(viewPriKeys);
        }
        removePermissionKeys(priKeys, plan);
    }

    private void removePermissionKeys(Collection<String> priKeys, DynamicTableViewResponse.WritePlan plan) {
        if (priKeys == null || priKeys.isEmpty()) {
            return;
        }
        Set<String> activePermissions = new LinkedHashSet<String>();
        if (plan != null) {
            activePermissions.addAll(plan.getPermissionCreates());
            activePermissions.addAll(plan.getPermissionKeeps());
        }
        for (String priKey : priKeys) {
            if (priKey != null && priKey.trim().length() > 0 && !activePermissions.contains(priKey)) {
                ORMService.getInstance().executeHQL("delete from " + CmPriGroupRelate.class.getName()
                        + " where priKey = ?", priKey);
                ORMService.getInstance().removeByPk(CmPri.class.getName(), priKey);
            }
        }
    }

    private static Set<String> childKeys() {
        Set<String> keys = new HashSet<String>();
        keys.add("columns");
        keys.add("showColumns");
        keys.add("formColumns");
        keys.add("lineColumns");
        keys.add("querys");
        keys.add("extQuerys");
        keys.add("limits");
        keys.add("prepareExecs");
        keys.add("parents");
        keys.add("beforeExecs");
        keys.add("afterExecs");
        keys.add("sysSubs");
        keys.add("viewSubs");
        keys.add("sysBtns");
        keys.add("itemBtns");
        keys.add("summaryBtns");
        keys.add("weixin");
        return keys;
    }

    private String sqlTypeName(int typeCode) {
        switch (typeCode) {
            case Types.BIT:
                return "BIT";
            case Types.TINYINT:
                return "TINYINT";
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.INTEGER:
                return "INTEGER";
            case Types.BIGINT:
                return "BIGINT";
            case Types.FLOAT:
                return "FLOAT";
            case Types.REAL:
                return "REAL";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.NUMERIC:
                return "NUMERIC";
            case Types.DECIMAL:
                return "DECIMAL";
            case Types.CHAR:
                return "CHAR";
            case Types.VARCHAR:
                return "VARCHAR";
            case Types.LONGVARCHAR:
                return "LONGVARCHAR";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.BINARY:
                return "BINARY";
            case Types.VARBINARY:
                return "VARBINARY";
            case Types.LONGVARBINARY:
                return "LONGVARBINARY";
            case Types.BLOB:
                return "BLOB";
            case Types.CLOB:
                return "CLOB";
            case Types.BOOLEAN:
                return "BOOLEAN";
            case -15:
                return "NCHAR";
            case -9:
                return "NVARCHAR";
            case -16:
                return "LONGNVARCHAR";
            case 2011:
                return "NCLOB";
            default:
                return String.valueOf(typeCode);
        }
    }
}
