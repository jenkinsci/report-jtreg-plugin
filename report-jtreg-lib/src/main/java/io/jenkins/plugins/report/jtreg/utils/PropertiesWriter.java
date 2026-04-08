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
package io.jenkins.plugins.report.jtreg.utils;

import io.jenkins.plugins.report.jtreg.model.BuildReport;
import io.jenkins.plugins.report.jtreg.model.ProjectReport;
import io.jenkins.plugins.report.jtreg.model.Suite;

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
     * @param jsonFile the JSON file location (used to determine build directory)
     */
    public static void cacheReport(List<Suite> reportShort, File jsonFile) {
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
            File buildDir = jsonFile.getParentFile();
            int buildNumber = Integer.parseInt(buildDir.getName());
            BuildReport br = new BuildReport(buildNumber, nameb.toString().trim(), passedSumm, failedSumm, errorSumm, reportShort, totalSumm, notRunSumm);
            cacheSumms(buildDir.getParentFile().getParentFile(), Arrays.asList(new BuildReport[]{br}));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static final String CACHED_SUMM_RESULTS_PROPERTIES = "cached-summ-results.properties";
    private static final String CACHED_SUMM_REGRESSIONS_PROPERTIES = "cached-summ-regressions.properties";

    /**
     * Caches project report totals (results and regressions) to properties files.
     *
     * @param rootBuild the root build directory
     * @param projectReport the project report to cache
     */
    public static void cacheTotals(File rootBuild, ProjectReport projectReport) {
        try {
            cacheTotalsImpl(rootBuild, projectReport);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Implementation of cacheTotals that throws IOException.
     * This happily ignores combined jck+jtreg reporting, but as it is never used, it may be already corrupted elsewhere.
     *
     * @param rootBuild the root build directory
     * @param projectReport the project report to cache
     * @throws IOException if an I/O error occurs
     */
    public static void cacheTotalsImpl(File rootBuild, ProjectReport projectReport) throws IOException {
        cacheSumms(rootBuild, projectReport.getReports());
        for (int i = 0; i < projectReport.getReports().size(); i++) {
            BuildReport buildReport = projectReport.getReports().get(i);
            File cachedRegressions = getCachedRegressionsFile(rootBuild, buildReport);
            if (!cachedRegressions.exists()) {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cachedRegressions), "utf-8"))) {
                    bw.write("jrp.improvements=" + projectReport.getImprovements().get(i));
                    bw.newLine();
                    bw.write("jrp.regressions=" + projectReport.getRegressions().get(i));
                    bw.newLine();
                }
            }
        }
    }

    /**
     * Caches build report summaries to properties files.
     *
     * @param rootBuild the root build directory
     * @param reports the list of build reports to cache
     * @throws IOException if an I/O error occurs
     */
    public static void cacheSumms(File rootBuild, List<? extends BuildReport> reports) throws IOException {
        for (BuildReport report : reports) {
            File cachedResults = getCachedResultsFile(rootBuild, report);
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

    private static File getCachedRegressionsFile(File rootBuild, BuildReport buildReport) {
        return getCachedFile(rootBuild, buildReport, CACHED_SUMM_REGRESSIONS_PROPERTIES);
    }

    private static File getCachedResultsFile(File rootBuild, BuildReport report) {
        return getCachedFile(rootBuild, report, CACHED_SUMM_RESULTS_PROPERTIES);
    }

    private static File getCachedFile(File root, BuildReport report, String name) {
        return new File(new File(new File(root, "builds"), report.getBuildNumber() + ""), name);
    }
}

// Made with Bob
