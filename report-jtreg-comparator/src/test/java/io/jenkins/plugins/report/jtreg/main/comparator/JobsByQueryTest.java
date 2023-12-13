package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByQuery;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.DirListing;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.ListDirListing;
import io.jenkins.plugins.report.jtreg.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.formatters.PlainFormatter;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;

public class JobsByQueryTest {
    private static ArrayList<File> dummyJobs;

    @BeforeAll
    public static void createDummyJobs() {
        ArrayList<String> dummyJobsStrings = new ArrayList<>();
        dummyJobsStrings.add("crypto~tests-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.vagrant-x11.defaultgc.fips.lnxagent.jfroff");
        dummyJobsStrings.add("jtreg~full-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff");
        dummyJobsStrings.add("jtreg~full-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff");
        dummyJobsStrings.add("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff");
        dummyJobsStrings.add("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff");
        dummyJobsStrings.add("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-release.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff");
        dummyJobsStrings.add("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-release.sdk-f36.x86_64.vagrant-wayland.defaultgc.ignorecp.lnxagent.jfron");
        dummyJobsStrings.add("jtreg~tier1-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff");
        dummyJobsStrings.add("jtreg~tier1-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff");
        dummyJobsStrings.add("reproducers~regular-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.vagrant-x11.defaultgc.defaultcp.lnxagent.jfroff");
        dummyJobsStrings.add("rhqe-jp11-ojdk11~rpms-f36.x86_64-slowdebug.sdk");

        DirListing dl = new ListDirListing(dummyJobsStrings);
        dummyJobs = dl.getJobsInDir();
    }

    private static ArrayList<String> convertJobsListToNamesList(ArrayList<File> jobsList) {
        ArrayList<String> namesList = new ArrayList<>();
        for (File job : jobsList) {
            namesList.add(job.getName());
        }
        return namesList;
    }

    @Test
    public void testFullCorrectQueryWithJob() {
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 shenandoah ignorecp lnxagent jfroff";
        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        Assertions.assertEquals(1, containsJobs.size());
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff"));
    }

    @Test
    public void testFullWrongQueryWithJob() {
        String queryString = "jtreg~tier1 jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 shenandoah ignorecp lnxagent jfroff";
        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        Assertions.assertFalse(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff"));
    }

    @Test
    public void testQueryWithAsterisks() {
        String queryString = "jtreg~full * * * * * * * * * * defaultgc * * jfroff";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.parseArguments("--force", null);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        Assertions.assertEquals(3, containsJobs.size());
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-release.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
    }

    @Test
    public void testQueryWithSets() {
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 {fastdebug,release} sdk f36 x86_64 {testfarm,vagrant} {x11,wayland} defaultgc ignorecp lnxagent {jfroff,jfron}";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        Assertions.assertEquals(3, containsJobs.size());
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-release.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-release.sdk-f36.x86_64.vagrant-wayland.defaultgc.ignorecp.lnxagent.jfron"));
    }

    @Test
    public void testQueryWithUnclosedSet() {
        // should throw en exception
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 {defaultgc,shenandoah ignorecp lnxagent jfroff";
        try {
            JobsByQuery jbq = new JobsByQuery();
            jbq.parseArguments("--query", queryString);
            jbq.addJobs(dummyJobs);
            jbq.filterJobs();

            Assertions.fail("The test did not threw an exception.");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testQueryWithExclamationMarks() {
        String queryString = "jtreg~full !jp8 !ojdk8~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 !shenandoah ignorecp lnxagent jfroff";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        Assertions.assertEquals(2, containsJobs.size());
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
    }

    @Test
    public void testQueryWithExclamationMarksWithSets() {
        String queryString = "!{reproducers~regular,crypto~tests} jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 defaultgc !{defaultcp,fips} lnxagent jfroff";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        Assertions.assertEquals(2, containsJobs.size());
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        Assertions.assertTrue(containsJobs.contains("jtreg~tier1-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
    }

    @Test
    public void testShorterQueryWithLongerJob() {
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 fastdebug sdk";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        Assertions.assertEquals(2, containsJobs.size());
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        Assertions.assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff"));
    }

    @Test
    public void testQueryWithShorterJob() {
        String queryString = "rhqe jp11 ojdk11~rpms f36 x86_64 slowdebug sdk f36 x86_64 vagrant x11 defaultgc legacy lnxagent jfroff";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        Assertions.assertEquals(1, containsJobs.size());
        Assertions.assertTrue(containsJobs.contains("rhqe-jp11-ojdk11~rpms-f36.x86_64-slowdebug.sdk"));
    }

    @Test
    public void testQueryWithCombinationOfAll() {
        String queryString = "!{jtreg~full,jtreg~tier1} {jp11,jp17} {ojdk11~rpms,ojdk17~rpms} f36 x86_64 fastdebug sdk f36 x86_64 vagrant x11 !shenandoah * *";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());
        System.out.println(containsJobs);

        Assertions.assertEquals(2, containsJobs.size());
        Assertions.assertTrue(containsJobs.contains("crypto~tests-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.vagrant-x11.defaultgc.fips.lnxagent.jfroff"));
        Assertions.assertTrue(containsJobs.contains("reproducers~regular-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.vagrant-x11.defaultgc.defaultcp.lnxagent.jfroff"));
    }

    private String crlfToLf(String s) {
        // replaces windows CRLF newline to unix LF newline
        // needed for the tests to pass both on linux and windows
        return s.replace("\r\n", "\n");
    }

    @Test
    public void testPrintJobs() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);

        String queryString = "!{jtreg~full,jtreg~tier1} {jp11,jp17} {ojdk11~rpms,ojdk17~rpms} f36 x86_64 fastdebug sdk f36 x86_64 vagrant x11 !shenandoah * *";
        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        File tmpdir = Files.createTempDirectory("reportJtregBuildTestDir").toFile();
        File changelogFile = new File(tmpdir, "changelog.xml");
        byte[] orig = BuildsTest.class.getResourceAsStream("/io/jenkins/plugins/report/jtreg/main/comparator/dummyJob/builds/1/changelog.xml").readAllBytes();
        Files.write(changelogFile.toPath(), orig);

        Options.Configuration nvrConfig = new Options.Configuration(changelogFile.getName(), "/build/nvr", Options.Locations.Build);
        nvrConfig.setValue("");

        Options options = new Options();
        options.getConfiguration("result").setValue(".*"); // --skip-failed false
        options.addConfiguration("nvr", nvrConfig);
        options.setNumberOfBuilds(0);
        options.setFormatter(new PlainFormatter(printStream));

        JobsPrinting.printJobs(jbq.getJobs(), options);

        Assertions.assertEquals("crypto~tests-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.vagrant-x11.defaultgc.fips.lnxagent.jfroff:\n" +
                "reproducers~regular-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.vagrant-x11.defaultgc.defaultcp.lnxagent.jfroff:\n", crlfToLf(outStream.toString()));
    }

    @Test
    public void testPrintVariants() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);
        Formatter formatter = new PlainFormatter(printStream);

        String queryString = "!{crypto~tests,reproducers~regular} * * f36 * !slowdebug sdk f36 x86_64 {testfarm,vagrant} * * * * *";
        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.parseArguments("--force", null);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        JobsPrinting.printVariants(jbq.getJobs(), formatter);

        Assertions.assertEquals("1) jtreg~full, jtreg~tier1, \n" +
                "2) jp11, jp17, \n" +
                "3) ojdk11~rpms, ojdk17~rpms, \n" +
                "4) f36, \n" +
                "5) x86_64, \n" +
                "6) fastdebug, release, \n" +
                "7) sdk, \n" +
                "8) f36, \n" +
                "9) x86_64, \n" +
                "10) testfarm, vagrant, \n" +
                "11) x11, wayland, \n" +
                "12) defaultgc, shenandoah, \n" +
                "13) ignorecp, \n" +
                "14) lnxagent, \n" +
                "15) jfroff, jfron, \n", crlfToLf(outStream.toString()));
    }
}
