package com.riversoft.module.oauth;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.riversoft.core.db.DataCondition;
import com.riversoft.core.db.DataPO;
import com.riversoft.core.db.ORMService;
import com.riversoft.module.thirdpart.ThirdpartService;
import com.riversoft.platform.SessionManager;
import com.riversoft.platform.po.CmPri;
import com.riversoft.platform.po.UsUser;

public class OAuthService {
    private static final Logger logger = LoggerFactory.getLogger(OAuthService.class);

    private static final String AUTH_CODE_ENTITY = "CmThirdpartAuthCode";
    private static final String ACCESS_TOKEN_ENTITY = "CmThirdpartAccessToken";
    private static final long AUTH_CODE_TTL_MILLIS = 5L * 60L * 1000L;
    private static final long ACCESS_TOKEN_TTL_MILLIS = 7200L * 1000L;
    private static final long ACCESS_TOKEN_EXPIRES_IN = 7200L;

    private final ThirdpartService thirdpartService = new ThirdpartService();

    public Map<String, Object> validateAuthorize(String responseType, String clientId, String redirectUri) {
        String requestId = requestId();
        if (!StringUtils.equals("code", responseType)) {
            logInfo(requestId, clientId, null, null, "deny", "unsupported_response_type");
            return error("invalid_request", "unsupported response_type.");
        }

        Map<String, Object> thirdpart = findActiveThirdpart(clientId);
        if (thirdpart == null) {
            logInfo(requestId, clientId, null, null, "deny", "inactive_client");
            return error("invalid_client", "client is inactive or not found.");
        }

        String thirdpartKey = stringValue(thirdpart.get("thirdpartKey"));
        if (!ThirdpartService.isAllowedRedirectUri(stringValue(thirdpart.get("redirectUris")), redirectUri)) {
            logInfo(requestId, clientId, thirdpartKey, null, "deny", "redirect_mismatch");
            return error("invalid_request", "redirect_uri is not allowed.");
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("thirdpart", thirdpart);
        result.put("requestId", requestId);
        logInfo(requestId, clientId, thirdpartKey, null, "allow", "ok");
        return result;
    }

    public boolean currentUserCanAccess(Map<String, Object> thirdpart) {
        String requestId = requestId();
        String clientId = thirdpart == null ? null : stringValue(thirdpart.get("clientId"));
        String thirdpartKey = thirdpart == null ? null : stringValue(thirdpart.get("thirdpartKey"));
        if (thirdpart == null || thirdpart.get("pri") == null) {
            logInfo(requestId, clientId, thirdpartKey, null, "deny", "missing_pri");
            return false;
        }
        UsUser user = SessionManager.getUser();
        if (user == null) {
            logInfo(requestId, clientId, thirdpartKey, null, "deny", "user_not_logged_in");
            return false;
        }
        try {
            if (SessionManager.isAdmin()) {
                logInfo(requestId, clientId, thirdpartKey, user.getUid(), "allow", "admin");
                return true;
            }
            Object pri = thirdpart.get("pri");
            boolean allowed = pri instanceof CmPri && SessionManager.check((CmPri) pri);
            logInfo(requestId, clientId, thirdpartKey, user.getUid(), allowed ? "allow" : "deny",
                    allowed ? "pri_allowed" : "pri_denied");
            return allowed;
        } catch (RuntimeException e) {
            logInfo(requestId, clientId, thirdpartKey, user.getUid(), "deny", "context_unavailable");
            return false;
        }
    }

    public String createAuthorizationCode(Map<String, Object> thirdpart, String userId, String redirectUri,
            String state) {
        String requestId = requestId();
        String clientId = thirdpart == null ? null : stringValue(thirdpart.get("clientId"));
        String thirdpartKey = thirdpart == null ? null : stringValue(thirdpart.get("thirdpartKey"));
        String code = OAuthSecurity.generateOpaqueValue();
        Date now = now();

        DataPO po = new DataPO(AUTH_CODE_ENTITY);
        po.set("id", OAuthSecurity.generateOpaqueValue());
        po.set("codeHash", OAuthSecurity.sha256Hex(code));
        po.set("clientId", clientId);
        po.set("thirdpartKey", thirdpartKey);
        po.set("userId", userId);
        po.set("redirectUri", redirectUri);
        po.set("state", state);
        po.set("expiresAt", new Date(now.getTime() + authorizationCodeTtlMillis()));
        po.set("createTime", now);
        save(po.toEntity());

        logInfo(requestId, clientId, thirdpartKey, userId, "issue_code", "ok");
        return code;
    }

    public Map<String, Object> exchangeCode(String clientId, String clientSecret, String code, String redirectUri) {
        String requestId = requestId();
        Map<String, Object> thirdpart = findActiveThirdpart(clientId);
        if (thirdpart == null) {
            logInfo(requestId, clientId, null, null, "deny", "inactive_client");
            return error("invalid_client", "client is inactive or not found.");
        }

        String thirdpartKey = stringValue(thirdpart.get("thirdpartKey"));
        if (!ThirdpartService.matchesSecret(stringValue(thirdpart.get("clientSecretHash")), clientSecret)) {
            logInfo(requestId, clientId, thirdpartKey, null, "deny", "client_secret_mismatch");
            return error("invalid_client", "client authentication failed.");
        }

        Map<String, Object> authCode = findAuthCodeByHash(OAuthSecurity.sha256Hex(StringUtils.defaultString(code)));
        if (authCode == null || authCode.get("usedAt") != null || isExpired((Date) authCode.get("expiresAt"))) {
            logInfo(requestId, clientId, thirdpartKey, null, "deny", "invalid_grant");
            return error("invalid_grant", "authorization code is invalid.");
        }

        String userId = stringValue(authCode.get("userId"));
        if (!StringUtils.equals(clientId, stringValue(authCode.get("clientId")))
                || !StringUtils.equals(thirdpartKey, stringValue(authCode.get("thirdpartKey")))
                || !StringUtils.equals(redirectUri, stringValue(authCode.get("redirectUri")))) {
            logInfo(requestId, clientId, thirdpartKey, userId, "deny", "grant_binding_mismatch");
            return error("invalid_grant", "authorization code binding mismatch.");
        }

        Date now = now();
        DataPO authCodePO = new DataPO(AUTH_CODE_ENTITY, authCode);
        authCodePO.set("usedAt", now);
        update(authCodePO.toEntity());

        String accessToken = OAuthSecurity.generateOpaqueValue();
        DataPO tokenPO = new DataPO(ACCESS_TOKEN_ENTITY);
        tokenPO.set("id", OAuthSecurity.generateOpaqueValue());
        tokenPO.set("tokenHash", OAuthSecurity.sha256Hex(accessToken));
        tokenPO.set("clientId", clientId);
        tokenPO.set("thirdpartKey", thirdpartKey);
        tokenPO.set("userId", userId);
        tokenPO.set("expiresAt", new Date(now.getTime() + accessTokenTtlMillis()));
        tokenPO.set("createTime", now);
        save(tokenPO.toEntity());

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("access_token", accessToken);
        result.put("token_type", "Bearer");
        result.put("expires_in", Long.valueOf(accessTokenExpiresIn()));
        result.put("userid", userId);
        logInfo(requestId, clientId, thirdpartKey, userId, "issue_token", "ok");
        return result;
    }

    public Map<String, Object> loadUserInfo(String bearerToken) {
        String requestId = requestId();
        Map<String, Object> token = findAccessTokenByHash(OAuthSecurity.sha256Hex(StringUtils.defaultString(bearerToken)));
        if (token == null || token.get("revokedAt") != null || isExpired((Date) token.get("expiresAt"))) {
            logInfo(requestId, null, null, null, "deny", "invalid_token");
            return error("invalid_token", "access token is invalid.");
        }

        Date now = now();
        DataPO tokenPO = new DataPO(ACCESS_TOKEN_ENTITY, token);
        tokenPO.set("lastUsedAt", now);
        update(tokenPO.toEntity());

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("userid", token.get("userId"));
        result.put("client_id", token.get("clientId"));
        result.put("thirdpart_key", token.get("thirdpartKey"));
        logInfo(requestId, stringValue(token.get("clientId")), stringValue(token.get("thirdpartKey")),
                stringValue(token.get("userId")), "userinfo", "ok");
        return result;
    }

    protected Map<String, Object> findActiveThirdpart(String clientId) {
        if (StringUtils.isBlank(clientId)) {
            return null;
        }
        try {
            return thirdpartService.findActiveByClientId(clientId);
        } catch (RuntimeException e) {
            logger.debug("OAuth client lookup failed. clientId={} reason=invalid_client_id", clientId);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> findAuthCodeByHash(String codeHash) {
        return (Map<String, Object>) ORMService.getInstance().find(AUTH_CODE_ENTITY,
                new DataCondition().setStringEqual("codeHash", codeHash).toEntity());
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> findAccessTokenByHash(String tokenHash) {
        return (Map<String, Object>) ORMService.getInstance().find(ACCESS_TOKEN_ENTITY,
                new DataCondition().setStringEqual("tokenHash", tokenHash).toEntity());
    }

    protected void save(Map<String, Object> po) {
        ORMService.getInstance().save(po);
    }

    protected void update(Map<String, Object> po) {
        ORMService.getInstance().update(po);
    }

    protected Date now() {
        return new Date();
    }

    protected long authorizationCodeTtlMillis() {
        return AUTH_CODE_TTL_MILLIS;
    }

    protected long accessTokenTtlMillis() {
        return ACCESS_TOKEN_TTL_MILLIS;
    }

    protected long accessTokenExpiresIn() {
        return ACCESS_TOKEN_EXPIRES_IN;
    }

    private boolean isExpired(Date expiresAt) {
        return expiresAt == null || !expiresAt.after(now());
    }

    private Map<String, Object> error(String error, String description) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("error", error);
        result.put("error_description", description);
        return result;
    }

    private String requestId() {
        return OAuthSecurity.generateOpaqueValue();
    }

    private void logInfo(String requestId, String clientId, String thirdpartKey, String userId, String result,
            String reason) {
        logger.info("OAuth runtime. requestId={} clientId={} thirdpartKey={} userId={} result={} reason={}",
                requestId, clientId, thirdpartKey, userId, result, reason);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
