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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final public class Constants {

    public static final List<String> prefixedFiles = Collections.unmodifiableList(Arrays.asList(
            "report.json",
            "tests-list.json",
            "jr-diff.json",
            "report-summary.txt",
            "report-problems.txt",
            "report-diff.txt",
            "report-all-tests.txt"
    ));
    public static final String REPORT_JSON = prefixedFiles.get(0);
    public static final String REPORT_TESTS_LIST_JSON = prefixedFiles.get(1);
    public static final String REPORT_DIFF = prefixedFiles.get(2);
    public static final String REPORT_SUMMARY_TXT = prefixedFiles.get(3);
    public static final String REPORT_PROBLEMS_TXT = prefixedFiles.get(4);
    public static final String REPORT_DIFF_TXT = prefixedFiles.get(5);
    public static final String REPORT_ALL_TESTS_TXT = prefixedFiles.get(6);

    public static final List<String> unprefixedFiles = Collections.unmodifiableList(Arrays.asList(
            "cached-summ-results.properties",
            "cached-summ-regressions.properties"
    ));
    public static final String CACHED_SUMM_RESULTS_PROPERTIES =  unprefixedFiles.get(0);
    public static final String CACHED_SUMM_REGRESSIONS_PROPERTIES = unprefixedFiles.get(1);


    public static final String IRRELEVANT_GLOB_STRING = "report-{runtime,devtools,compiler}.xml.gz";

    public static final String LIST_BACKEND = "/list.html";
    public static final String COMPARATOR_BACKEND = "/comp.html";
    public static final String DIFF_BACKEND = "/diff.html";
    public static final double VAGUE_QUERY_THRESHOLD = 0.5;
    public static final int VAGUE_QUERY_LENGTH_THRESHOLD = 4;
    public static final String COMPARATOR_TABLE_CSS =
            "<style>\n" +
                    ".tooltip {\n" +
                    "  position: relative;\n" +
                    "  display: inline-block;\n" +
                    "}\n" +
                    ".tooltip .tooltiptext {\n" +
                    "  visibility: hidden;\n" +
                            "  width: 240px;\n" +
                            "  background-color: grey;\n" +
                            "  color: #fff;\n" +
                            "  text-align: left;\n" +
                            "  border-radius: 6px;\n" +
                            "  padding: 5px 0;\n" +
                            "  /* Position the tooltip */\n" +
                            "  position: absolute;\n" +
                            "  z-index: 1;\n" +
                            "  top: 100%;\n" +
                            "  left: 50%;\n" +
                            "  margin-left: -60px;" +
                            "}\n" +
                            ".tooltip:hover .tooltiptext {\n" +
                            "  visibility: visible;\n" +
                            "}\n" +
                    ".contents {\n" +
                    "    font-family: monospace, monospace;\n" +
                    "}\n" +
                    "\n" +
                    ":target {border-color: #3399FF;border-style: dashed;}\n" +
                    ".NameBuildLine{color:Green}\n" +
                    ".NameBuildSummary{color:DodgerBlue}\n" +
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

    public static final String TRACE_DIFF_TABLE_CSS =
            "<style>\n" +
            ".contents {\n" +
            "    font-family: monospace, monospace;\n" +
            "}\n" +
            "\n" +
            "table, td {\n" +
            "    border: 1px solid black;\n" +
            "    border-collapse: collapse;\n" +
            "    padding: 0.25em;\n" +
            "}\n" +
            "</style>";


    public static List<Path> getAllFiles(String prefix, List<String> additionaFiles, Path buildPath) {
        List<Path> filesToBackup = new ArrayList<>();
        for(String file: prefixedFiles) {
            Path path = buildPath.resolve(prefix + "-" + file);
            if (Files.exists(path)) {
                filesToBackup.add(path);
            }
        }
        for(List<String> list: Arrays.asList(unprefixedFiles, additionaFiles == null ? new ArrayList<String>():additionaFiles)) {
            for (String file : list) {
                Path path = buildPath.resolve(file);
                if (Files.exists(path)) {
                    filesToBackup.add(path);
                }
            }
        }
        return filesToBackup;
    }
}
