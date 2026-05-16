package com.riversoft.api.modules.database_operations;

import com.riversoft.api.http.ApiException;
import com.riversoft.platform.db.DbHelper;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatabaseOperationServiceTest {

    @Test
    public void queryRequiresSelect() {
        DatabaseOperationService service = new DatabaseOperationService(new StubDbHelper());
        DatabaseOperationRequest request = new DatabaseOperationRequest();
        request.setSql("update TB_TABLE set DESCRIPTION=? where NAME=?");
        request.setArgs(new Object[]{"x", "T1"});
        try {
            service.query(request);
        } catch (ApiException e) {
            assertEquals("DBOPS_SQL_NOT_ALLOWED", e.getCode());
            assertEquals(422, e.getStatus());
            return;
        }
        throw new AssertionError("query should reject non-select sql");
    }

    @Test
    public void findReturnsMap() {
        DatabaseOperationService service = new DatabaseOperationService(new StubDbHelper());
        DatabaseOperationRequest request = new DatabaseOperationRequest();
        request.setSql("select NAME from TB_TABLE where NAME=?");
        request.setArgs(new Object[]{"RV_TEST"});
        Map<String, Object> data = service.find(request);
        assertTrue(data.containsKey("item"));
    }

    @Test
    public void saveAllowsNullGeneratedKeyForExplicitPrimaryKeyInsert() {
        DatabaseOperationService service = new DatabaseOperationService(new StubDbHelper(null));
        DatabaseOperationRequest request = new DatabaseOperationRequest();
        request.setSql("insert into TMP_V170TEST (ID, TXT_TITLE) values (?, ?)");
        request.setArgs(new Object[]{"ID001", "标题"});

        Map<String, Object> result = service.save(request);

        assertTrue(result.containsKey("id"));
        assertEquals(null, result.get("id"));
    }

    private static final class StubDbHelper extends DbHelper {
        private final Long id;

        private StubDbHelper() {
            this(Long.valueOf(1L));
        }

        private StubDbHelper(Long id) {
            this.id = id;
        }

        @Override
        public java.util.List<?> query(String sql, Object... args) {
            return Collections.emptyList();
        }

        @Override
        public Map<String, Object> find(String sql, Object... args) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("NAME", "RV_TEST");
            return row;
        }

        @Override
        public Long save(String sql, Object... args) {
            return id;
        }
    }
}
