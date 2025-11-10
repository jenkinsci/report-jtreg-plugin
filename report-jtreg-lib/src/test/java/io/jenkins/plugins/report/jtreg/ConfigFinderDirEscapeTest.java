package io.jenkins.plugins.report.jtreg;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigFinderDirEscapeTest {

    private static File tmpDir = null;
    private static File parentDir = null;
    private static File childDir = null;
    private static File topFile = null;
    private static File parentFile = null;
    private static File childFile = null;

    @BeforeAll
    static void beforeAll() throws Exception {
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
    }

    @Test
    void testCorrectFileHierarchy() {
        // correct, should not throw any exceptions:
        ConfigFinder.checkIfConfigIsInParent(tmpDir, topFile);
        ConfigFinder.checkIfConfigIsInParent(tmpDir, parentFile);
        ConfigFinder.checkIfConfigIsInParent(tmpDir, childFile);
        ConfigFinder.checkIfConfigIsInParent(parentDir, parentFile);
        ConfigFinder.checkIfConfigIsInParent(parentDir, childFile);
    }

    @Test
    void testGivingFileAsParentDir() {
        assertThrows(RuntimeException.class, () ->
            ConfigFinder.checkIfConfigIsInParent(topFile, childFile));
    }

    @Test
    void testGivingInvalidParentDirectory() {
        assertThrows(RuntimeException.class, () ->
            ConfigFinder.checkIfConfigIsInParent(new File(tmpDir, "something"), childFile));
    }

    @Test
    void testForEscapingParentDir1() {
        assertThrows(RuntimeException.class, () ->
            ConfigFinder.checkIfConfigIsInParent(childDir, parentFile));
    }

    @Test
    void testForEscapingParentDir2() {
        assertThrows(RuntimeException.class, () ->
            ConfigFinder.checkIfConfigIsInParent(childDir, topFile));
    }

    @Test
    void testForEscapingParentDir3() {
        assertThrows(RuntimeException.class, () ->
            ConfigFinder.checkIfConfigIsInParent(parentDir, topFile));
    }
}
