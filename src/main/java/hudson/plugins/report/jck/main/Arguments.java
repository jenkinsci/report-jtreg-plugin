/*
 * The MIT License
 *
 * Copyright 2016 
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
package hudson.plugins.report.jck.main;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Arguments {

    private final String[] base;
    private String output_type;
    private final List<String> views;
    private final List<String> mainArgs;
    private final List<File> dirsToWork;
    private boolean fill = false;
    private boolean argsAreDirs;
    private String jenkinsDir;
    private String jobName;
    private File jobDir;
    private File buildsDir;
    private int latestBuild;
    private boolean skipFailed;

    public Arguments(String[] args) {
        this.views = new ArrayList<>(args.length);
        this.mainArgs = new ArrayList<>(args.length);
        this.dirsToWork = new ArrayList<>(args.length);
        if (args.length == 0) {
            System.out.println(" [" + output + "=" + argsToHelp(knownOutputs) + "] [" + view + "=" + argsToHelp(knownViews) + "]+ DIR1 DIR2 DIR3 ... DIRn");
            System.out.println("  or  ");
            System.out.println(" [" + output + "=" + argsToHelp(knownOutputs) + "] [" + view + "=" + argsToHelp(knownViews) + "]+ JOB_NAME jobPointer1 jobPointer2 jobPointer3 ... jobPointerN");
            System.out.println("    default output is 'plain text'. 0-1 of " + output + " is allowed.");
            System.out.println("    default view is 'all'. 0-N of " + view + " is allowed.");
            System.out.println("    job pointers are numbers. If zero or negative, then it is 0 for last one, -1 for one beofre last ...");
            System.out.println("    When using even (>1)number of job pointers, you can use " + fillSwitch + " switch to consider them as rows");
            System.out.println("    Anither strange argument is " + skipFailedSwitch + " which will skip failed/notexisting dirs during listing.");
            throw new RuntimeException("At least one param expected");
        }
        this.base = new String[args.length];
        //clean dashes
        for (int i = 0; i < args.length; i++) {
            base[i] = args[i].replaceAll("^-+", "-");
        }
        //verify single output
        int outputs = 0;
        for (String base1 : base) {
            if (base1.startsWith(output + "=")) {
                outputs++;
            }
        }
        if (outputs > 1) {
            throw new RuntimeException("none or one " + output + " expected. you have " + outputs);
        }
        for (String base1 : base) {
            String opt = base1.split("=")[0];
            if (opt.startsWith("-") && !arrayContains(switches, opt)) {
                System.err.println("WARNING unknown param " + opt);

            }
        }
    }

    private static final String output = "-output";
    private static final String view = "-view";
    private static final String fillSwitch = "-fill";
    private static final String skipFailedSwitch = "-skip-failed";

    private static final String[] switches = {output, view, fillSwitch, skipFailedSwitch};

    private static final String output_html = "html";
    private static final String output_color = "color";
    private static final String[] knownOutputs = new String[]{output_color, output_html};

    private static final String view_info_summary = "info-summary";
    private static final String view_info_summary_suites = "info-summary-suites";
    private static final String view_info_problems = "info-problems";

    private static final String view_diff_summary = "diff-summary";
    private static final String view_diff_summary_suites = "diff-summary-suites";
    private static final String view_diff_details = "diff-details";
    private static final String view_diff_list = "diff-list";
    private static final String[] knownViews = new String[]{
        view_info_summary, view_info_summary_suites, view_info_problems,
        view_diff_summary, view_diff_summary_suites, view_diff_details, view_diff_list
    };

    public Arguments parse() {
        for (int i = 0; i < base.length; i++) {
            String arg = base[i];
            if (arg.startsWith(output + "=")) {
                output_type = arg.split("=")[1];
                if (!arrayContains(knownOutputs, output_type)) {
                    throw new RuntimeException("uknown arg for " + output + " - " + output_type);
                }
            } else if (arg.startsWith(view + "=")) {
                String nextView = arg.split("=")[1];
                if (!arrayContains(knownViews, nextView)) {
                    throw new RuntimeException("uknown arg for " + view + " - " + nextView);
                }
                views.add(nextView);
            } else if (arg.equals(fillSwitch)) {
                fill = true;
            } else if (arg.equals(skipFailedSwitch)) {
                skipFailed = true;
            } else {
                mainArgs.add(arg);
            }
        }
        if (mainArgs.isEmpty()) {
            throw new RuntimeException("No main argument. At elast one Directory is expected");
        }

        if (fill && mainArgs.size() <= 2) {
            throw new RuntimeException(fillSwitch + " can be used only for two or more job pointers. You have " + (mainArgs.size() - 1));
        }
        //fill canbe used with odd number of jobPointers
        // so removing one for job name
        if (fill && (mainArgs.size() - 1) % 2 == 1) {
            throw new RuntimeException(fillSwitch + " can be used only for even number of main arguments (job name donot count)");
        }
        Boolean allSameKind = null;
        for (String string : mainArgs) {
            if (allSameKind == null) {
                allSameKind = new File(string).isDirectory();
            } else if (!new File(string).isDirectory() == allSameKind) {
                for (String badArg : mainArgs) {
                    System.err.println(badArg + " - " + new File(string).isDirectory());
                }
                throw new RuntimeException("Sorry, all main arguments must be directories or all must not be an directories. You have mix");
            }
        }
        argsAreDirs = allSameKind;

        jenkinsDir = System.getProperty("jenkins_home");
        if (jenkinsDir == null) {
            jenkinsDir = System.getenv("JENKINS_HOME");
        }
        if (!argsAreDirs) {
            if (jenkinsDir == null) {
                throw new RuntimeException("You are working in jenkins jobs mode, but non -Djenkins_home nor $JENKINS_HOME is specified");
            }
            jobName = mainArgs.get(0);
            jobDir = new File(jenkinsDir + "/jobs/" + jobName);
            buildsDir = new File(jobDir, "builds");
            latestBuild = getLatestBuildId(buildsDir);
            if (fill) {
                //skipping first one - job name
                for (int i = 1; i < mainArgs.size(); i += 2) {
                    int from = Integer.valueOf(mainArgs.get(i));
                    int to = Integer.valueOf(mainArgs.get(i + 1));
                    if (from <= 0) {
                        from = latestBuild + from;
                    }
                    if (to <= 0) {
                        to = latestBuild + to;
                    }
                    from = sanitize(from);
                    to = sanitize(to);
                    if (from > to) {
                        for (int x = from; x >= to; x--) {
                            add(new File(buildsDir, String.valueOf(x)));
                        }
                    } else {
                        for (int x = from; x <= to; x++) {
                            add(new File(buildsDir, String.valueOf(x)));
                        }
                    }
                }
            } else {
                //skipping first one - job name
                for (int i = 1; i < mainArgs.size(); i++) {
                    int jobId = Integer.valueOf(mainArgs.get(i));
                    if (jobId <= 0) {
                        jobId = latestBuild + jobId;
                    }
                    jobId = sanitize(jobId);
                    add(new File(buildsDir, String.valueOf(jobId)));
                }
            }
        } else {
            //no jenkins mode
            for (String string : mainArgs) {
                add(new File(string));
            }
        }
        if (dirsToWork.isEmpty()) {
            throw new RuntimeException("No directories to work on at the end!");
        }
        return this;
    }

    private String argsToHelp(String[] outputs) {
        StringBuilder sb = new StringBuilder();
        for (String o : outputs) {
            sb.append(o).append("|");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private boolean arrayContains(String[] as, String s) {
        for (String a : as) {
            if (a.equals(s)) {
                return true;
            }
        }
        return false;
    }

    private static int getLatestBuildId(File jobDir) {
        if (jobDir.exists() && jobDir.isDirectory()) {
            String[] files = jobDir.list();
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

    private void add(File file) {
        if (skipFailed) {
            if (!file.exists()
                    || !new File(file, "log").exists()
                    || tail(new File(file, "log")).matches(".*(ABORTED|FAILURE).*")) {
                System.err.println("File " + file + " excluded - " + skipFailedSwitch + " specified, and it seems to be fialed build");
            } else {
                dirsToWork.add(file);
                System.err.println("Added " + file + " !");
            }
        } else {
            dirsToWork.add(file);
            System.err.println("Added " + file + " !");
        }
    }

    //maybe linux only, not utf8 valid solution... nto much tested, just copypasted and worked
    private String tail(File file) {
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

    public List<File> getDirsToWork() {
        return dirsToWork;
    }

    public boolean viewInfoSummary() {
        return views.isEmpty() || views.contains(view_info_summary);
    }

    public boolean viewInfoSummarySuites() {
        return views.isEmpty() || views.contains(view_info_summary_suites);
    }

    public boolean viewInfoProblems() {
        return views.isEmpty() || views.contains(view_info_problems);
    }

    public boolean viewDiffSummary() {
        return views.isEmpty() || views.contains(view_diff_summary);
    }

    public boolean viewDiffSummarySuites() {
        return views.isEmpty() || views.contains(view_diff_summary_suites);
    }

    public boolean viewDiffDetails() {
        return views.isEmpty() || views.contains(view_diff_details);
    }

    public boolean viewDiffList() {
        return views.isEmpty() || views.contains(view_diff_list);
    }

    private int sanitize(int jobId) {
        if (jobId <= 0) {
            return 1;
        }
        return jobId;
    }
}
