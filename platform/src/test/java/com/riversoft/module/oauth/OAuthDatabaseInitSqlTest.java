package com.riversoft.module.oauth;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.junit.Test;

public class OAuthDatabaseInitSqlTest {

    private static final String[] REQUIRED_SQL_TOKENS = { "CREATE TABLE IF NOT EXISTS `CM_THIRDPART`",
            "CREATE TABLE IF NOT EXISTS `CM_THIRDPART_AUTH_CODE`",
            "CREATE TABLE IF NOT EXISTS `CM_THIRDPART_ACCESS_TOKEN`",
            "UNIQUE KEY `UK_CM_THIRDPART_CLIENT_ID`",
            "UNIQUE KEY `UK_CM_THIRDPART_AUTH_CODE_HASH`",
            "UNIQUE KEY `UK_CM_THIRDPART_ACCESS_TOKEN_HASH`" };

	private static final String[] REQUIRED_MENU_TOKENS = {
			"('sys_thirdpart', '1', 'sys', '/thirdpart/ThirdpartAction/index.shtml', 'manage', 'link_edit.png', '第三方系统', '1', '13'",
			"('sys_thirdpart', '1', 'sys_thirdpart', '第三方系统', NULL, '1', '2', '${true}')" };

	private static final String[] REQUIRED_FULL_MENU_TOKENS = {
			"('sys_menu',1,'sys','/manager/MenuAction/index.shtml','manage','application_side_list.png','用户菜单',1,195",
			"('sys_thirdpart',1,'sys','/thirdpart/ThirdpartAction/index.shtml','manage','link_edit.png','第三方系统',1,196",
			"('sys_thirdpart',1,'sys_thirdpart','第三方系统',NULL,1,2,'${true}')" };

    @Test
    public void defaultInitializationSqlContainsOAuthTables() throws Exception {
        assertOAuthTables(gzipReader("../database/bpmt-min.sql.gz"));
        assertOAuthTables(gzipReader("../database/bpmt.sql.gz"));
        assertOAuthTables(Files.newBufferedReader(Paths.get("../database/bpmt-db.sql"), StandardCharsets.UTF_8));
        assertOAuthTables(
                Files.newBufferedReader(Paths.get("../database/v1.5.0-oauth-tables.sql"), StandardCharsets.UTF_8));
    }

    @Test
    public void defaultInitializationSqlContainsThirdpartManagementMenu() throws Exception {
        assertSqlContains(gzipReader("../database/bpmt-min.sql.gz"), REQUIRED_MENU_TOKENS,
                "thirdpart management menu");
        assertSqlContains(gzipReader("../database/bpmt.sql.gz"), REQUIRED_FULL_MENU_TOKENS,
                "thirdpart management menu");
        assertSqlContains(Files.newBufferedReader(Paths.get("../database/bpmt-db.sql"), StandardCharsets.UTF_8),
                REQUIRED_MENU_TOKENS, "thirdpart management menu");
    }

    private void assertOAuthTables(BufferedReader reader) throws IOException {
        Set<String> missing = new LinkedHashSet<String>(Arrays.asList(REQUIRED_SQL_TOKENS));
        try {
            String line;
            while ((line = reader.readLine()) != null && !missing.isEmpty()) {
                for (String token : REQUIRED_SQL_TOKENS) {
                    if (line.indexOf(token) >= 0) {
                        missing.remove(token);
                    }
                }
            }
        } finally {
            reader.close();
        }
        assertTrue("Missing OAuth SQL tokens: " + missing, missing.isEmpty());
    }

    private void assertSqlContains(BufferedReader reader, String[] requiredTokens, String label) throws IOException {
        Set<String> missing = new LinkedHashSet<String>(Arrays.asList(requiredTokens));
        try {
            String line;
            while ((line = reader.readLine()) != null && !missing.isEmpty()) {
                for (String token : requiredTokens) {
                    if (line.indexOf(token) >= 0) {
                        missing.remove(token);
                    }
                }
            }
        } finally {
            reader.close();
        }
        assertTrue("Missing " + label + " SQL tokens: " + missing, missing.isEmpty());
    }

    private BufferedReader gzipReader(String path) throws IOException {
        InputStream input = new GZIPInputStream(Files.newInputStream(Paths.get(path)));
        return new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
    }
}
