package com.riversoft.api.modules.report_views;

import com.riversoft.platform.po.CmPri;
import com.riversoft.platform.po.VwUrl;
import org.junit.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class OrmReportViewRepositoryTest {
    @Test
    public void replaceViewConfigRunsUpdateDeleteAndSaveInTransaction() {
        RecordingTransactionManager tx = new RecordingTransactionManager();
        TransactionalOrmReportRepository repository = new TransactionalOrmReportRepository(tx);

        repository.replaceViewConfig(url("SALES_REPORT", "rep_list"), new LinkedHashMap<String, Object>(),
                new ReportViewResponse.WritePlan());

        assertEquals(1, tx.begins);
        assertEquals(1, tx.commits);
        assertEquals(0, tx.rollbacks);
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRED, tx.lastDefinition.getPropagationBehavior());
        assertEquals("updateUrl:SALES_REPORT", repository.operations.get(0));
        assertEquals("updateEntity:VwReport", repository.operations.get(1));
        assertEquals("removeChildren:SALES_REPORT", repository.operations.get(2));
        assertEquals("saveChildren", repository.operations.get(3));
    }

    @Test
    public void createViewConfigRunsSaveUrlConfigAndPermissionDeleteInTransaction() {
        RecordingTransactionManager tx = new RecordingTransactionManager();
        TransactionalOrmReportRepository repository = new TransactionalOrmReportRepository(tx);
        ReportViewResponse.WritePlan plan = new ReportViewResponse.WritePlan();
        plan.getPermissionDeletes().add("report.SALES_REPORT.old.view");

        repository.createViewConfig(url("SALES_REPORT", "rep_list"), new LinkedHashMap<String, Object>(), plan);

        assertEquals(1, tx.begins);
        assertEquals(1, tx.commits);
        assertEquals("saveUrl:SALES_REPORT", repository.operations.get(0));
        assertEquals("save:SALES_REPORT", repository.operations.get(1));
        assertEquals("removePermission:report.SALES_REPORT.old.view", repository.operations.get(2));
    }

    @Test
    public void patchColumnsOnlyReplacesColumnChildren() {
        RecordingTransactionManager tx = new RecordingTransactionManager();
        TransactionalOrmReportRepository repository = new TransactionalOrmReportRepository(tx);

        repository.patchViewConfig(url("SALES_REPORT", "rep_list"), ReportViewSection.COLUMNS,
                new LinkedHashMap<String, Object>(), new ReportViewResponse.WritePlan());

        assertEquals(1, tx.begins);
        assertEquals(1, tx.commits);
        assertEquals("updateUrl:SALES_REPORT", repository.operations.get(0));
        assertEquals("removeSection:COLUMNS:SALES_REPORT", repository.operations.get(1));
        assertEquals("saveSection:COLUMNS", repository.operations.get(2));
    }

    @Test
    public void attachExistingPermissionsReplacesDynamicCmPriValues() {
        RecordingTransactionManager tx = new RecordingTransactionManager();
        TransactionalOrmReportRepository repository = new TransactionalOrmReportRepository(tx);
        CmPri existing = new CmPri();
        existing.setPriKey("report.SALES_REPORT.column.ORDER_NO.view");
        repository.existingPermission = existing;
        CmPri detached = new CmPri();
        detached.setPriKey(existing.getPriKey());
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("pri", detached);

        repository.attachExistingPermissions(values);

        assertEquals(existing, values.get("pri"));
    }

    private static VwUrl url(String viewKey, String viewClass) {
        VwUrl url = new VwUrl();
        url.setViewKey(viewKey);
        url.setViewClass(viewClass);
        url.setDescription("销售报表");
        url.setLoginType(Integer.valueOf(1));
        return url;
    }

    private static class TransactionalOrmReportRepository extends OrmReportViewRepository {
        private final PlatformTransactionManager transactionManager;
        private final List<String> operations = new ArrayList<String>();
        private CmPri existingPermission;

        private TransactionalOrmReportRepository(PlatformTransactionManager transactionManager) {
            this.transactionManager = transactionManager;
        }

        @Override
        protected PlatformTransactionManager transactionManager() {
            return transactionManager;
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
        public void saveViewConfig(String viewKey, Map<String, Object> reportMap) {
            operations.add("save:" + viewKey);
        }

        @Override
        public void updateDynamicEntity(String entityName, Map<String, Object> values) {
            operations.add("updateEntity:" + entityName);
        }

        @Override
        protected void removeAllChildConfig(String viewKey) {
            operations.add("removeChildren:" + viewKey);
        }

        @Override
        protected void saveChildConfig(Map<String, Object> reportMap) {
            operations.add("saveChildren");
        }

        @Override
        protected void removeSectionConfig(String viewKey, ReportViewSection section) {
            operations.add("removeSection:" + section.name() + ":" + viewKey);
        }

        @Override
        protected void saveSectionConfig(ReportViewSection section, Map<String, Object> reportMap) {
            operations.add("saveSection:" + section.name());
        }

        @Override
        protected void removePermissionKey(String priKey) {
            operations.add("removePermission:" + priKey);
        }

        @Override
        protected CmPri findPermission(String priKey) {
            if (existingPermission != null && existingPermission.getPriKey().equals(priKey)) {
                return existingPermission;
            }
            return null;
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
