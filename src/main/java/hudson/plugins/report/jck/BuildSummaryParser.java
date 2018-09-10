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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static hudson.plugins.report.jck.Constants.REPORT_JSON;
import static hudson.plugins.report.jck.Constants.REPORT_TESTS_LIST_JSON;
import hudson.plugins.report.jck.model.SuiteTestsWithResults;
import hudson.plugins.report.jck.model.SuitesWithResults;
import hudson.plugins.report.jck.wrappers.RunWrapper;
import hudson.plugins.report.jck.wrappers.RunWrapperFromDir;
import hudson.plugins.report.jck.wrappers.RunWrapperFromRun;
import hudson.util.RunList;

public class BuildSummaryParser {

    private static interface ListProvider {

        String getList();

        int getSurrounding();
    }

    private final Set<String> prefixes = new HashSet<>();
    private final AbstractReportPublisher settings;

    public BuildSummaryParser(Collection<String> prefixes, AbstractReportPublisher settings) {
        if (prefixes == null || prefixes.isEmpty()) {
            throw new IllegalArgumentException("Prefixes cannot be null or empty");
        }
        this.prefixes.addAll(prefixes);
        this.settings = settings;
    }

    List<String> getBlacklisted(Job<?, ?> job) {
        return getBlacklisted(job.getBuilds());

    }

    List<String> getWhitelisted(Job<?, ?> job) {
        return getWhitelisted(job.getBuilds());
    }

    int getWhiteListSizeWithoutSurroundings(Job<?, ?> job) {
        return getWhiteListSizeWithoutSurroundings(job.getBuilds()).size();
    }

    List<String> getBlacklisted(RunList<?> runs) {
        return getList(runs, new ListProvider() {
            @Override
            public String getList() {
                return settings.getResultsBlackList();
            }

            @Override
            public int getSurrounding() {
                return 0;
            }
        });
    }

    List<String> getWhitelisted(RunList<?> runs) {
        return getList(runs, new ListProvider() {
            @Override
            public String getList() {
                return settings.getResultsWhiteList();
            }

            @Override
            public int getSurrounding() {
                return settings.getRangeAroundWlist();
            }
        });
    }

    List<String> getWhiteListSizeWithoutSurroundings(RunList<?> runs) {
        return getList(runs, new ListProvider() {
            @Override
            public String getList() {
                return settings.getResultsWhiteList();
            }

            @Override
            public int getSurrounding() {
                return 0;
            }
        });
    }

    List<String> getList(RunList<?> runs, ListProvider provider) {
        int limit = getMaxItems();
        List<String> listed = new ArrayList<>(limit);
        List<BuildReport> list = new ArrayList<>();
        Run[] builds = runs.toArray(new Run[0]);
        for (int i = 0; i < builds.length; i++) {
            Run run = builds[i];
            if (run.getResult() == null || run.getResult().isWorseThan(Result.UNSTABLE)) {
                continue;
            }
            if (settings != null && provider.getList() != null && !provider.getList().trim().isEmpty()) {
                String[] items = provider.getList().split("\\s+");
                for (String item : items) {
                    if (run.getDisplayName().matches(item)) {
                        for (int j = -(provider.getSurrounding()); j < provider.getSurrounding() + 1; j++) {
                            if (i + j >= 0 && i + j < builds.length) {
                                /*Preventing duplicates in whitelist. Not because of the graph, there is
                                already chunk of code preventing from showing duplicity in the graph.
                                (The final list are recreated again with help of these lists)
                                Its because lenght of whitelist which is shown over the graph.*/
                                if (!listed.contains(builds[i + j].getDisplayName())) {
                                    listed.add(builds[i + j].getDisplayName());
                                }
                            }
                        }
                    }
                }
            }
        }
        return listed;
    }

    private int getMaxItems() {
        int limit = 10;
        if (settings != null) {
            limit = settings.getIntMaxBuilds();
        }
        return limit;
    }

    public List<BuildReport> parseJobReports(Job<?, ?> job) {
        return parseJobReports(job.getBuilds());
    }

    public List<BuildReport> parseJobReports(RunList<?> runs) {
        int limit = getMaxItems();
        List<BuildReport> list = new ArrayList<>();
        List<String> blacklisted = getBlacklisted(runs);
        List<String> whitelisted = getWhitelisted(runs);
        for (Run run : runs) {
            if (run.getResult() == null || run.getResult().isWorseThan(Result.UNSTABLE)) {
                continue;
            }
            if (blacklisted.contains(run.getDisplayName())) {
                continue;
            }
            if (!whitelisted.contains(run.getDisplayName()) && !whitelisted.isEmpty()) {
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

    public BuildReport parseJobReports(File dir1) {
        try {
            return parseBuildReport(new RunWrapperFromDir(dir1));
        } catch (Exception ignore) {
            ignore.printStackTrace();
            return null;
        }
    }

    public BuildReport parseBuildReport(Run<?, ?> build) throws Exception {
        return parseBuildReport(new RunWrapperFromRun(build));
    }

    public BuildReport parseBuildReport(RunWrapper build) throws Exception {
        List<Suite> suites = parseBuildSummary(build.getRoot());
        int passed = 0;
        int failed = 0;
        int error = 0;
        int total = 0;
        int notRun = 0;

        for (Suite suite : suites) {
            passed += suite.getReport().getTestsPassed();
            failed += suite.getReport().getTestsFailed();
            error += suite.getReport().getTestsError();
            total += suite.getReport().getTestsTotal();
            notRun += suite.getReport().getTestsNotRun();
        }

        /*
        This condition is very unhappy.  
        Tck is saving to json total summ of tests as all *runnable* tests.  So total=total_runable+notRun (unexpected)
        jtregs are saving total sum of tests of all *run* tests. so   total_runable=total+notRun
        To do this properly, means to fix it in {jck,jtreg}reportPublisher
        but it also means to regenerate all the results:(
         */
        //if (prefixes.contains("jck")){
        //   total -= notRun;
        //}
        //you may seen the incoherency between:
        //https://github.com/judovana/jenkins-report-jck/pull/8/files#diff-55fe100eb47db6ceae5e4a79319d5f1cR147
        //and
        //https://github.com/judovana/jenkins-report-jck/pull/7/files#diff-bac5b237e72448e452669002e9d2eac1R74
        //in addition this chunk seems not fixing the issue of:
        //jtregs currenlty do not have any excluded tests. Once thy have, the graph will probably become broken
        return new BuildReport(build.getNumber(), build.getName(), passed, failed, error, suites, total, notRun);
    }

    public BuildReportExtended parseBuildReportExtended(Run<?, ?> build) throws Exception {
        Run<?, ?> previousNotFailedBuild = build.getPreviousNotFailedBuild();
        return parseBuildReportExtended(new RunWrapperFromRun(build), new RunWrapperFromRun(previousNotFailedBuild));
    }

    public BuildReportExtended parseBuildReportExtended(RunWrapper build, RunWrapper previousNotFailedBuild) throws Exception {
        List<SuiteTests> currentBuildTestsList = parseSuiteTests(build.getRoot());
        List<SuiteTests> prevBuildTestsList;
        if (previousNotFailedBuild != null) {
            prevBuildTestsList = parseSuiteTests(previousNotFailedBuild.getRoot());
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
                Set<TestDescriptor> currentTestsDescriptors = suite.getReport().getTestProblems().stream()
                        .sequential()
                        .map(t -> new TestDescriptor(t.getName(), t.getStatus()))
                        .collect(Collectors.toSet());
                currentTestsDescriptors.addAll(currentBuildTestsList.stream()
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
                Set<TestDescriptor> previousTestsDescriptors = prevReportsMap.get(suite.getName()).getTestProblems().stream()
                        .sequential()
                        .map(t -> new TestDescriptor(t.getName(), t.getStatus()))
                        .collect(Collectors.toSet());
                previousTestsDescriptors.addAll(prevBuildTestsList.stream()
                        .sequential()
                        .filter(s -> suite.getName().equals(s.getName()))
                        .flatMap(s -> s.getTests().stream())
                        .filter(s -> !previousFailedErroredTests.contains(s))
                        .map(s -> new TestDescriptor(s, TestStatus.PASSED))
                        .collect(Collectors.toList()));

                List<TestDescriptor> testChanges = currentTestsDescriptors.stream()
                        .sequential()
                        .filter(t -> !previousTestsDescriptors.contains(t))
                        .collect(Collectors.toList());

                Set<String> previousTests = previousTestsDescriptors.stream()
                        .sequential()
                        .map(d -> d.name)
                        .collect(Collectors.toSet());
                Set<String> currentTests = currentTestsDescriptors.stream()
                        .sequential()
                        .map(d -> d.name)
                        .collect(Collectors.toSet());

                SuiteTestChanges changes = new SuiteTestChanges(
                        // suite name:
                        suite.getName(),
                        // new failures:
                        testChanges.stream()
                        .sequential()
                        .filter(t -> t.status == TestStatus.FAILED)
                        .map(t -> t.name)
                        .collect(Collectors.toList()),
                        // new errors:
                        testChanges.stream()
                        .sequential()
                        .filter(t -> t.status == TestStatus.ERROR)
                        .map(t -> t.name)
                        .collect(Collectors.toList()),
                        // new fixes:
                        testChanges.stream()
                        .sequential()
                        .filter(t -> t.status == TestStatus.PASSED)
                        .map(t -> t.name)
                        .collect(Collectors.toList()),
                        // added tests:
                        currentTests.stream()
                        .sequential()
                        .filter(s -> !previousTests.contains(s))
                        .sorted()
                        .collect(Collectors.toList()),
                        // removed tests:
                        previousTests.stream()
                        .sequential()
                        .filter(s -> !currentTests.contains(s))
                        .sorted()
                        .collect(Collectors.toList()));
                result.add(changes);
            }
        }
        SuitesWithResults allTests = null;
        try {
            allTests = SuitesWithResults.create(currentBuildTestsList, parseBuildReport(build));
        } catch (Exception ex) {
            ex.printStackTrace();
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
                result,
                currentReport.getTotal(),
                currentReport.getNotRun(),
                allTests);
    }

    private List<Suite> parseBuildSummary(Run<?, ?> build) throws Exception {
        return parseBuildSummary(build.getRootDir());
    }

    private List<Suite> parseBuildSummary(File rootDir) throws Exception {
        List<Suite> result = new ArrayList<>();
        for (String prefix : prefixes) {
            File reportFile = new File(rootDir, prefix + "-" + REPORT_JSON);
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
        return parseSuiteTests(build.getRootDir());
    }

    /**
     * this is very costly mehtod, use rarely
     *
     * @param build
     * @return list of all tests in suite
     * @throws Exception
     */
    public List<SuiteTests> parseSuiteTests(File build) throws Exception {
        List<SuiteTests> result = new ArrayList<>();
        for (String prefix : prefixes) {
            File suiteTestsFile = new File(build, prefix + "-" + REPORT_TESTS_LIST_JSON);
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
