package io.jenkins.plugins.report.jtreg.main.comparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;



public class BuildsTest {

    private static File buildFile;

    @org.junit.BeforeClass
    public static void createChangelogFile() throws IOException {
        File tmpdir = Files.createTempDirectory("reportJtregBuildTestDir").toFile();
        buildFile = new File(tmpdir.getAbsolutePath() + "/build.xml");
        byte[] orig = BuildsTest.class.getResourceAsStream("/io/jenkins/plugins/report/jtreg/main/comparator/dummyNvr1.xml").readAllBytes();
        Files.write(buildFile.toPath(), orig);
    }

    @org.junit.Test
    public void checkForNvrTest() {
        boolean b = Builds.checkForNvr(new File("unused in this call"), "*");
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(buildFile.getParentFile(), "blah");
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(buildFile.getParentFile(), "{blah}");
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(buildFile.getParentFile(), "{blah, bleh}");
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(buildFile.getParentFile(), "java-11-openjdk-11.0.20.0.8-3.el8");
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(buildFile.getParentFile(), "{blah,java-11-openjdk-11.0.20.0.8-3.el8, bleh}");
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(buildFile.getParentFile(), "java-11-openjdk-11.0.20.0.8-3");
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(buildFile.getParentFile(), "java-11-openjdk-11.0.20.0.8-3.*");
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(buildFile.getParentFile(), "{blah,java-11-openjdk-11.0.20.0.8-3.*, bleh}");
        org.junit.Assert.assertTrue(b);
    }
}