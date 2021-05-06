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

import com.google.gson.GsonBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.report.jck.model.Report;
import hudson.plugins.report.jck.model.ReportFull;
import hudson.plugins.report.jck.model.Suite;
import hudson.plugins.report.jck.model.SuiteTests;
import hudson.plugins.report.jck.parsers.ReportParser;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.kohsuke.stapler.DataBoundSetter;

import static hudson.plugins.report.jck.Constants.REPORT_JSON;
import static hudson.plugins.report.jck.Constants.REPORT_TESTS_LIST_JSON;
import hudson.plugins.report.jck.model.BuildReport;
import java.util.Arrays;

abstract public class AbstractReportPublisher extends Recorder {

    private String reportFileGlob;
    private String resultsBlackList;
    private String resultsWhiteList;
    private String maxBuilds;
    private int rangeAroundWlist;

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
                s -> s.getReport() != null && (s.getReport().getTestsError() != 0 || s.getReport().getTestsFailed() != 0) || s.getReport().getTestsTotal() <= 0)) {
            build.setResult(Result.UNSTABLE);
        }
        storeFailuresSummary(report, new File(build.getRootDir(), prefix() + "-" + REPORT_JSON));
        storeFullTestsList(report, new File(build.getRootDir(), prefix() + "-" + REPORT_TESTS_LIST_JSON));
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

    private void storeFailuresSummary(List<Suite> reportFull, File jsonFile) throws IOException {
        List<Suite> reportShort = reportFull.stream()
                .sequential()
                .map(s -> new Suite(
                        s.getName(),
                        new Report(
                                s.getReport().getTestsPassed(),
                                s.getReport().getTestsNotRun(),
                                s.getReport().getTestsFailed(),
                                s.getReport().getTestsError(),
                                s.getReport().getTestsTotal(),
                                s.getReport().getTestProblems())))
                .sorted()
                .collect(Collectors.toList());
        try (Writer out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(jsonFile)),
                StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(reportShort, out);
        }
        cacheReport(reportShort, jsonFile);
    }

    private void cacheReport(List<Suite> reportShort, File jsonFile) {
        try {
            int passedSumm = 0;
            int notRunSumm = 0;
            int failedSumm = 0;
            int errorSumm = 0;
            int totalSumm = 0;
            StringBuilder nameb = new StringBuilder();
            for (Suite s : reportShort) {
                passedSumm += s.getReport().getTestsPassed();
                notRunSumm += s.getReport().getTestsNotRun();
                failedSumm += s.getReport().getTestsFailed();
                errorSumm += s.getReport().getTestsError();
                totalSumm += s.getReport().getTestsTotal();
                nameb.append(s.getName()).append(" ");
            }
            File buildDir = jsonFile.getParentFile();
            int buildNumber = Integer.parseInt(buildDir.getName());
            BuildReport br = new BuildReport(buildNumber, nameb.toString().trim(), passedSumm, failedSumm, errorSumm, reportShort, totalSumm, notRunSumm);
            ReportProjectAction.cacheSumms(buildDir.getParentFile().getParentFile(), Arrays.asList(new BuildReport[]{br}));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void storeFullTestsList(List<Suite> reportFull, File jsonFile) throws IOException {
        List<SuiteTests> suites = reportFull.stream()
                .sequential()
                .map(s -> new SuiteTests(
                        s.getName(),
                        s.getReport() instanceof ReportFull ? ((ReportFull) s.getReport()).getTestsList() : null))
                .sorted()
                .collect(Collectors.toList());
        try (Writer out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(jsonFile)),
                StandardCharsets.UTF_8)) {
            new GsonBuilder().create().toJson(suites, out);
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
    public void setResultsBlackList(String resultsBlackList) {
        this.resultsBlackList = resultsBlackList;
    }

    public String getResultsBlackList() {
        return resultsBlackList;
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

    public String getResultsWhiteList() {
        return resultsWhiteList;
    }

    @DataBoundSetter
    public void setResultsWhiteList(String resultsWhiteList) {
        this.resultsWhiteList = resultsWhiteList;
    }

    public int getRangeAroundWlist() {
        return rangeAroundWlist;
    }

    @DataBoundSetter
    public void setRangeAroundWlist(int rangeAroundWlist) {
        this.rangeAroundWlist = rangeAroundWlist;
    }

}
