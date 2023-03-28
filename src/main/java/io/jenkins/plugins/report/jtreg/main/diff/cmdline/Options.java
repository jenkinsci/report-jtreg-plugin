/*
 * The MIT License
 *
 * Copyright 2016
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
package io.jenkins.plugins.report.jtreg.main.diff.cmdline;

import io.jenkins.plugins.report.jtreg.main.diff.formatters.ColorFormatter;
import io.jenkins.plugins.report.jtreg.main.diff.formatters.HtmlFormatter;
import io.jenkins.plugins.report.jtreg.main.diff.formatters.PlainFormatter;
import io.jenkins.plugins.report.jtreg.main.diff.formatters.BasicFormatter;
import io.jenkins.plugins.report.jtreg.main.diff.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.main.diff.formatters.HtmlFormatter2;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Options {

    private boolean skipFailed = true;
    private final List<File> dirsToWork;
    private boolean fill = false;
    private String output_type;
    private final List<String> views;
    private BasicFormatter formatter;
    private PrintStream stream;
    private Pattern trackingRegex = Pattern.compile(".*");
    private Pattern trackingRegexChanges = Pattern.compile(".*");

    public Options() {
        this.views = new ArrayList<>();
        this.dirsToWork = new ArrayList<>();
    }

    public List<File> getDirsToWork() {
        return dirsToWork;
    }

    public boolean hideMisses() {
        return views.contains(Arguments.view_hide_misses);
    }

    public boolean hideTotals() {
        return views.contains(Arguments.view_hide_totals);
    }

    public boolean hideNegatives() {
        return views.contains(Arguments.view_hide_negatives);
    }

    public boolean hidePositives() {
        return views.contains(Arguments.view_hide_positives);
    }

    public boolean hideValues() {
        return views.contains(Arguments.view_info_hidevalues);
    }

    public boolean viewInfoSummary() {
        return views.isEmpty() || views.contains(Arguments.view_info_summary) || views.contains(Arguments.view_info);
    }

    public boolean viewInfoSummarySuites() {
        return views.isEmpty() || views.contains(Arguments.view_info_summary_suites) || views.contains(Arguments.view_info);
    }

    public boolean viewInfoProblems() {
        return views.isEmpty() || views.contains(Arguments.view_info_problems) || views.contains(Arguments.view_info);
    }
    public boolean viewAllTests() {
        return views.isEmpty() || views.contains(Arguments.view_all_tests);
    }

    public boolean viewDiffSummary() {
        return views.isEmpty() || views.contains(Arguments.view_diff_summary) || views.contains(Arguments.view_diff);
    }

    public boolean viewDiffSummarySuites() {
        return views.isEmpty() || views.contains(Arguments.view_diff_summary_suites) || views.contains(Arguments.view_diff);
    }

    public boolean viewDiffDetails() {
        return views.isEmpty() || views.contains(Arguments.view_diff_details) || views.contains(Arguments.view_diff);
    }

    public boolean viewDiffList() {
        return views.isEmpty() || views.contains(Arguments.view_diff_list) || views.contains(Arguments.view_diff);
    }

    void setOutputType(String output_type) {
        this.output_type = output_type;
    }

    void addView(String nextView) {
        views.add(nextView);
    }

    void setTrackingRegex(Pattern regex) {
        this.trackingRegex=regex;
    }

    public Pattern getTrackingRegex() {
        return trackingRegex;
    }

    void setTrackingRegexChanges(Pattern regex) {
        this.trackingRegexChanges=regex;
    }

    public Pattern getTrackingRegexChanges() {
        return trackingRegexChanges;
    }

    public boolean isInfo() {
        return (this.viewInfoProblems() || this.viewInfoSummary() || this.viewInfoSummarySuites() || views.contains(Arguments.view_info));
    }

    public boolean isDiff() {
        return this.viewDiffDetails() || this.viewDiffList() || this.viewDiffSummary() || this.viewDiffSummarySuites() || views.contains(Arguments.view_diff);
    }

    void setFill(boolean b) {
        fill = b;
    }

    public boolean isFill() {
        return fill;
    }

    void setSkipFailed(boolean b) {
        skipFailed = b;
    }

    public boolean isSkipFailed() {
        return skipFailed;
    }

    boolean add(File file) {
        if (skipFailed) {
            if (!file.exists()
                    || JobsRecognition.jobsRecognition().shouldBeSkipped(file)) {
                System.err.println("File " + file + " excluded -  skipping of failed builds allowed, and it seems to be fialed build");
                return false;
            } else {
                return addImpl(file);
            }
        } else {
            return addImpl(file);
        }
    }

    private boolean addImpl(File file) {
        if (dirsToWork.size() >= 2) {
            if (file.equals(dirsToWork.get(dirsToWork.size() - 1)) && file.equals(dirsToWork.get(dirsToWork.size() - 2))) {
                System.err.println("Excluding " + file + " - three same files in row");
                return false;
            }
        }
        dirsToWork.add(file);
        System.err.println("Added " + file + " !");
        return true;
    }

    public Formatter getFormatter() {
        if (formatter == null) {
            if (Arguments.output_color.equals(output_type)) {
                formatter = new ColorFormatter(stream);
            } else if (Arguments.output_html.equals(output_type)) {
                formatter = new HtmlFormatter(stream);
            } else if (Arguments.output_html2.equals(output_type)) {
                formatter = new HtmlFormatter2(stream);
            } else {
                formatter = new PlainFormatter(stream);
            }
        }
        return formatter;

    }

    public void setStream(PrintStream o) {
        this.stream = o;
    }

    public PrintStream getStream() {
        return stream;
    }
}
