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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;

import java.util.Collections;

public class SuiteTestsWithResults implements java.io.Serializable {
    //fixme pass from build system
    public static final String DIFF_SERVER = "http://hydra.brq.redhat.com:9090/diff.html";
    //++wycheproof-jp8-ojdk8~udev~upstream-el6.x86_64-jfrenabled.release.sdk-el7z.x86_64.beaker-x11.defaultgc.legacy.lnxagent.jfroff+3+++jtreg~tier1-jp8-ojdk8~u~upstream-win2012.x86_64-hotspot.release.sdk-win10.x86_64.vagrant-x11.defaultgc.legacy.lnxagent.jfroff++0
    private static final String DIFF_URL = DIFF_SERVER + "?generated-part=+-view%3Dall-tests+++-view%3Dinfo-summary+++-view%3Dinfo-summary-suites+++-output%3Dhtml++&custom-part=";//+job+number //eg as above;
    private static final int MAX = 1000;

    private final String name;
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

    public String getLink() {
        return DIFF_URL + job + "+" + id;
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
