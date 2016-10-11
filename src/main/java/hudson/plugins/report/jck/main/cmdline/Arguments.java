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
package hudson.plugins.report.jck.main.cmdline;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Arguments {

    private final String[] base;
    private final List<String> mainArgs;
    private boolean argsAreDirs;

    public Arguments(String[] args) {
        this.mainArgs = new ArrayList<>(args.length);
        if (args.length == 0) {
            printHelp(System.out);
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
            if (!JobsRecognition.isNumber(opt)) {
                //not u number.. is known?
                if (opt.startsWith("-") && !JobsRecognition.arrayContains(switches, opt)) {
                    System.err.println("WARNING unknown param " + opt);

                }
            }
        }
    }

    public static String printHelp() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        printHelp(ps);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void printHelp(PrintStream p) {
        p.println(" options DIR1 DIR2 DIR3 ... DIRn");
        p.println("  or  ");
        p.println(" options JOB_NAME-1 buildPointer1.1 buildPointer1.2  ... jobPointer1.N JOB_NAME-2 buildPointer2.1 buildPointer2.2  ... jobPointer2.N ... JOB_NAME-N ...jobPointerN.N");
        p.println("     If you use only jobname, then its builds will be listed:  ");
        p.println("  options:  ");
        p.println(" " + output + "=" + argsToHelp(knownOutputs));
        p.println(" default output is 'plain text'. 0-1 of " + output + " is allowed.");
        p.println(" " + view + "=" + argsToHelp(knownViews));
        p.println(" default view is 'all'. 0-N of " + view + " is allowed. Best view is " + bestViewToString() + "");
        p.println(" job pointers are numbers. If zero or negative, then it is 0 for last one, -1 for one beofre last ...");
        p.println(" Unknow job will lead to listing of jobs.");
        p.println(" When using even number of build pointers, you can use " + fillSwitch + " switch to consider them as rows");
        p.println(" Another strange argument is " + keepFailedSwitch + " which will include failed/aborted/not-existing builds/dirs during listing.");
    }

    private static final String output = "-output";
    private static final String view = "-view";

    private static final String fillSwitch = "-fill";
    private static final String keepFailedSwitch = "-keep-failed";

    public static final String[] knownBoolSwitches = sortA(new String[]{fillSwitch, keepFailedSwitch});

    private static final String[] switches = concat(new String[]{output, view}, knownBoolSwitches);

    public static final String output_html = "html";
    static final String output_html2 = "html2";
    static final String output_color = "color";
    public static final String[] knownOutputs = sortA(new String[]{output_html, output_color, output_html2});

    static final String view_info_summary = "info-summary";
    static final String view_info_summary_suites = "info-summary-suites";
    static final String view_info_problems = "info-problems";
    static final String view_info = "info";
    static final String view_info_hidevalues = "info-hide-details";

    static final String view_diff_summary = "diff-summary";
    static final String view_diff_summary_suites = "diff-summary-suites";
    static final String view_diff_details = "diff-details";
    static final String view_diff_list = "diff-list";
    static final String view_diff = "diff";

    static final String view_hide_positives = "hide-positives";
    static final String view_hide_negatives = "hide-negatives";
    static final String view_hide_misses = "hide-misses";
    static final String view_hide_totals = "hide-totals";

    public static final String[] bestViews = sortA(new String[]{
        view_diff_list, view_info, view_info_hidevalues
    });

    public static final String[] knownViews = sortA(new String[]{
        view_info_summary, view_info_summary_suites, view_info_problems, view_info_hidevalues,
        view_diff_summary, view_diff_summary_suites, view_diff_details, view_diff_list,
        view_hide_positives, view_hide_negatives, view_hide_misses, view_hide_totals,
        view_diff, view_info
    });

    public Options parse() {
        Options result = new Options();
        result.setStream(System.out);
        for (String arg : base) {
            if (arg.startsWith(output + "=")) {
                String output_type = arg.split("=")[1];
                if (!JobsRecognition.arrayContains(knownOutputs, output_type)) {
                    System.err.println(argsToHelp(knownOutputs));
                    throw new RuntimeException("unknown arg for " + output + " - " + output_type);
                }
                result.setOutputType(output_type);
            } else if (arg.startsWith(view + "=")) {
                String nextView = arg.split("=")[1];
                if (!JobsRecognition.arrayContains(knownViews, nextView)) {
                    System.err.println(argsToHelp(knownViews));
                    throw new RuntimeException("unknown arg for " + view + " - " + nextView);
                }
                result.addView(nextView);
            } else if (arg.equals(fillSwitch)) {
                result.setFill(true);
            } else if (arg.equals(keepFailedSwitch)) {
                result.setSkipFailed(false);
            } else {
                mainArgs.add(arg);
            }
        }
        if (mainArgs.isEmpty()) {
            throw new RuntimeException("No main argument. At elast one directory or jobname is expected");
        }

        if (result.isFill() && mainArgs.size() <= 2) {
            throw new RuntimeException(fillSwitch + " can be used only for two or more job pointers. You have " + (mainArgs.size() - 1));
        }
        Boolean allSameKind = null;
        for (String string : mainArgs) {
            if (allSameKind == null) {
                allSameKind = new File(string).isDirectory();
            } else if (!new File(string).isDirectory() == allSameKind) {
                mainArgs.stream().forEach((badArg) -> {
                    System.err.println(badArg + " - " + new File(string).isDirectory());
                });
                throw new RuntimeException("Sorry, all main arguments must be directories or all must not be an directories. You have mix");
            }
        }
        argsAreDirs = allSameKind;

        if (!argsAreDirs) {
            if (JobsRecognition.jobsRecognition().getJenkinsDir() == null) {
                throw new RuntimeException("You are working in jenkins jobs mode, but non -Djenkins_home nor $JENKINS_HOME is specified");
            }
            if (mainArgs.size() == 1) {
                //only information about job will be printed
                JobsRecognition.jobsRecognition().printJobInfo(mainArgs.get(0), result.getFormatter());
                System.exit(0);
            }
            String jobName = null;
            Integer latestBuild = null;
            if (result.isFill()) {
                int i = -1;
                while (true) {
                    i++;
                    if (i >= mainArgs.size()) {
                        break;
                    }
                    String arg = mainArgs.get(i);
                    if (!JobsRecognition.isNumber(arg)) {
                        jobName = arg;
                        JobsRecognition.jobsRecognition().checkJob(jobName);
                        latestBuild = JobsRecognition.jobsRecognition().getLatestBuildId(jobName);
                        System.err.println("latest build for " + jobName + " is " + latestBuild);
                        continue;
                    }
                    if (jobName == null) {
                        throw new RuntimeException("You are tying to specify build " + arg + " but not have no job specified ahead.");
                    }
                    int from = Integer.valueOf(arg);
                    i++;
                    arg = mainArgs.get(i);
                    if (!JobsRecognition.isNumber(arg)) {
                        throw new RuntimeException("You have " + fillSwitch + " set, but when reading " + arg + " it looks like odd number of arguments. Even expected");
                    }
                    int to = Integer.valueOf(arg);
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
                            result.add(JobsRecognition.jobsRecognition().creteBuildDir(jobName, x));
                        }
                    } else {
                        for (int x = from; x <= to; x++) {
                            result.add(JobsRecognition.jobsRecognition().creteBuildDir(jobName, x));
                        }
                    }
                }
            } else {
                for (int i = 0; i < mainArgs.size(); i++) {
                    String arg = mainArgs.get(i);
                    if (!JobsRecognition.isNumber(arg)) {
                        jobName = arg;
                        JobsRecognition.jobsRecognition().checkJob(jobName);
                        latestBuild = JobsRecognition.jobsRecognition().getLatestBuildId(jobName);
                        System.err.println("latest build for " + jobName + " is " + latestBuild);
                        continue;
                    }
                    if (jobName == null) {
                        throw new RuntimeException("You are tying to specify build " + arg + " but not have no job specified ahead.");
                    }
                    int origJobId = Integer.valueOf(mainArgs.get(i));
                    int jobId = origJobId;
                    if (jobId <= 0) {
                        jobId = latestBuild + jobId;
                    }
                    jobId = sanitize(jobId);
                    if (result.isSkipFailed()) {
                        while (true) {
                            //iterating untill we find an passing build
                            boolean added = result.add(JobsRecognition.jobsRecognition().creteBuildDir(jobName, jobId));
                            if (added) {
                                break;
                            }
                            if (origJobId <= 0) {
                                jobId--;
                            } else {
                                jobId++;
                            }
                            if ((jobId < 1) || (jobId > latestBuild)) {
                                break;
                            }
                        }
                    } else {
                        result.add(JobsRecognition.jobsRecognition().creteBuildDir(jobName, jobId));
                    }
                }
            }
        } else {
            //no jenkins mode
            mainArgs.stream().forEach((string) -> {
                result.add(new File(string));
            });
        }
        if (result.getDirsToWork().isEmpty()) {
            throw new RuntimeException("No directories to work on at the end!");
        }
        return result;
    }

    private static String argsToHelp(String[] outputs) {
        StringBuilder sb = new StringBuilder();
        for (String o : outputs) {
            sb.append(o).append("|");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private int sanitize(int jobId) {
        if (jobId <= 0) {
            return 1;
        }
        return jobId;
    }

    private static String[] sortA(String[] string) {
        Arrays.sort(string);
        return string;
    }

    public static String[] concat(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c = new String[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private static String bestViewToString() {
        StringBuilder sb = new StringBuilder();
        for (String v : bestViews) {
            sb.append(" " + view + "=").append(v).append(" ");
        }
        return sb.toString();
    }

}
