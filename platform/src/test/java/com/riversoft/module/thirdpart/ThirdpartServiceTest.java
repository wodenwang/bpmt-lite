package com.riversoft.module.thirdpart;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.riversoft.platform.po.CmPri;

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

    @Test
    public void preparesThirdpartPermissionCatalog() {
        CmPri pri = new CmPri();

        ThirdpartService.prepareThirdpartPri(pri, "demo-app", "演示系统");

        assertEquals(CmPri.Catelog.THIRDPART.getCode(), pri.getCatelogType());
        assertEquals("demo-app", pri.getCatelogKey());
        assertEquals("演示系统", pri.getBusiName());
    }
}
