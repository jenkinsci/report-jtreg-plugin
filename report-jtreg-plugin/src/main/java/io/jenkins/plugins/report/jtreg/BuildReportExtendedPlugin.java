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

import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.SuiteTestChanges;
import io.jenkins.plugins.report.jtreg.model.SuiteTestsWithResultsPlugin;
import io.jenkins.plugins.report.jtreg.model.SuitesWithResults;

import java.util.List;

public class BuildReportExtendedPlugin extends BuildReportExtended {
    private final String job;

    public BuildReportExtendedPlugin(int buildNumber, String buildName, int passed, int failed, int error, List<Suite> suites,
                               List<String> addedSuites, List<String> removedSuites, List<SuiteTestChanges> testChanges, int total, int notRun, SuitesWithResults allTests, String job) {
        super(buildNumber, buildName, passed, failed, error, suites, addedSuites, removedSuites, testChanges, total, notRun, allTests, job);
        this.job = job;
    }

    private static String getDiffUrlStub(){
        return SuiteTestsWithResultsPlugin.getDiffServer() + "?generated-part=+-view%3Ddiff-list+++-view%3Ddiff-summary+++-view%3Ddiff-summary-suites+++-view%3Dinfo-problems+++-view%3Dinfo-summary+++-output%3Dhtml++-fill++&custom-part=";//+job+numbers //eg as above;
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
        return SuiteTestsWithResultsPlugin.getDiffServer() + "?generated-part=+-view%3Dinfo+++-output%3Dhtml++&custom-part=";//+job+numbers //eg as above;
    }

    private static String getCompUrlStub() {
        return SuiteTestsWithResultsPlugin.getCompServer() + "?generated-part=&custom-part=--compare+";//+job+numbers //eg as above;
    }

    public String getLinkTraces() {
        return getTracesUrlStub() + job + "+" + getBuildNumber();
    }

    public boolean isDiffTool() {
        return JenkinsReportJckGlobalConfig.isGlobalDiffUrl();
    }

    public String getCompareArches() {
        return getCompUrlStub() + ("--query+" + job.replaceAll("-\\.", "+") + "+--nvr+" + getBuildName()).replace("#","%23");
    }

    public String getCompareOsses() {
        return getCompUrlStub() + ("--query+" + job.replaceAll("-\\.", "+") + "+--nvr+" + getBuildName()).replace("#","%23");
    }

    public String getCompareVariants() {
        return getCompUrlStub() + ("--query+" + job.replaceAll("-\\.", "+") + "+--nvr+" + getBuildName()).replace("#","%23");
    }
}