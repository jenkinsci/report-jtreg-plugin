package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.diff.cmdline.JobsRecognition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;



public class BuildsTest {

    private static File changelogFile;

    @org.junit.BeforeClass
    public static void createChangelogFile() throws IOException {
        File tmpdir = Files.createTempDirectory("reportJtregBuildTestDir").toFile();
        changelogFile = JobsRecognition.creteChangelogFile(tmpdir);
        byte[] orig = BuildsTest.class.getResourceAsStream("/io/jenkins/plugins/report/jtreg/main/comparator/dummyNvr1.xml").readAllBytes();
        Files.write(changelogFile.toPath(), orig);
    }

    @org.junit.Test
    public void checkForNvrTest() {
        boolean b = Builds.checkForNvr(new File("unused in this call"), "*");
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "blah");
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "{blah}");
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "{blah, bleh}");
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "java-17-openjdk-portable-17.0.6.0.10-6.el7openjdkportable");
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "{blah,java-17-openjdk-portable-17.0.6.0.10-6.el7openjdkportable, bleh}");
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "java-17-openjdk-portable-17.0.6.0.10-6");
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "java-17-openjdk-portable-17.0.6.0.10-6.*");
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "{blah,java-17-openjdk-portable-17.0.6.0.10-6.*, bleh}");
        org.junit.Assert.assertTrue(b);



    }
}