package io.jenkins.plugins.report.jtreg.main.comparator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.fail;

public class QueryStringTest {
    private static final File[] listOfJobs = new File[10];

    @BeforeClass
    public static void setupDummyJobs() {
        listOfJobs[0] = new File("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff");
        listOfJobs[1] = new File("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff");
        listOfJobs[2] = new File("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-release.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff");
        listOfJobs[3] = new File("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-release.sdk-f36.x86_64.vagrant-wayland.defaultgc.ignorecp.lnxagent.jfron");
        listOfJobs[4] = new File("jtreg~full-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff");
        listOfJobs[5] = new File("jtreg~full-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff");
        listOfJobs[6] = new File("jtreg~tier1-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.defaultgc.ignorecp.lnxagent.jfroff");
        listOfJobs[7] = new File("jtreg~tier1-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.testfarm-x11.shenandoah.ignorecp.lnxagent.jfroff");
        listOfJobs[8] = new File("reproducers~regular-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.vagrant-x11.defaultgc.defaultcp.lnxagent.jfroff");
        listOfJobs[9] = new File("crypto~tests-jp11-ojdk11~rpms-f36.x86_64-fastdebug.sdk-f36.x86_64.vagrant-x11.defaultgc.fips.lnxagent.jfroff");
    }

    @Test
    public void testFullCorrectQueryWithJob() {
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 shenandoah ignorecp lnxagent jfroff";
        QueryString.resetCachedQuery();
        Assert.assertTrue(QueryString.checkJobWithQuery(listOfJobs[0], queryString));
    }

    @Test
    public void testFullWrongQueryWithJob() {
        String queryString = "jtreg~tier1 jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 shenandoah ignorecp lnxagent jfroff";
        QueryString.resetCachedQuery();
        Assert.assertFalse(QueryString.checkJobWithQuery(listOfJobs[0], queryString));
    }

    @Test
    public void testQueryWithAsterisks() {
        String queryString = "jtreg~full * * * * * * * * * * defaultgc * * jfroff";
        // array corresponding with jobs, that should match the query
        boolean[] shouldMatch = {false, true, true, false, false, true, false, false, false, false};
        QueryString.resetCachedQuery();
        for (int i = 0; i < listOfJobs.length; i++) {
            if (shouldMatch[i]) {
                Assert.assertTrue(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            } else {
                Assert.assertFalse(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            }
        }
    }

    @Test
    public void testQueryWithSets() {
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 {fastdebug,release} sdk f36 x86_64 {testfarm,vagrant} {x11,wayland} defaultgc ignorecp lnxagent {jfroff,jfron}";
        boolean[] shouldMatch = {false, true, true, true, false, false, false, false, false, false};

        QueryString.resetCachedQuery();
        for (int i = 0; i < listOfJobs.length; i++) {
            if (shouldMatch[i]) {
                Assert.assertTrue(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            } else {
                Assert.assertFalse(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            }
        }
    }

    @Test
    public void testQueryWithUnclosedSet() {
        // should throw en exception
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 {defaultgc,shenandoah ignorecp lnxagent jfroff";
        try {
            QueryString.resetCachedQuery();
            QueryString.checkJobWithQuery(listOfJobs[0], queryString);
            fail("The method did not threw an exception.");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testQueryWithExclamationMarks() {
        String queryString = "jtreg~full !jp8 !ojdk8~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 !shenandoah ignorecp lnxagent jfroff";
        boolean[] shouldMatch = {false, true, false, false, false, true, false, false, false, false};
        QueryString.resetCachedQuery();
        for (int i = 0; i < listOfJobs.length; i++) {
            if (shouldMatch[i]) {
                Assert.assertTrue(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            } else {
                Assert.assertFalse(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            }
        }
    }

    @Test
    public void testQueryWithExclamationMarksWithSets() {
        String queryString = "!{reproducers~regular,crypto~tests} jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 defaultgc !{defaultcp,fips} lnxagent jfroff";
        boolean[] shouldMatch = {false, true, false, false, false, false, true, false, false, false};
        QueryString.resetCachedQuery();
        for (int i = 0; i < listOfJobs.length; i++) {
            if (shouldMatch[i]) {
                Assert.assertTrue(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            } else {
                Assert.assertFalse(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            }
        }
    }

    @Test
    public void testShorterQueryWithLongerJob() {
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 fastdebug sdk";
        boolean[] shouldMatch = {true, true, false, false, false, false, false, false, false, false};
        QueryString.resetCachedQuery();
        for (int i = 0; i < listOfJobs.length; i++) {
            if (shouldMatch[i]) {
                Assert.assertTrue(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            } else {
                Assert.assertFalse(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            }
        }
    }

    @Test
    public void testQueryWithShorterJob() {
        String queryString = "jtreg~full jp17 ojdk17~rpms f36 x86_64 fastdebug sdk f36 x86_64 testfarm x11 shenandoah ignorecp lnxagent jfroff";
        File shortJob = new File("jtreg~full-jp17-ojdk17~rpms-f36.x86_64-fastdebug.sdk");
        QueryString.resetCachedQuery();
        Assert.assertTrue(QueryString.checkJobWithQuery(shortJob, queryString));
    }

    @Test
    public void testQueryWithCombinationOfAll() {
        String queryString = "!{jtreg~full,jtreg~tier1} {jp11,jp17} {ojdk11~rpms,ojdk17~rpms} f36 x86_64 fastdebug sdk f36 x86_64 vagrant x11 !shenandoah * *";
        boolean[] shouldMatch = {false, false, false, false, false, false, false, false, true, true};
        QueryString.resetCachedQuery();
        for (int i = 0; i < listOfJobs.length; i++) {
            if (shouldMatch[i]) {
                Assert.assertTrue(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            } else {
                Assert.assertFalse(QueryString.checkJobWithQuery(listOfJobs[i], queryString));
            }
        }
    }
}
