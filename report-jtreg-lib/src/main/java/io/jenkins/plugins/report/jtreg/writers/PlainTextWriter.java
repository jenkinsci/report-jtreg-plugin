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
import io.jenkins.plugins.report.jtreg.model.ReportFull;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.SuiteTestChanges;
import io.jenkins.plugins.report.jtreg.model.SuiteTests;
import io.jenkins.plugins.report.jtreg.model.SuitesWithResults;
import io.jenkins.plugins.report.jtreg.model.Test;
import io.jenkins.plugins.report.jtreg.model.TestOutput;
import io.jenkins.plugins.report.jtreg.model.TestStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Utility class for writing human-readable plain text reports.
 * Provides methods to generate comprehensive test reports in natural English.
 */
public class PlainTextWriter {

    private PlainTextWriter() {
        // Utility class, prevent instantiation
    }

    /**
     * Writes a summary report with test results overview.
     * Includes counts and lists of failed, errored, and skipped tests.
     *
     * @param suites the list of suites to report
     * @param jobName the job name
     * @param buildName the build name
     * @param buildNumber the build number
     * @param outputPath the output file path
     * @throws IOException if an I/O error occurs
     */
    public static void writeSummaryReport(List<Suite> suites, String jobName, String buildName, int buildNumber, Path outputPath, String url) throws IOException {

        // Calculate totals
        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        int errorTests = 0;
        int skippedTests = 0;

        List<Test> allFailedTests = new ArrayList<>();
        List<Test> allErrorTests = new ArrayList<>();
        List<Test> allSkippedTests = new ArrayList<>();

        for (Suite suite : suites) {
            totalTests += suite.getReport().getTestsTotal();
            passedTests += suite.getReport().getTestsPassed();
            failedTests += suite.getReport().getTestsFailed();
            errorTests += suite.getReport().getTestsError();
            skippedTests += suite.getReport().getTestsNotRun();

            // Collect problem tests

            if (suite.getReport().getTestProblems() != null) {
                for (Test test : suite.getReport().getTestProblems()) {
                    if (test.getStatus() == TestStatus.FAILED) {
                        allFailedTests.add(test);
                    } else if (test.getStatus() == TestStatus.ERROR) {
                        allErrorTests.add(test);
                    } else if (test.getStatus() == TestStatus.NOT_RUN) {
                        allSkippedTests.add(test);
                    }
                }
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8, 
                                                             TRUNCATE_EXISTING, CREATE)) {
            // Write header
            writeHeader(writer, jobName, buildName, buildNumber, url, -1, -1);
            
            // Write summary
            writer.write(String.format("We ran %d test%s in total. ", totalTests, pluralize(totalTests)));
            writer.write(String.format("From those, %d test%s passed, ", passedTests, pluralize(passedTests)));
            writer.write(String.format("%d test%s failed, ", failedTests, pluralize(failedTests)));
            writer.write(String.format("%d test%s had errors, ", errorTests, pluralize(errorTests)));
            writer.write(String.format("and %d test%s %s skipped.%n%n",
                                      skippedTests, pluralize(skippedTests), 
                                      skippedTests == 1 ? "was" : "were"));
            
            // Write failed tests
            if (!allFailedTests.isEmpty()) {
                writer.write(String.format("The following %d test%s failed:%n",
                                          allFailedTests.size(), pluralize(allFailedTests.size())));
                for (Test test : allFailedTests) {
                    writer.write("  - " + test.getName() + "\n");
                }
                writer.write("\n");
            }
            
            // Write error tests
            if (!allErrorTests.isEmpty()) {
                writer.write(String.format("The following %d test%s had errors:%n",
                                          allErrorTests.size(), pluralize(allErrorTests.size())));
                for (Test test : allErrorTests) {
                    writer.write("  - " + test.getName() + "\n");
                }
                writer.write("\n");
            }
            
            // Write skipped tests
            if (!allSkippedTests.isEmpty()) {
                writer.write(String.format("The following %d test%s %s skipped:%n",
                                          allSkippedTests.size(), pluralize(allSkippedTests.size()),
                                          allSkippedTests.size() == 1 ? "was" : "were"));
                for (Test test : allSkippedTests) {
                    writer.write("  - " + test.getName() + "\n");
                }
                writer.write("\n");
            }
            
            // Write suite breakdown
            writer.write("\nTest Suite Breakdown:\n");
            writer.write("=====================\n\n");
            for (Suite suite : suites) {
                writer.write(String.format("Suite: %s%n", suite.getName()));
                writer.write(String.format("  Total: %d, Passed: %d, Failed: %d, Errors: %d, Skipped: %d%n%n",
                                          suite.getReport().getTestsTotal(),
                                          suite.getReport().getTestsPassed(),
                                          suite.getReport().getTestsFailed(),
                                          suite.getReport().getTestsError(),
                                          suite.getReport().getTestsNotRun()));
            }
            footer(writer, jobName, buildName, buildNumber, url, "summary report", "unknown");
        }
    }

    /**
     * Writes a detailed report of failed and errored tests with stack traces and outputs.
     *
     * @param suites the list of suites to report
     * @param jobName the job name
     * @param buildName the build name
     * @param buildNumber the build number
     * @param outputPath the output file path
     * @throws IOException if an I/O error occurs
     */
    public static void writeProblemsReport(List<Suite> suites, String jobName, String buildName, int buildNumber, Path outputPath, String url) throws IOException {

        // Calculate totals
        int failedTests = 0;
        int errorTests = 0;

        for (Suite suite : suites) {
            failedTests += suite.getReport().getTestsFailed();
            errorTests += suite.getReport().getTestsError();
        }
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8,
                                                             TRUNCATE_EXISTING, CREATE)) {

            // Write header
            writeHeader(writer, jobName, buildName, buildNumber, url, -1, -1);
            
            // Write summary
            writer.write(String.format("This report focuses on the %d failed test%s and %d test%s with errors.%n%n",
                                      failedTests, pluralize(failedTests),
                                      errorTests, pluralize(errorTests)));
            
            writer.write("Detailed Problem Reports:\n");
            writer.write("=========================\n\n");
            
            // Write detailed information for each problem test
            for (Suite suite : suites) {
                boolean suiteHeaderWritten = false;

                if  (suite.getReport().getTestProblems() != null ) {
                    for (Test test : suite.getReport().getTestProblems()) {
                        if (test.getStatus() == TestStatus.FAILED || test.getStatus() == TestStatus.ERROR) {
                            if (!suiteHeaderWritten) {
                                writer.write(String.format("%n=== Suite: %s ===%n%n", suite.getName()));
                                suiteHeaderWritten = true;
                            }

                            writer.write(String.format("Test: %s%n", test.getName()));
                            writer.write(String.format("Status: %s%n", test.getStatus()));

                            if (test.getStatusLine() != null && !test.getStatusLine().isEmpty()) {
                                writer.write(String.format("Status Line: %s%n", test.getStatusLine()));
                            }

                            // Write outputs (stack traces, logs, etc.)
                            if (test.getOutputs() != null && !test.getOutputs().isEmpty()) {
                                writer.write("\nOutputs:\n");
                                for (TestOutput output : test.getOutputs()) {
                                    writer.write(String.format("%n  [%s]:%n", output.getName()));
                                    writer.write("  " + output.getValue().replace("\n", "\n  ") + "\n");
                                }
                            }

                            writer.write("\n" + "=".repeat(80) + "\n\n");
                        }
                    }
                } else {
                    writer.write("\nAll good!\n");
                }
            }
            
            if (failedTests == 0 && errorTests == 0) {
                writer.write("No failed or errored tests to report. All tests passed successfully!\n");
            }
            footer(writer, jobName, buildName, buildNumber, url, "failures report", "unknown");
        }
    }

    /**
     * Writes a diff report showing test changes compared to previous build.
     * Includes improvements, regressions, new errors, added and removed tests.
     *
     * @param buildReportExtended the extended build report with diff information
     * @param outputPath the output file path
     * @throws IOException if an I/O error occurs
     */
    public static void writeDiffReport(BuildReportExtended buildReportExtended, Path outputPath, String url) throws IOException {

        // Calculate totals
        int totalImprovements = 0;
        int totalRegressions = 0;
        int totalNewErrors = 0;
        int totalAdded = 0;
        int totalRemoved = 0;

        for (SuiteTestChanges changes : buildReportExtended.getTestChanges()) {
            totalImprovements += changes.getFixes().size();
            totalRegressions += changes.getFailures().size();
            totalNewErrors += changes.getErrors().size();
            totalAdded += changes.getAdded().size();
            totalRemoved += changes.getRemoved().size();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8,
                                                             TRUNCATE_EXISTING, CREATE)) {
            // Write header
            writeHeader(writer, buildReportExtended.getJob(), buildReportExtended.getBuildName(), buildReportExtended.getBuildNumber(), url, buildReportExtended.getTimestamp(), buildReportExtended.getDuration());

            writer.write("This report shows changes compared to the previous, latest stable or unstable, build.\n\n");
            
            // Write summary
            writer.write("Summary of Changes:\n");
            writer.write("===================\n\n");
            writer.write(String.format("Test Improvements (new fixes): %d%n", totalImprovements));
            writer.write(String.format("Test Regressions (new failures): %d%n", totalRegressions));
            writer.write(String.format("New Regressions (new errors): %d%n", totalNewErrors));
            writer.write(String.format("Tests Added: %d%n", totalAdded));
            writer.write(String.format("Tests Removed: %d%n", totalRemoved));
            writer.write(String.format("Suites Added: %d%n", buildReportExtended.getAddedSuites().size()));
            writer.write(String.format("Suites Removed: %d%n%n", buildReportExtended.getRemovedSuites().size()));
            
            // Write detailed changes per suite
            writer.write("Detailed Changes by Suite:\n");
            writer.write("==========================\n\n");
            
            for (SuiteTestChanges changes : buildReportExtended.getTestChanges()) {
                boolean hasChanges = !changes.getFixes().isEmpty() || !changes.getFailures().isEmpty() ||
                                    !changes.getErrors().isEmpty() || !changes.getAdded().isEmpty() ||
                                    !changes.getRemoved().isEmpty();
                
                if (hasChanges) {
                    writer.write(String.format("Suite: %s%n", changes.getName()));
                    writer.write("-".repeat(changes.getName().length() + 7) + "\n\n");
                    
                    // Improvements
                    if (!changes.getFixes().isEmpty()) {
                        writer.write(String.format("  Improvements (%d test%s now passing):%n",
                                                  changes.getFixes().size(), pluralize(changes.getFixes().size())));
                        for (String test : changes.getFixes()) {
                            writer.write("    ✓ " + test + "\n");
                        }
                        writer.write("\n");
                    }
                    
                    // Regressions
                    if (!changes.getFailures().isEmpty()) {
                        writer.write(String.format("  Regressions (%d test%s now failing):%n",
                                                  changes.getFailures().size(), pluralize(changes.getFailures().size())));
                        for (String test : changes.getFailures()) {
                            writer.write("    ✗ " + test + "\n");
                        }
                        writer.write("\n");
                    }
                    
                    // New errors
                    if (!changes.getErrors().isEmpty()) {
                        writer.write(String.format("  New Errors (%d test%s with new errors):%n",
                                                  changes.getErrors().size(), pluralize(changes.getErrors().size())));
                        for (String test : changes.getErrors()) {
                            writer.write("    ⚠ " + test + "\n");
                        }
                        writer.write("\n");
                    }
                    
                    // Added tests
                    if (!changes.getAdded().isEmpty()) {
                        writer.write(String.format("  Added Tests (%d new test%s):%n",
                                                  changes.getAdded().size(), pluralize(changes.getAdded().size())));
                        for (String test : changes.getAdded()) {
                            writer.write("    + " + test + "\n");
                        }
                        writer.write("\n");
                    }
                    
                    // Removed tests
                    if (!changes.getRemoved().isEmpty()) {
                        writer.write(String.format("  Removed Tests (%d test%s removed):%n",
                                                  changes.getRemoved().size(), pluralize(changes.getRemoved().size())));
                        for (String test : changes.getRemoved()) {
                            writer.write("    - " + test + "\n");
                        }
                        writer.write("\n");
                    }
                    
                    writer.write("\n");
                }
            }
            
            // Write suite changes
            if (!buildReportExtended.getAddedSuites().isEmpty()) {
                writer.write("Added Suites:\n");
                for (String suite : buildReportExtended.getAddedSuites()) {
                    writer.write("  + " + suite + "\n");
                }
                writer.write("\n");
            }
            
            if (!buildReportExtended.getRemovedSuites().isEmpty()) {
                writer.write("Removed Suites:\n");
                for (String suite : buildReportExtended.getRemovedSuites()) {
                    writer.write("  - " + suite + "\n");
                }
                writer.write("\n");
            }
            
            if (totalImprovements == 0 && totalRegressions == 0 && totalNewErrors == 0 && 
                totalAdded == 0 && totalRemoved == 0 && 
                buildReportExtended.getAddedSuites().isEmpty() && 
                buildReportExtended.getRemovedSuites().isEmpty()) {
                writer.write("No changes detected compared to the previous, latest stable or unstable, build.\n");
            }
            footer(writer, buildReportExtended.getJob(), buildReportExtended.getBuildName(), buildReportExtended.getBuildNumber(), url, "detailed diff report", buildReportExtended.getDateIso());
        }
    }

    /**
     * Writes a complete listing of all tests that were run.
     *
     * @param reportFull the extended build report with all test information
     * @param outputPath the output file path
     * @throws IOException if an I/O error occurs
     */
    public static void writeAllTestsReport(List<Suite> reportFull, String jobName, String buildName, int buildNumber, Path outputPath, String url) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8, 
                                                             TRUNCATE_EXISTING, CREATE)) {
            // Write header
            writeHeader(writer, jobName, buildName, buildNumber, url, -1, -1);
            List<String> testsSum = JsonReportWriter.suitesToTests(reportFull).stream().flatMap( s -> s.getTests() == null?new ArrayList<String>().stream():s.getTests().stream()).collect(Collectors.toList());
            writer.write(String.format("This report lists all %d test%s that were run in this build in %d suite%s.%n%n", testsSum.size(), pluralize(testsSum.size()), reportFull.size(), pluralize(reportFull.size())));

            writer.write("Complete Test Listing:\n");
            writer.write("======================\n\n");

            // Write all tests by suite
            for (Suite suite : reportFull) {
                writer.write(String.format("Suite: %s%n", suite.getName()));
                writer.write("-".repeat(suite.getName().length() + 7) + "\n");
                List<String> tests;
                if (suite.getReport() instanceof  ReportFull) {
                    tests = ((ReportFull) suite.getReport()).getTestsList();
                } else {
                    tests = new ArrayList<>();
                }
                writer.write(String.format("  Total tests in this suite: %d%n%n", tests.size()));

                for (String test : tests) {
                    String statusSymbol = SuitesWithResults.isProblem(test, suite.getReport().getTestProblems()) ? "✗" : "✓";
                    writer.write(String.format("  %s %s%n", statusSymbol, test));
                    }

                    writer.write("\n");
                }
            footer(writer, jobName, buildName, buildNumber, url, "complete test listing", "unknown");
        }
    }

    /**
     * Writes the common header for all report types.
     */
    private static void writeHeader(BufferedWriter writer, String jobName, String buildName, int buildNumber, String url, long buildtime, long duration) throws IOException {
        writer.write("=" .repeat(80) + "\n");
        writer.write(String.format("Test Report for %s%n", jobName));
        writer.write(String.format("Build name: %s (ID %d)%n", buildName, buildNumber));
        writer.write(String.format("Build timestamp: %s ms%n", toKnown(buildtime, false, false)));
        writer.write(String.format("Build duration: %s ms%n", toKnown(duration, false, false)));
        writer.write("=".repeat(80) + "\n\n");
        introduction(writer, jobName, buildName, buildNumber, url, buildtime, duration);

    }

    private static void introduction(BufferedWriter writer, String jobName, String buildName, int buildNumber, String url, long buildtime, long duration) throws IOException {
        String s = "This results are related to build of " + buildName + "/" + buildNumber + " in test of " + jobName + ". It started at " + toKnown(buildtime, true, false) + " and had duration of " + toKnown(duration, true, true) + ".";
        writer.write(s + "\n\n");
    }

    private static void footerr(BufferedWriter writer, String jobName, String buildName, int buildNumber, String url, long buildtime, long duration) throws IOException {
        {
            writer.write("=".repeat(80) + "\\n");
            String s = "This results are related to build of " + buildName + "/" + buildNumber + " in test of " + jobName + "." + "It started at " + toKnown(buildtime, true, true) + " and had duration of " + toKnown(duration, true, true) + ".";
            writer.write(s + "\n\n");
            writer.write("=".repeat(80) + "\n\n\n");
        }
    }


    private static String toKnown(long time, boolean port, boolean duration) {
        if (time <= 0) {
            return "unknown";
        } else {
            if (!port) {
                return "" + time;
            } else {
                if (duration) {
                    return Duration.ofMillis(time).toString();
                } else {
                    return new Date(time).toInstant().toString();
                }
            }
        }
    }

    private static void footer(BufferedWriter writer, String jobName, String buildName, int buildNumber, String url, String testType, String date) throws IOException {
        if (url != null) {
            writer.write("=".repeat(80) + "\n\n");
            String page = url + "/job/" + jobName;
            writer.write("You can see the job page at: " + page + "\n");
            String build = page + "/" + buildNumber;
            writer.write("You can see the build  page at: " + build + "\n");
            writer.write("You can see the build artifacts at: " + build + "/artifact" + "\n");
            writer.write("You can see the build log at: " + build + "/console" + "\n");
            writer.write("You can see the build full log at: " + build + "/consoleFull" + "\n");
            String java = build + "/java-reports";
            writer.write("You can see the report: " + java + "\n");
            writer.write("You can see the report's problems: " + java + "#problems\n");
            writer.write("You can see the report's diff: " + java + "#diff\n");
            writer.write("You can see the report's listing: " + java + "#all\n");
        }
        writer.write("\n" + "=".repeat(80) + "\n\n");
        writer.write("End of " + testType + " of build " + buildName + "/" + buildNumber + " in job " + jobName + " from " + date + ".\n");
        writer.write("\n" + "=".repeat(80) + "\n\n");
    }

    /**
     * Returns "s" for plural or empty string for singular.
     */
    private static String pluralize(int count) {
        return count == 1 ? "" : "s";
    }
}

// Made with Bob
