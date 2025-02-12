/*
 * The MIT License
 *
 * Copyright 2016.
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
package io.jenkins.plugins.report.jtreg.formatters;

import java.io.PrintStream;
import java.util.List;

public abstract class BasicFormatter implements Formatter {

    protected PrintStream o;

    public BasicFormatter(PrintStream o) {
        this.o = o;
    }

    public enum TypeOfDiff {
        PATCH, INLINE, SIDEBYSIDE
    }

    @Override
    public void print(String s) {
        o.print(s);
    }

    @Override
    public void println(String s) {
        o.println(s);
    }

    @Override
    public void println() {
        println("");
    }

    @Override
    public void closeBuildsList() {
        println("");
        reset();
    }

    @Override
    public void small() {

    }

    @Override
    public void pre() {

    }

    @Override
    public void preClose() {

    }

    @Override
    public void printTable(JtregPluginServicesCell[][] table, int rowSize, int columnSize) {

    }

    @Override
    public String generateTableHeaderItem(String jobsName, String buildId, List<String> otherLines, String urlStub) {
        return "";
    }

    @Override
    public void printDiff(String traceOne, String traceTwo, String nameOne, String nameTwo, BasicFormatter.TypeOfDiff typeOfDiff) {

    }

    @Override
    public void printColumns(String[] titles, List<String>... columns) {
        if (titles.length != columns.length) {
            throw new RuntimeException("Different number of titles and columns");
        }
        int maxColumnLines = 0;
        int[] maximumLenghts = new int[titles.length];
        for (int i = 0; i < titles.length; i++) {
            maximumLenghts[i] = titles.length;
        }
        for (int i = 0; i < columns.length; i++) {
            List<String> column = columns[i];
            if (column.size() > maxColumnLines) {
                maxColumnLines = column.size();
            }
            for (String line : column) {
                if (line.length() > maximumLenghts[i]) {
                    maximumLenghts[i] = line.length();
                }
            }
        }
        for (int i = 0; i < titles.length; i++) {
            print(Formatter.append(titles[i], maximumLenghts[i]));
        }
        println();
        for (int linenumber = 0; linenumber < maxColumnLines; linenumber++) {
            for (int i = 0; i < columns.length; i++) {
                String s = "";
                if (linenumber < columns[i].size()) {
                    s = columns[i].get(linenumber);
                }
                print(Formatter.append(s, maximumLenghts[i]));
            }
            println();
        }
    }
}
