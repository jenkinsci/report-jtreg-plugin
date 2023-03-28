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
package io.jenkins.plugins.report.jtreg;

import io.jenkins.plugins.report.jtreg.model.BuildReport;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.SuiteTestChanges;
import io.jenkins.plugins.report.jtreg.model.SuiteTestsWithResults;
import io.jenkins.plugins.report.jtreg.model.SuitesWithResults;

import java.util.List;

public class BuildReportExtended extends BuildReport {

    private final List<String> addedSuites;
    private final List<String> removedSuites;
    private final List<SuiteTestChanges> testChanges;
    private final int total;
    private final int notRun;
    private final SuitesWithResults allTests;
    private final String job;

    public BuildReportExtended(int buildNumber, String buildName, int passed, int failed, int error, List<Suite> suites,
                               List<String> addedSuites, List<String> removedSuites, List<SuiteTestChanges> testChanges, int total, int notRun, SuitesWithResults allTests, String job) {
        super(buildNumber, buildName, passed, failed, error, suites, total, notRun);
        this.job = job;
        this.addedSuites = addedSuites;
        this.removedSuites = removedSuites;
        this.testChanges = testChanges;
        this.total = total;
        this.notRun = notRun;
        this.allTests = allTests;
    }

    public String getPreviousLink() {
        return "../../" + (getBuildNumber() - 1) + "/java-reports";
    }

    public String getPreviousLinkName() {
        return " << " + (getBuildNumber() - 1) + " << ";
    }

    public String getNextLink() {
        return "../../" + (getBuildNumber() + 1) + "/java-reports";
    }

    public String getNextLinkName() {
        return " >> " + (getBuildNumber() + 1) + " >> ";
    }

    public List<String> getAddedSuites() {
        return addedSuites;
    }

    public List<String> getRemovedSuites() {
        return removedSuites;
    }

    public List<SuiteTestChanges> getTestChanges() {
        return testChanges;
    }

    public String getJob() {
        return job;
    }

    @Override
    public int getTotal() {
        return total;
    }

    @Override
    public int getNotRun() {
        return notRun;
    }

    public SuitesWithResults getAllTests() {
        return allTests;
    }

    private static String getDiffUrlStub(){
        return SuiteTestsWithResults.getDiffServer() + "?generated-part=+-view%3Ddiff-list+++-view%3Ddiff-summary+++-view%3Ddiff-summary-suites+++-view%3Dinfo-problems+++-view%3Dinfo-summary+++-output%3Dhtml++-fill++&custom-part=";//+job+numbers //eg as above;
    }

    public String getLinkDiff() {
        return getDiffUrlStub() + job + "+" + getBuildNumber() + "+" + lowestBuildForFil();
    }

    private int lowestBuildForFil() {
        if (getBuildNumber() > 10) {
            return getBuildNumber() - 10;
        } else {
            return 1;
        }
    }

    private static String getTracesUrlStub() {
        return SuiteTestsWithResults.getDiffServer() + "?generated-part=+-view%3Dinfo+++-output%3Dhtml++&custom-part=";//+job+numbers //eg as above;
    }

    public String getLinkTraces() {
        return getTracesUrlStub() + job + "+" + getBuildNumber();
    }

    public boolean isDiffTool() {
        return JenkinsReportJckGlobalConfig.isGlobalDiffUrl();
    }

}
