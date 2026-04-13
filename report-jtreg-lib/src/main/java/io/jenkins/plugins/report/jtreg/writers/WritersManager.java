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

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.Constants;
import io.jenkins.plugins.report.jtreg.model.Suite;

public class WritersManager {

    public static void storeAllSummaries(String prefix, List<Suite> reportFull, File rootDir, String buildName, String url) throws IOException {
        writeJsons(prefix, reportFull, rootDir);
        PropertiesWriter.writeReportSummaryProperties(reportFull, rootDir);
        writePlainTextSummaries(prefix, reportFull, rootDir, buildName, url);
    }

    //Note, that this diff is not, and should not be, used in comparison - that should remain dynamic
    public static void storeAllDiffs(String prefix, BuildReportExtended buildReportExtended, File rootDir, String url) throws IOException {
        buildReportExtended = purify(buildReportExtended);
        File diffJson = new File(rootDir, prefix + "-" + Constants.REPORT_DIFF);
        JsonReportWriter.writeBuildReportExtended(diffJson, buildReportExtended);
        PropertiesWriter.writeReportPropertiesRegressions(rootDir, buildReportExtended);
        writePlainTextDiff(prefix, buildReportExtended, rootDir, url);
    }

    private static void writeJsons(String prefix, List<Suite> reportFull, File rootDir) throws IOException {
        File jsonFile1 = new File(rootDir, prefix + "-" + Constants.REPORT_JSON);
        File jsonFile2 = new File(rootDir, prefix + "-" + Constants.REPORT_TESTS_LIST_JSON);
        JsonReportWriter.writeSummaryReport(reportFull, jsonFile1.toPath());
        JsonReportWriter.writeTestListReport(reportFull, jsonFile2.toPath());
    }

    private static void writePlainTextSummaries(String prefix, List<Suite> reportFull, File rootDir, String buildName, String url) throws IOException {
        // Extract job name and build info from rootDir structure
        // rootDir is typically: /path/to/job/builds/buildNumber
        int buildNumber = Integer.parseInt(rootDir.getName());
        File jobDir = rootDir.getParentFile().getParentFile();
        String jobName = jobDir.getName();
        // Write summary report
        File summaryFile = new File(rootDir, prefix + "-" + Constants.REPORT_SUMMARY_TXT);
        PlainTextWriter.writeSummaryReport(reportFull, jobName, buildName, buildNumber, summaryFile.toPath(), url);
        // Write problems report
        File problemsFile = new File(rootDir, prefix + "-" + Constants.REPORT_PROBLEMS_TXT);
        PlainTextWriter.writeProblemsReport(reportFull, jobName, buildName, buildNumber, problemsFile.toPath(), url);
        // Write all tests report
        File allTestsFile = new File(rootDir, prefix + "-" + Constants.REPORT_ALL_TESTS_TXT);
        PlainTextWriter.writeAllTestsReport(reportFull, jobName, buildName, buildNumber,allTestsFile.toPath(), url);
    }

    private static void writePlainTextDiff(String prefix, BuildReportExtended buildReportExtended, File rootDir, String url) throws IOException {
        // Write diff report
        File diffFile = new File(rootDir, prefix + "-" + Constants.REPORT_DIFF_TXT);
        PlainTextWriter.writeDiffReport(buildReportExtended, diffFile.toPath(), url);
    }

    private static BuildReportExtended purify(BuildReportExtended br) {
        return new BuildReportExtended(br.getBuildNumber(), br.getBuildName(), br.getPassed(), br.getFailed(), br.getError(), null, br.getAddedSuites(), br.getRemovedSuites(), br.getTestChanges(), br.getTotal(), br.getNotRun(), null, br.getJob(), br.getTimestamp(), br.getDuration());
    }

}
