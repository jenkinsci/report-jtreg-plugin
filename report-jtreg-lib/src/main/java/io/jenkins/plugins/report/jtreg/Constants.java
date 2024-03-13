/*
 * The MIT License
 *
 * Copyright 2015-2023 report-jtreg plugin contributors
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
package io.jenkins.plugins.report.jtreg;

final public class Constants {

    public static final String REPORT_JSON = "report.json";
    public static final String REPORT_TESTS_LIST_JSON = "tests-list.json";
    public static final String IRRELEVANT_GLOB_STRING = "report-{runtime,devtools,compiler}.xml.gz";
    public static final double VAGUE_QUERY_THRESHOLD = 0.5;
    public static final int VAGUE_QUERY_LENGTH_THRESHOLD = 4;
    public static final String COMPARATOR_TABLE_CSS =
                    "<style>\n" +
                    ".contents {\n" +
                    "    font-family: monospace, monospace;\n" +
                    "}\n" +
                    "\n" +
                    ".blk {\n" +
                    "    color: Black;\n" +
                    "}\n" +
                    "\n" +
                    "details {\n" +
                    "    padding-left: 35px;\n" +
                    "}\n" +
                    "\n" +
                    "table, td {\n" +
                    "    border: 1px solid black;\n" +
                    "    border-collapse: collapse;\n" +
                    "    color: Red;\n" +
                    "    padding: 0.5em;\n" +
                    "}\n" +
                    "</style>";
    public static final String COMPARATOR_TABLE_JAVASCRIPT =
                    "<script>\n" +
                    "let opened = false;\n" +
                    "\n" +
                    "function expandOrCollapse() {\n" +
                    "    let detailsElements = document.querySelectorAll('details');\n" +
                    "\n" +
                    "    detailsElements.forEach((detailsElement) => {\n" +
                    "        if (opened) {\n" +
                    "            detailsElement.removeAttribute('open');\n" +
                    "        } else {\n" +
                    "            detailsElement.setAttribute('open', '');\n" +
                    "        }\n" +
                    "    });\n" +
                    "\n" +
                    "    opened = !opened;\n" +
                    "}\n" +
                    "</script>";
}
