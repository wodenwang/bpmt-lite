package com.riversoft.api.modules.dynamic_table_views;

import com.riversoft.api.http.ApiException;
import com.riversoft.core.db.ORMService;
import com.riversoft.platform.po.CmPri;
import com.riversoft.platform.po.VwUrl;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DynamicTableViewServiceTest {
    @Test
    public void dryRunCreateDoesNotWriteRepository() {
        RecordingRepository repository = new RecordingRepository();
        DynamicTableViewService service = new DynamicTableViewService(repository);

        Map<String, Object> result = service.create(snapshot("CRM_CUSTOMER_VIEW"), true);

        assertTrue(repository.savedUrls.isEmpty());
        assertTrue(repository.savedViewConfigs.isEmpty());
        assertEquals(0, repository.createAttempts);
        assertEquals(Integer.valueOf(0), Integer.valueOf(repository.flushes));
        DynamicTableViewResponse.WritePlan plan = (DynamicTableViewResponse.WritePlan) result.get("plan");
        assertTrue(plan.isDryRun());
        assertTrue(plan.getCreates().contains("VW_URL"));
        assertTrue(plan.getCreates().contains("VW_DYN_TABLE"));
    }

    @Test
    public void createWritesUrlAndTableWhenNotDryRun() {
        RecordingRepository repository = new RecordingRepository();
        DynamicTableViewService service = new DynamicTableViewService(repository);

        service.create(snapshot("CRM_CUSTOMER_VIEW"), false);

        assertEquals(1, repository.createAttempts);
        assertEquals("CRM_CUSTOMER_VIEW", repository.createdUrl.getViewKey());
        assertTrue(repository.createdViewConfigs.containsKey("CRM_CUSTOMER_VIEW"));
        assertTrue(repository.savedUrls.isEmpty());
        assertTrue(repository.savedViewConfigs.isEmpty());
        assertEquals(1, repository.flushes);
    }

    @Test
    public void exportReturnsSnapshot() {
        RecordingRepository repository = new RecordingRepository();
        DynamicTableViewSnapshot source = snapshot("CRM_CUSTOMER_VIEW");
        repository.seedDynView(source);
        DynamicTableViewService service = new DynamicTableViewService(repository);

        Map<String, Object> result = service.export("CRM_CUSTOMER_VIEW");

        DynamicTableViewSnapshot exported = (DynamicTableViewSnapshot) result.get("snapshot");
        assertEquals("CRM_CUSTOMER_VIEW", exported.getViewKey());
        assertEquals("CRM_CUSTOMER", exported.getBase().getTableName());
        assertEquals("ID", exported.getFields().getSystemFields().get(0).getName());
    }

    @Test
    public void validateInvalidReturnsValidFalse() {
        RecordingRepository repository = new RecordingRepository();
        DynamicTableViewService service = new DynamicTableViewService(repository);
        DynamicTableViewSnapshot invalid = snapshot("CRM_CUSTOMER_VIEW");
        invalid.getBase().setTableName(null);

        Map<String, Object> result = service.validate(invalid);

        assertEquals(Boolean.FALSE, result.get("valid"));
        assertFalse(((List<?>) result.get("errors")).isEmpty());
    }

    @Test
    public void patchFieldsDryRunDoesNotWriteAndReplacesFieldsInReturnedSnapshot() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedDynView(snapshot("CRM_CUSTOMER_VIEW"));
        DynamicTableViewService service = new DynamicTableViewService(repository);
        DynamicTableViewSnapshot.Fields fields = new DynamicTableViewSnapshot.Fields();
        DynamicTableViewSnapshot.Field name = field("NAME", "客户名称");
        fields.setSystemFields(Collections.singletonList(name));
        fields.setListOrder(Collections.singletonList("NAME"));

        Map<String, Object> result = service.patch("CRM_CUSTOMER_VIEW", DynamicTableViewSection.FIELDS, fields, true);

        DynamicTableViewSnapshot patched = (DynamicTableViewSnapshot) result.get("snapshot");
        assertEquals("NAME", patched.getFields().getSystemFields().get(0).getName());
        assertTrue(repository.savedViewConfigs.isEmpty());
        assertTrue(repository.removedTableConfigKeys.isEmpty());
        DynamicTableViewResponse.WritePlan plan = (DynamicTableViewResponse.WritePlan) result.get("plan");
        assertEquals(Collections.singletonList("fields"), plan.getUpdatedSections());
        assertEquals(0, repository.flushes);
    }

    @Test
    public void patchFieldsRealPathCallsPatchViewConfigOnly() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedDynView(snapshot("CRM_CUSTOMER_VIEW"));
        DynamicTableViewService service = new DynamicTableViewService(repository);
        DynamicTableViewSnapshot.Fields fields = new DynamicTableViewSnapshot.Fields();
        DynamicTableViewSnapshot.Field name = field("NAME", "客户名称");
        fields.setSystemFields(Collections.singletonList(name));
        fields.setListOrder(Collections.singletonList("NAME"));

        Map<String, Object> result = service.patch("CRM_CUSTOMER_VIEW", DynamicTableViewSection.FIELDS, fields, false);

        DynamicTableViewResponse.WritePlan plan = (DynamicTableViewResponse.WritePlan) result.get("plan");
        assertEquals(Collections.singletonList("fields"), plan.getUpdatedSections());
        assertEquals(1, repository.patchAttempts);
        assertEquals(DynamicTableViewSection.FIELDS, repository.patchedSections.get(0));
        assertTrue(repository.patchedViewConfigs.containsKey("CRM_CUSTOMER_VIEW"));
        assertEquals(0, repository.replaceAttempts);
        assertEquals(0, repository.updateUrlAttempts);
        assertEquals(1, repository.flushes);
    }

    @Test
    public void deleteRequiresConfirmViewKey() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedDynView(snapshot("CRM_CUSTOMER_VIEW"));
        DynamicTableViewService service = new DynamicTableViewService(repository);

        try {
            service.delete("CRM_CUSTOMER_VIEW", "OTHER_VIEW");
            fail("Expected confirm required error");
        } catch (ApiException e) {
            assertEquals("DYNAMIC_TABLE_VIEW_CONFIRM_REQUIRED", e.getCode());
        }
        assertTrue(repository.removedViewKeys.isEmpty());
    }

    @Test
    public void deleteConfirmedRemovesConfigNotBusinessTableFlags() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedDynView(snapshotWithPermission("CRM_CUSTOMER_VIEW"));
        DynamicTableViewService service = new DynamicTableViewService(repository);

        Map<String, Object> result = service.delete("CRM_CUSTOMER_VIEW", "CRM_CUSTOMER_VIEW");

        assertEquals(Collections.singletonList("CRM_CUSTOMER_VIEW"), repository.removedViewKeys);
        assertEquals(1, repository.removePlans.size());
        assertTrue(repository.removePlans.get(0).getPermissionDeletes()
                .contains("dyn.CRM_CUSTOMER_VIEW.field.ID.view"));
        assertEquals(Boolean.TRUE, result.get("deleted"));
        assertEquals(Boolean.FALSE, result.get("businessTableDeleted"));
        assertEquals(Boolean.FALSE, result.get("businessDataDeleted"));
        assertEquals(1, repository.flushes);
    }

    @Test
    public void replaceRejectsNonDynView() {
        RecordingRepository repository = new RecordingRepository();
        repository.urls.put("REPORT_VIEW", url("REPORT_VIEW", "report"));
        DynamicTableViewService service = new DynamicTableViewService(repository);

        try {
            service.replace("REPORT_VIEW", snapshot("REPORT_VIEW"), false);
            fail("Expected non dyn error");
        } catch (ApiException e) {
            assertEquals("DYNAMIC_TABLE_VIEW_NOT_DYN", e.getCode());
            assertEquals(409, e.getStatus());
        }
    }

    @Test
    public void replaceRealPathCallsRepositoryReplaceViewConfigOnce() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedDynView(snapshot("CRM_CUSTOMER_VIEW"));
        DynamicTableViewService service = new DynamicTableViewService(repository);

        service.replace("CRM_CUSTOMER_VIEW", snapshot("CRM_CUSTOMER_VIEW"), false);

        assertEquals(1, repository.replaceAttempts);
        assertEquals("CRM_CUSTOMER_VIEW", repository.replacedUrl.getViewKey());
        assertTrue(repository.replacedViewConfigs.containsKey("CRM_CUSTOMER_VIEW"));
        assertTrue(repository.removedTableConfigKeys.isEmpty());
        assertTrue(repository.savedViewConfigs.isEmpty());
        assertEquals(0, repository.updateUrlAttempts);
        assertEquals(1, repository.flushes);
    }

    @Test
    public void replaceFailureDoesNotRemoveBeforeSaveAtServiceLayer() {
        RecordingRepository repository = new RecordingRepository();
        repository.seedDynView(snapshot("CRM_CUSTOMER_VIEW"));
        repository.failReplace = true;
        DynamicTableViewService service = new DynamicTableViewService(repository);

        try {
            service.replace("CRM_CUSTOMER_VIEW", snapshot("CRM_CUSTOMER_VIEW"), false);
            fail("Expected replace failure");
        } catch (IllegalStateException e) {
            assertEquals("replace failed", e.getMessage());
        }

        assertEquals(1, repository.replaceAttempts);
        assertTrue(repository.removedTableConfigKeys.isEmpty());
        assertTrue(repository.savedViewConfigs.isEmpty());
        assertEquals(0, repository.flushes);
    }

    @Test
    public void ormReplaceViewConfigRunsTableUpdateChildBulkDeleteAndSaveInRequiredTransaction() {
        RecordingTransactionManager transactionManager = new RecordingTransactionManager();
        TransactionalOrmRepository repository = new TransactionalOrmRepository(transactionManager);

        repository.replaceViewConfig(url("CRM_CUSTOMER_VIEW", "dyn"), new LinkedHashMap<String, Object>(),
                new DynamicTableViewResponse.WritePlan());

        assertEquals(1, transactionManager.begins);
        assertEquals(1, transactionManager.commits);
        assertEquals(0, transactionManager.rollbacks);
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRED,
                transactionManager.lastDefinition.getPropagationBehavior());
        assertEquals(4, repository.operations.size());
        assertEquals("updateUrl:CRM_CUSTOMER_VIEW", repository.operations.get(0));
        assertEquals("updateEntity:VwDynTable", repository.operations.get(1));
        assertEquals("bulkDeleteChildren:CRM_CUSTOMER_VIEW", repository.operations.get(2));
        assertEquals("saveChildren", repository.operations.get(3));
        assertFalse(repository.operations.contains("remove:CRM_CUSTOMER_VIEW"));
    }

    @Test
    public void ormReplaceViewConfigRollsBackWhenSaveChildrenFailsWithoutRemovingTable() {
        RecordingTransactionManager transactionManager = new RecordingTransactionManager();
        TransactionalOrmRepository repository = new TransactionalOrmRepository(transactionManager);
        repository.failSave = true;

        try {
            repository.replaceViewConfig(url("CRM_CUSTOMER_VIEW", "dyn"), new LinkedHashMap<String, Object>(),
                    new DynamicTableViewResponse.WritePlan());
            fail("Expected save failure");
        } catch (IllegalStateException e) {
            assertEquals("save failed", e.getMessage());
        }

        assertEquals(1, transactionManager.begins);
        assertEquals(0, transactionManager.commits);
        assertEquals(1, transactionManager.rollbacks);
        assertEquals(4, repository.operations.size());
        assertEquals("updateUrl:CRM_CUSTOMER_VIEW", repository.operations.get(0));
        assertEquals("updateEntity:VwDynTable", repository.operations.get(1));
        assertEquals("bulkDeleteChildren:CRM_CUSTOMER_VIEW", repository.operations.get(2));
        assertEquals("saveChildren", repository.operations.get(3));
        assertFalse(repository.operations.contains("remove:CRM_CUSTOMER_VIEW"));
    }

    @Test
    public void ormCreateViewConfigRunsUrlAndConfigInRequiredTransaction() {
        RecordingTransactionManager transactionManager = new RecordingTransactionManager();
        TransactionalOrmRepository repository = new TransactionalOrmRepository(transactionManager);

        repository.createViewConfig(url("CRM_CUSTOMER_VIEW", "dyn"), new LinkedHashMap<String, Object>(),
                new DynamicTableViewResponse.WritePlan());

        assertEquals(1, transactionManager.begins);
        assertEquals(1, transactionManager.commits);
        assertEquals(0, transactionManager.rollbacks);
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRED,
                transactionManager.lastDefinition.getPropagationBehavior());
        assertEquals(2, repository.operations.size());
        assertEquals("saveUrl:CRM_CUSTOMER_VIEW", repository.operations.get(0));
        assertEquals("save:CRM_CUSTOMER_VIEW", repository.operations.get(1));
    }

    @Test
    public void ormPatchWeixinUsesBulkDeleteInsteadOfRemoveByPk() {
        RecordingTransactionManager transactionManager = new RecordingTransactionManager();
        TransactionalOrmRepository repository = new TransactionalOrmRepository(transactionManager);

        repository.patchViewConfig(url("CRM_CUSTOMER_VIEW", "dyn"), DynamicTableViewSection.WEIXIN,
                new LinkedHashMap<String, Object>(), new DynamicTableViewResponse.WritePlan());

        assertTrue(repository.operations.contains("bulkDelete:VwDynWeixin:CRM_CUSTOMER_VIEW"));
        assertFalse(repository.operations.contains("removeEntity:VwDynWeixin:CRM_CUSTOMER_VIEW"));
    }

    @Test
    public void ormSaveMappedChildAttachesExistingPermissionBeforeSave() {
        RecordingTransactionManager transactionManager = new RecordingTransactionManager();
        TransactionalOrmRepository repository = new TransactionalOrmRepository(transactionManager);
        CmPri existing = new CmPri();
        existing.setPriKey("pri-existing");
        repository.existingPermission = existing;
        Map<String, Object> child = new LinkedHashMap<String, Object>();
        child.put("$type$", "VwDynColumn");
        CmPri transientPermission = new CmPri();
        transientPermission.setPriKey("pri-existing");
        child.put("pri", transientPermission);

        repository.saveMappedChild(child);

        assertTrue(repository.savedValues.containsKey("pri"));
        assertTrue(repository.savedValues.get("pri") == existing);
    }

    @Test
    public void flushAndClearViewCacheEvictsDynamicViewRegions() {
        TransactionalOrmRepository repository = new TransactionalOrmRepository(new RecordingTransactionManager());

        repository.flushAndClearViewCache("CRM_CUSTOMER_VIEW");

        assertTrue(repository.operations.contains("flush"));
        assertTrue(repository.operations.contains("clear"));
        assertTrue(repository.operations.contains("evictViewCacheRegions"));
    }

    private DynamicTableViewSnapshot snapshot(String viewKey) {
        DynamicTableViewSnapshot snapshot = new DynamicTableViewSnapshot();
        snapshot.setViewKey(viewKey);
        snapshot.setDescription("客户资料维护视图");
        snapshot.setLoginRequired(true);
        snapshot.getBase().setTableName("CRM_CUSTOMER");
        snapshot.getBase().setDisplayName("客户资料");
        snapshot.getBase().setLayoutColumns(Integer.valueOf(2));
        snapshot.getBase().setInitQuery(Boolean.TRUE);
        snapshot.getBase().setPageLimit(Integer.valueOf(20));
        snapshot.getBase().getDefaultSort().setField("ID");
        snapshot.getBase().getDefaultSort().setDirection("desc");
        snapshot.getFields().getSystemFields().add(field("ID", "主键"));
        snapshot.getFields().getListOrder().add("ID");
        return snapshot;
    }

    private DynamicTableViewSnapshot snapshotWithPermission(String viewKey) {
        DynamicTableViewSnapshot snapshot = snapshot(viewKey);
        DynamicTableViewSnapshot.PermissionSet permissions = new DynamicTableViewSnapshot.PermissionSet();
        permissions.setView("dyn." + viewKey + ".field.ID.view");
        snapshot.getFields().getSystemFields().get(0).setPermissions(permissions);
        return snapshot;
    }

    private DynamicTableViewSnapshot.Field field(String name, String displayName) {
        DynamicTableViewSnapshot.Field field = new DynamicTableViewSnapshot.Field();
        field.setName(name);
        field.setDisplayName(displayName);
        field.setShowInDetail(Boolean.TRUE);
        field.setShowInForm(Boolean.TRUE);
        field.setShowInList(Boolean.TRUE);
        field.setWidget("text");
        return field;
    }

    private static VwUrl url(String viewKey, String viewClass) {
        VwUrl url = new VwUrl();
        url.setViewKey(viewKey);
        url.setViewClass(viewClass);
        url.setDescription("客户资料维护视图");
        url.setLoginType(Integer.valueOf(1));
        url.setLockFlag(Integer.valueOf(0));
        url.setCreateUid("admin");
        return url;
    }

    private static class RecordingRepository implements DynamicTableViewRepository {
        private final Map<String, VwUrl> urls = new LinkedHashMap<String, VwUrl>();
        private final Map<String, Map<String, Object>> tables = new LinkedHashMap<String, Map<String, Object>>();
        private final List<VwUrl> savedUrls = new ArrayList<VwUrl>();
        private final Map<String, Map<String, Object>> savedViewConfigs = new LinkedHashMap<String, Map<String, Object>>();
        private final Map<String, Map<String, Object>> createdViewConfigs = new LinkedHashMap<String, Map<String, Object>>();
        private final Map<String, Map<String, Object>> replacedViewConfigs = new LinkedHashMap<String, Map<String, Object>>();
        private final Map<String, Map<String, Object>> patchedViewConfigs = new LinkedHashMap<String, Map<String, Object>>();
        private final List<String> removedViewKeys = new ArrayList<String>();
        private final List<String> removedTableConfigKeys = new ArrayList<String>();
        private final List<DynamicTableViewResponse.WritePlan> removePlans = new ArrayList<DynamicTableViewResponse.WritePlan>();
        private final List<DynamicTableViewSection> patchedSections = new ArrayList<DynamicTableViewSection>();
        private VwUrl createdUrl;
        private VwUrl replacedUrl;
        private int createAttempts;
        private int replaceAttempts;
        private int patchAttempts;
        private int updateUrlAttempts;
        private boolean failReplace;
        private int flushes;

        public List<VwUrl> listDynUrls(int start, int limit) {
            return new ArrayList<VwUrl>(urls.values());
        }

        public int countDynUrls() {
            return urls.size();
        }

        public VwUrl findUrl(String viewKey) {
            return urls.get(viewKey);
        }

        public Map<String, Object> findTable(String viewKey) {
            return tables.get(viewKey);
        }

        public Map<String, Object> findTableDefinition(String tableName) {
            if (!"CRM_CUSTOMER".equals(tableName)) {
                return null;
            }
            Map<String, Object> table = new LinkedHashMap<String, Object>();
            table.put("name", tableName);
            table.put("primaryKeyName", "ID");
            table.put("primaryKeyType", "VARCHAR");
            return table;
        }

        public Map<String, Object> findColumnDefinition(String tableName, String columnName) {
            if (!"CRM_CUSTOMER".equals(tableName)) {
                return null;
            }
            if (!"ID".equals(columnName) && !"NAME".equals(columnName)) {
                return null;
            }
            Map<String, Object> column = new LinkedHashMap<String, Object>();
            column.put("tableName", tableName);
            column.put("name", columnName);
            column.put("typeName", "VARCHAR");
            column.put("totalSize", Integer.valueOf(100));
            column.put("primaryKey", Boolean.valueOf("ID".equals(columnName)));
            column.put("required", Boolean.valueOf("ID".equals(columnName)));
            return column;
        }

        public VwUrl saveUrl(VwUrl url) {
            savedUrls.add(url);
            urls.put(url.getViewKey(), url);
            return url;
        }

        public void updateUrl(VwUrl url) {
            updateUrlAttempts++;
            urls.put(url.getViewKey(), url);
        }

        public void createViewConfig(VwUrl url,
                                     Map<String, Object> tableMap,
                                     DynamicTableViewResponse.WritePlan plan) {
            createAttempts++;
            createdUrl = url;
            createdViewConfigs.put(url.getViewKey(), tableMap);
            urls.put(url.getViewKey(), url);
            tables.put(url.getViewKey(), tableMap);
        }

        public void saveViewConfig(String viewKey, Map<String, Object> tableMap) {
            savedViewConfigs.put(viewKey, tableMap);
            tables.put(viewKey, tableMap);
        }

        public void replaceViewConfig(String viewKey, Map<String, Object> tableMap) {
            replaceAttempts++;
            if (failReplace) {
                throw new IllegalStateException("replace failed");
            }
            replacedViewConfigs.put(viewKey, tableMap);
            tables.put(viewKey, tableMap);
        }

        public void replaceViewConfig(VwUrl url,
                                      Map<String, Object> tableMap,
                                      DynamicTableViewResponse.WritePlan plan) {
            replaceAttempts++;
            if (failReplace) {
                throw new IllegalStateException("replace failed");
            }
            replacedUrl = url;
            urls.put(url.getViewKey(), url);
            replacedViewConfigs.put(url.getViewKey(), tableMap);
            tables.put(url.getViewKey(), tableMap);
        }

        public void patchViewConfig(VwUrl url,
                                    DynamicTableViewSection section,
                                    Map<String, Object> tableMap,
                                    DynamicTableViewResponse.WritePlan plan) {
            patchAttempts++;
            patchedSections.add(section);
            urls.put(url.getViewKey(), url);
            patchedViewConfigs.put(url.getViewKey(), tableMap);
            tables.put(url.getViewKey(), tableMap);
        }

        public void saveDynamicEntity(String entityName, Map<String, Object> values) {
        }

        public void updateDynamicEntity(String entityName, Map<String, Object> values) {
        }

        public void removeDynamicEntity(String entityName, Object id) {
        }

        public void removeDynamicTableConfig(String viewKey) {
            removedTableConfigKeys.add(viewKey);
            tables.remove(viewKey);
        }

        public void removeViewConfig(String viewKey) {
            removedViewKeys.add(viewKey);
            urls.remove(viewKey);
            tables.remove(viewKey);
        }

        public void removeViewConfig(String viewKey, DynamicTableViewResponse.WritePlan plan) {
            removedViewKeys.add(viewKey);
            removePlans.add(plan);
            urls.remove(viewKey);
            tables.remove(viewKey);
        }

        public void flushAndClearViewCache(String viewKey) {
            flushes++;
        }

        private void seedDynView(DynamicTableViewSnapshot snapshot) {
            VwUrl url = url(snapshot.getViewKey(), "dyn");
            urls.put(snapshot.getViewKey(), url);
            tables.put(snapshot.getViewKey(), new DynamicTableViewMapper().toTableMap(snapshot));
        }
    }

    private static class TransactionalOrmRepository extends OrmDynamicTableViewRepository {
        private final PlatformTransactionManager transactionManager;
        private final List<String> operations = new ArrayList<String>();
        private final Map<String, Object> savedValues = new LinkedHashMap<String, Object>();
        private final ORMService ormService = new RecordingOrmService(operations);
        private CmPri existingPermission;
        private boolean failSave;

        private TransactionalOrmRepository(PlatformTransactionManager transactionManager) {
            this.transactionManager = transactionManager;
        }

        @Override
        protected PlatformTransactionManager transactionManager() {
            return transactionManager;
        }

        @Override
        protected ORMService ormService() {
            return ormService;
        }

        @Override
        protected void evictViewCacheRegions(ORMService service) {
            operations.add("evictViewCacheRegions");
        }

        @Override
        public VwUrl saveUrl(VwUrl url) {
            operations.add("saveUrl:" + url.getViewKey());
            return url;
        }

        @Override
        public void updateUrl(VwUrl url) {
            operations.add("updateUrl:" + url.getViewKey());
        }

        @Override
        public void removeDynamicTableConfig(String viewKey) {
            operations.add("remove:" + viewKey);
        }

        @Override
        public void saveViewConfig(String viewKey, Map<String, Object> tableMap) {
            operations.add("save:" + viewKey);
            if (failSave) {
                throw new IllegalStateException("save failed");
            }
        }

        @Override
        public void updateDynamicEntity(String entityName, Map<String, Object> values) {
            operations.add("updateEntity:" + entityName);
        }

        @Override
        public void removeDynamicEntity(String entityName, Object id) {
            operations.add("removeEntity:" + entityName + ":" + id);
        }

        @Override
        protected void removeAllChildConfig(String viewKey) {
            operations.add("bulkDeleteChildren:" + viewKey);
        }

        @Override
        protected void removeEntitiesByViewKey(String viewKey, String... entityNames) {
            for (String entityName : entityNames) {
                operations.add("bulkDelete:" + entityName + ":" + viewKey);
            }
        }

        @Override
        protected void saveChildConfig(Map<String, Object> tableMap) {
            operations.add("saveChildren");
            if (failSave) {
                throw new IllegalStateException("save failed");
            }
        }

        @Override
        public void saveDynamicEntity(String entityName, Map<String, Object> values) {
            operations.add("saveEntity:" + entityName);
            savedValues.clear();
            savedValues.putAll(values);
        }

        @Override
        protected CmPri findPermission(String priKey) {
            if (existingPermission != null && existingPermission.getPriKey().equals(priKey)) {
                return existingPermission;
            }
            return null;
        }
    }

    private static class RecordingOrmService extends ORMService {
        private final List<String> operations;

        private RecordingOrmService(List<String> operations) {
            this.operations = operations;
        }

        @Override
        public void flush() {
            operations.add("flush");
        }

        @Override
        public void clear() {
            operations.add("clear");
        }
    }

    private static class RecordingTransactionManager implements PlatformTransactionManager {
        private int begins;
        private int commits;
        private int rollbacks;
        private TransactionDefinition lastDefinition;

        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            begins++;
            lastDefinition = definition;
            return new SimpleTransactionStatus();
        }

        public void commit(TransactionStatus status) throws TransactionException {
            commits++;
        }

        public void rollback(TransactionStatus status) throws TransactionException {
            rollbacks++;
        }
    }
}
