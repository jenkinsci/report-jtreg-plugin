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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hudson.model.Project;
import io.jenkins.plugins.report.jtreg.model.BuildReport;
import io.jenkins.plugins.report.jtreg.model.ProjectReport;

public class ReportProjectActionUtils  {

    public static ProjectReport getReport(Set<String> prefixes, Project job,  int limitOverwrite) {
        AbstractReportPublisher settings = ReportAction.getAbstractReportPublisher(job.getPublishersList());
        List<? extends BuildReport> reports = new BuildSummaryParserPlugin(prefixes, settings).parseJobReports(job, limitOverwrite);
        ProjectReport report = new ProjectReport(
                reports,
                collectImprovements(reports),
                collectRegressions(reports));
        return report;
    }


    private static List<Integer> collectImprovements(List<? extends BuildReport> reports) {
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

    private static List<Integer> collectRegressions(List<? extends BuildReport> reports) {
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

    static private Set<String> collectTestNames(BuildReport report) {
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
