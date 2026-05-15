package com.riversoft.api.modules.report_views;

import com.riversoft.core.BeanFactory;
import com.riversoft.core.db.ORMService;
import com.riversoft.platform.po.CmPri;
import com.riversoft.platform.po.CmPriGroupRelate;
import com.riversoft.platform.po.VwUrl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class OrmReportViewRepository implements ReportViewRepository {
    private static final String REPORT_VIEW_CLASS = "rep_list";
    private static final Set<String> CHILD_KEYS = childKeys();

    @SuppressWarnings("unchecked")
    public List<VwUrl> listReportUrls(int start, int limit) {
        return (List<VwUrl>) ORMService.getInstance().queryHQLPage(
                "from " + VwUrl.class.getName() + " where viewClass = ? order by viewKey asc",
                start, limit, REPORT_VIEW_CLASS);
    }

    public int countReportUrls() {
        Long count = (Long) ORMService.getInstance().findHQL(
                "select count(1) from " + VwUrl.class.getName() + " where viewClass = ?", REPORT_VIEW_CLASS);
        return count == null ? 0 : count.intValue();
    }

    public VwUrl findUrl(String viewKey) {
        return (VwUrl) ORMService.getInstance().findByPk(VwUrl.class.getName(), viewKey);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> findReport(String viewKey) {
        return (Map<String, Object>) ORMService.getInstance().findByPk("VwReport", viewKey);
    }

    public VwUrl saveUrl(VwUrl url) {
        ORMService.getInstance().savePO(url);
        return url;
    }

    public void updateUrl(VwUrl url) {
        ORMService.getInstance().updatePO(url);
    }

    public void createViewConfig(final VwUrl url,
                                 final Map<String, Object> reportMap,
                                 final ReportViewResponse.WritePlan plan) {
        inTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                saveUrl(url);
                saveViewConfig(url.getViewKey(), reportMap);
                removePermissions(plan);
            }
        });
    }

    public void saveViewConfig(String viewKey, Map<String, Object> reportMap) {
        saveDynamicEntity("VwReport", reportValues(reportMap));
        saveChildConfig(reportMap);
    }

    public void replaceViewConfig(final VwUrl url,
                                  final Map<String, Object> reportMap,
                                  final ReportViewResponse.WritePlan plan) {
        inTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                updateUrl(url);
                updateDynamicEntity("VwReport", reportValues(reportMap));
                removeAllChildConfig(url.getViewKey());
                saveChildConfig(reportMap);
                removePermissions(plan);
            }
        });
    }

    public void patchViewConfig(final VwUrl url,
                                final ReportViewSection section,
                                final Map<String, Object> reportMap,
                                final ReportViewResponse.WritePlan plan) {
        inTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                updateUrl(url);
                patchReportConfig(url.getViewKey(), section, reportMap);
                removePermissions(plan);
            }
        });
    }

    public void removeViewConfig(final String viewKey, final ReportViewResponse.WritePlan plan) {
        inTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                removeReportConfig(viewKey);
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

    public void removeReportConfig(String viewKey) {
        removeAllChildConfig(viewKey);
        removeDynamicEntity("VwReport", viewKey);
    }

    public void flushAndClearViewCache(String viewKey) {
        ORMService.getInstance().flush();
        ORMService.getInstance().clear();
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
        Map<String, Object> saveValues = new LinkedHashMap<String, Object>(child);
        saveValues.remove("foreigns");
        attachExistingPermissions(saveValues);
        saveDynamicEntity(String.valueOf(type), saveValues);
        Object foreigns = child.get("foreigns");
        if (foreigns instanceof Collection) {
            for (Object foreign : (Collection<Object>) foreigns) {
                saveMappedChild(foreign);
            }
        }
    }

    protected void saveMappedValue(Object value) {
        if (value instanceof Collection) {
            for (Object item : (Collection<?>) value) {
                saveMappedChild(item);
            }
        } else {
            saveMappedChild(value);
        }
    }

    protected void attachExistingPermissions(Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof CmPri) {
                CmPri pri = (CmPri) value;
                if (pri.getPriKey() == null || pri.getPriKey().trim().length() == 0) {
                    continue;
                }
                CmPri existing = findPermission(pri.getPriKey());
                if (existing != null) {
                    entry.setValue(existing);
                }
            } else if (value instanceof String) {
                String priKey = ((String) value).trim();
                if (priKey.length() == 0) {
                    continue;
                }
                CmPri existing = findPermission(priKey);
                if (existing != null) {
                    entry.setValue(existing);
                }
            }
        }
    }

    protected CmPri findPermission(String priKey) {
        return (CmPri) ORMService.getInstance().findByPk(CmPri.class.getName(), priKey);
    }

    protected Map<String, Object> reportValues(Map<String, Object> reportMap) {
        Map<String, Object> report = new LinkedHashMap<String, Object>();
        if (reportMap == null) {
            return report;
        }
        for (Map.Entry<String, Object> entry : reportMap.entrySet()) {
            if (!CHILD_KEYS.contains(entry.getKey())) {
                report.put(entry.getKey(), entry.getValue());
            }
        }
        return report;
    }

    protected void saveChildConfig(Map<String, Object> reportMap) {
        for (String childKey : CHILD_KEYS) {
            saveMappedValue(reportMap == null ? null : reportMap.get(childKey));
        }
    }

    protected void patchReportConfig(String viewKey, ReportViewSection section, Map<String, Object> reportMap) {
        if (ReportViewSection.BASE.equals(section) || ReportViewSection.SCRIPTS.equals(section)) {
            updateDynamicEntity("VwReport", reportValues(reportMap));
            return;
        }
        removeSectionConfig(viewKey, section);
        saveSectionConfig(section, reportMap);
    }

    protected void saveSectionConfig(ReportViewSection section, Map<String, Object> reportMap) {
        for (String childKey : sectionChildKeys(section)) {
            saveMappedValue(reportMap == null ? null : reportMap.get(childKey));
        }
    }

    protected void removeAllChildConfig(String viewKey) {
        removeEntitiesByViewKey(viewKey,
                "VwReportColumnShow",
                "VwReportColumnLine",
                "VwReportQuery",
                "VwReportLimit",
                "VwReportExecPrepare",
                "VwReportSubView",
                "VwReportBtnSys",
                "VwReportBtnItem",
                "VwReportBtnSummary",
                "VwReportWeixin");
    }

    protected void removeSectionConfig(String viewKey, ReportViewSection section) {
        if (ReportViewSection.COLUMNS.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwReportColumnShow", "VwReportColumnLine");
        } else if (ReportViewSection.QUERIES.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwReportQuery");
        } else if (ReportViewSection.LIMITS.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwReportLimit");
        } else if (ReportViewSection.VARIABLES.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwReportExecPrepare");
        } else if (ReportViewSection.SUBVIEWS.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwReportSubView");
        } else if (ReportViewSection.BUTTONS.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwReportBtnSys", "VwReportBtnItem", "VwReportBtnSummary");
        } else if (ReportViewSection.WEIXIN.equals(section)) {
            removeEntitiesByViewKey(viewKey, "VwReportWeixin");
        }
    }

    protected void removeEntitiesByViewKey(String viewKey, String... entityNames) {
        for (String entityName : entityNames) {
            ORMService.getInstance().executeHQL("delete from " + entityName + " where viewKey = ?", viewKey);
        }
    }

    private Set<String> sectionChildKeys(ReportViewSection section) {
        Set<String> keys = new LinkedHashSet<String>();
        if (ReportViewSection.COLUMNS.equals(section)) {
            keys.add("showColumns");
            keys.add("lineColumns");
        } else if (ReportViewSection.QUERIES.equals(section)) {
            keys.add("querys");
        } else if (ReportViewSection.LIMITS.equals(section)) {
            keys.add("limits");
        } else if (ReportViewSection.VARIABLES.equals(section)) {
            keys.add("prepareExecs");
        } else if (ReportViewSection.SUBVIEWS.equals(section)) {
            keys.add("viewSubs");
        } else if (ReportViewSection.BUTTONS.equals(section)) {
            keys.add("sysBtns");
            keys.add("itemBtns");
            keys.add("summaryBtns");
        } else if (ReportViewSection.WEIXIN.equals(section)) {
            keys.add("weixin");
        }
        return keys;
    }

    private void removePermissions(ReportViewResponse.WritePlan plan) {
        if (plan == null || plan.getPermissionDeletes() == null || plan.getPermissionDeletes().isEmpty()) {
            return;
        }
        removePermissionKeys(plan.getPermissionDeletes());
    }

    private void removeViewPermissions(String viewKey, ReportViewResponse.WritePlan plan) {
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
        removePermissionKeys(priKeys);
    }

    private void removePermissionKeys(Collection<String> priKeys) {
        if (priKeys == null || priKeys.isEmpty()) {
            return;
        }
        for (String priKey : priKeys) {
            if (priKey != null && priKey.trim().length() > 0) {
                removePermissionKey(priKey);
            }
        }
    }

    protected void removePermissionKey(String priKey) {
        ORMService.getInstance().executeHQL("delete from " + CmPriGroupRelate.class.getName()
                + " where priKey = ?", priKey);
        ORMService.getInstance().removeByPk(CmPri.class.getName(), priKey);
    }

    private static Set<String> childKeys() {
        Set<String> keys = new HashSet<String>();
        keys.add("showColumns");
        keys.add("lineColumns");
        keys.add("querys");
        keys.add("limits");
        keys.add("prepareExecs");
        keys.add("viewSubs");
        keys.add("sysBtns");
        keys.add("itemBtns");
        keys.add("summaryBtns");
        keys.add("weixin");
        return Collections.unmodifiableSet(keys);
    }
}
