package io.jenkins.plugins.report.jtreg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigFinderDirEscapeTest {
    private static File tmpDir = null;
    private static File parentDir = null;
    private static File childDir = null;
    private static File topFile = null;
    private static File parentFile = null;
    private static File childFile = null;

    @org.junit.BeforeClass
    public static void createContext() {
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
    }

    @org.junit.Test
    public void testCorrectFileHierarchy() {
        // correct, should not throw any exceptions:
        ConfigFinder.checkIfConfigIsInParent(tmpDir, topFile);
        ConfigFinder.checkIfConfigIsInParent(tmpDir, parentFile);
        ConfigFinder.checkIfConfigIsInParent(tmpDir, childFile);
        ConfigFinder.checkIfConfigIsInParent(parentDir, parentFile);
        ConfigFinder.checkIfConfigIsInParent(parentDir, childFile);
    }

    @org.junit.Test(expected = RuntimeException.class)
    public void testGivingFileAsParentDir() {
        ConfigFinder.checkIfConfigIsInParent(topFile, childFile);
    }

    @org.junit.Test(expected = RuntimeException.class)
    public void testGivingInvalidParentDirectory() {
        ConfigFinder.checkIfConfigIsInParent(new File(tmpDir, "something"), childFile);
    }

    @org.junit.Test(expected = RuntimeException.class)
    public void testForEscapingParentDir1() {
        ConfigFinder.checkIfConfigIsInParent(childDir, parentFile);
    }

    @org.junit.Test(expected = RuntimeException.class)
    public void testForEscapingParentDir2() {
        ConfigFinder.checkIfConfigIsInParent(childDir, topFile);
    }

    @org.junit.Test(expected = RuntimeException.class)
    public void testForEscapingParentDir3() {
        ConfigFinder.checkIfConfigIsInParent(parentDir, topFile);
    }
}
