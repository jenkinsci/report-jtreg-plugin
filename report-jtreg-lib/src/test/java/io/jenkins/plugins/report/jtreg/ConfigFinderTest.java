package io.jenkins.plugins.report.jtreg;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

public class ConfigFinderTest {

    private static File testXml;
    private static File testJson;

    @org.junit.BeforeClass
    public static void createChangelogFile() throws IOException {
        testXml = new File(Objects.requireNonNull(ConfigFinderTest.class.getResource("config.xml")).getFile());
        testJson = new File(Objects.requireNonNull(ConfigFinderTest.class.getResource("config.json")).getFile());
    }

    @org.junit.Test
    public void findInXmlConfigTest() {
        String value = ConfigFinder.findInConfigStatic(testXml, "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        org.junit.Assert.assertNotNull(value);
        org.junit.Assert.assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);

        String valueCached = ConfigFinder.findInConfigStatic(testXml, "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        org.junit.Assert.assertNotNull(valueCached);
        org.junit.Assert.assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", valueCached);

        String valueNull = ConfigFinder.findInConfigStatic(new File("null.xml"), "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        org.junit.Assert.assertNull(valueNull);
    }

    @org.junit.Test
    public void findInJsonConfigTest() {
        String value = ConfigFinder.findInConfigStatic(testJson, "nvr", "$.build.actions.hudson-plugins-scm-koji-KojiEnvVarsAction.nvr");
        org.junit.Assert.assertNotNull(value);
        org.junit.Assert.assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);
    }
}
