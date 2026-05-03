package com.riversoft.module.thirdpart;

import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.riversoft.core.db.DataCondition;
import com.riversoft.core.db.DataPO;
import com.riversoft.core.db.ORMService;
import com.riversoft.core.exception.ExceptionType;
import com.riversoft.core.exception.SystemRuntimeException;
import com.riversoft.module.oauth.OAuthSecurity;
import com.riversoft.platform.po.CmPri;

public class ThirdpartService {
    private static final String ENTITY_NAME = "CmThirdpart";
    private static final Pattern SAFE_KEY = Pattern.compile("^[A-Za-z0-9_.-]{1,100}$");

    public static boolean isAllowedRedirectUri(String redirectUris, String candidate) {
        if (StringUtils.isBlank(redirectUris) || StringUtils.isBlank(candidate)) {
            return false;
        }
        String value = candidate;
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return false;
        }
        String[] configuredUris = redirectUris.split("[\\n;,]");
        for (String configuredUri : configuredUris) {
            if (value.equals(configuredUri.trim())) {
                return true;
            }
        }
        return false;
    }

    public static String hashSecret(String secret) {
        if (StringUtils.isBlank(secret)) {
            throw new SystemRuntimeException(ExceptionType.BUSINESS, "clientSecret不能为空.");
        }
        return OAuthSecurity.sha256Hex(secret);
    }

    public static boolean matchesSecret(String secretHash, String secret) {
        if (StringUtils.isBlank(secretHash) || StringUtils.isBlank(secret)) {
            return false;
        }
        return OAuthSecurity.constantTimeEquals(secretHash, hashSecret(secret));
    }

    public static void validateThirdpartKey(String thirdpartKey) {
        validateSafeValue(thirdpartKey, "thirdpartKey");
    }

    public static void validateClientId(String clientId) {
        validateSafeValue(clientId, "clientId");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> findActiveByClientId(String clientId) {
        validateClientId(clientId);
        return (Map<String, Object>) ORMService.getInstance().find(ENTITY_NAME,
                new DataCondition().setStringEqual("clientId", clientId).setNumberEqual("activeFlag", "1").toEntity());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> findActiveByThirdpartKey(String thirdpartKey) {
        validateThirdpartKey(thirdpartKey);
        return (Map<String, Object>) ORMService.getInstance().find(ENTITY_NAME,
                new DataCondition().setStringEqual("thirdpartKey", thirdpartKey).setNumberEqual("activeFlag", "1")
                        .toEntity());
    }

    public String createThirdpart(Map<String, Object> input, CmPri pri) {
        if (input == null) {
            throw new SystemRuntimeException(ExceptionType.BUSINESS, "外部系统不能为空.");
        }
        String thirdpartKey = trimToNull(input.get("thirdpartKey"));
        String clientId = trimToNull(input.get("clientId"));
        validateThirdpartKey(thirdpartKey);
        validateClientId(clientId);
        validateRequired(input, "thirdpartName");
        validateRequired(input, "redirectUris");
        if (pri == null) {
            throw new SystemRuntimeException(ExceptionType.BUSINESS, "权限点不能为空.");
        }
        prepareThirdpartPri(pri, thirdpartKey, trimToNull(input.get("thirdpartName")));

        String clientSecret = OAuthSecurity.generateOpaqueValue();
        Date now = new Date();
        DataPO po = new DataPO(ENTITY_NAME);
        po.set("thirdpartKey", thirdpartKey);
        po.set("thirdpartName", trimToNull(input.get("thirdpartName")));
        po.set("clientId", clientId);
        po.set("clientSecretHash", hashSecret(clientSecret));
        po.set("redirectUris", trimToNull(input.get("redirectUris")));
        po.set("homeUrl", trimToNull(input.get("homeUrl")));
        po.set("pri", pri);
        po.set("activeFlag", normalizeActiveFlag(input.get("activeFlag")));
        po.set("description", trimToNull(input.get("description")));
        po.set("createTime", now);
        po.set("updateTime", now);
        ORMService.getInstance().save(po.toEntity());
        return clientSecret;
    }

    @SuppressWarnings("unchecked")
    public void updateThirdpart(String thirdpartKey, Map<String, Object> input, CmPri pri) {
        validateThirdpartKey(thirdpartKey);
        if (input == null) {
            throw new SystemRuntimeException(ExceptionType.BUSINESS, "外部系统不能为空.");
        }
        Map<String, Object> entity = (Map<String, Object>) ORMService.getInstance().findByPk(ENTITY_NAME, thirdpartKey);
        if (entity == null) {
            throw new SystemRuntimeException(ExceptionType.BUSINESS, "外部系统不存在.");
        }
        DataPO po = new DataPO(ENTITY_NAME, entity);
        setRequiredIfPresent(po, input, "thirdpartName");
        if (input.containsKey("clientId")) {
            String clientId = trimToNull(input.get("clientId"));
            validateClientId(clientId);
            po.set("clientId", clientId);
        }
        if (input.containsKey("clientSecret")) {
            po.set("clientSecretHash", hashSecret(trimToNull(input.get("clientSecret"))));
        }
        setRequiredIfPresent(po, input, "redirectUris");
        setIfPresent(po, input, "homeUrl");
        if (input.containsKey("activeFlag")) {
            po.set("activeFlag", normalizeActiveFlag(input.get("activeFlag")));
        }
        setIfPresent(po, input, "description");
        if (pri != null) {
            prepareThirdpartPri(pri, thirdpartKey, trimToNull(po.get("thirdpartName")));
            po.set("pri", pri);
        }
        po.set("updateTime", new Date());
        ORMService.getInstance().update(po.toEntity());
    }

    private static void validateSafeValue(String value, String fieldName) {
        if (StringUtils.isBlank(value) || !SAFE_KEY.matcher(value).matches()) {
            throw new SystemRuntimeException(ExceptionType.BUSINESS, fieldName + "格式不正确.");
        }
    }

    private static void validateRequired(Map<String, Object> input, String fieldName) {
        if (StringUtils.isBlank(trimToNull(input.get(fieldName)))) {
            throw new SystemRuntimeException(ExceptionType.BUSINESS, fieldName + "不能为空.");
        }
    }

    private static void setIfPresent(DataPO po, Map<String, Object> input, String fieldName) {
        if (input.containsKey(fieldName)) {
            po.set(fieldName, trimToNull(input.get(fieldName)));
        }
    }

    private static void setRequiredIfPresent(DataPO po, Map<String, Object> input, String fieldName) {
        if (input.containsKey(fieldName)) {
            validateRequired(input, fieldName);
            po.set(fieldName, trimToNull(input.get(fieldName)));
        }
    }

    private static Integer normalizeActiveFlag(Object value) {
        if (value == null) {
            return Integer.valueOf(1);
        }
        if (value instanceof Number) {
            int activeFlag = ((Number) value).intValue();
            if (activeFlag == 1 || activeFlag == 0) {
                return Integer.valueOf(activeFlag);
            }
            throw new SystemRuntimeException(ExceptionType.BUSINESS, "activeFlag格式不正确.");
        }
        String activeFlag = trimToNull(value);
        if ("1".equals(activeFlag)) {
            return Integer.valueOf(1);
        }
        if ("0".equals(activeFlag)) {
            return Integer.valueOf(0);
        }
        throw new SystemRuntimeException(ExceptionType.BUSINESS, "activeFlag格式不正确.");
    }

    static void prepareThirdpartPri(CmPri pri, String thirdpartKey, String thirdpartName) {
        if (pri == null) {
            return;
        }
        validateThirdpartKey(thirdpartKey);
        pri.setCatelogType((Integer) CmPri.Catelog.THIRDPART.getCode());
        pri.setCatelogKey(thirdpartKey);
        pri.setBusiName(thirdpartName);
    }

    private static String trimToNull(Object value) {
        return value == null ? null : StringUtils.trimToNull(String.valueOf(value));
    }
}
