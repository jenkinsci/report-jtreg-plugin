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
package io.jenkins.plugins.report.jtreg;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Project;
import io.jenkins.plugins.report.jtreg.model.ProjectReport;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractReportProjectAction implements Action {

    protected final Job<?, ?> job;
    protected final Set<String> prefixes = new HashSet<>();

    public AbstractReportProjectAction(Job<?, ?> job, Set<String> prefixes) {
        if (job == null) {
            throw new IllegalArgumentException("Job cannot be null");
        }
        if (prefixes == null || prefixes.isEmpty()) {
            throw new IllegalArgumentException("Prefixes cannot be null or empty");
        }
        this.job = job;
        this.prefixes.addAll(prefixes);
    }

    public void addPrefix(String prefix) {
        prefixes.add(prefix);
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        AbstractReportPublisher settings = ReportAction.getAbstractReportPublisher(((Project)job).getPublishersList());
        List<String> blisted = new BuildSummaryParserPlugin(prefixes, settings).getDenylisted(job);
        List<String> wlisted = new BuildSummaryParserPlugin(prefixes, settings).getAllowlisted(job);
        int allowListSizeWithoutSurroundings = new BuildSummaryParserPlugin(prefixes, settings).getAllowListSizeWithoutSurroundings(job);
        String appendix = "";
        if (blisted.size() > 0 && wlisted.size() > 0) {
            appendix = " (denylisted " + blisted.size() + ")" + " (allowlisted " + allowListSizeWithoutSurroundings + "/" + Integer.toString(wlisted.size()-allowListSizeWithoutSurroundings) + ")";
        } else if (blisted.size() > 0) {
            appendix = " (denylisted " + blisted.size() + ")";
        } else if (wlisted.size() > 0) {
            appendix = " (allowlisted " + allowListSizeWithoutSurroundings + "/" + Integer.toString(wlisted.size()-allowListSizeWithoutSurroundings) + ")";
        }
        return prefixes.stream()
                .sequential()
                .map(s -> s.toUpperCase())
                .collect(Collectors.joining(", ", "", " " + getReportSuffix() + appendix));
    }

    // This is called when chart is shown on main page
    public ProjectReport getChartData() {
        ProjectReport report = ReportProjectActionUtils.getReport(prefixes, (Project) job, 0);
        return report;
    }

    protected abstract String getReportSuffix();
}

// Made with Bob
