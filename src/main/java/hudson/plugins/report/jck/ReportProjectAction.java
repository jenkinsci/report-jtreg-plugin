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

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Project;
import static hudson.plugins.report.jck.ReportAction.getAbstractReportPublisher;
import hudson.plugins.report.jck.model.BuildReport;
import hudson.plugins.report.jck.model.ProjectReport;
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
        AbstractReportPublisher settings = getAbstractReportPublisher(((Project)job).getPublishersList());
        List<String> blisted = new BuildSummaryParser(prefixes, settings).getBlacklisted(job);
        String appendix = "";
        if (blisted.size()>0){
            appendix=" (blacklisted "+blisted.size()+")";
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
        AbstractReportPublisher settings = getAbstractReportPublisher(((Project)job).getPublishersList());
        List<BuildReport> reports = new BuildSummaryParser(prefixes, settings).parseJobReports(job);
        return new ProjectReport(
                reports,
                collectImprovements(reports),
                collectRegressions(reports));
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

}
