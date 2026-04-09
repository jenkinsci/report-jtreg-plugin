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

import com.google.gson.GsonBuilder;
import io.jenkins.plugins.report.jtreg.model.Report;
import io.jenkins.plugins.report.jtreg.model.ReportFull;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.SuiteTests;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * Utility class for writing JSON reports from Suite data.
 * Provides methods to generate both summary reports and full test list reports.
 */
public class JsonReportWriter {

      /**
     * Writes a summary report to a path with pretty printing.
     * The summary includes test counts (passed, failed, error, total, not run)
     * with the full test details.
     *
     * @param suites the list of suites to write
     * @param outputPath the output path
     * @throws IOException if an I/O error occurs
     */
    public static void writeSummaryReport(List<Suite> suites, Path outputPath) throws IOException {
        List<Suite> reportShort = suites.stream()
                .sequential()
                .map(s -> new Suite(
                        s.getName(),
                        new Report(
                                s.getReport().getTestsPassed(),
                                s.getReport().getTestsNotRun(),
                                s.getReport().getTestsFailed(),
                                s.getReport().getTestsError(),
                                s.getReport().getTestsTotal(),
                                s.getReport().getTestProblems())))
                .sorted()
                .collect(Collectors.toList());
        try (Writer out = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8, TRUNCATE_EXISTING, CREATE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(reportShort, out);
        }
    }

     /**
     * Writes a full test list report to a path without pretty printing.
     * The report includes the complete list of tests for each suite.
     *
     * @param suites the list of suites to write
     * @param outputPath the output path
     * @throws IOException if an I/O error occurs
     */
    public static void writeTestListReport(List<Suite> suites, Path outputPath) throws IOException {
        List<SuiteTests> suiteTests = suites.stream()
                .sequential()
                .map(s -> new SuiteTests(
                        s.getName(),
                        s.getReport() instanceof ReportFull ? ((ReportFull) s.getReport()).getTestsList() : null))
                .sorted()
                .collect(Collectors.toList());
        try (Writer out = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8, TRUNCATE_EXISTING, CREATE)) {
            new GsonBuilder().create().toJson(suiteTests, out);
        }
    }
}

// Made with Bob
