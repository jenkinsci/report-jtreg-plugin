/*
 * The MIT License
 *
 * Copyright 2015-2026 report-jtreg plugin contributors
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

package io.jenkins.plugins.report.jtreg.writers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.Constants;
import io.jenkins.plugins.report.jtreg.model.Metadata;
import io.jenkins.plugins.report.jtreg.model.Suite;

public class WritersManager {

    public static void storeAllSummaries(String prefix, List<Suite> reportFull, File rootDir, Metadata metadata) throws IOException {
        writeJsons(prefix, reportFull, rootDir, metadata);
        PropertiesWriter.writeReportSummaryProperties(reportFull, rootDir);
    }

    public static void storeAllSummaries(String prefix, List<Suite> reportFull, BuildReportExtended buildReportExtended, File rootDir, Metadata metadata) throws IOException {
        writeJsons(prefix, reportFull, rootDir, metadata);
        if (buildReportExtended != null) {
            File diffJson = new File(rootDir, prefix + "-" + Constants.REPORT_DIFF);
            JsonReportWriter.writeBuildReportExtended(diffJson, buildReportExtended);
            PropertiesWriter.writeReportPropertiesRegressions(rootDir, buildReportExtended);
        }

    }

    private static void writeJsons(String prefix, List<Suite> reportFull, File rootDir, Metadata metadata) throws IOException {
        File jsonFile1 = new File(rootDir, prefix + "-" + Constants.REPORT_JSON);
        File jsonFile2 = new File(rootDir, prefix + "-" + Constants.REPORT_TESTS_LIST_JSON);
        File jsonFile3 = new File(rootDir, prefix + "-" + Constants.REPORT_METADATA);
        JsonReportWriter.writeSummaryReport(reportFull, jsonFile1.toPath());
        JsonReportWriter.writeTestListReport(reportFull, jsonFile2.toPath());
        if (metadata != null) {
            JsonReportWriter.writeMetadataReport(metadata, jsonFile3.toPath());
        }
    }

}
