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
package io.jenkins.plugins.report.jtreg.main.diff.cmdline;

import io.jenkins.plugins.report.jtreg.ConfigFinder;
import io.jenkins.plugins.report.jtreg.formatters.Formatter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class JobsRecognition {

    private static JobsRecognition INSTANCE;
    private static final Map<File, String> cache = new HashMap<>();

    public static JobsRecognition jobsRecognition() {
        if (INSTANCE == null) {
            INSTANCE = new JobsRecognition();
        }
        return INSTANCE;
    }

    private String jenkinsDir;
    private File jobsDir;

    public JobsRecognition() {
        jenkinsDir = System.getProperty("jenkins_home");
        if (jenkinsDir == null) {
            jenkinsDir = System.getenv("JENKINS_HOME");
        }
        jobsDir = new File(jenkinsDir, "jobs");
    }

    public File getJobsDir() {
        return jobsDir;
    }

    public String getJenkinsDir() {
        return jenkinsDir;
    }

    public static String[] getNonNullListing(File f){
        String[] l = f.list();
        if (l == null){
            throw new NullPointerException("`" + f + "` do not exists or is file or not accessible or hidden io error occured");
        }
        return l;
    }

    public String[] getPossibleJobs() {
        String[] possibleJobs = getNonNullListing(jobsDir);
        Arrays.sort(possibleJobs, Collections.reverseOrder());
        return possibleJobs;
    }

    public boolean isJob(String jobName) {
        return arrayContains(getPossibleJobs(), jobName);
    }

    private File creteJobFile(String jobName) {
        return new File(jobsDir, jobName);
    }

    private File creteBuildsDir(String jobName) {
        return new File(creteJobFile(jobName), "builds");
    }

    public File creteBuildDir(String jobName, int id) {
        return new File(creteBuildsDir(jobName), String.valueOf(id));
    }

    private File creteLogFile(String jobName, int id) {
        return creteLogFile(creteBuildDir(jobName, id));
    }

    private static File creteLogFile(File dir) {
        return new File(dir, "log");
    }

    public File creteChangelogFile(String jobName, int id) {
        return creteChangelogFile(creteBuildDir(jobName, id));
    }

    public static File creteChangelogFile(File dir) {
        return new File(dir, "changelog.xml");
    }

    public static boolean arrayContains(String[] as, String s) {
        for (String a : as) {
            if (a.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public int getLatestBuildId(String jobName) {
        return getLatestBuildId(creteBuildsDir(jobName));
    }

    private static int getLatestBuildId(File jobDir) {
        if (jobDir.exists() && jobDir.isDirectory()) {
            String[] files = getNonNullListing(jobDir);
            List<Integer> results = new ArrayList<>(files.length);
            for (String file : files) {
                try {
                    Integer i = Integer.valueOf(file);
                    results.add(i);
                } catch (Exception ex) {
                    System.err.println(jobDir + "/" + file + " is not number.");
                }
            }
            Collections.sort(results);
            return results.get(results.size() - 1);
        } else {
            throw new RuntimeException(jobDir + " do not exists or is not directory");
        }
    }

    public void checkJob(String jobName) {
        if (!isJob(jobName)) {
            System.err.println("Possible jobs");
            String[] pj = getPossibleJobs();
            for (String jobs : pj) {
                System.err.println(jobs);
            }
            throw new RuntimeException("Unknown job `" + jobName + "`");
        }
    }

    void printJobInfo(String jobName, Formatter formatter) {
        checkJob(jobName);
        String[] builds = getNonNullListing(creteBuildsDir(jobName));
        List<Integer> results = new ArrayList<>(builds.length);
        for (String build : builds) {
            if (isNumber(build)) {
                results.add(Integer.valueOf(build));
            }
        }
        int latest = getLatestBuildId(jobName);
        Collections.sort(results);
        formatter.initDoc();
        for (Integer result : results) {
            formatter.small();
            File f = creteBuildDir(jobName, result);
            if (isUnknown(f)) {
                formatter.startColor(Formatter.SupportedColors.Magenta);
            } else if (isFailed(f)) {
                formatter.startColor(Formatter.SupportedColors.Red);
            } else if (isAborted(f)) {
                formatter.startColor(Formatter.SupportedColors.LightMagenta);
            } else if (isUnstable(f)) {
                formatter.startColor(Formatter.SupportedColors.Yellow);
            } else if (isPassed(f)) {
                formatter.startColor(Formatter.SupportedColors.Green);
            } else {
                formatter.startColor(Formatter.SupportedColors.Cyan);
            }
            formatter.print("" + result + "(" + (result - latest) + "): ");
            formatter.print("" + getBuildXmlDisplayName(f));
            String tt = JobsRecognition.tail(creteLogFile(jobName, result));
            if (tt != null) {
                //tt = tt.trim();
            }
            formatter.print(" [" + tt + "]");
            if (isUnknown(f)) {
                formatter.print(" [unknown status!]");
            }
            formatter.closeBuildsList();
        }
        formatter.closeDoc();
    }

    public static boolean isNumber(String s) {
        try {
            Integer.valueOf(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String getChangelogsNvr(File buildPath) {
        return new ConfigFinder(creteChangelogFile(buildPath), "nvr", "/build/nvr").findInConfig();
    }

    public static String getBuildXmlDisplayName(File buildPath) {
        return new ConfigFinder(new File(buildPath, "build.xml"), "display-name", "/build/displayName").findInConfig();
    }

    //maybe linux only, not utf8 valid solution... nto much tested, just copypasted and worked
    public static String tail(File file) {
        if (!file.exists()) {
            return null;
        }
        if (cache.get(file) != null) {
            return cache.get(file);
        }
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;

                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }

                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            cache.put(file, lastLine);
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null) {
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //this is not static in purpose
    public boolean shouldBeSkipped(File f) {
        if (isUnknown(f)) {
            return true;
        }
        return (isAborted(f) || isFailed(f));
    }

    private final Pattern ABRTD = Pattern.compile(".*ABORTED.*");
    private final Pattern FAILD = Pattern.compile(".*FAILURE.*");
    private final Pattern UNSTB = Pattern.compile(".*UNSTABLE.*");
    private final Pattern PASSD = Pattern.compile(".*SUCCESS.*");

    public boolean isAborted(File f) {
        return ABRTD.matcher(tail(creteLogFile(f))).matches();
    }

    public boolean isFailed(File f) {
        return FAILD.matcher(tail(creteLogFile(f))).matches();
    }

    public boolean isUnstable(File f) {
        return UNSTB.matcher(tail(creteLogFile(f))).matches();
    }

    public boolean isPassed(File f) {
        return PASSD.matcher(tail(creteLogFile(f))).matches();
    }

    public boolean isUnknown(File f) {
        if (!f.exists()) {
            return true;
        }
        try {
            return !(isAborted(f) || isFailed(f) || isUnstable(f) || isPassed(f));
        } catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }
    }

}
