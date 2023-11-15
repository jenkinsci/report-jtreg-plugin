package io.jenkins.plugins.report.jtreg.main.comparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class BuildsTest {

    private static File changelogFile;

    @org.junit.BeforeClass
    public static void createChangelogFile() throws IOException {
        File tmpdir = Files.createTempDirectory("reportJtregBuildTestDir").toFile();
        changelogFile = new File(tmpdir, "changelog.xml");
        byte[] orig = BuildsTest.class.getResourceAsStream("/io/jenkins/plugins/report/jtreg/main/comparator/dummyNvr1.xml").readAllBytes();
        Files.write(changelogFile.toPath(), orig);
    }

    @org.junit.Test
    public void checkForNvrTest() {
        Options.Configuration nvrConfig = new Options.Configuration(changelogFile.getName(), "/build/nvr");
        boolean b = Builds.checkForNvr(new File("unused in this call"), "*", nvrConfig);
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "blah", nvrConfig);
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "{blah}", nvrConfig);
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "{blah, bleh}", nvrConfig);
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "java-17-openjdk-portable-17.0.6.0.10-6.el7openjdkportable", nvrConfig);
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "{blah,java-17-openjdk-portable-17.0.6.0.10-6.el7openjdkportable, bleh}", nvrConfig);
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "java-17-openjdk-portable-17.0.6.0.10-6", nvrConfig);
        org.junit.Assert.assertFalse(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "java-17-openjdk-portable-17.0.6.0.10-6.*", nvrConfig);
        org.junit.Assert.assertTrue(b);
        b = Builds.checkForNvr(changelogFile.getParentFile(), "{blah,java-17-openjdk-portable-17.0.6.0.10-6.*, bleh}", nvrConfig);
        org.junit.Assert.assertTrue(b);
    }
}