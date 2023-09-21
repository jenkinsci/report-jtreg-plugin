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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.report.jtreg.model.*;

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

import io.jenkins.plugins.report.jtreg.wrappers.RunWrapper;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDir;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromRun;
import hudson.util.RunList;

public class BuildSummaryParserPlugin extends BuildSummaryParser {

    private static interface ListProvider {

        String getList();

        int getSurrounding();
    }

    private final Set<String> prefixes = new HashSet<>();
    private final AbstractReportPublisher settings;

    public BuildSummaryParserPlugin(Collection<String> prefixes, AbstractReportPublisher settings) {
        super(prefixes);
        this.prefixes.addAll(prefixes);
        this.settings = settings;
    }

    List<String> getDenylisted(Job<?, ?> job) {
        return getDenylisted(job.getBuilds());

    }

    List<String> getAllowlisted(Job<?, ?> job) {
        return getAllowlisted(job.getBuilds());
    }

    int getAllowListSizeWithoutSurroundings(Job<?, ?> job) {
        return getAllowListSizeWithoutSurroundings(job.getBuilds()).size();
    }

    List<String> getDenylisted(RunList<?> runs) {
        return getList(runs, new ListProvider() {
            @Override
            public String getList() {
                if (settings == null) {
                    return "";
                } else {
                    return settings.getResultsDenyList();
                }
            }

            @Override
            public int getSurrounding() {
                return 0;
            }
        });
    }

    List<String> getAllowlisted(RunList<?> runs) {
        return getList(runs, new ListProvider() {
            @Override
            public String getList() {
                if (settings == null) {
                    return "";
                } else {
                    return settings.getResultsAllowList();
                }
            }

            @Override
            public int getSurrounding() {
                return settings.getRangeAroundAlist();
            }
        });
    }

    List<String> getAllowListSizeWithoutSurroundings(RunList<?> runs) {
        return getList(runs, new ListProvider() {
            @Override
            public String getList() {
                return settings.getResultsAllowList();
            }

            @Override
            public int getSurrounding() {
                return 0;
            }
        });
    }

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    List<String> getList(RunList<?> runs, ListProvider provider) {
        final String list = provider.getList();
        if (settings == null || list == null || list.trim().isEmpty()) {
            return Collections.emptyList();
        }
        int limit = getMaxItems();
        List<String> listed = new ArrayList<>(limit);
        Run[] builds = runs.toArray(new Run[0]);
        for (int i = 0; i < builds.length; i++) {
            Run run = builds[i];
            if (run.getResult() == null || run.getResult().isWorseThan(Result.UNSTABLE)) {
                continue;
            }
            String[] items = list.split("\\s+");
            for (String item : items) {
                if (run.getDisplayName().matches(item)) {
                    int numberOfFailedBuilds = 0;
                    for (int j = 0; j <= provider.getSurrounding() + numberOfFailedBuilds; j++) {
                        if (addNotFailedBuild(i + j, listed, builds)) {
                            numberOfFailedBuilds++;
                        }
                    }
                    numberOfFailedBuilds = 0;
                    for (int j = -1; j >= -(provider.getSurrounding() + numberOfFailedBuilds); j--) {
                        if (addNotFailedBuild(i + j, listed, builds)) {
                            numberOfFailedBuilds++;
                        }
                    }
                }
            }
        }
        return listed;
    }

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    private boolean addNotFailedBuild(int position, List<String> result, Run[] builds) {
        if (position >= 0 && position < builds.length) {
            boolean crashed = builds[position].getResult() == null || builds[position].getResult().isWorseThan(Result.UNSTABLE);
            if (crashed) {
                return true;
            }
            /*Preventing duplicates in allowlist. Not because of the graph, there is
            already chunk of code preventing from showing duplicity in the graph.
            (The final list are recreated again with help of these lists)
            Its because lenght of allowlist which is shown over the graph.
            BUG
            We have some point(a) which is in range around allowlist and point(b) which
            have same name but its not in range. Bug is that both points are shown in result
            its caused by generating second array(graph points) from names contained in this array*/
            if (!result.contains(builds[position].getDisplayName())) {
                result.add(builds[position].getDisplayName());
            }
        }
        return false;
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

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    public List<BuildReport> parseJobReports(RunList<?> runs) {
        int limit = getMaxItems();
        List<BuildReport> list = new ArrayList<>();
        List<String> denylisted = getDenylisted(runs);
        List<String> allowlisted = getAllowlisted(runs);
        for (Run run : runs) {
            if (run.getResult() == null || run.getResult().isWorseThan(Result.UNSTABLE)) {
                continue;
            }
            if (denylisted.contains(run.getDisplayName())) {
                continue;
            }
            if (!allowlisted.contains(run.getDisplayName()) && !allowlisted.isEmpty()) {
                continue;
            }

            try {
                BuildReport report = parseBuildReport(run);
                if (!report.isInvalid()) {
                    list.add(report);
                }
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

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    public BuildReportExtendedPlugin parseBuildReportExtended(Run<?, ?> build) throws Exception {
        AbstractProject project = ((AbstractBuild) build).getProject();
        Run[] builds = (Run[]) project.getBuilds().toArray(new Run[0]);
        RunWrapperFromRun previousPassedOrUnstable = null;
        //0 is latest one eg #115, where [lenght-1] is first  one = #0
        int thisInArray = -1;
        for (int i = 0; i < builds.length; i++) {
            if (builds[i].equals(build)) {
                thisInArray = i;
                break;
            }
        }
        //the comparsion would be of latest (see +1 lower) against last stable. Not sure what is worse or better
        if (thisInArray == -1) {
            System.err.println("Warning " + build.toString() + " not found in builds of #" + builds.length);
        }
        for (int i = thisInArray + 1; i < builds.length; i++) {
            Run run = builds[i];
            if (run != null && run.getResult() != null && !run.getResult().isWorseThan(Result.UNSTABLE)) {
                previousPassedOrUnstable = new RunWrapperFromRun(run);
                break;
            }
        }
        return parseBuildReportExtended(new RunWrapperFromRun(build), previousPassedOrUnstable);
    }

    public BuildReportExtendedPlugin parseBuildReportExtended(RunWrapper build, RunWrapper previousPassedOrUnstable) throws Exception {
        List<SuiteTests> currentBuildTestsList = parseSuiteTests(build.getRoot());
        List<SuiteTests> prevBuildTestsList;
        if (previousPassedOrUnstable != null) {
            prevBuildTestsList = parseSuiteTests(previousPassedOrUnstable.getRoot());
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
        if (previousPassedOrUnstable != null) {
            BuildReport previousReport = parseBuildReport(previousPassedOrUnstable);
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
        String job = build.getRoot().getParentFile().getParentFile().getName();
        try {
            allTests = SuitesWithResults.create(currentBuildTestsList, parseBuildReport(build), job, build.getNumber());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new BuildReportExtendedPlugin(
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
                allTests,
                job);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Alhoug never called, this method is here to demonstrate (and ocassionally being used) how to access the root dir from run/build")
    private List<Suite> parseBuildSummary(Run<?, ?> build) throws Exception {
        return parseBuildSummary(build.getRootDir());
    }

    private List<Suite> parseBuildSummary(File rootDir) throws Exception {
        List<Suite> result = new ArrayList<>();
        for (String prefix : prefixes) {
            File reportFile = new File(rootDir, prefix + "-" + Constants.REPORT_JSON);
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

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Alhoug never called, this method is here to demonstrate (and ocassionally being used) how to access the root dir from run/build")
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
            File suiteTestsFile = new File(build, prefix + "-" + Constants.REPORT_TESTS_LIST_JSON);
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

    private static class TestDescriptor {

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
