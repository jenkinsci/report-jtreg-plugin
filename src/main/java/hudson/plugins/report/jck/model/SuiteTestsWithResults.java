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

import java.util.ArrayList;
import java.util.List;

import java.util.Collections;

public class SuiteTestsWithResults implements java.io.Serializable {

    private final String name;
    private final List<StringWithResult> tests;

    public SuiteTestsWithResults(String name, List<StringWithResult> tests) {
        this.name = name;
        this.tests = tests;
    }

    public String getName() {
        return name;
    }

    public List<StringWithResult> getTests() {
        return Collections.unmodifiableList(tests);
    }

    public List<StringWithResult> getTestsLimited() {
        if (tests.size() <= 1000) {
            return getTests();
        } else {
            final List<StringWithResult> truncated = new ArrayList<>(1001);
            for (int i = 0; i < 1000; i++) {
                StringWithResult get = tests.get(i);
                truncated.add(get);
            }
            truncated.add(new StringWithResult("... Shown 1000 from " + tests.size() + ". To see remaining " + (tests.size() - 1000) + " use our cmdline diff tool!", null));
            return truncated;
        }
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
