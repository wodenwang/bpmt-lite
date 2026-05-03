package com.riversoft.module.thirdpart;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ThirdpartServiceTest {

    @Test
    public void matchesConfiguredRedirectUriExactly() {
        assertTrue(ThirdpartService.isAllowedRedirectUri("http://127.0.0.1/demo/callback",
                "http://127.0.0.1/demo/callback"));
        assertTrue(ThirdpartService.isAllowedRedirectUri("http://a/cb\nhttp://b/cb", "http://b/cb"));
        assertFalse(ThirdpartService.isAllowedRedirectUri("http://a/cb", " http://a/cb"));
        assertFalse(ThirdpartService.isAllowedRedirectUri("http://a/cb", "http://a/cb "));
        assertFalse(ThirdpartService.isAllowedRedirectUri("http://a/cb", "http://a/cb/extra"));
        assertFalse(ThirdpartService.isAllowedRedirectUri("http://a/cb", "javascript:alert(1)"));
    }

    @Test
    public void hashesAndMatchesClientSecret() {
        assertTrue(ThirdpartService.matchesSecret(ThirdpartService.hashSecret("secret"), "secret"));
    }
}
