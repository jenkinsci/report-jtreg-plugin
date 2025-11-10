package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByQuery;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.DirListing;
import io.jenkins.plugins.report.jtreg.main.comparator.listing.ListDirListing;
import io.jenkins.plugins.report.jtreg.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.formatters.PlainFormatter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobsByQueryTest {

    private static ArrayList<File> dummyJobs;

    @BeforeAll
    static void beforeAll() {
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
    void testFullCorrectQueryWithJob() {
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 shenandoah ignorecp lnxagent jfroff";
        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        assertEquals(1, containsJobs.size());
        assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff"));
    }

    @Test
    void testFullWrongQueryWithJob() {
        String queryString = "jtreg~tier1 jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 shenandoah ignorecp lnxagent jfroff";
        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        assertFalse(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff"));
    }

    @Test
    void testQueryWithAsterisks() {
        String queryString = "jtreg~full * * * * * * * * * * defaultgc * * jfroff";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.parseArguments("--force", null);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        assertEquals(3, containsJobs.size());
        assertTrue(containsJobs.contains("jtreg~full-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-release.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
    }

    @Test
    void testQueryWithSets() {
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 {fastdebug,release} sdk f36 x86_64 {testfarm,vagrant} {x11,wayland} defaultgc ignorecp lnxagent {jfroff,jfron}";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        assertEquals(3, containsJobs.size());
        assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-release.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-release.sdk-f36.x86_64.vagrant-wayland.defaultgc.ignorecp.lnxagent.jfron"));
    }

    @Test
    void testQueryWithUnclosedSet() {
        // should throw en exception
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 {defaultgc,shenandoah ignorecp lnxagent jfroff";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        assertThrows(Exception.class, jbq::filterJobs);
    }

    @Test
    void testQueryWithExclamationMarks() {
        String queryString = "jtreg~full !jp8 !ojdk8~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 !shenandoah ignorecp lnxagent jfroff";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        assertEquals(2, containsJobs.size());
        assertTrue(containsJobs.contains("jtreg~full-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
    }

    @Test
    void testQueryWithExclamationMarksWithSets() {
        String queryString = "!{reproducers~regular,crypto~tests} jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 defaultgc !{defaultcp,fips} lnxagent jfroff";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        assertEquals(2, containsJobs.size());
        assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        assertTrue(containsJobs.contains("jtreg~tier1-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
    }

    @Test
    void testShorterQueryWithLongerJob() {
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 fastdebug sdk";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        assertEquals(2, containsJobs.size());
        assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff"));
        assertTrue(containsJobs.contains("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff"));
    }

    @Test
    void testQueryWithShorterJob() {
        String queryString = "rhqe jp11 ojdk11~rpms f36 x86_64 slowdebug sdk f36 x86_64 vagrant x11 defaultgc legacy lnxagent jfroff";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());

        assertEquals(1, containsJobs.size());
        assertTrue(containsJobs.contains("rhqe-jp11-ojdk11~rpms-f36.x86_64-slowdebug.sdk"));
    }

    @Test
    void testQueryWithCombinationOfAll() {
        String queryString = "!{jtreg~full,jtreg~tier1} {jp11,jp17} {ojdk11~rpms,ojdk17~rpms} f36 x86_64 fastdebug sdk f36 x86_64 vagrant x11 !shenandoah * *";

        JobsByQuery jbq = new JobsByQuery();
        jbq.parseArguments("--query", queryString);
        jbq.addJobs(dummyJobs);
        jbq.filterJobs();

        ArrayList<String> containsJobs = convertJobsListToNamesList(jbq.getJobs());
        System.out.println(containsJobs);

        assertEquals(2, containsJobs.size());
        assertTrue(containsJobs.contains("crypto~tests-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.vagrant-x11.defaultgc.fips.lnxagent.jfroff"));
        assertTrue(containsJobs.contains("reproducers~regular-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.vagrant-x11.defaultgc.defaultcp.lnxagent.jfroff"));
    }

    private static String crlfToLf(String s) {
        // replaces windows CRLF newline to unix LF newline
        // needed for the tests to pass both on linux and windows
        return s.replace("\r\n", "\n");
    }

    @Test
    void testPrintVariants() {
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

        assertEquals("1) jtreg~full, jtreg~tier1, \n" +
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
