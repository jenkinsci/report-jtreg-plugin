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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
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
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Backs up existing report files to a zip archive and stores new summaries.
     * This method creates a single zip archive containing all existing report files,
     * with a timestamp in the zip filename, and then stores the new summaries using WritersManager.
     *
     * @param prefix     the prefix for report files (e.g., "jck" or "jtreg")
     * @param suitesList the list of suites to store
     * @param buildPath  the path to the build directory
     * @throws IOException if an I/O error occurs during file operations
     */
    static void backupAndStoreSummaries(String prefix, List<Suite> suitesList, Path buildPath, RecreateArgs params) throws Exception {
        Path zipPath = backup(prefix, buildPath, params, false);
        checkResultOfCurrentBuild(buildPath);
        int jobId = Integer.parseInt(buildPath.toFile().getName());
        String displayName = getDisplayName(buildPath, jobId);
        // write static files
        WritersManager.storeAllSummaries(prefix, suitesList, buildPath.toFile(), displayName, params.getUrl(), params.getKinds());
        RunWrapper found = findPreviousBuild(buildPath, jobId);
        long timeStamp = Long.parseLong(ConfigFinder.findInConfigStatic(new File(buildPath.toFile(), "build.xml"), "timestamp", "/build/timestamp"));
        //warning, duration will change (to better), that is correct
        long duration = Long.parseLong(ConfigFinder.findInConfigStatic(new File(buildPath.toFile(), "build.xml"), "duration", "/build/duration"));
        BuildReportExtended br = new BuildSummaryParser(Arrays.asList(prefix), null/*?*/).parseBuildReportExtended(new RunWrapperFromDirWithName(buildPath.toFile(), timeStamp, duration, displayName), found);
        // write diff with all metadata
        WritersManager.storeAllDiffs(prefix, br, buildPath.toFile(), params.getUrl(), params.getKinds());
        export(prefix, buildPath, params, zipPath, displayName, br, jobId);


    }

    private static void checkResultOfCurrentBuild(Path buildPath) {
        String result = ConfigFinder.findInConfigStatic(new File(buildPath.toFile(), "build.xml"), "result", "/build/result");
        if (!SUCCESS_DUPLICATE.equals(result) && !UNSTABLE_DUPLICATE.equals(result)) {
            System.err.println("Warning, processing invalid job. Result is " + result);
        }
    }

    private static RunWrapper findPreviousBuild(Path buildPath, int jobId) {
        //find previous build (if any)
        RunWrapper found = null;
        for (int i = jobId - 1; i > 0; i--) {
            File oldDir = new File(buildPath.toFile().getParentFile(), "" + i);
            String resultOld = ConfigFinder.findInConfigStatic(new File(oldDir, "build.xml"), "result", "/build/result");
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

    private static void export(String prefix, Path buildPath, RecreateArgs params, Path zipPath, String displayName, BuildReportExtended br, int jobId) throws IOException {
        String outDirParam = params.getOut();
        Path outDirImpl = null;
        boolean removeOutDir = false;
        if (outDirParam == null) {
            if (params.getJobDb() != null || params.getNvrDb() != null) {
                outDirImpl = Files.createTempDirectory("jtregReportPluginRecreate");
                removeOutDir = true;
            }
        } else {
            outDirImpl = new File(outDirParam).toPath();
        }
        //no export!
        if (outDirImpl != null) {
            List<Path> allFiles = getAllFiles(prefix, params.getAdditionalFiles(), buildPath);
            copyWithOverwrite(allFiles, outDirImpl);
            //restore originals
            if (!params.isNoRestore()) {
                restoreBackup(buildPath, zipPath, true);
            }
            allFiles = getAllFiles(prefix, params.getAdditionalFiles(), outDirImpl);
            //postprocess the copy
            if (params.getJobDb() != null) {
                File jobDir = new File(params.getJobDb() + "/" + br.getJob() + "/" + displayName + "/" + jobId);
                copyWithOverwrite(allFiles, jobDir.toPath());
            }
            if (params.getNvrDb() != null) {
                File nvrDir = new File(params.getNvrDb() + "/" + displayName + "/" + br.getJob() + "/" + jobId);
                copyWithOverwrite(allFiles, nvrDir.toPath());


            }
        }
        if (removeOutDir && outDirImpl != null) {
            File[] files = outDirImpl.toFile().listFiles();
            //it should be plain
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            System.err.println("Warning: could not delete " + file.getAbsolutePath());
                        }
                    }
                }
            }
            Files.delete(outDirImpl);
        }

    }

    private static void copyWithOverwrite(List<Path> allFiles, Path outDir) throws IOException {
        Files.createDirectories(outDir);
        for (Path file : allFiles) {
            Path targetPath = outDir.resolve(file.getFileName());
            Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"})
    private static Path backup(String prefix, Path buildPath, RecreateArgs params, boolean delete) throws IOException {
        long timestamp = System.currentTimeMillis();
        String zipFileName = "backup_" + prefix + "_" + timestamp + ".zip";
        Path zipPath = buildPath.resolve(zipFileName);
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            for (Path file : getAllFiles(prefix, params.getAdditionalFiles(), buildPath)) {
                if (Files.exists(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                    //we delete only the default stack which can be easily regenerated
                    if (delete && getAllFiles(prefix, new ArrayList<>(), buildPath).contains(file)) {
                        Files.delete(file);
                    }
                }
            }
        }
        return zipPath;
    }

    private static List<Path> restoreBackup(Path buildPath, Path zipPath, boolean delete) throws IOException {
        List<Path> extractedFiles = new ArrayList<>();
        if (zipPath != null && Files.exists(zipPath)) {
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    Path extractedFile = buildPath.resolve(zipEntry.getName());
                    Files.deleteIfExists(extractedFile);
                    Files.copy(zis, extractedFile);
                    zis.closeEntry();
                    extractedFiles.add(extractedFile);
                }
            }
            if (delete) {
                Files.deleteIfExists(zipPath);
            }
        }
        return extractedFiles;
    }

    static List<Path> findArchives(Path buildPath, Recreate typesProvider) {
        final List<Path> archives = new ArrayList<>();
        try {
            Files.walkFileTree(buildPath.resolve("archive"), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (typesProvider.isResultsArchive(file.toString())) {
                        archives.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return archives;
    }
}