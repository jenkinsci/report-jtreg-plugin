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

    public void pre(){
        print("<pre>");
    }

    public void preClose(){
        print("</pre>");
    }

    @Override
    public void printTable(String[][] table, int rowSize, int columnSize) {
        super.println("<style>table, td { border: 1px solid black; border-collapse: collapse; padding: 0.5em; }</style>");

        // first print the first row definitions
        super.println("<ul>");
        for (int i = 1; i < table[0].length; i++) {
            super.println("<li><b>" + i + ":</b> " + table[0][i] + "</li>");
            table[0][i] = "<b>" + i + "</b>"; // replace the item with its definition (number)
        }
        super.println("</ul>");

        // print the table
        super.println("<table>");
        for (int i = 0; i < rowSize; i++) {
            super.println("<tr>");
            for (int j = 0; j < columnSize; j++) {
                if (table[i][j] != null) {
                    if (table[i][j].equals("X")) {
                        super.println("<td style=\"color:Red\">" + table[i][j] + "</td>");
                    } else {
                        super.println("<td>" + table[i][j] + "</td>");
                    }
                } else {
                    super.println("<td></td>");
                }
            }
            super.println("</tr>");
        }
        super.println("</table>");
    }
}
