package com.riversoft.module.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.riversoft.core.db.DataPO;
import com.riversoft.module.thirdpart.ThirdpartService;

public class OAuthServiceTest {

    @Test
    public void unsupportedResponseTypeReturnsInvalidRequest() {
        TestOAuthService service = new TestOAuthService();

        Map<String, Object> result = service.validateAuthorize("token", "client-a", "http://127.0.0.1/callback");

        assertEquals("invalid_request", result.get("error"));
    }

    @Test
    public void inactiveClientReturnsInvalidClient() {
        TestOAuthService service = new TestOAuthService();

        Map<String, Object> result = service.validateAuthorize("code", "missing-client",
                "http://127.0.0.1/callback");

        assertEquals("invalid_client", result.get("error"));
    }

    @Test
    public void redirectMismatchReturnsInvalidRequest() {
        TestOAuthService service = new TestOAuthService();
        service.addThirdpart(thirdpart("app-a", "client-a", "secret", "http://127.0.0.1/callback"));

        Map<String, Object> result = service.validateAuthorize("code", "client-a", "http://evil.example/callback");

        assertEquals("invalid_request", result.get("error"));
    }

    @Test
    public void authCodeCanBeConsumedOnce() {
        TestOAuthService service = new TestOAuthService();
        Map<String, Object> thirdpart = thirdpart("app-a", "client-a", "secret", "http://127.0.0.1/callback");
        service.addThirdpart(thirdpart);
        String code = service.createAuthorizationCode(thirdpart, "admin", "http://127.0.0.1/callback", "state-a");

        Map<String, Object> first = service.exchangeCode("client-a", "secret", code, "http://127.0.0.1/callback");
        Map<String, Object> second = service.exchangeCode("client-a", "secret", code, "http://127.0.0.1/callback");

        assertNotNull(first.get("access_token"));
        assertEquals("Bearer", first.get("token_type"));
        assertEquals(Long.valueOf(7200L), first.get("expires_in"));
        assertEquals("admin", first.get("userid"));
        assertEquals("invalid_grant", second.get("error"));
        assertEquals(1, service.tokens.size());
    }

    @Test
    public void concurrentAuthCodeExchangeSignsAtMostOneToken() {
        TestOAuthService service = new TestOAuthService();
        service.alwaysReadFreshAuthCode = true;
        Map<String, Object> thirdpart = thirdpart("app-a", "client-a", "secret", "http://127.0.0.1/callback");
        service.addThirdpart(thirdpart);
        String code = service.createAuthorizationCode(thirdpart, "admin", "http://127.0.0.1/callback", "state-a");

        Map<String, Object> first = service.exchangeCode("client-a", "secret", code, "http://127.0.0.1/callback");
        Map<String, Object> second = service.exchangeCode("client-a", "secret", code, "http://127.0.0.1/callback");

        assertNotNull(first.get("access_token"));
        assertEquals("invalid_grant", second.get("error"));
        assertEquals(1, service.tokens.size());
        assertEquals(2, service.consumeAttempts);
    }

    @Test
    public void expiredAuthCodeReturnsInvalidGrant() {
        TestOAuthService service = new TestOAuthService();
        service.authCodeTtlMillis = -1000L;
        Map<String, Object> thirdpart = thirdpart("app-a", "client-a", "secret", "http://127.0.0.1/callback");
        service.addThirdpart(thirdpart);
        String code = service.createAuthorizationCode(thirdpart, "admin", "http://127.0.0.1/callback", null);

        Map<String, Object> result = service.exchangeCode("client-a", "secret", code,
                "http://127.0.0.1/callback");

        assertEquals("invalid_grant", result.get("error"));
        assertEquals(0, service.tokens.size());
    }

    @Test
    public void tokenLookupReturnsUserInfoOnlyBeforeExpiresAt() {
        TestOAuthService service = new TestOAuthService();
        Map<String, Object> thirdpart = thirdpart("app-a", "client-a", "secret", "http://127.0.0.1/callback");
        service.addThirdpart(thirdpart);
        String code = service.createAuthorizationCode(thirdpart, "admin", "http://127.0.0.1/callback", null);
        Map<String, Object> tokenResult = service.exchangeCode("client-a", "secret", code,
                "http://127.0.0.1/callback");

        Map<String, Object> userInfo = service.loadUserInfo((String) tokenResult.get("access_token"));
        service.tokens.get(0).put("expiresAt", new Date(service.now.getTime() - 1000L));
        Map<String, Object> expired = service.loadUserInfo((String) tokenResult.get("access_token"));

        assertEquals("admin", userInfo.get("userid"));
        assertEquals("client-a", userInfo.get("client_id"));
        assertEquals("app-a", userInfo.get("thirdpart_key"));
        assertNotNull(service.tokens.get(0).get("lastUsedAt"));
        assertEquals("invalid_token", expired.get("error"));
    }

    @Test
    public void validateAuthorizeReturnsThirdpartForValidRequest() {
        TestOAuthService service = new TestOAuthService();
        service.addThirdpart(thirdpart("app-a", "client-a", "secret", "http://127.0.0.1/callback"));

        Map<String, Object> result = service.validateAuthorize("code", "client-a", "http://127.0.0.1/callback");

        assertFalse(result.containsKey("error"));
        assertNotNull(result.get("thirdpart"));
        assertNotNull(result.get("requestId"));
    }

    private Map<String, Object> thirdpart(String thirdpartKey, String clientId, String clientSecret,
            String redirectUris) {
        Map<String, Object> thirdpart = new HashMap<String, Object>();
        thirdpart.put("thirdpartKey", thirdpartKey);
        thirdpart.put("clientId", clientId);
        thirdpart.put("clientSecretHash", ThirdpartService.hashSecret(clientSecret));
        thirdpart.put("redirectUris", redirectUris);
        thirdpart.put("activeFlag", Integer.valueOf(1));
        return thirdpart;
    }

    private static class TestOAuthService extends OAuthService {
        private final List<Map<String, Object>> thirdparts = new ArrayList<Map<String, Object>>();
        private final List<Map<String, Object>> authCodes = new ArrayList<Map<String, Object>>();
        private final List<Map<String, Object>> tokens = new ArrayList<Map<String, Object>>();
        private Date now = new Date(1000000L);
        private long authCodeTtlMillis = 5L * 60L * 1000L;
        private boolean alwaysReadFreshAuthCode;
        private int consumeAttempts;

        void addThirdpart(Map<String, Object> thirdpart) {
            thirdparts.add(thirdpart);
        }

        @Override
        protected Map<String, Object> findActiveThirdpart(String clientId) {
            for (Map<String, Object> thirdpart : thirdparts) {
                if (clientId.equals(thirdpart.get("clientId")) && Integer.valueOf(1).equals(thirdpart.get("activeFlag"))) {
                    return thirdpart;
                }
            }
            return null;
        }

        @Override
        protected Map<String, Object> findAuthCodeByHash(String codeHash) {
            Map<String, Object> authCode = findByHash(authCodes, "codeHash", codeHash);
            if (authCode == null || !alwaysReadFreshAuthCode) {
                return authCode;
            }
            Map<String, Object> copy = new HashMap<String, Object>(authCode);
            copy.put("usedAt", null);
            return copy;
        }

        @Override
        protected Map<String, Object> findAccessTokenByHash(String tokenHash) {
            return findByHash(tokens, "tokenHash", tokenHash);
        }

        @Override
        protected void save(Map<String, Object> po) {
            String entityName = new DataPO((String) po.get("$type$"), po).getEntityName();
            if ("CmThirdpartAuthCode".equals(entityName)) {
                authCodes.add(po);
            } else if ("CmThirdpartAccessToken".equals(entityName)) {
                tokens.add(po);
            }
        }

        @Override
        protected void update(Map<String, Object> po) {
            // In-memory maps are updated by reference through DataPO.
        }

        @Override
        protected boolean consumeAuthorizationCode(Map<String, Object> authCode, Date usedAt) {
            consumeAttempts++;
            Map<String, Object> stored = findByHash(authCodes, "codeHash", (String) authCode.get("codeHash"));
            if (stored == null || stored.get("usedAt") != null || !((Date) stored.get("expiresAt")).after(usedAt)) {
                return false;
            }
            stored.put("usedAt", usedAt);
            return true;
        }

        @Override
        protected Date now() {
            return now;
        }

        @Override
        protected long authorizationCodeTtlMillis() {
            return authCodeTtlMillis;
        }

        private Map<String, Object> findByHash(List<Map<String, Object>> rows, String field, String value) {
            for (Map<String, Object> row : rows) {
                if (value.equals(row.get(field))) {
                    return row;
                }
            }
            return null;
        }
    }
}
