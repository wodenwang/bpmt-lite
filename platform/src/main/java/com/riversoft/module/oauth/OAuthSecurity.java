package com.riversoft.module.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

public final class OAuthSecurity {
    private static final SecureRandom RANDOM = new SecureRandom();

    private OAuthSecurity() {
    }

    public static String generateOpaqueValue() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Hex.encodeHexString(bytes);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Hex.encodeHexString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 is unavailable.", e);
        }
    }

    public static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        byte[] a = left.getBytes(StandardCharsets.UTF_8);
        byte[] b = right.getBytes(StandardCharsets.UTF_8);
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    public static String redact(String value) {
        return StringUtils.isEmpty(value) ? "" : "[REDACTED]";
    }
}
