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

import io.jenkins.plugins.report.jtreg.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildReportExtendedPlugin extends BuildReportExtended {
    private final String job;

    public BuildReportExtendedPlugin(int buildNumber, String buildName, int passed, int failed, int error, List<Suite> suites,
                               List<String> addedSuites, List<String> removedSuites, List<SuiteTestChanges> testChanges, int total, int notRun, SuitesWithResults allTests, String job) {
        super(buildNumber, buildName, passed, failed, error, suites, addedSuites, removedSuites, testChanges, total, notRun, allTests, job);
        this.job = job;
    }

    public List<ComparatorLinksGroup> getMatchedComparatorLinksGroups() {
        List<ComparatorLinksGroup> matchedComparatorLinksGroup = new ArrayList<>();
        for (ComparatorLinksGroup link : JenkinsReportJckGlobalConfig.getGlobalComparatorLinksGroups()) {
            if (job.matches(link.getJobMatchRegex())) {
                matchedComparatorLinksGroup.add(link);
            }
        }

        return matchedComparatorLinksGroup;
    }

    public String createComparatorLinkUrl(LinkToComparator ltc) {
        StringBuilder url = new StringBuilder(getCompUrlStub());

        for (String arg : ltc.getComparatorArguments().split("\n")) {
            url.append(arg);
            url.append("+");
        }

        url.append("--regex+");
        url.append(parseToRegex(ltc.getSpliterator(), ltc.getQuery()));

        return url.toString()
                .replace(" ", "+")
                .replace("#", "%23")
                .replace(".", "%2E");
    }

    private String parseToRegex(String spliterator, String query) {
        String[] splitJob = job.split(spliterator);

        String converted = query;

        // finds %N in the query from Jenkins config and replaces it with corresponding part of job name
        Pattern p = Pattern.compile("%-?[0-9]+");
        Matcher m = p.matcher(converted);

        while (m.find()) {
            int number = Integer.parseInt(converted.substring(m.start() + 1, m.end()));

            String replacement;
            if (number > 0) {
                replacement = splitJob[number - 1];
            } else if (number < 0) {
                replacement = splitJob[splitJob.length + number];
            } else {
                throw new RuntimeException("The number in query cannot be zero, only positive or negative whole numbers!");
            }

            converted = converted.replaceFirst("%-?[0-9]+", replacement);
            m = p.matcher(converted);
        }

        return converted;
    }

    private static String getDiffUrlStub(){
        return SuiteTestsWithResultsPlugin.getDiffServer() + "?generated-part=+-view%3Ddiff-list+++-view%3Ddiff-summary+++-view%3Ddiff-summary-suites+++-view%3Dinfo-problems+++-view%3Dinfo-summary+++-output%3Dhtml++-fill++&custom-part=";//+job+numbers //eg as above;
    }

    public String getLinkDiff() {
        return getDiffUrlStub() + getJob() + "+" + getBuildNumber() + "+" + lowestBuildForFil();
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
        return SuiteTestsWithResultsPlugin.getCompServer() + "?generated-part=&custom-part=";
    }

    public String getLinkTraces() {
        return getTracesUrlStub() + getJob() + "+" + getBuildNumber();
    }

    public boolean isDiffTool() {
        return JenkinsReportJckGlobalConfig.isGlobalDiffUrl();
    }

    private static String createDiffUrl() {
        return SuiteTestsWithResultsPlugin.getDiffServer() + "?generated-part=+-view%3Dall-tests+++-output%3Dhtml++-fill++";
    }

    public String getTrackingUrl(Test test) {
        return (createDiffUrl()+"&custom-part=-track%3D"+test.getName()+"++"+getJob()+"++0+-365").replaceAll("#", "%23");
    }
}