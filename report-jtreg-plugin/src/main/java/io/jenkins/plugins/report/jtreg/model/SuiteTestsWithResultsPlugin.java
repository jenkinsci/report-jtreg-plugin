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
package io.jenkins.plugins.report.jtreg.model;

import io.jenkins.plugins.report.jtreg.JenkinsReportJckGlobalConfig;

import java.util.List;

public class SuiteTestsWithResultsPlugin extends SuiteTestsWithResults {

    public static String getDiffServer() {
        return JenkinsReportJckGlobalConfig.getGlobalDiffUrl()  + "/diff.html";
    }
    public static String getCompServer() {
        return JenkinsReportJckGlobalConfig.getGlobalDiffUrl()  + "/comp.html";
    }

    private static String getDiffUrlStub() {
        return getDiffServer() + "?generated-part=+-view%3Dall-tests+++-view%3Dinfo-summary+++-view%3Dinfo-summary-suites+++-output%3Dhtml++&custom-part=";//+job+number //eg as above;
    }

    private final String job;
    private final int id;

    public SuiteTestsWithResultsPlugin(String name, List<StringWithResult> tests, String job, int id) {
        super(name, tests, job, id);
        this.job = job;
        this.id = id;
    }

    public String getLink() {
        return getDiffUrlStub() + job + "+" + id;
    }
}