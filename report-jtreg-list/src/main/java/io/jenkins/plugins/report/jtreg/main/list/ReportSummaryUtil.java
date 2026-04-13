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
package io.jenkins.plugins.report.jtreg.main.list;

import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.writers.WritersManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.jenkins.plugins.report.jtreg.Constants.REPORT_JSON;
import static io.jenkins.plugins.report.jtreg.Constants.REPORT_TESTS_LIST_JSON;

/**
 * Utility class for backing up and storing report summaries.
 */
public class ReportSummaryUtil {

    /**
     * Backs up existing report files to a zip archive and stores new summaries.
     * This method creates a single zip archive containing all existing report files,
     * with a timestamp in the zip filename, and then stores the new summaries using WritersManager.
     *
     * @param prefix the prefix for report files (e.g., "jck" or "jtreg")
     * @param suitesList the list of suites to store
     * @param buildPath the path to the build directory
     * @throws IOException if an I/O error occurs during file operations
     */
    public static void backupAndStoreSummaries(String prefix, List<Suite> suitesList, Path buildPath) throws IOException {
        try {
            Path summaryPath = buildPath.resolve(prefix + "-" + REPORT_JSON);
            Path testsListPath = buildPath.resolve(prefix + "-" + REPORT_TESTS_LIST_JSON);
            
            List<Path> filesToBackup = new ArrayList<>();
            if (Files.exists(summaryPath)) {
                filesToBackup.add(summaryPath);
            }
            if (Files.exists(testsListPath)) {
                filesToBackup.add(testsListPath);
            }
            
            if (!filesToBackup.isEmpty()) {
                long timestamp = System.currentTimeMillis();
                String zipFileName = "backup_" + prefix + "_" + timestamp + ".zip";
                Path zipPath = buildPath.resolve(zipFileName);
                try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                    for (Path file : filesToBackup) {
                        ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                        zos.putNextEntry(zipEntry);
                        Files.copy(file, zos);
                        zos.closeEntry();
                    }
                }
                // Delete the original files after successful backup
                for (Path file : filesToBackup) {
                    Files.deleteIfExists(file);
                }
            }
        } finally {
            //the metadata would be missing displayName. It is (optionally) hidden in build.xml as /build/displayName
            //buildId is directory name, project s ../../name
            WritersManager.storeAllSummaries(prefix, suitesList, buildPath.toFile(), "unknown", null);
        }
    }
}

// Made with Bob
