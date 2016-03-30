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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.plugins.report.jck.model.BuildReport;
import hudson.plugins.report.jck.model.Report;
import hudson.plugins.report.jck.model.Suite;
import hudson.plugins.report.jck.model.SuiteTestChanges;
import hudson.plugins.report.jck.model.SuiteTests;
import hudson.plugins.report.jck.model.TestStatus;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static hudson.plugins.report.jck.Constants.REPORT_JSON;
import static hudson.plugins.report.jck.Constants.REPORT_TESTS_LIST_JSON;

public class BuildSummaryParser {

    private final Set<String> prefixes = new HashSet<>();

    public BuildSummaryParser(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }
        this.prefixes.addAll(prefixes);
    }

    public BuildSummaryParser(Set<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) {
            throw new IllegalArgumentException("Prefixes cannot be null or empty");
        }
        this.prefixes.addAll(prefixes);
    }

    public List<BuildReport> parseJobReports(Job<?, ?> job) {
        return parseJobReports(job, 10);
    }

    public List<BuildReport> parseJobReports(Job<?, ?> job, int limit) {
        List<BuildReport> list = new ArrayList<>();
        for (Run run : job.getBuilds()) {
            if (run.getResult() == null || run.getResult().isWorseThan(Result.UNSTABLE)) {
                continue;
            }
            try {
                BuildReport report = parseBuildReport(run);
                list.add(report);
            } catch (Exception ignore) {
            }
            if (list.size() == limit) {
                break;
            }
        }
        Collections.reverse(list);
        return list;
    }

    public BuildReport parseBuildReport(Run<?, ?> build) throws Exception {
        List<Suite> suites = parseBuildSummary(build);
        int passed = 0;
        int failed = 0;
        int error = 0;

        for (Suite suite : suites) {
            passed += suite.getReport().getTestsPassed();
            failed += suite.getReport().getTestsFailed();
            error += suite.getReport().getTestsError();
        }

        return new BuildReport(build.getNumber(), build.getDisplayName(), passed, failed, error, suites);
    }

    public BuildReportExtended parseBuildReportExtended(Run<?, ?> build) throws Exception {
        List<SuiteTests> currentBuildTestsList = parseSuiteTests(build);
        List<SuiteTests> prevBuildTestsList;
        Run<?, ?> previousNotFailedBuild = build.getPreviousNotFailedBuild();
        if (previousNotFailedBuild != null) {
            prevBuildTestsList = parseSuiteTests(previousNotFailedBuild);
        } else {
            prevBuildTestsList = new ArrayList<>();
        }

        Set<String> prevSuites = prevBuildTestsList.stream()
                .sequential()
                .map(s -> s.getName())
                .collect(Collectors.toSet());
        List<String> addedSuites = currentBuildTestsList.stream()
                .sequential()
                .map(s -> s.getName())
                .filter(s -> !prevSuites.contains(s))
                .collect(Collectors.toList());

        Set<String> currentSuites = currentBuildTestsList.stream()
                .sequential()
                .map(s -> s.getName())
                .collect(Collectors.toSet());
        List<String> removedSuites = prevBuildTestsList.stream()
                .sequential()
                .map(s -> s.getName())
                .filter(s -> !currentSuites.contains(s))
                .collect(Collectors.toList());

        List<SuiteTestChanges> result = new ArrayList<>();
        BuildReport currentReport = parseBuildReport(build);
        if (previousNotFailedBuild != null) {
            BuildReport previousReport = parseBuildReport(previousNotFailedBuild);
            Map<String, Report> prevReportsMap = previousReport.getSuites().stream()
                    .sequential()
                    .collect(Collectors.toMap(s -> s.getName(), s -> s.getReport()));
            for (Suite suite : currentReport.getSuites()) {
                if (!prevReportsMap.containsKey(suite.getName())) {
                    continue;
                }

                Set<String> currentFailedEroredTests = suite.getReport().getTestProblems().stream()
                        .sequential()
                        .map(t -> t.getName())
                        .collect(Collectors.toSet());
                Set<TestDescriptor> currentTests = suite.getReport().getTestProblems().stream()
                        .sequential()
                        .map(t -> new TestDescriptor(t.getName(), t.getStatus()))
                        .collect(Collectors.toSet());
                currentTests.addAll(currentBuildTestsList.stream()
                        .sequential()
                        .filter(s -> suite.getName().equals(s.getName()))
                        .flatMap(s -> s.getTests().stream())
                        .filter(s -> !currentFailedEroredTests.contains(s))
                        .map(s -> new TestDescriptor(s, TestStatus.PASSED))
                        .collect(Collectors.toList()));

                Set<String> previousFailedErroredTests = prevReportsMap.get(suite.getName()).getTestProblems().stream()
                        .sequential()
                        .map(t -> t.getName())
                        .collect(Collectors.toSet());
                Set<TestDescriptor> previousTests = prevReportsMap.get(suite.getName()).getTestProblems().stream()
                        .sequential()
                        .map(t -> new TestDescriptor(t.getName(), t.getStatus()))
                        .collect(Collectors.toSet());
                previousTests.addAll(prevBuildTestsList.stream()
                        .sequential()
                        .filter(s -> suite.getName().equals(s.getName()))
                        .flatMap(s -> s.getTests().stream())
                        .filter(s -> !previousFailedErroredTests.contains(s))
                        .map(s -> new TestDescriptor(s, TestStatus.PASSED))
                        .collect(Collectors.toList()));

                List<TestDescriptor> testChanges = currentTests.stream()
                        .sequential()
                        .filter(t -> !previousTests.contains(t))
                        .collect(Collectors.toList());

                SuiteTestChanges changes = new SuiteTestChanges(
                        suite.getName(),
                        testChanges.stream()
                        .sequential()
                        .filter(t -> t.status == TestStatus.FAILED)
                        .map(t -> t.name)
                        .collect(Collectors.toList()),
                        testChanges.stream()
                        .sequential()
                        .filter(t -> t.status == TestStatus.ERROR)
                        .map(t -> t.name)
                        .collect(Collectors.toList()),
                        testChanges.stream()
                        .sequential()
                        .filter(t -> t.status == TestStatus.PASSED)
                        .map(t -> t.name)
                        .collect(Collectors.toList()),
                        addedSuites,
                        removedSuites);
                result.add(changes);
            }
        }

        return new BuildReportExtended(
                currentReport.getBuildNumber(),
                currentReport.getBuildName(),
                currentReport.getPassed(),
                currentReport.getFailed(),
                currentReport.getError(),
                currentReport.getSuites(),
                addedSuites,
                removedSuites,
                result);
    }

    private List<Suite> parseBuildSummary(Run<?, ?> build) throws Exception {
        List<Suite> result = new ArrayList<>();
        for (String prefix : prefixes) {
            File reportFile = new File(build.getRootDir(), prefix + "-" + REPORT_JSON);
            if (reportFile.exists() && reportFile.isFile() && reportFile.canRead()) {
                try (Reader in = new InputStreamReader(new BufferedInputStream(new FileInputStream(reportFile)),
                        StandardCharsets.UTF_8)) {
                    List<Suite> list = new Gson().fromJson(in, new TypeToken<List<Suite>>() {
                    }.getType());
                    result.addAll(list);
                }
            }
        }
        return result;
    }

    private List<SuiteTests> parseSuiteTests(Run<?, ?> build) throws Exception {
        List<SuiteTests> result = new ArrayList<>();
        for (String prefix : prefixes) {
            File suiteTestsFile = new File(build.getRootDir(), prefix + "-" + REPORT_TESTS_LIST_JSON);
            if (suiteTestsFile.exists() && suiteTestsFile.isFile() && suiteTestsFile.canRead()) {
                try (Reader in = new InputStreamReader(new BufferedInputStream(new FileInputStream(suiteTestsFile)),
                        StandardCharsets.UTF_8)) {
                    List<SuiteTests> list = new Gson().fromJson(in, new TypeToken<List<SuiteTests>>() {
                    }.getType());
                    result.addAll(list);
                }
            }
        }
        return result;
    }

    private class TestDescriptor {

        private final String name;
        private final TestStatus status;

        public TestDescriptor(String name, TestStatus status) {
            this.name = name;
            this.status = status;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.name);
            hash = 79 * hash + Objects.hashCode(this.status);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TestDescriptor other = (TestDescriptor) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (this.status != other.status) {
                return false;
            }
            return true;
        }

    }

}
