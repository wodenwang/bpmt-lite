package com.riversoft.module.oauth.wechat;

public class OAuthWechatLoginException extends RuntimeException {
    private final String reason;
    private final String safeMessage;
    private final String userId;

    public OAuthWechatLoginException(String reason, String safeMessage, String userId, Throwable cause) {
        super(safeMessage, cause);
        this.reason = reason;
        this.safeMessage = safeMessage;
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public String getSafeMessage() {
        return safeMessage;
    }

    public String getUserId() {
        return userId;
    }
}
