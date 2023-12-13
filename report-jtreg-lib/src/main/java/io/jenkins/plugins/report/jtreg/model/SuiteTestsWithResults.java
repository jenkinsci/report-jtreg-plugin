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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;

import java.util.Collections;

public class SuiteTestsWithResults implements java.io.Serializable {
    private static final int MAX = 1000;
    private final String name;
    private UrlsProvider urlsProvider;

    @SuppressFBWarnings(value = "SE_BAD_FIELD", justification =  "should be internal implementation only, and thus ArrayList and that is serialisable")
    private final List<StringWithResult> tests;
    private final String job;
    private final int id;

    public SuiteTestsWithResults(String name, List<StringWithResult> tests, String job, int id) {
        this.name = name;
        this.tests = tests;
        this.job = job;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<StringWithResult> getTests() {
        return Collections.unmodifiableList(tests);
    }

    public List<StringWithResult> getTestsLimited() {
        if (tests.size() <= MAX) {
            return getTests();
        } else {
            final List<StringWithResult> truncated = new ArrayList<>(1001);
            for (int i = 0; i < MAX; i++) {
                StringWithResult get = tests.get(i);
                truncated.add(get);
            }
            return truncated;
        }
    }

    public String getSentence() {
        if (tests == null || tests.isEmpty() ) {
            return "... No tests at all. Feel free to use our cmdline diff tool or ";
        } else if (tests.size() <= MAX) {
            return "... Shown all " + tests.size() + " tests. Feel free to use our cmdline diff tool or ";
        } else {
            return "... Shown " + MAX + " from " + tests.size() + " tests. To see remaining " + (tests.size() - 1000) + " use our cmdline diff tool or ";
        }
    }


    public void setUrlsProvider(UrlsProvider urlsProvider) {
        this.urlsProvider = urlsProvider;
    }

    private String getDiffUrlStub() {
        return urlsProvider.getDiffServer() + "?generated-part=+-view%3Dall-tests+++-view%3Dinfo-summary+++-view%3Dinfo-summary"
                + "-suites+++-output"
                + "%3Dhtml++&custom-part=";//+job+number //eg as above;
    }

    public String getLink() {
        return getDiffUrlStub() + job + "+" + id;
    }

    public static class StringWithResult {

        private final String testName;
        private final TestStatusSimplified status;

        public StringWithResult(String testName, TestStatusSimplified status) {
            this.testName = testName;
            this.status = status;
        }

        public TestStatusSimplified getStatus() {
            return status;
        }

        public String getTestName() {
            return testName;
        }

    }

    public static enum TestStatusSimplified {
        PASSED_OR_MISSING, FAILED_OR_ERROR;

        //jelly dont know enums!
        public boolean isFailed() {
            return (this == FAILED_OR_ERROR);
        }

        public boolean isPassed() {
            return (this == PASSED_OR_MISSING);
        }

        @Override
        public String toString() {
            switch (this) {
                case PASSED_OR_MISSING:
                    return "(PASS or MISSING)";
                case FAILED_OR_ERROR:
                    return "(FAILED or ERROR)";
            }
            throw new RuntimeException("Unknown enum member");
        }
    }
}