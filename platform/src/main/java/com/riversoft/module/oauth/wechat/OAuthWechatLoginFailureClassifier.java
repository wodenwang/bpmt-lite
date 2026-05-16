package com.riversoft.module.oauth.wechat;

import org.apache.commons.lang3.StringUtils;

final class OAuthWechatLoginFailureClassifier {
    private OAuthWechatLoginFailureClassifier() {
    }

    static OAuthWechatLoginException classify(String userId, RuntimeException cause) {
        String uid = StringUtils.trimToEmpty(userId);
        String message = cause == null ? "" : StringUtils.trimToEmpty(cause.getMessage());
        if (message.contains("系统维护中,暂停用户登陆")) {
            return failure("bpmt_login_paused", uid, "微信授权已成功，但 BPMT 当前处于维护/暂停模式，用户[" + uid
                    + "]无法建立登录态。请管理员检查 safe.role 或 safe.admin 配置。", cause);
        }
        if (message.contains("找不到用户[")) {
            return failure("bpmt_user_not_found", uid, "微信授权已成功，但 BPMT 中找不到用户[" + uid
                    + "]。请管理员检查企业微信 UserId 与 BPMT 用户账号映射。", cause);
        }
        if (message.contains("账号已失效")) {
            return failure("bpmt_user_disabled", uid, "微信授权已成功，但 BPMT 用户[" + uid
                    + "]账号已失效。请管理员启用用户或更换绑定账号。", cause);
        }
        if (message.contains("当前网络环境不安全")) {
            return failure("bpmt_user_ip_denied", uid, "微信授权已成功，但 BPMT 拒绝了用户[" + uid
                    + "]当前网络环境。请管理员检查用户 IP 白名单或上游代理地址。", cause);
        }
        if (message.contains("归属的组织") || message.contains("归属的角色") || message.contains("组织与角色")) {
            return failure("bpmt_user_relationship_invalid", uid, "微信授权已成功，但 BPMT 用户[" + uid
                    + "]的组织或角色关系不可用。请管理员检查用户所属组织、角色和权限组。", cause);
        }
        return failure("bpmt_login_failed", uid, "微信授权已成功，但 BPMT 本地登录态建立失败。请联系管理员并提供 Request ID。", cause);
    }

    private static OAuthWechatLoginException failure(String reason, String userId, String safeMessage,
            RuntimeException cause) {
        return new OAuthWechatLoginException(reason, safeMessage, userId, cause);
    }
}
