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

    @Test
    public void defaultInitializationSqlContainsOAuthTables() throws Exception {
        assertOAuthTables(gzipReader("../database/bpmt-min.sql.gz"));
        assertOAuthTables(gzipReader("../database/bpmt.sql.gz"));
        assertOAuthTables(Files.newBufferedReader(Paths.get("../database/bpmt-db.sql"), StandardCharsets.UTF_8));
        assertOAuthTables(
                Files.newBufferedReader(Paths.get("../database/v1.5.0-oauth-tables.sql"), StandardCharsets.UTF_8));
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

    private BufferedReader gzipReader(String path) throws IOException {
        InputStream input = new GZIPInputStream(Files.newInputStream(Paths.get(path)));
        return new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
    }
}
