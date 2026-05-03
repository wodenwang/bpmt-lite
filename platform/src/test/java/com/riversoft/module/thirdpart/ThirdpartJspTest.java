package com.riversoft.module.thirdpart;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class ThirdpartJspTest {

    @Test
    public void managementPageUsesThirdpartNameAndSelectFilter() throws Exception {
        String jsp = readWebapp("xhtml/thirdpart/ThirdpartAction/main.jsp");

        assertTrue(jsp.contains("<div title=\"第三方系统\">"));
        assertTrue(jsp.contains("<th>是否启用</th>"));
        assertTrue(jsp.contains("name=\"_ne_activeFlag\""));
        assertTrue(jsp.contains("cmd=\"select[YES_NO(全部)]\""));
        assertFalse(jsp.contains("外部系统管理"));
        assertFalse(jsp.contains("<th>启用状态</th>"));
    }

    @Test
    public void toggleActiveUsesBpmtConfirmControl() throws Exception {
        String jsp = readWebapp("xhtml/thirdpart/ThirdpartAction/main.jsp");

        assertTrue(jsp.contains("Ui.confirm('确认启用第三方系统[' + key + ']?', function()"));
        assertTrue(jsp.contains("Ui.confirm('确认停用第三方系统[' + key + ']?', function()"));
    }

    @Test
    public void priGroupFormIncludesThirdpartPermissionTab() throws Exception {
        String jsp = readWebapp("xhtml/manager/pri/PriGroupAction/form.jsp");

        assertTrue(jsp.contains("title=\"第三方系统权限\""));
        assertTrue(jsp.contains("thirdpartPri.shtml?groupId=${vo.groupId}"));
    }

    @Test
    public void priMenuUsesDomLookupForPermissionIdsWithDots() throws Exception {
        String jsp = readWebapp("xhtml/manager/pri/PriGroupAction/menu_pri.jsp");

        assertTrue(jsp.contains("document.getElementById('${_zone}_pri_' + treeNode.pri.priKey)"));
        assertFalse(jsp.contains("$('#${_zone}_pri_' + treeNode.pri.priKey)"));
    }

    private String readWebapp(String relativePath) throws Exception {
        return new String(Files.readAllBytes(Paths.get("src/main/webapp", relativePath)), StandardCharsets.UTF_8);
    }
}
