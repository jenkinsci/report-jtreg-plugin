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
import io.jenkins.plugins.report.jtreg.parsers.JtregReportParser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class RecreateJtregReportSummaries {

    //FIXME the isResultsArchive - despite the parser is later searrching for jtr.xml -
    // the initial pattern may be to vague. The cmdline mus accept parameter to narrow that
    //Also note that sfxs are doing the same...
    //TODO allow alternative path for backup
    //TODO allow alternative path for new files (then do not delete)
    //TODO FIXME add setup-able top-level URL for future usage
    public static void main(String[] args) throws Exception {
        new RecreateJtregReportSummaries().work();
    }

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    private void work() throws Exception {
        if (ReportSummaryUtil.isBuildDir(new File("."))) {
            recreateJtregReportSummaryForBuild(new File(".").getCanonicalFile().toPath());
        } else {
            try (Stream<Path> dirsStream = Files.list(Paths.get(".").toAbsolutePath().normalize())) {
                dirsStream.sequential().filter(d -> !Files.isSymbolicLink(d)).forEach(this::recreateJtregReportSummaryForBuild);
            }
        }
    }

    private static final String sfxs = "zip,tar,tar.gz,tar.bz2,tar.xz";

    private static boolean isJtregArchive(String s){
        String[] ss = sfxs.split(",");
        for (String s1 : ss) {
            if (s.endsWith(s1)){
                return true;
            }
        }
        return false;
    }

    @SuppressFBWarnings(value = {"REC_CATCH_EXCEPTION", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    private void recreateJtregReportSummaryForBuild(Path buildPath) {
        if (!ReportSummaryUtil.isBuildDir(buildPath.toFile())){
            return;
        }
        System.err.println("Processing: " + buildPath);
        final List<Path> archives = new ArrayList<>();
        try {
            Files.walkFileTree(buildPath.resolve("archive"), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isJtregArchive(file.toString())) {
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

        try (Stream<Path> tckReportsStream = archives.stream()) {
            List<Suite> suitesList = tckReportsStream.sequential()
                    .filter(p -> isResultsArchive(p))
                    .map(this::jtregReportToSuite)
                    .filter(s -> s != null)
                    .collect(Collectors.toList());
            ReportSummaryUtil.backupAndStoreSummaries("jtreg", suitesList, buildPath, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static boolean isResultsArchive(Path p) {
        return p.toString().endsWith(".xml") ||
                p.toString().endsWith(".xml.gz") ||
                p.toString().endsWith(".xml.xz") ||
                p.toString().endsWith(".tar.gz") ||
                p.toString().endsWith(".tar.xz");
    }

    private Suite jtregReportToSuite(Path path) {
        try {
            return new JtregReportParser().parsePath(path);
        }catch ( Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
