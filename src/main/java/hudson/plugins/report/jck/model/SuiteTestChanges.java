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

public class SuiteTestChanges {

    private final String name;
    private final List<String> failures;
    private final List<String> errors;
    private final List<String> fixes;
    private final List<String> added;
    private final List<String> removed;

    public SuiteTestChanges(String name, List<String> failures, List<String> errors, List<String> fixes, List<String> added, List<String> removed) {
        this.name = name;
        this.failures = failures;
        this.errors = errors;
        this.fixes = fixes;
        this.added = added;
        this.removed = removed;
    }

    public String getName() {
        return name;
    }

    public List<String> getAdded() {
        return added;
    }

    public List<String> getRemoved() {
        return removed;
    }

    public List<String> getFailures() {
        return failures;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getFixes() {
        return fixes;
    }

}
