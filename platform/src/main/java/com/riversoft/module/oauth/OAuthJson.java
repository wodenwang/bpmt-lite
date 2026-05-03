package com.riversoft.module.oauth;

import java.util.HashMap;
import java.util.Map;

import com.riversoft.platform.po.UsGroup;
import com.riversoft.platform.po.UsRole;
import com.riversoft.platform.po.UsUser;

public final class OAuthJson {

    private OAuthJson() {
    }

    public static Map<String, Object> token(String accessToken, int expiresIn, String userid) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("access_token", accessToken);
        result.put("token_type", "Bearer");
        result.put("expires_in", Integer.valueOf(expiresIn));
        result.put("userid", userid);
        return result;
    }

    public static Map<String, Object> error(String code, String description) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("error", code);
        result.put("error_description", description);
        return result;
    }

    public static Map<String, Object> userinfo(UsUser user, UsGroup group, UsRole role) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (user != null) {
            result.put("userid", user.getUid());
            result.put("name", user.getBusiName());
        }
        if (group != null) {
            Map<String, Object> groupInfo = new HashMap<String, Object>();
            groupInfo.put("groupKey", group.getGroupKey());
            groupInfo.put("name", group.getBusiName());
            result.put("group", groupInfo);
        }
        if (role != null) {
            Map<String, Object> roleInfo = new HashMap<String, Object>();
            roleInfo.put("roleKey", role.getRoleKey());
            roleInfo.put("name", role.getBusiName());
            result.put("role", roleInfo);
        }
        return result;
    }
}
