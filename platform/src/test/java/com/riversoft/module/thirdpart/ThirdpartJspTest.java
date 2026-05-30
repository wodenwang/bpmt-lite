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

    @Test
    public void thirdpartFormContainsWechatLoginSettings() throws Exception {
        String jsp = readWebapp("xhtml/thirdpart/ThirdpartAction/form.jsp");

        assertTrue(jsp.contains("<th colspan=\"2\">微信登录</th>"));
        assertTrue(jsp.contains("name=\"wechatLoginEnabled\""));
        assertTrue(jsp.contains("name=\"wechatType\" cmd=\"select[@com.riversoft.module.thirdpart.ThirdpartWechatType(请选择);null;240px]\""));
        assertTrue(jsp.contains("name=\"wechatKey\""));
        assertTrue(jsp.contains("name=\"wechatScope\" cmd=\"select[@com.riversoft.module.thirdpart.ThirdpartWechatScope;240px]\""));
        assertTrue(jsp.contains("<th colspan=\"2\">访问控制</th>"));
        assertFalse(jsp.contains("<select name=\"wechatType\">"));
        assertFalse(jsp.contains("<select name=\"wechatScope\">"));
    }

    @Test
    public void thirdpartListShowsWechatLoginSummary() throws Exception {
        String jsp = readWebapp("xhtml/thirdpart/ThirdpartAction/list.jsp");

        assertTrue(jsp.contains("<th field=\"wechatType\">微信登录</th>"));
        assertTrue(jsp.contains("企业号: ${vo.wechatKey}"));
        assertTrue(jsp.contains("服务号: ${vo.wechatKey}"));
        assertTrue(jsp.contains("关闭"));
        assertFalse(jsp.contains("clientSecretHash"));
    }

    @Test
    public void thirdpartListContainsAiOnboardingPromptDialog() throws Exception {
        String jsp = readWebapp("xhtml/thirdpart/ThirdpartAction/list.jsp");

        assertTrue(jsp.contains("name=\"aiPrompt\""));
        assertTrue(jsp.contains("AI 接入提示词"));
        assertTrue(jsp.contains("bpmt-ai-prompt-dialog"));
        assertTrue(jsp.contains("BPMT OAuth Authorization Code"));
        assertTrue(jsp.contains("CLIENT_SECRET_PLACEHOLDER"));
        assertTrue(jsp.contains("BPMT_API_APP_KEY"));
        assertTrue(jsp.contains("BPMT_API_APP_SECRET"));
        assertTrue(jsp.contains("AGENTS.md"));
        assertTrue(jsp.contains("CLAUDE.md"));
        assertFalse(jsp.contains("clientSecretHash"));
    }

    private String readWebapp(String relativePath) throws Exception {
        return new String(Files.readAllBytes(Paths.get("src/main/webapp", relativePath)), StandardCharsets.UTF_8);
    }
}
