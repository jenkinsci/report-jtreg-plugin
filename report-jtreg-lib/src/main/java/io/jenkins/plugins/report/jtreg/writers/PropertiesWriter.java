/*
 * The MIT License
 *
 * Copyright 2015-2026 report-jtreg plugin contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.report.jtreg.writers;

import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.model.BuildReport;
import io.jenkins.plugins.report.jtreg.model.ProjectReport;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.SuiteTestChanges;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for writing report properties to properties files.
 */
public class PropertiesWriter {

    private PropertiesWriter() {
        // Utility class, prevent instantiation
    }

    /**
     * Caches the report summary to properties files.
     * 
     * @param reportShort the list of suites to cache
     * @param buildDir the build directory
     */
    public static void writeReportSummaryProperties(List<Suite> reportShort, File buildDir) {
        int buildNumber = Integer.parseInt(buildDir.getName());
        File rootBuild = buildDir.getParentFile().getParentFile();
        File cachedResults = getCachedResultsFile(rootBuild, buildNumber);
        writeReportSummaryPropertiesImpl(reportShort, buildNumber, cachedResults);
    }

    /**
     * Caches project report totals (results and regressions) to properties files.
     *
     * @param rootBuild the root build directory
     * @param projectReport the project report to cache
     */
    public static void writeReportPropertiesRegressions(File rootBuild, BuildReportExtended buildReportExtended) {
        try {
            File buildParent = rootBuild.getParentFile().getParentFile();
            File cachedRegressions = getCachedRegressionsFile(buildParent, buildReportExtended.getBuildNumber());
            cacheRegressionsImpl(cachedRegressions, buildReportExtended);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //this is creating fake report, without any details
    private static void writeReportSummaryPropertiesImpl(List<Suite> reportShort, int buildNumber, File cachedResults) {
        try {
            int passedSumm = 0;
            int notRunSumm = 0;
            int failedSumm = 0;
            int errorSumm = 0;
            int totalSumm = 0;
            StringBuilder nameb = new StringBuilder();
            for (Suite s : reportShort) {
                passedSumm += s.getReport().getTestsPassed();
                notRunSumm += s.getReport().getTestsNotRun();
                failedSumm += s.getReport().getTestsFailed();
                errorSumm += s.getReport().getTestsError();
                totalSumm += s.getReport().getTestsTotal();
                nameb.append(s.getName()).append(" ");
            }
            BuildReport br = new BuildReport(buildNumber, nameb.toString().trim(), passedSumm, failedSumm, errorSumm, reportShort, totalSumm, notRunSumm);
            writeReportSummaryPropertiesImpl(cachedResults, Arrays.asList(new BuildReport[]{br}));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Implementation of cacheTotals that throws IOException.
     * This happily ignores combined jck+jtreg reporting, but as this feature is never used, it may be already corrupted elsewhere.
     *
     * @param cachedRegressions file forsecondary report
     * @param br the project report to cache
     * @throws IOException if an I/O error occurs
     */
    private static void cacheRegressionsImpl(File cachedRegressions, BuildReportExtended br) throws IOException {
            if (!cachedRegressions.exists()) {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cachedRegressions), "utf-8"))) {
                    bw.write("jrp.improvements=" + allImprovements(br.getTestChanges()));
                    bw.newLine();
                    bw.write("jrp.regressions=" + allFailures(br.getTestChanges()));
                    bw.newLine();
                    bw.write("jrp.nwerrors=" + allErrors(br.getTestChanges()));
                    bw.newLine();
                    bw.write("jrp.added=" + allAdded(br.getTestChanges()));
                    bw.newLine();
                    bw.write("jrp.removes=" + allRemoved(br.getTestChanges()));
                    bw.newLine();
                    bw.write("jrp.addedSuites=" + br.getAddedSuites().size());
                    bw.newLine();
                    bw.write("jrp.removedSuites=" + br.getRemovedSuites().size());
                    bw.newLine();
                }
            }
    }

    private static int allImprovements(List<SuiteTestChanges> testChanges) {
        int i = 0;
        for (SuiteTestChanges testChange : testChanges) {
            i += testChange.getFixes().size();
        }
        return i;
    }

    private static int allFailures(List<SuiteTestChanges> testChanges) {
        int i = 0;
        for (SuiteTestChanges testChange : testChanges) {
            i += testChange.getFailures().size();
        }
        return i;
    }

    private static int allErrors(List<SuiteTestChanges> testChanges) {
        int i = 0;
        for (SuiteTestChanges testChange : testChanges) {
            i += testChange.getErrors().size();
        }
        return i;
    }

    private static int allAdded(List<SuiteTestChanges> testChanges) {
        int i = 0;
        for (SuiteTestChanges testChange : testChanges) {
            i += testChange.getAdded().size();
        }
        return i;
    }

    private static int allRemoved(List<SuiteTestChanges> testChanges) {
        int i = 0;
        for (SuiteTestChanges testChange : testChanges) {
            i += testChange.getRemoved().size();
        }
        return i;
    }



    /**
     * Caches build report summaries to properties files.
     *
     * @param cachedResults the root build directory
     * @param reports the list of build reports to cache
     * @throws IOException if an I/O error occurs
     */
    private static void writeReportSummaryPropertiesImpl(File cachedResults, List<? extends BuildReport> reports) throws IOException {
        for (BuildReport report : reports) {
            if (!cachedResults.exists()) {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cachedResults), "utf-8"))) {
                    bw.write("jrp.errors=" + report.getError());
                    bw.newLine();
                    bw.write("jrp.failures=" + report.getFailed());
                    bw.newLine();
                    bw.write("jrp.notrun=" + report.getNotRun());
                    bw.newLine();
                    bw.write("jrp.passed=" + report.getPassed());
                    bw.newLine();
                    bw.write("jrp.total=" + report.getTotal());
                    bw.newLine();
                    bw.write("jrp.failedAndErrors=" + (report.getFailed() + report.getError()));
                    bw.newLine();
                }
            }
        }
    }

    private static final String CACHED_SUMM_RESULTS_PROPERTIES = "cached-summ-results.properties";
    private static final String CACHED_SUMM_REGRESSIONS_PROPERTIES = "cached-summ-regressions.properties";

    private static File getCachedRegressionsFile(File rootBuild, int buildNumber) {
        return getCachedFile(rootBuild, buildNumber, CACHED_SUMM_REGRESSIONS_PROPERTIES);
    }

    private static File getCachedResultsFile(File rootBuild, int buildNumber) {
        return getCachedFile(rootBuild, buildNumber, CACHED_SUMM_RESULTS_PROPERTIES);
    }

    private static File getCachedFile(File root,int buildNumber, String name) {
        return new File(new File(new File(root, "builds"), buildNumber + ""), name);
    }

}

// Made with Bob
