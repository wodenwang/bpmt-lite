package com.riversoft.platform.po;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CmPriTest {

    @Test
    public void thirdpartPermissionCatalogIsAvailable() {
        assertEquals(Integer.valueOf(4), CmPri.Catelog.THIRDPART.getCode());
        assertEquals("第三方系统权限", CmPri.Catelog.THIRDPART.getShowName());
        assertEquals(CmPri.Catelog.THIRDPART, CmPri.Catelog.fromCode(4));
    }
}
