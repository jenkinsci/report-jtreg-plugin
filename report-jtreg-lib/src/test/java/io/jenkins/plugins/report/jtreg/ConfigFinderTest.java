package io.jenkins.plugins.report.jtreg;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigFinderTest {

    private static File testXml;
    private static File testJson;
    private static File testProperties;

    @BeforeAll
    static void beforeAll() {
        testXml = new File(Objects.requireNonNull(ConfigFinderTest.class.getResource("config.xml")).getFile());
        testJson = new File(Objects.requireNonNull(ConfigFinderTest.class.getResource("config.json")).getFile());
        testProperties = new File(Objects.requireNonNull(ConfigFinderTest.class.getResource("config.properties")).getFile());
    }

    @Test
    void findInXmlConfigTest() {
        String value = ConfigFinder.findInConfigStatic(testXml, "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        assertNotNull(value);
        assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);

        // test caching of values
        String valueCached = ConfigFinder.findInConfigStatic(testXml, "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        assertNotNull(valueCached);
        assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", valueCached);

        String nonExistingValue = ConfigFinder.findInConfigStatic(testXml, "none", "/none");
        assertNull(nonExistingValue);

        String wrongFile = ConfigFinder.findInConfigStatic(new File("null.xml"), "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        assertNull(wrongFile);
    }

    @Test
    void findInJsonConfigTest() {
        String value = ConfigFinder.findInConfigStatic(testJson, "nvr", "$.build.actions.hudson-plugins-scm-koji-KojiRevisionState.build.rpms.hudson-plugins-scm-koji-model-RPM[0].nvr");
        assertNotNull(value);
        assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);

        String nonExistingValue = ConfigFinder.findInConfigStatic(testJson, "none", "build.none");
        assertNull(nonExistingValue);

        String nonString = ConfigFinder.findInConfigStatic(testJson, "non-string", "build.actions");
        assertNull(nonString);

        String nullArray = ConfigFinder.findInConfigStatic(testJson, "null-array", "null[0]");
        assertNull(nullArray);

        String nonArray = ConfigFinder.findInConfigStatic(testJson, "non-array", "build[0]");
        assertNull(nonArray);

        String wrongFile = ConfigFinder.findInConfigStatic(new File("null.json"), "nvr", "$.nvr");
        assertNull(wrongFile);
    }

    @Test
    void findInPropertiesConfigTest() {
        String value = ConfigFinder.findInConfigStatic(testProperties, "nvr", "nvr");
        assertNotNull(value);
        assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);

        String nonExistingValue = ConfigFinder.findInConfigStatic(testProperties, "none", "none");
        assertNull(nonExistingValue);

        String wrongFile = ConfigFinder.findInConfigStatic(new File("null.properties"), "nvr", "nvr");
        assertNull(wrongFile);
    }

    @Test
    void createObjectTest() {
        ConfigFinder cf = new ConfigFinder(testXml, "nvr-again", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        String value = cf.findInConfig();

        assertNotNull(value);
        assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);
    }
}
