/*
 * The MIT License
 *
 * Copyright 2016 jvanek.
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
import java.util.Arrays;
import java.util.List;

public class PlainFormatter extends BasicFormatter {

    public PlainFormatter(PrintStream stream) {
        super(stream);
    }

    @Override
    public void startBold() {
        //no op
    }

    @Override
    public void startColor(SupportedColors color) {
        //no op
    }

    @Override
    public void reset() {
        //no op
    }

    @Override
    public void initDoc() {
        //no op
    }

    @Override
    public void closeDoc() {
        //no op
    }

    @Override
    public void startTitle2() {
        //no op
    }

    @Override
    public void startTitle1() {
        //no op
    }

    @Override
    public void startTitle3() {
        //no op
    }

    @Override
    public void startTitle4() {
        //no op
    }

    @Override
    public void printTable(JtregPluginServicesCell[][] table, int rowSize, int columnSize) {
        // first print the first row definitions
        for (int i = 1; i < table[0].length; i++) {
            super.println(i + ") " + table[0][i].renderCell());
            table[0][i] = this.createCell(Integer.toString(i)); // replace the item with its definition (number)
        }

        // get the length of the longest item in each column
        int[] lengths = new int[table[0].length];
        Arrays.fill(lengths, 0);
        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j < columnSize; j++) {
                if (table[i][j] != null && table[i][j].cellWidth() > lengths[j]) {
                    lengths[j] = table[i][j].cellWidth();
                }
            }
        }

        // print the table
        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j < columnSize; j++) {
                int len = 0;
                if (table[i][j] != null) {
                    super.print(table[i][j].renderCell() + " ");
                    len = table[i][j].cellWidth();
                } else {
                    super.print(" ");
                }
                // printing spaces to make the table look better
                for (int k = len; k < lengths[j]; k++) {
                    super.print(" ");
                }
                super.print("| ");
            }
            super.print("\n");
        }
    }

    @Override
    public String generateTableHeaderItem(String jobName, String buildId, List<String> otherLines) {
        StringBuilder headerItem = new StringBuilder();
        // main line
        String mainLine = jobName + " - build:" + buildId;
        headerItem.append(mainLine);
        // other lines
        for (String line : otherLines) {
            headerItem.append("\n\t\t").append(line);
        }

        return headerItem.toString();
    }

    @Override
    public JtregPluginServicesCell generateTableHeaderItemAsCell(String jobName, String buildId, List<String> otherLines) {
        return this.createCell(generateTableHeaderItem(jobName, buildId, otherLines));
    }
}
