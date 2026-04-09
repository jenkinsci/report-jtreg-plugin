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
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Project;
import hudson.model.Result;
import io.jenkins.plugins.report.jtreg.model.*;
import io.jenkins.plugins.report.jtreg.parsers.ReportParser;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import io.jenkins.plugins.report.jtreg.writers.WritersManager;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundSetter;

abstract public class AbstractReportPublisher extends Recorder {

    private String reportFileGlob;
    private String resultsDenyList;
    private String resultsAllowList;
    private String maxBuilds;
    private int rangeAroundAlist;
    private static Logger logger = Logger.getLogger(AbstractReportPublisher.class.getName());

    public AbstractReportPublisher(String reportFileGlob) {
        this.reportFileGlob = reportFileGlob;
    }

    abstract protected String defaultReportFileGlob();

    abstract protected ReportParser createReportParser();

    abstract protected String prefix();

    @Override
    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    final public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        String reportFileGlob = getReportFileGlob();
        if (reportFileGlob == null || reportFileGlob.trim().isEmpty()) {
            reportFileGlob = defaultReportFileGlob();
        }
        if (!reportFileGlob.startsWith("glob:")) {
            reportFileGlob = "glob:" + reportFileGlob;
        }
        List<Suite> report = build.getWorkspace().act(
                new ReportParserCallable(reportFileGlob, createReportParser()));
        if (report.stream().anyMatch(
                s -> s.getReport() != null && (s.getReport().getTestsError() != 0 || s.getReport().getTestsFailed() != 0))) {
            build.setResult(Result.UNSTABLE);
        }
        if (report.stream().anyMatch(
                s -> s.getReport() != null && (s.getReport().getTestsTotal() <= 0 || s.getReport().getTestsTotal() == s.getReport().getTestsNotRun()))) {
            String s ="no file by glob " + reportFileGlob + ", no jtr.xml file in tar.xz, wrong xml.xz for jck or no test runs";
            System.err.println(s);
            logger.severe(s);
            build.setResult(Result.FAILURE);
        }
        //first we create the jsons for this run
        WritersManager.storeAllSummaries(prefix(),report, build.getRootDir(),
                new Metadata(build.getDisplayName(), build.getId(), build.getProject().getName()));
        //now we can reuse them to compute diff
        ProjectReport reports = ReportProjectActionUtils.getReport(Set.of(prefix()), (Project) build.getProject(), 2);
        WritersManager.storeAllSummaries(prefix(),report, reports, build.getRootDir(),
                new Metadata(build.getDisplayName(), build.getId(), build.getProject().getName()));
        addReportAction(build);
        return true;
    }

    private void addReportAction(AbstractBuild<?, ?> build) {
        ReportAction action = build.getAction(ReportAction.class);
        if (action == null) {
            action = new ReportAction(build);
            action.addPrefix(prefix());
            build.addAction(action);
        } else {
            action.addPrefix(prefix());
        }
    }

    @Override
    final public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @DataBoundSetter
    public void setReportFileGlob(String reportFileGlob) {
        this.reportFileGlob = reportFileGlob;
    }

    public String getReportFileGlob() {
        return reportFileGlob;
    }

    @DataBoundSetter
    public void setResultsDenyList(String resultsDenyList) {
        this.resultsDenyList = resultsDenyList;
    }

    public String getResultsDenyList() {
        return resultsDenyList;
    }

    @DataBoundSetter
    public void setMaxBuilds(String maxBuilds) {
        this.maxBuilds = maxBuilds;
    }

    public String getMaxBuilds() {
        if (maxBuilds == null) {
            return "10";
        }
        return maxBuilds;
    }

    public int getIntMaxBuilds() {
        try {
            return Integer.parseInt(getMaxBuilds().trim());
        } catch (NumberFormatException ex) {
            return 10;
        }
    }

    public String getResultsAllowList() {
        return resultsAllowList;
    }

    @DataBoundSetter
    public void setResultsAllowList(String resultsAllowList) {
        this.resultsAllowList = resultsAllowList;
    }

    public int getRangeAroundAlist() {
        return rangeAroundAlist;
    }

    @DataBoundSetter
    public void setRangeAroundAlist(int rangeAroundAlist) {
        this.rangeAroundAlist = rangeAroundAlist;
    }

}
