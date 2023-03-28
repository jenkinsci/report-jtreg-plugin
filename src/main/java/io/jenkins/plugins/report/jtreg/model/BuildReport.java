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
package io.jenkins.plugins.report.jtreg.model;

import io.jenkins.plugins.chartjs.Chartjs;
import java.util.List;

public class BuildReport implements java.io.Serializable {

    private final int buildNumber;
    private final String buildName;
    private final String buildNameShortened;
    private final int passed;
    private final int failed;
    private final int error;
    private final int total;
    private final int notRun;
    private final int run;
    private final List<Suite> suites;

    public BuildReport(int buildNumber, String buildName, int passed, int failed, int error, List<Suite> suites, int total, int notRun) {
        this.buildNumber = buildNumber;
        this.buildName = buildName;
        this.passed = passed;
        this.failed = failed;
        this.error = error;
        this.suites = suites;
        this.total = total;
        this.notRun = notRun;
        this.run = 0;
        this.buildNameShortened=Chartjs.getShortName(buildName, buildNumber);
    }

    public String getBuildNameShortened() {
        return buildNameShortened;
    }
    

    public int getBuildNumber() {
        return buildNumber;
    }

    public String getBuildName() {
        return buildName;
    }

    public int getPassed() {
        return passed;
    }

    public int getFailed() {
        return failed;
    }

    public int getError() {
        return error;
    }

    public List<Suite> getSuites() {
        return suites;
    }

    public int getTotal(){
        return total;
    }

    public int getNotRun(){
        return notRun;
    }
    public int getRun(){
        return total - notRun;
    }

}
