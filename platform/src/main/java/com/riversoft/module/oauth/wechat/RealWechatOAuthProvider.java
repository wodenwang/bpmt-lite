package com.riversoft.module.oauth.wechat;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.riversoft.core.Config;
import com.riversoft.core.db.ORMService;
import com.riversoft.module.thirdpart.ThirdpartService;
import com.riversoft.platform.SessionManager;
import com.riversoft.platform.SessionManager.SessionAttributeKey;
import com.riversoft.platform.po.UsUser;
import com.riversoft.platform.web.WxActionAspect;
import com.riversoft.weixin.mp.oauth2.MpOAuth2s;
import com.riversoft.weixin.qy.base.CorpSetting;
import com.riversoft.weixin.qy.oauth2.QyOAuth2s;
import com.riversoft.weixin.qy.oauth2.bean.QyUser;
import com.riversoft.wx.mp.MpAppSetting;
import com.riversoft.wx.mp.service.MpAppService;

public class RealWechatOAuthProvider implements WechatOAuthProvider {

    public String buildAuthorizationUrl(String wechatType, String wechatKey, String wechatScope, String callbackUrl) {
        if (ThirdpartService.WECHAT_TYPE_AGENT.equals(wechatType)) {
            AgentOAuthConfig agentConfig = loadAgentConfig(wechatKey);
            return QyOAuth2s.with(agentConfig.toCorpSetting()).authenticationUrl(callbackUrl, null);
        }
        if (ThirdpartService.WECHAT_TYPE_MP.equals(wechatType)) {
            Map<String, Object> mpConfig = loadMpConfig(wechatKey);
            MpAppSetting setting = MpAppService.getInstance().getAppSetting(mpConfig);
            if (setting == null) {
                throw new OAuthWechatConfigException("WxMp配置无效.");
            }
            String scope = StringUtils.isBlank(wechatScope) ? ThirdpartService.WECHAT_SCOPE_BASE : wechatScope;
            return MpOAuth2s.with(setting).authenticationUrl(callbackUrl, scope);
        }
        throw new IllegalArgumentException("unsupported wechatType: " + wechatType);
    }

    public String loginByCode(HttpServletRequest request, String wechatType, String wechatKey, String wechatScope,
            String code) {
        if (ThirdpartService.WECHAT_TYPE_AGENT.equals(wechatType)) {
            AgentOAuthConfig agentConfig = loadAgentConfig(wechatKey);
            QyUser qyUser = QyOAuth2s.with(agentConfig.toCorpSetting()).userInfo(code);
            String userId = qyUser.getUserId();
            try {
                SessionManager.doUserLogin(request, userId);
            } catch (RuntimeException e) {
                throw OAuthWechatLoginFailureClassifier.classify(userId, e);
            }
            return userId;
        }
        if (ThirdpartService.WECHAT_TYPE_MP.equals(wechatType)) {
            loadMpConfig(wechatKey);
            new WxActionAspect().mpCodeLogin(request, wechatKey, code);
            UsUser user = userFromSession(request);
            if (user == null || StringUtils.isBlank(user.getUid())) {
                throw new IllegalStateException("微信服务号登录未建立BPMT用户会话.");
            }
            return user.getUid();
        }
        throw new IllegalArgumentException("unsupported wechatType: " + wechatType);
    }

    static UsUser userFromSession(HttpServletRequest request) {
        Object user = request.getSession().getAttribute(SessionAttributeKey.USER.toString());
        return user instanceof UsUser ? (UsUser) user : null;
    }

    @SuppressWarnings("unchecked")
    private AgentOAuthConfig loadAgentConfig(String wechatKey) {
        Object agentConfig = ORMService.getInstance().findByPk("WxAgent", wechatKey);
        if (!(agentConfig instanceof Map)) {
            throw new OAuthWechatConfigException("WxAgent配置不存在.");
        }
        return validateAgentConfig((Map<String, Object>) agentConfig, Config.get("wx.qy.corpId"),
                Config.get("wx.qy.corpSecret"));
    }

    static AgentOAuthConfig validateAgentConfig(Map<String, Object> config, String defaultCorpId,
            String defaultCorpSecret) {
        String agentKey = configValue(config, "agentKey");
        if (StringUtils.isBlank(agentKey)) {
            throw new OAuthWechatConfigException("WxAgent配置不完整: agentKey不能为空.");
        }
        String agentId = configValue(config, "agentId");
        if (StringUtils.isBlank(agentId)) {
            throw new OAuthWechatConfigException("WxAgent配置不完整: agentId不能为空.");
        }
        String corpId = StringUtils.trimToNull(defaultCorpId);
        if (StringUtils.isBlank(corpId)) {
            throw new OAuthWechatConfigException("WxAgent配置不完整: wx.qy.corpId不能为空.");
        }
        String secret = StringUtils.trimToNull(configValue(config, "agentSecret"));
        if (StringUtils.isBlank(secret)) {
            secret = StringUtils.trimToNull(defaultCorpSecret);
        }
        if (StringUtils.isBlank(secret)) {
            throw new OAuthWechatConfigException("WxAgent配置不完整: agentSecret不能为空.");
        }
        return new AgentOAuthConfig(agentKey, agentId, corpId, secret);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadMpConfig(String wechatKey) {
        Object mpConfig = ORMService.getInstance().findByPk("WxMp", wechatKey);
        if (!(mpConfig instanceof Map)) {
            throw new OAuthWechatConfigException("WxMp配置不存在.");
        }
        Map<String, Object> config = (Map<String, Object>) mpConfig;
        validateMpConfig(config);
        return config;
    }

    static void validateMpConfig(Map<String, Object> config) {
        requireMpConfigValue(config, "mpKey");
        requireMpConfigValue(config, "appId");
        requireMpConfigValue(config, "appSecret");
        requireMpConfigValue(config, "visitorTable");
    }

    private static void requireMpConfigValue(Map<String, Object> config, String key) {
        if (StringUtils.isBlank(configValue(config, key))) {
            throw new OAuthWechatConfigException("WxMp配置不完整: " + key + "不能为空.");
        }
    }

    private static String configValue(Map<String, Object> config, String key) {
        Object value = config == null ? null : config.get(key);
        return value == null ? null : StringUtils.trimToNull(String.valueOf(value));
    }

    static class AgentOAuthConfig {
        private final String agentKey;
        private final String agentId;
        private final String corpId;
        private final String secret;

        AgentOAuthConfig(String agentKey, String agentId, String corpId, String secret) {
            this.agentKey = agentKey;
            this.agentId = agentId;
            this.corpId = corpId;
            this.secret = secret;
        }

        CorpSetting toCorpSetting() {
            return new CorpSetting(corpId, secret);
        }

        String getAgentKey() {
            return agentKey;
        }

        String getAgentId() {
            return agentId;
        }

        String getCorpId() {
            return corpId;
        }

        String getSecret() {
            return secret;
        }
    }
}
