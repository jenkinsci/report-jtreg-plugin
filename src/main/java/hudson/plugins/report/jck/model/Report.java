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
package hudson.plugins.report.jck.model;

import java.util.List;

public class Report implements java.io.Serializable {

    private final int testsPassed;
    private final int testsNotRun;
    private final int testsFailed;
    private final int testsError;
    private final int testsTotal;
    private final List<Test> testProblems;

    public Report(int testsPassed, int testsNotRun, int testsFailed, int testsError, int testsTotal, List<Test> testProblems) {
        this.testsPassed = testsPassed;
        this.testsNotRun = testsNotRun;
        this.testsFailed = testsFailed;
        this.testsError = testsError;
        this.testsTotal = testsTotal;
        this.testProblems = testProblems;
    }

    public int getTestsPassed() {
        return testsPassed;
    }

    public int getTestsNotRun() {
        return testsNotRun;
    }

    public int getTestsFailed() {
        return testsFailed;
    }

    public int getTestsError() {
        return testsError;
    }

    public int getTestsTotal() {
        return testsTotal;
    }

    public List<Test> getTestProblems() {
        return testProblems;
    }

}
