package io.jenkins.plugins.report.jtreg;

import java.io.*;
import java.nio.file.Files;

public class ConfigFinderTest {

    private static File testConfig;

    @org.junit.BeforeClass
    public static void createChangelogFile() throws IOException {
        testConfig = new File("config.xml");
        byte[] bytes = ConfigFinderTest.class.getResourceAsStream("/io/jenkins/plugins/report/jtreg/config.xml").readAllBytes();
        Files.write(testConfig.toPath(), bytes);
    }

    @org.junit.Test
    public void findInConfigTest() {
        String value = ConfigFinder.findInConfigStatic(testConfig, "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        org.junit.Assert.assertNotNull(value);
        org.junit.Assert.assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);

        String valueCached = ConfigFinder.findInConfigStatic(testConfig, "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        org.junit.Assert.assertNotNull(valueCached);
        org.junit.Assert.assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", valueCached);

        String valueNull = ConfigFinder.findInConfigStatic(new File("null.xml"), "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        org.junit.Assert.assertNull(valueNull);
    }
}
