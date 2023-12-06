package io.jenkins.plugins.report.jtreg.main.comparator;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class BuildsTest {

    private static File job;

    @org.junit.BeforeClass
    public static void createChangelogFile() {
        job = new File(Objects.requireNonNull(BuildsTest.class.getResource("dummyJob")).getFile());
    }

    @org.junit.Test
    public void checkForNvrTest() {
        Options.Configuration nvrConfig = new Options.Configuration("changelog.xml", "/build/nvr", Options.Locations.Build);

        Options options = new Options();
        options.setSkipFailed(false);
        options.setNumberOfBuilds(2);

        // matches both
        nvrConfig.setValue("");
        options.addConfiguration("nvr", nvrConfig);
        List<File> builds = Builds.getBuilds(job, options);
        org.junit.Assert.assertEquals(2, builds.size());

        nvrConfig.setValue(".*");
        options.addConfiguration("nvr", nvrConfig);
        builds = Builds.getBuilds(job, options);
        org.junit.Assert.assertEquals(2, builds.size());

        nvrConfig.setValue("{blah,.*}");
        options.addConfiguration("nvr", nvrConfig);
        builds = Builds.getBuilds(job, options);
        org.junit.Assert.assertEquals(2, builds.size());


        // matches nothing
        nvrConfig.setValue("blah");
        options.addConfiguration("nvr", nvrConfig);
        builds = Builds.getBuilds(job, options);
        org.junit.Assert.assertEquals(0, builds.size());

        nvrConfig.setValue("{blah}");
        options.addConfiguration("nvr", nvrConfig);
        builds = Builds.getBuilds(job, options);
        org.junit.Assert.assertEquals(0, builds.size());

        nvrConfig.setValue("{blah,bleh}");
        options.addConfiguration("nvr", nvrConfig);
        builds = Builds.getBuilds(job, options);
        org.junit.Assert.assertEquals(0, builds.size());


        // matches first
        nvrConfig.setValue("java-21-openjdk-portable-21\\.0\\.6\\.0\\.10-6\\.el7openjdkportable");
        options.addConfiguration("nvr", nvrConfig);
        builds = Builds.getBuilds(job, options);
        org.junit.Assert.assertEquals(1, builds.size());
        org.junit.Assert.assertEquals("1", builds.get(0).getName());

        nvrConfig.setValue("{java-21-openjdk-portable-21\\.0\\.6\\.0\\.10-6\\.el7openjdkportable}");
        options.addConfiguration("nvr", nvrConfig);
        builds = Builds.getBuilds(job, options);
        org.junit.Assert.assertEquals(1, builds.size());
        org.junit.Assert.assertEquals("1", builds.get(0).getName());

        nvrConfig.setValue("{blah,java-21-openjdk-portable-21\\.0\\.6\\.0\\.10-6\\.el7openjdkportable,bleh}");
        options.addConfiguration("nvr", nvrConfig);
        builds = Builds.getBuilds(job, options);
        org.junit.Assert.assertEquals(1, builds.size());
        org.junit.Assert.assertEquals("1", builds.get(0).getName());

        nvrConfig.setValue("{blah,java-21-.*}");
        options.addConfiguration("nvr", nvrConfig);
        builds = Builds.getBuilds(job, options);
        org.junit.Assert.assertEquals(1, builds.size());
        org.junit.Assert.assertEquals("1", builds.get(0).getName());
    }
}