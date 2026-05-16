package com.riversoft.platform;

import static org.junit.Assert.assertEquals;

import java.util.jar.Attributes;

import org.junit.Test;

public class PlatformVersionTest {

	@Test
	public void resolvesVersionFromManifestImplementationVersion() {
		Attributes attributes = new Attributes();
		attributes.putValue("Implementation-Version", "1.7.2");

		assertEquals("1.7.2", Platform.resolvePlatformVersion(attributes));
	}

	@Test
	public void fallsBackToSystemPropertyWhenManifestVersionMissing() {
		String previous = System.getProperty("bpmt.version");
		System.setProperty("bpmt.version", "1.7.2-test");
		try {
			assertEquals("1.7.2-test", Platform.resolvePlatformVersion(new Attributes()));
		} finally {
			if (previous == null) {
				System.clearProperty("bpmt.version");
			} else {
				System.setProperty("bpmt.version", previous);
			}
		}
	}
}
