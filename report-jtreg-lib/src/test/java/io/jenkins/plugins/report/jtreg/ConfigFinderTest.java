package io.jenkins.plugins.report.jtreg;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

public class ConfigFinderTest {

    private static File testXml;
    private static File testJson;
    private static File testProperties;

    @org.junit.BeforeClass
    public static void createChangelogFile() {
        testXml = new File(Objects.requireNonNull(ConfigFinderTest.class.getResource("config.xml")).getFile());
        testJson = new File(Objects.requireNonNull(ConfigFinderTest.class.getResource("config.json")).getFile());
        testProperties = new File(Objects.requireNonNull(ConfigFinderTest.class.getResource("config.properties")).getFile());
    }

    @org.junit.Test
    public void findInXmlConfigTest() {
        String value = ConfigFinder.findInConfigStatic(testXml, "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        org.junit.Assert.assertNotNull(value);
        org.junit.Assert.assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);

        // test caching of values
        String valueCached = ConfigFinder.findInConfigStatic(testXml, "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        org.junit.Assert.assertNotNull(valueCached);
        org.junit.Assert.assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", valueCached);

        String nonExistingValue = ConfigFinder.findInConfigStatic(testXml, "none", "/none");
        org.junit.Assert.assertNull(nonExistingValue);

        String wrongFile = ConfigFinder.findInConfigStatic(new File("null.xml"), "nvr", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        org.junit.Assert.assertNull(wrongFile);
    }

    @org.junit.Test
    public void findInJsonConfigTest() {
        String value = ConfigFinder.findInConfigStatic(testJson, "nvr", "$.build.actions.hudson-plugins-scm-koji-KojiRevisionState.build.rpms.hudson-plugins-scm-koji-model-RPM[0].nvr");
        org.junit.Assert.assertNotNull(value);
        org.junit.Assert.assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);

        String nonExistingValue = ConfigFinder.findInConfigStatic(testJson, "none", "build.none");
        org.junit.Assert.assertNull(nonExistingValue);

        String nonString = ConfigFinder.findInConfigStatic(testJson, "non-string", "build.actions");
        org.junit.Assert.assertNull(nonString);

        String nullArray = ConfigFinder.findInConfigStatic(testJson, "null-array", "null[0]");
        org.junit.Assert.assertNull(nullArray);

        String nonArray = ConfigFinder.findInConfigStatic(testJson, "non-array", "build[0]");
        org.junit.Assert.assertNull(nonArray);

        String wrongFile = ConfigFinder.findInConfigStatic(new File("null.json"), "nvr", "$.nvr");
        org.junit.Assert.assertNull(wrongFile);
    }

    @org.junit.Test
    public void findInPropertiesConfigTest() {
        String value = ConfigFinder.findInConfigStatic(testProperties, "nvr", "nvr");
        org.junit.Assert.assertNotNull(value);
        org.junit.Assert.assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);

        String nonExistingValue = ConfigFinder.findInConfigStatic(testProperties, "none", "none");
        org.junit.Assert.assertNull(nonExistingValue);

        String wrongFile = ConfigFinder.findInConfigStatic(new File("null.properties"), "nvr", "nvr");
        org.junit.Assert.assertNull(wrongFile);
    }

    @org.junit.Test
    public void createObjectTest() {
        ConfigFinder cf = new ConfigFinder(testXml, "nvr-again", "/build/actions/hudson.plugins.scm.koji.KojiRevisionState/build/nvr");
        String value = cf.findInConfig();

        org.junit.Assert.assertNotNull(value);
        org.junit.Assert.assertEquals("java-11-openjdk-11.0.20.0.8-3.el8", value);
    }

    @org.junit.Test
    public void checkDirectoryEscapingTest() {
        File tmpDir = null;
        File parentDir = null;
        File childDir = null;
        File topFile = null;
        File parentFile = null;
        File childFile = null;

        try {
            tmpDir = Files.createTempDirectory("tmpDirPrefix").toFile();
            parentDir = new File(tmpDir, "parent");
            childDir = new File(parentDir, "child");
            parentDir.mkdir();
            childDir.mkdir();

            topFile = new File(tmpDir, "topFile.json");
            parentFile = new File(parentDir, "parentFile.json");
            childFile = new File(childDir, "childFile.json");
            topFile.createNewFile();
            parentFile.createNewFile();
            childFile.createNewFile();
        } catch (IOException e) {
            org.junit.Assert.fail("Exception when creating test's temp directories or files.");
        }

        // correct, should not throw any exceptions:
        ConfigFinder.checkIfConfigIsInParent(tmpDir, topFile);
        ConfigFinder.checkIfConfigIsInParent(tmpDir, parentFile);
        ConfigFinder.checkIfConfigIsInParent(tmpDir, childFile);
        ConfigFinder.checkIfConfigIsInParent(parentDir, parentFile);
        ConfigFinder.checkIfConfigIsInParent(parentDir, childFile);

        // invalid parent directory, should throw:
        try {
            ConfigFinder.checkIfConfigIsInParent(topFile, childFile);
            org.junit.Assert.fail("Should have thrown an exception");
        } catch (RuntimeException e) {
            // expected
        }
        try {
            ConfigFinder.checkIfConfigIsInParent(new File(tmpDir, "something"), childFile);
            org.junit.Assert.fail("Should have thrown an exception");
        } catch (RuntimeException e) {
            // expected
        }

        // escaping from parent directory, should throw:
        try {
            ConfigFinder.checkIfConfigIsInParent(childDir, parentFile);
            org.junit.Assert.fail("Should have thrown an exception");
        } catch (RuntimeException e) {
            // expected
        }
        try {
            ConfigFinder.checkIfConfigIsInParent(childDir, topFile);
            org.junit.Assert.fail("Should have thrown an exception");
        } catch (RuntimeException e) {
            // expected
        }
        try {
            ConfigFinder.checkIfConfigIsInParent(parentDir, topFile);
            org.junit.Assert.fail("Should have thrown an exception");
        } catch (RuntimeException e) {
            // expected
        }
    }
}
