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

import io.jenkins.plugins.report.jtreg.utils.MoreStrings;

import java.util.Objects;

public class Suite implements Comparable<Suite>, java.io.Serializable {

    private final String name;
    private final Report report;

    public Suite(String name, Report report) {
        this.name = name;
        this.report = report;
    }

    public String getName() {
        return name;
    }

    public Report getReport() {
        return report;
    }

    @Override
    public int compareTo(Suite o) {
        return MoreStrings.compareStrings(name, o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Suite suite = (Suite) o;
        return Objects.equals(name, suite.name) && Objects.equals(report, suite.report);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, report);
    }

    @Override
    public String toString() {
        return "Suite{" + "name='" + name + '\'' + ", report=" + report + '}';
    }
}
