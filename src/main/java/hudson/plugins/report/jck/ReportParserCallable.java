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
package hudson.plugins.report.jck;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.plugins.report.jck.model.Report;
import hudson.plugins.report.jck.model.Suite;
import hudson.plugins.report.jck.parsers.ReportParser;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportParserCallable implements FilePath.FileCallable<List<Suite>> {

    public static final Suite FAKE_SUITE = new Suite("Fake suite", new Report(0, 0, 0, 0, 0, null));

    private final String reportMatcherGlob;
    private final ReportParser reportParser;

    public ReportParserCallable(String reportMatcherGlob, ReportParser reportParser) {
        this.reportParser = reportParser;
        if (reportMatcherGlob == null || reportMatcherGlob.isEmpty()) {
            this.reportMatcherGlob = "glob:*.{xml,xml.gz}";
        } else {
            if (!reportMatcherGlob.startsWith("glob:")) {
                reportMatcherGlob = "glob:" + reportMatcherGlob;
            }
            this.reportMatcherGlob = reportMatcherGlob;
        }
    }

    @Override
    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    public List<Suite> invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(reportMatcherGlob);
        try (Stream<Path> filesStream = Files.walk(f.toPath()).sequential()) {
            List<Suite> result = filesStream
                    .filter(p -> pathMatcher.matches(p.getFileName()))
                    .map(reportParser::parsePath)
                    .filter(e -> e != null)
                    .sorted()
                    .collect(Collectors.toList());
            if (result != null && result.size() == 0) {
                result.add(getFakeSuite());
            }
            return result;
        }
    }

    @Override
    public void checkRoles(RoleChecker rc) throws SecurityException {
    }

    private Suite getFakeSuite() {
        return FAKE_SUITE;
    }
}
