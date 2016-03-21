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
package hudson.plugins.report.jck.main;

import com.google.gson.GsonBuilder;
import hudson.plugins.report.jck.JckReportParser;
import hudson.plugins.report.jck.model.Suite;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hudson.plugins.report.jck.Constants.REPORT_JSON;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class RecreateJckReportSummaries {

    public static void main(String[] args) throws Exception {
        new RecreateJckReportSummaries().work();
    }

    private void work() throws Exception {
        try (Stream<Path> dirsStream = Files.list(Paths.get("").toAbsolutePath().normalize())) {
            dirsStream.sequential()
                    .filter(d -> !Files.isSymbolicLink(d))
                    .forEach(this::recreateJckReportSummaryForBuild);
        }
    }

    private void recreateJckReportSummaryForBuild(Path buildPath) {
        Path tckReportsArchive = buildPath.resolve("archive").resolve("tck");
        if (!Files.exists(tckReportsArchive)) {
            return;
        }
        try (Stream<Path> tckReportsStream = Files.list(tckReportsArchive)) {

            List<Suite> suitesList = tckReportsStream.sequential()
                    .filter(p -> p.toString().endsWith(".xml") || p.toString().endsWith(".xml.gz"))
                    .map(this::jckReportToSuite)
                    .filter(s -> s != null)
                    .collect(Collectors.toList());

            Path summaryPath = buildPath.resolve(REPORT_JSON);
            if (Files.exists(summaryPath)) {
                Files.move(summaryPath, buildPath.resolve("backup_" + REPORT_JSON), REPLACE_EXISTING);
            }

            try (Writer out = Files.newBufferedWriter(summaryPath, StandardCharsets.UTF_8, TRUNCATE_EXISTING, CREATE)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(suitesList, out);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Suite jckReportToSuite(Path path) {
        return new JckReportParser().parsePath(path);
    }
}
