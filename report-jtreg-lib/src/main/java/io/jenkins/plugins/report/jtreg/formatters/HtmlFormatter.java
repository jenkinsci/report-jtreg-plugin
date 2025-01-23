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
import io.jenkins.plugins.report.jtreg.Constants;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HtmlFormatter extends StringMappedFormatter {

    public HtmlFormatter(PrintStream stream) {
        super(stream);
        fillColors();
    }

    @Override
    public void print(String s) {
        super.print(sanitize(s));
    }

    @Override
    public void println(String s) {
        super.println(sanitize(s) + "<br/>");
    }

    private String sanitize(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("  ", "&nbsp; ");
    }

    private void fillColors() {
        colors.put(SupportedColors.Black, template("black"));
        colors.put(SupportedColors.Red, template("red"));
        colors.put(SupportedColors.Green, template("green"));
        colors.put(SupportedColors.Yellow, template("yellow"));
        colors.put(SupportedColors.Blue, template("blue"));
        colors.put(SupportedColors.Magenta, template("magenta"));
        colors.put(SupportedColors.Cyan, template("cyan"));
        colors.put(SupportedColors.LightRed, template("OrangeRed"));
        colors.put(SupportedColors.LightGreen, template("LightGreen"));
        colors.put(SupportedColors.LightYellow, template("LightYellow"));
        colors.put(SupportedColors.LightBlue, template("LightBlue"));
        colors.put(SupportedColors.LightMagenta, template("Violet"));
        colors.put(SupportedColors.LightCyan, template("LightCyan"));
    }

    protected List<String> clossingBuffer = new LinkedList();

    private String template(String color) {
        //keep only one space! see sanitize
        return "<span style='color:" + color + "'>";
    }

    @Override
    public void startBold() {
        print("<b>");
        clossingBuffer.add("</b>");
    }

    @Override
    public void startColor(SupportedColors color) {
        print(getColor(color));
        clossingBuffer.add("</span>");
    }

    @Override
    public void reset() {
        while (!clossingBuffer.isEmpty()) {
            print(clossingBuffer.get(clossingBuffer.size() - 1));
            clossingBuffer.remove(clossingBuffer.size() - 1);
        }
    }

    @Override
    public void initDoc() {
        println("<div style='background-color:black;color:white'>");
    }

    @Override
    public void closeDoc() {
        println("</div>");
    }

    @Override
    public void startTitle2() {
        print("<h2>");
        clossingBuffer.add("</h2>");
    }

    @Override
    public void startTitle4() {
        print("<h4>");
        clossingBuffer.add("</h4>");
    }

    @Override
    public void startTitle3() {
        print("<h3>");
        clossingBuffer.add("</h3>");
    }

    @Override
    public void startTitle1() {
        print("<h1>");
        clossingBuffer.add("</h1>");
    }

    public void pre() {
        print("<pre>");
    }

    public void preClose() {
        print("</pre>");
    }

    @Override
    public void printTable(JtregPluginServicesCell[][] table, int rowSize, int columnSize) {
        super.println(Constants.COMPARATOR_TABLE_CSS); // print styles

        super.println("<div class='contents'>"); // start the section

        // first print the first row definitions
        super.println("<ul>");
        for (int i = 1; i < table[0].length; i++) {
            // make the definition and the table header linkable between each other
            super.println("<li><b id='legend-" + i + "'><a href='#table-" + i + "'>" + i + ":</a></b> " + table[0][i].renderCell() + "</li>");
            table[0][i] =
                    this.createCell(new JtregPluginServicesLinkWithTooltip("<b id='table-" + i + "'><a href='#legend-" + i +
                            "'>" + i + "</a></b>")); // replace the item with its definition (number)
        }
        super.println("</ul>");

        super.println("<button onclick='expandOrCollapse()' style='margin-bottom:25px'>expand / collapse all</button>");

        // print the table itself
        super.println("<table>");
        for (int i = 0; i < rowSize; i++) {
            super.println("<tr>");
            for (int j = 0; j < columnSize; j++) {
                if (table[i][j] != null) {
                    if (table[i][j].contentEquals("X")) {
                        super.println("<td>" + table[i][j].renderCell() + "</td>");

                    }
                    //this will need to be redone per-item
                    //JtregPluginServicesCell currently do not have method for that, but should be easy to do
                    else if (table[i][j].contentMatches("^[1-9]?[0-9]$|^100$")) {
                        // the table is displaying numbers from 0-100 (percentage), color code them
                        int number = Integer.parseInt(table[i][j].getCellContent());
                        if (number == 100) {
                            super.println("<td style='color:DeepSkyBlue'>" + table[i][j].renderCell() + "</td>");
                        } else if (number > 90) {
                            super.println("<td style='color:ForestGreen'>" + table[i][j].renderCell() + "</td>");
                        } else if (number > 30) {
                            super.println("<td style='color:DarkOrange'>" + table[i][j].renderCell() + "</td>");
                        } else {
                            // will be red
                            super.println("<td>" + table[i][j].renderCell() + "</td>");
                        }
                    } else {
                        super.println("<td class='blk'>" + table[i][j].renderCell() + "</td>");
                    }
                } else {
                    super.println("<td></td>");
                }
            }
            super.println("</tr>");
        }

        super.println("</table>");
        super.println("</div>"); // end the section
        super.println(Constants.COMPARATOR_TABLE_JAVASCRIPT); // print the javascript to expand/collapse the properties
    }

    @Override
    public String generateTableHeaderItem(String jobName, String buildId, List<String> otherLines, String urlStub) {
        if (urlStub == null) {
            urlStub = "..";
        }
        StringBuilder headerItem = new StringBuilder();
        // main line
        String mainLine =
                "<a class='NameBuildLine' href='" + urlStub + "/job/" + jobName + "'>"
                        + jobName + "</a>" + " - " + "<a class='NameBuildLine' href='" + urlStub + "/job/" + jobName + "/" + buildId + "'>build</a>"
                        + ":" + "<a class='NameBuildLine' href='" + urlStub + "/job/" + jobName + "/" + buildId + "/java-reports'>" + buildId +
                        "</a>";
        headerItem.append("<span class='NameBuildLineWrap'>").append(mainLine).append("</span><br>");
        // other lines, hidden by default
        headerItem.append("<small><details>");
        headerItem.append("<summary class='NameBuildSummary'>properties</summary>");
        for (String line : otherLines) {
            headerItem.append(line).append("<br>");
        }
        headerItem.append("</details></small>");

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

            // print the patch with formatting
            for (String line : unifiedDiff) {
                if (line.matches("^(---|\\+\\+\\+).*$")) {
                    // lines starting with --- or +++, bold
                    super.println("<b>" + line + "</b><br>");
                } else if (line.matches("^@@.*$")) {
                    // lines starting with @@, cyan
                    super.println("<span style='color:DeepSkyBlue'>" + line + "</span><br>");
                } else if (line.matches("^-.*$")) {
                    // lines starting with -, red
                    super.println("<span style='color:Red'>" + line + "</span><br>");
                } else if (line.matches("^\\+.*$")) {
                    // lines starting with +, green
                    super.println("<span style='color:Green'>" + line + "</span><br>");
                } else {
                    // other lines, print normally
                    super.println(line + "<br>");
                }
            }
        } else if (typeOfDiff == TypeOfDiff.INLINE) {
            // define the generator for inline diff
            DiffRowGenerator generator = DiffRowGenerator.create()
                    .showInlineDiffs(true)
                    .mergeOriginalRevised(true)
                    .inlineDiffByWord(true)
                    .oldTag(f -> f ? "<s style='color:Red'>" : "</s>")
                    .newTag(f -> f ? "<b style='color:Green'>" : "</b>")
                    .build();

            // create the diff
            List<DiffRow> rows = generator.generateDiffRows(listOne, listTwo);

            super.println("<b>" + nameOne + " --- " + nameTwo + "</b><br>" + "\n"); // print the names of the builds

            // and print the diff
            for (DiffRow row : rows) {
                super.println(row.getOldLine() + "<br>");
            }
        } else {
            DiffRowGenerator generator = DiffRowGenerator.create()
                    .showInlineDiffs(true)
                    .inlineDiffByWord(true)
                    .oldTag(f -> f ? "<s style='color:Red'>" : "</s>")
                    .newTag(f -> f ? "<b style='color:Green'>" : "</b>")
                    .build();
            List<DiffRow> rows = generator.generateDiffRows(listOne, listTwo);

            super.println(Constants.TRACE_DIFF_TABLE_CSS);
            super.println("<div class='contents'><table>");
            super.println("<tr><th><b>" + nameOne + "</b></th><th><b>" + nameTwo + "</b></th></tr>");

            for (DiffRow row : rows) {
                super.println("<tr><td>" + row.getOldLine() + "</td><td>" + row.getNewLine() + "</td></tr>");
            }

            super.println("</table></div>");
        }
    }

    @Override
    public JtregPluginServicesCell generateTableHeaderItemAsCell(String jobName, String buildId, List<String> otherLines, String urlStub) {
        return this.createCell(new JtregPluginServicesLinkWithTooltip(generateTableHeaderItem(jobName, buildId, otherLines, urlStub)));
    }

    @Override
    public JtregPluginServicesCell createCell(List<JtregPluginServicesLinkWithTooltip> content) {
        if (content == null) {
            return null;
        }
        return new JtregPluginServicesCell(content);
    }

    @Override
    public void println() {
        //this is just adding new line to source code.
        //for <br>. use println("");
        super.println();
    }

    @Override
    public void printColumns(String[] titles, List<String>... columns) {
        if (titles.length != columns.length) {
            throw new RuntimeException("Different number of titles and columns");
        }
        print("<div id='finalColumns' class='finalColumnsHolder'>");
        println();
        println();
        int w = (100 / columns.length) - 2;
        for (int i = 0; i < columns.length; i++) {
            String flow = "left";
            if (i == columns.length - 1) {
                flow = "reset";
            }
            print("<div id='finalColumn" + i + "' class='finalColumn' style='width:" + w + "%;overflow-x: scroll; white-space:nowrap; float: " + flow + "; margin:2px'>");
            println();
            print("<b>" + titles[i] + "</b>");
            println();
            for (String s : columns[i]) {
                println(s);
            }
            print("</div>");
            println();
        }
        print("</div>");
        println();
    }

}
