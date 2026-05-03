package com.riversoft.module.oauth;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;

public class OAuthHbmMappingTest {
    @Test
    public void thirdpartMappingsExistOnClasspath() {
        assertNotNull(resource("hbm/common/CM_THIRDPART.hbm.xml"));
        assertNotNull(resource("hbm/common/CM_THIRDPART_AUTH_CODE.hbm.xml"));
        assertNotNull(resource("hbm/common/CM_THIRDPART_ACCESS_TOKEN.hbm.xml"));
    }

    private InputStream resource(String name) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }
}
