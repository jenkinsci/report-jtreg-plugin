/*
 * The MIT License
 *
 * Copyright 2015-2023 report-jtreg plugin contributors
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
package io.jenkins.plugins.report.jtreg.main.list;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.parsers.JckReportParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecreateJckReportSummaries {

    public static void main(String[] args) throws Exception {
        new RecreateJckReportSummaries().work();
    }

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    private void work() throws Exception {
        try (Stream<Path> dirsStream = Files.list(Paths.get("").toAbsolutePath().normalize())) {
            dirsStream.sequential()
                    .filter(d -> !Files.isSymbolicLink(d))
                    .forEach(this::recreateJckReportSummaryForBuild);
        }
    }

    @SuppressFBWarnings(value = {"REC_CATCH_EXCEPTION", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    private void recreateJckReportSummaryForBuild(Path buildPath) {
        Path tckReportsArchive = buildPath.resolve("archive").resolve("tck");
        if (!Files.exists(tckReportsArchive)) {
            return;
        }
        try (Stream<Path> tckReportsStream = Files.list(tckReportsArchive)) {
            List<Suite> suitesList = tckReportsStream.sequential()
                    .filter(p -> p.toString().endsWith(".xml") || p.toString().endsWith(".xml.gz") || p.toString().endsWith(".xml.xz"))
                    .map(this::jckReportToSuite)
                    .filter(s -> s != null)
                    .collect(Collectors.toList());
            ReportSummaryUtil.backupAndStoreSummaries("jck", suitesList, buildPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Suite jckReportToSuite(Path path) {
        return new JckReportParser().parsePath(path);
    }
}
