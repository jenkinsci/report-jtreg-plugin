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

public class SuitesWithResults implements java.io.Serializable {

    public static SuitesWithResults create(List<SuiteTests> currentBuildTestsList, BuildReport problems, String job, int id) {
        List<SuiteTestsWithResults> futureSuitests = new ArrayList<>(currentBuildTestsList.size());
        for (SuiteTests suiteTest : currentBuildTestsList) {
            final List<SuiteTestsWithResults.StringWithResult> testsForThisSuite = new ArrayList<>(suiteTest.getTests().size());
            for (String test : suiteTest.getTests()) {
                if (isProblem(suiteTest.getName(), test, problems.getSuites())) {
                    testsForThisSuite.add(new SuiteTestsWithResults.StringWithResult(test, SuiteTestsWithResults.TestStatusSimplified.FAILED_OR_ERROR));
                } else {
                    testsForThisSuite.add(new SuiteTestsWithResults.StringWithResult(test, SuiteTestsWithResults.TestStatusSimplified.PASSED_OR_MISSING));
                }
            }
            futureSuitests.add(new SuiteTestsWithResults(suiteTest.getName(), testsForThisSuite, job, id));
        }
        return new SuitesWithResults(futureSuitests);
    }

    private static boolean isProblem(String suiteName, String test, List<Suite> probelms) {
        for (Suite probelm : probelms) {
            if (probelm.getName().equals(suiteName)) {
                for (Test t : probelm.getReport().getTestProblems()) {
                    if (t.getName().equals(test)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private final List<SuiteTestsWithResults> suitests;

    public SuitesWithResults(List<SuiteTestsWithResults> suitests) {
        this.suitests = suitests;
    }

    public List<SuiteTestsWithResults> getAllTestsAndSuites() {
        return Collections.unmodifiableList(suitests);
    }

}
