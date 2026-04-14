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
package io.jenkins.plugins.report.jtreg.main.recreate;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.report.jtreg.BuildReportExtended;
import io.jenkins.plugins.report.jtreg.BuildSummaryParser;
import io.jenkins.plugins.report.jtreg.ConfigFinder;
import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapper;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDir;
import io.jenkins.plugins.report.jtreg.wrappers.RunWrapperFromDirWithName;
import io.jenkins.plugins.report.jtreg.writers.WritersManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static io.jenkins.plugins.report.jtreg.Constants.getAllFiles;

/**
 * Utility class for backing up and storing report summaries.
 */
public class ReportSummaryUtil {


    public static final String SUCCESS_DUPLICATE = "SUCCESS";
    public static final String UNSTABLE_DUPLICATE = "UNSTABLE";

    static boolean isBuildDir(File dir) {
        try {
            return new File(dir, "archive").exists() && (dir.getCanonicalFile().getName().matches("[0-9]+"));
        }catch (IOException e){
            return false;
        }
    }

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
    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"})
    static void backupAndStoreSummaries(String prefix, List<Suite> suitesList, Path buildPath, RecreateArgs params) throws Exception {
        List<Path> filesToBackup = getAllFiles(prefix, buildPath);
        Path zipPath = backup(prefix, buildPath, filesToBackup, true);
        checkResultOfCurrentBuild(buildPath);
        int jobId = Integer.valueOf(buildPath.toFile().getName());
        String displayName = getDisplayName(buildPath, jobId);
        // write static files
        WritersManager.storeAllSummaries(prefix, suitesList, buildPath.toFile(), displayName, params.getUrl());
        RunWrapper found = findPreviousBuild(buildPath, jobId);
        long timeStamp = Long.valueOf(ConfigFinder.findInConfigStatic(new File(buildPath.toFile(), "build.xml"), "timestamp", "/build/timestamp"));
        //warning, duration will change (to better), that is correct
        long duration = Long.valueOf(ConfigFinder.findInConfigStatic(new File(buildPath.toFile(), "build.xml"), "duration", "/build/duration"));
        BuildReportExtended br = new BuildSummaryParser(Arrays.asList(prefix), null/*?*/).parseBuildReportExtended(new RunWrapperFromDirWithName(buildPath.toFile(), timeStamp, duration, displayName), found);
        // write diff with all metadata
        WritersManager.storeAllDiffs(prefix, br, buildPath.toFile(), params.getUrl());
        if (params.getOut() != null) {
            export(buildPath, params, filesToBackup, zipPath, displayName, br, jobId);
        }

    }

    private static void checkResultOfCurrentBuild(Path buildPath) {
        String result = ConfigFinder.findInConfigStatic(new File(buildPath.toFile(), "build.xml"), "nvr", "/build/result");
        if (!SUCCESS_DUPLICATE.equals(result) && !UNSTABLE_DUPLICATE.equals(result)) {
            System.err.println("Warning, processing invalid job. Result is " + result);
        }
    }

    private static RunWrapper findPreviousBuild(Path buildPath, int jobId) {
        //find previous build (if any)
        RunWrapper found = null;
        for (int i = jobId - 1; i > 0; i--) {
            File oldDir = new File(buildPath.toFile().getParentFile(), "" + i);
            String resultOld = ConfigFinder.findInConfigStatic(new File(oldDir, "build.xml"), "nvr", "/build/result");
            if (SUCCESS_DUPLICATE.equals(resultOld) || UNSTABLE_DUPLICATE.equals(resultOld)) {
                found = new RunWrapperFromDir(oldDir);
                break;
            }
        }
        return found;
    }

    private static String getDisplayName(Path buildPath, int jobId) {
        String displayName = ConfigFinder.findInConfigStatic(new File(buildPath.toFile(), "build.xml"), "nvr", "/build/displayName");
        if (displayName == null) {
            displayName = "#" + jobId;
        }
        return displayName;
    }

    private static void export(Path buildPath, RecreateArgs params, List<Path> filesToBackup, Path zipPath, String displayName, BuildReportExtended br, int jobId) throws IOException {
        Path outDir = new File(params.getOut()).toPath();
        if (!Files.exists(outDir)) {
            Files.createDirectories(outDir);
        }
        for (Path file : filesToBackup) {
            Path targetPath = outDir.resolve(file.getFileName());
            Files.copy(file, targetPath);
        }
        //restore originals
        if (!params.isNoRestore()) {
            restoreBackup(buildPath, zipPath, true);
        }
        //postprocess the copy
        System.out.println(" - " + displayName);
        System.out.println(" - " + br.getJob());
        System.out.println(" - " + jobId);
    }

    private static Path backup(String prefix, Path buildPath, List<Path> filesToBackup, boolean delete) throws IOException {
        long timestamp = System.currentTimeMillis();
        String zipFileName = "backup_" + prefix + "_" + timestamp + ".zip";
        Path zipPath = buildPath.resolve(zipFileName);
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (Path file : filesToBackup) {
                if (Files.exists(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
        }
        if (delete) {
            for (Path file : filesToBackup) {
                Files.deleteIfExists(file);
            }
        }
        return zipPath;
    }

    private static void restoreBackup(Path buildPath, Path zipPath, boolean delete) throws IOException {
        if (zipPath != null && Files.exists(zipPath)) {
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    Path extractedFile = buildPath.resolve(zipEntry.getName());
                    Files.deleteIfExists(extractedFile);
                    Files.copy(zis, extractedFile);
                    zis.closeEntry();
                }
            }
            if (delete) {
                Files.deleteIfExists(zipPath);
            }
        }
    }
}