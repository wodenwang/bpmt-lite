package com.riversoft.module.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OAuthSecurityTest {
    @Test
    public void sha256HexHashesKnownValue() {
        assertEquals("2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b",
                OAuthSecurity.sha256Hex("secret"));
    }

    @Test
    public void generatedSecretIsLongEnough() {
        assertTrue(OAuthSecurity.generateOpaqueValue().length() >= 32);
    }

    @Test
    public void redactsSensitiveValues() {
        assertEquals("[REDACTED]", OAuthSecurity.redact("abc"));
        assertEquals("", OAuthSecurity.redact(""));
    }
}
