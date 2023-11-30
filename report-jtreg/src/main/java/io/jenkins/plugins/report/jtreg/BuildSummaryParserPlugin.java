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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.report.jtreg.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromRun;
import hudson.util.RunList;

public class BuildSummaryParserPlugin extends BuildSummaryParser {

    private static final UrlsProvider urlsProvider= new UrlsProviderPlugin();
    private static interface ListProvider {

        String getList();

        int getSurrounding();
    }
    private final AbstractReportPublisher settings;

    public BuildSummaryParserPlugin(Collection<String> prefixes, AbstractReportPublisher settings) {
        super(prefixes, urlsProvider);
        this.prefixes.addAll(prefixes);
        this.settings = settings;
        this.buildReportExtendedFactory = new BuildReportExtendedPluginFactory();
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

    public List<BuildReportPlugin> parseJobReports(Job<?, ?> job) {
        return parseJobReports(job.getBuilds());
    }

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    public List<BuildReportPlugin> parseJobReports(RunList<?> runs) {
        int limit = getMaxItems();
        List<BuildReportPlugin> list = new ArrayList<>();
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
                    list.add(new BuildReportPlugin(
                            report.getBuildNumber(),
                            report.getBuildName(),
                            report.getPassed(),
                            report.getFailed(),
                            report.getError(),
                            report.getSuites(),
                            report.getTotal(),
                            report.getBuildNumber()
                    ));
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

    public BuildReport parseBuildReport(Run<?, ?> build) throws Exception {
        return parseBuildReport(new RunWrapperFromRun(build));
    }

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    public BuildReportExtended parseBuildReportExtended(Run<?, ?> build) throws Exception {
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

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Alhoug never called, this method is here to demonstrate (and ocassionally being used) how to access the root dir from run/build")
    private List<SuiteTests> parseSuiteTests(Run<?, ?> build) throws Exception {
        return parseSuiteTests(build.getRootDir());
    }
}
