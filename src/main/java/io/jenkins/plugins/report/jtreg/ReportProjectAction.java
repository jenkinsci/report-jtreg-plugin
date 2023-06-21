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
import io.jenkins.plugins.report.jtreg.model.BuildReport;
import io.jenkins.plugins.report.jtreg.model.ProjectReport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportProjectAction implements Action {

    private final Job<?, ?> job;
    private final Set<String> prefixes = new HashSet<>();

    public ReportProjectAction(Job<?, ?> job, Set<String> prefixes) {
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
        List<String> blisted = new BuildSummaryParser(prefixes, settings).getDenylisted(job);
        List<String> wlisted = new BuildSummaryParser(prefixes, settings).getAllowlisted(job);
        int allowListSizeWithoutSurroundings = new BuildSummaryParser(prefixes, settings).getAllowListSizeWithoutSurroundings(job);
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
                .collect(Collectors.joining(", ", "", " Reports"+appendix));
    }

    @Override
    public String getUrlName() {
        return "java-reports";
    }

    public ProjectReport getChartData() {
        AbstractReportPublisher settings = ReportAction.getAbstractReportPublisher(((Project) job).getPublishersList());
        List<BuildReport> reports = new BuildSummaryParser(prefixes, settings).parseJobReports(job);
        ProjectReport report = new ProjectReport(
                reports,
                collectImprovements(reports),
                collectRegressions(reports));
        cacheTotals(((Project) job).getRootDir(), report);
        return report;
    }

    private List<Integer> collectImprovements(List<BuildReport> reports) {
        List<Integer> result = new ArrayList<>();

        Set<String> prev = null;
        for (BuildReport report : reports) {
            if (prev == null) {
                prev = collectTestNames(report);
                result.add(0);
                continue;
            }
            Set<String> current = collectTestNames(report);

            long count = prev.stream()
                    .sequential()
                    .filter(s -> !current.contains(s))
                    .count();
            result.add((int) count);

            prev = current;
        }

        return result;
    }

    private List<Integer> collectRegressions(List<BuildReport> reports) {
        List<Integer> result = new ArrayList<>();
        Set<String> prev = null;
        for (BuildReport report : reports) {
            if (prev == null) {
                prev = collectTestNames(report);
                result.add(0);
                continue;
            }
            Set<String> current = collectTestNames(report);
            Set<String> finalPrev = new HashSet<>(prev);

            long count = current.stream()
                    .sequential()
                    .filter(s -> !finalPrev.contains(s))
                    .count();
            result.add((int) count);

            prev = current;
        }

        return result;
    }

    private Set<String> collectTestNames(BuildReport report) {
        return report.getSuites().stream()
                .sequential()
                .filter(s -> s.getReport().getTestProblems() != null)
                .filter(s -> s.getReport().getTestProblems().size() > 0)
                .flatMap(s -> s.getReport().getTestProblems().stream()
                        .sequential()
                        .map(t -> s.getName() + " / " + t.getName()))
                .collect(Collectors.toSet());
    }

    public static void cacheTotals(File rootBuild, ProjectReport projectRreport) {
        try {
            cacheTotalsImpl(rootBuild, projectRreport);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static final String CACHEDSUMMREGRESSIONSPROPERTIES = "cached-summ-regressions.properties";
    private static final String CACHEDSUMMRESULTSPROPERTIES = "cached-summ-results.properties";

    //this happily ignores combined jck+jtreg reporting, but as it is never used, it may be already corupted elsewhere
    static void cacheTotalsImpl(File rootBuild, ProjectReport projectRreport) throws IOException {
        cacheSumms(rootBuild, projectRreport.getReports());
        for (int i = 0; i < projectRreport.getReports().size(); i++) {
            BuildReport buildReport = projectRreport.getReports().get(i);
            File cachedRegressions = getCachedRegressionsFile(rootBuild, buildReport);
            if (!cachedRegressions.exists()) {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cachedRegressions), "utf-8"))) {
                    bw.write("jrp.improvements=" + projectRreport.getImprovements().get(i));
                    bw.newLine();
                    bw.write("jrp.regressions=" + projectRreport.getRegressions().get(i));
                    bw.newLine();
                }

            }

        }

    }

    private static File getCachedRegressionsFile(File rootBuild, BuildReport buildReport) {
        return getCachedFile(rootBuild, buildReport, CACHEDSUMMREGRESSIONSPROPERTIES);
    }

    private static File getCachedResultsFile(File rootBuild, BuildReport report) {
        return getCachedFile(rootBuild, report, CACHEDSUMMRESULTSPROPERTIES);
    }

    private static File getCachedFile(File root, BuildReport report, String name) {
        return new File(new File(new File(root, "builds"), report.getBuildNumber() + ""), name);
    }

//this happily ignores combined jck+jtreg reporting, but as it is never used, it may be already corupted elsewhere
    static void cacheSumms(File rootBuild, List<BuildReport> reports) throws IOException {
        for (BuildReport report : reports) {
            File cachedResults = getCachedResultsFile(rootBuild, report);
            if (!cachedResults.exists()) {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cachedResults), "utf-8"))) {
                    bw.write("jrp.errors=" + report.getError());
                    bw.newLine();
                    bw.write("jrp.failures=" + report.getFailed());
                    bw.newLine();
                    bw.write("jrp.notrun=" + report.getNotRun());
                    bw.newLine();
                    bw.write("jrp.passed=" + report.getPassed());
                    bw.newLine();
                    bw.write("jrp.total=" + report.getTotal());
                    bw.newLine();
                    bw.write("jrp.failedAndErrors=" + (report.getFailed() + report.getError()));
                    bw.newLine();
                }
            }
        }
    }
}
