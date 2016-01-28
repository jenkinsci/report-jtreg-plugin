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

import hudson.FilePath;
import hudson.plugins.report.jck.model.Report;
import hudson.plugins.report.jck.model.Suite;
import hudson.remoting.VirtualChannel;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.jenkinsci.remoting.RoleChecker;

public class JckReportParserCallable implements FilePath.FileCallable<List<Suite>> {

    private final String reportMatcherGlob;

    public JckReportParserCallable(String reportMatcherGlob) {
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
    public List<Suite> invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(reportMatcherGlob);
        List<Suite> result = Files.walk(f.toPath())
                .sequential()
                .filter(p -> pathMatcher.matches(p.getFileName()))
                .map(this::parsePath)
                .filter(e -> e != null)
                .sorted()
                .collect(Collectors.toList());
        return result;
    }

    private Suite parsePath(Path path) {
        try {
            try (InputStream in = streamPath(path)) {
                Report report = new JckReportParser().parseReport(in);
                return new Suite(suiteName(path), report);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private InputStream streamPath(Path path) throws IOException {
        InputStream stream = new BufferedInputStream(Files.newInputStream(path));
        if (path.toString().endsWith(".gz")) {
            return new GZIPInputStream(stream);
        }
        return stream;
    }

    private String suiteName(Path path) {
        String fullName = path.getFileName().toString();
        if (fullName.endsWith(".xml.gz")) {
            return fullName.substring(0, fullName.length() - 7);
        }
        if (fullName.endsWith(".xml")) {
            return fullName.substring(0, fullName.length() - 4);
        }
        throw new IllegalArgumentException("file name does not end with either .xml or .xml.gz extension: " + fullName);
    }

    @Override
    public void checkRoles(RoleChecker rc) throws SecurityException {
    }

}
