package com.riversoft.module.frame;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class LoginMaskCleanupTest {

    @Test
    public void coreClearsOrphanPageMasksOnRootPageLoad() throws Exception {
        String core = read("src/main/webapp/js/ws-core.js");

        assertTrue(core.contains("clearPageMask : function()"));
        assertTrue(core.contains("$('#loading').hide();"));
        assertTrue(core.contains("$('.ui-widget-overlay').remove();"));
        assertTrue(core.contains("$('body').removeClass('ui-dialog-open');"));
        assertTrue(core.contains("$('.am-dimmer').removeClass('am-active').hide();"));
        assertTrue(core.contains("$(window).bind('load pageshow'"));
    }

    @Test
    public void fullPageFooterInvokesCoreMaskCleanup() throws Exception {
        String footer = read("src/main/webapp/include/html_bottom.jsp");

        assertTrue(footer.contains("Core.clearPageMask();"));
        assertTrue(footer.contains("document.write('<style>#loading{display:none}<\\/style>');"));
    }

    private static String read(String relativePath) throws Exception {
        return new String(Files.readAllBytes(Paths.get(relativePath)), StandardCharsets.UTF_8);
    }
}
