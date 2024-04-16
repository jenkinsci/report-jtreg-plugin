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

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorFormatter extends StringMappedFormatter {
//those are NOT spaces but bash \e

    private static final String Default = "[39m";

    private static final String ResetAll = "[0m";

    private static final String Bold = "[1m";

    private static final String Black = "[30m";
    private static final String Red = "[31m";
    private static final String Green = "[32m";
    private static final String Yellow = "[33m";
    private static final String Blue = "[34m";
    private static final String Magenta = "[35m";
    private static final String Cyan = "[36m";
    private static final String LightRed = "[91m";
    private static final String LightGreen = "[92m";
    private static final String LightYellow = "[93m";
    private static final String LightBlue = "[94m";
    private static final String LightMagenta = "[95m";
    private static final String LightCyan = "[96m";

    private void fillColors() {
        colors.put(SupportedColors.Black, Black);
        colors.put(SupportedColors.Red, Red);
        colors.put(SupportedColors.Green, Green);
        colors.put(SupportedColors.Yellow, Yellow);
        colors.put(SupportedColors.Blue, Blue);
        colors.put(SupportedColors.Magenta, Magenta);
        colors.put(SupportedColors.Cyan, Cyan);
        colors.put(SupportedColors.LightRed, LightRed);
        colors.put(SupportedColors.LightGreen, LightGreen);
        colors.put(SupportedColors.LightYellow, LightYellow);
        colors.put(SupportedColors.LightBlue, LightBlue);
        colors.put(SupportedColors.LightMagenta, LightMagenta);
        colors.put(SupportedColors.LightCyan, LightCyan);
    }

    public ColorFormatter(PrintStream stream) {
        super(stream);
        fillColors();
    }

    @Override
    public void startBold() {
        print(Bold);
    }

    @Override
    public void startColor(SupportedColors color) {
        print(getColor(color));
    }

    @Override
    public void reset() {
        print(ResetAll);
        preset();
    }

    @Override
    public void initDoc() {
        reset();
    }

    @Override
    public void closeDoc() {
        print(ResetAll);
    }

    private void preset() {
        //sets black background and white font
        print("[49m");
        print("[97m");
    }

    @Override
    public void startTitle2() {
        startColor(SupportedColors.Cyan);
    }

    @Override
    public void startTitle1() {
        startColor(SupportedColors.Cyan);
        startBold();
    }

    @Override
    public void startTitle3() {
        startColor(SupportedColors.LightBlue);
    }

    @Override
    public void startTitle4() {
        startColor(SupportedColors.Blue);
    }

    @Override
    public void printTable(JtregPluginServicesCell[][] table, int rowSize, int columnSize) {
        // first print the first row definitions
        for (int i = 1; i < table[0].length; i++) {
            super.println(Bold + i + ") " + ResetAll + table[0][i].renderCell());
            table[0][i] = this.createCell(new JtregPluginServicesLinkWithTooltip(Integer.toString(i))); // replace the item with its
            // definition (number)
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
                    // print the first row (the numbers) bold
                    if (i == 0) {
                        super.print(Bold + table[i][j].renderCell() + ResetAll + " ");
                        // Xs will be red
                    } else if (table[i][j].contentEquals("X")) {
                        super.print(Red + table[i][j].renderCell() + ResetAll + " ");
                    }
                    //this will need to be redone per-item
                    //JtregPluginServicesCell currently do not have method for that, but should be easy to do
                    else if (table[i][j].contentMatches("^[1-9]?[0-9]$|^100$")) {
                        // the table is displaying numbers from 0-100 (percentage), color code them
                        int number = Integer.parseInt(table[i][j].getCellContent());
                        if (number == 100) {
                            super.print(Cyan + table[i][j].renderCell() + ResetAll + " ");
                        } else if (number > 90) {
                            super.print(Green + table[i][j].renderCell() + ResetAll + " ");
                        } else if (number > 30) {
                            super.print(Yellow + table[i][j].renderCell() + ResetAll + " ");
                        } else {
                            super.print(Red + table[i][j].renderCell() + ResetAll + " ");
                        }
                    } else {
                        super.print(table[i][j].renderCell() + " ");
                    }
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
    public String generateTableHeaderItem(String jobName, String buildId, List<String> otherLines, String urlStub) {
        StringBuilder headerItem = new StringBuilder();
        // main line
        String mainLine = jobName + " - build:" + buildId;
        headerItem.append(Bold).append(Green).append(mainLine).append(ResetAll);

        // other lines
        for (String line : otherLines) {
            headerItem.append("\n\t\t").append(Cyan).append(line).append(ResetAll);
        }

        return headerItem.toString();
    }

    @Override
    public void printDiff(String traceOne, String nameOne, String traceTwo, String nameTwo, BasicFormatter.TypeOfDiff typeOfDiff) {
        // split the traces by lines
        List<String> listOne = new ArrayList<>(List.of(traceOne.split(System.lineSeparator())));
        List<String> listTwo = new ArrayList<>(List.of(traceTwo.split(System.lineSeparator())));

        if (typeOfDiff == TypeOfDiff.PATCH) {
            // create patch from the traces
            Patch<String> diff = DiffUtils.diff(listOne, listTwo);
            List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(nameOne, nameTwo, listOne, diff, 0);

            // color the patch
            for (String line : unifiedDiff) {
                if (line.matches("^(---|\\+\\+\\+).*$")) {
                    // lines starting with --- or +++, bold
                    super.println(Bold + line + ResetAll);
                } else if (line.matches("^@@.*$")) {
                    // lines starting with @@, cyan
                    super.println(Cyan + line + ResetAll);
                } else if (line.matches("^-.*$")) {
                    // lines starting with -, red
                    super.println(Red + line + ResetAll);
                } else if (line.matches("^\\+.*$")) {
                    // lines starting with +, green
                    super.println(Green + line + ResetAll);
                } else {
                    // other lines, print normally
                    super.println(line);
                }
            }
        } else if (typeOfDiff == TypeOfDiff.INLINE) {
            // define the generator for inline diff
            DiffRowGenerator generator = DiffRowGenerator.create()
                    .showInlineDiffs(true)
                    .mergeOriginalRevised(true)
                    .inlineDiffByWord(true)
                    .oldTag(f -> f ? Red : ResetAll)
                    .newTag(f -> f ? Green + Bold : ResetAll)
                    .build();

            // create the diff
            List<DiffRow> rows = generator.generateDiffRows(listOne, listTwo);

            super.println(Bold + nameOne + " --- " + nameTwo + ResetAll + "\n"); // print the names of the builds

            // and print the diff
            for (DiffRow row : rows) {
                super.println(row.getOldLine());
            }
        } else {
            super.println("Color formatting does not support SIDEBYSIDE format, please use html formatter.");
        }
    }

    @Override
    public JtregPluginServicesCell generateTableHeaderItemAsCell(String jobName, String buildId, List<String> otherLines, String urlStub) {
        return this.createCell(new JtregPluginServicesLinkWithTooltip(generateTableHeaderItem(jobName, buildId,
                otherLines, urlStub)));
    }

    @Override
    public JtregPluginServicesCell createCell(List<JtregPluginServicesLinkWithTooltip> content) {
        if (content == null) {
            return null;
        }
        return new JtregPluginServicesCell(JtregPluginServicesLinkWithTooltip.toPlainList(content));
    }
}
