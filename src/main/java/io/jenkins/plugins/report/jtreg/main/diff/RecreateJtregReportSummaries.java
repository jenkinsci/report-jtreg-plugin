/*
 * The MIT License
 *
 * Copyright 2016 user.
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
package io.jenkins.plugins.report.jtreg.main.diff;

import com.google.gson.GsonBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.report.jtreg.model.Report;
import io.jenkins.plugins.report.jtreg.model.ReportFull;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.SuiteTests;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jenkins.plugins.report.jtreg.Constants.REPORT_JSON;
import static io.jenkins.plugins.report.jtreg.Constants.REPORT_TESTS_LIST_JSON;
import io.jenkins.plugins.report.jtreg.JtregReportPublisher;
import io.jenkins.plugins.report.jtreg.parsers.JtregReportParser;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class RecreateJtregReportSummaries {

    public static void main(String[] args) throws Exception {
        new RecreateJtregReportSummaries().work();
    }

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    private void work() throws Exception {
        try (Stream<Path> dirsStream = Files.list(Paths.get("").toAbsolutePath().normalize())) {
            dirsStream.sequential()
                    .filter(d -> !Files.isSymbolicLink(d))
                    .forEach(this::recreateJtregReportSummaryForBuild);
        }
    }

    @SuppressFBWarnings(value = {"REC_CATCH_EXCEPTION", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    private void recreateJtregReportSummaryForBuild(Path buildPath) {
        Path tckReportsArchive = buildPath.resolve("archive");
        if (!Files.exists(tckReportsArchive)) {
            return;
        }
        final List<Path> archives = new ArrayList<>();
        try {
            Files.walkFileTree(buildPath, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (JtregReportPublisher.isJtregArchive(file.toString())) {
                        archives.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        String prefix = "jtreg";
        try (Stream<Path> tckReportsStream = archives.stream()) {

            List<Suite> suitesList = tckReportsStream.sequential()
                    .map(this::jtregReportToSuite)
                    .filter(s -> s != null)
                    .collect(Collectors.toList());

            {
                Path summaryPath = buildPath.resolve(prefix + "-" + REPORT_JSON);
                if (Files.exists(summaryPath)) {
                    Files.move(summaryPath, buildPath.resolve("backup_" + prefix + "-" + REPORT_JSON), REPLACE_EXISTING);
                }
                List<Suite> reportShort = suitesList.stream()
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
                try (Writer out = Files.newBufferedWriter(summaryPath, StandardCharsets.UTF_8, TRUNCATE_EXISTING, CREATE)) {
                    new GsonBuilder().setPrettyPrinting().create().toJson(reportShort, out);
                }
            }
            {
                Path testsListPath = buildPath.resolve(prefix + "-" + REPORT_TESTS_LIST_JSON);
                if (Files.exists(testsListPath)) {
                    Files.move(testsListPath, buildPath.resolve("backup_" + prefix + "-" + REPORT_TESTS_LIST_JSON),
                            REPLACE_EXISTING);
                }
                List<SuiteTests> suites = suitesList.stream()
                        .sequential()
                        .map(s -> new SuiteTests(
                                s.getName(),
                                s.getReport() instanceof ReportFull ? ((ReportFull) s.getReport()).getTestsList() : null))
                        .sorted()
                        .collect(Collectors.toList());
                try (Writer out = Files.newBufferedWriter(testsListPath, StandardCharsets.UTF_8, TRUNCATE_EXISTING,
                        CREATE)) {
                    new GsonBuilder().create().toJson(suites, out);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Suite jtregReportToSuite(Path path) {
        return new JtregReportParser().parsePath(path);
    }
}
