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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Arguments {

    private final String[] base;
    private final List<String> mainArgs;
    private boolean argsAreDirs;
    private String jenkinsDir;
    private File jobsDir;
    private String[] possibleJobs;

    public Arguments(String[] args) {
        this.mainArgs = new ArrayList<>(args.length);
        if (args.length == 0) {
            System.out.println(" options DIR1 DIR2 DIR3 ... DIRn");
            System.out.println("  or  ");
            System.out.println(" options JOB_NAME-1 buildPointer1.1 buildPointer1.2  ... jobPointer1.N JOB_NAME-2 buildPointer2.1 buildPointer2.2  ... jobPointer2.N ... JOB_NAME-N ...jobPointerN.N");
            System.out.println("  options:  ");
            System.out.println(" " + output + "=" + argsToHelp(knownOutputs));
            System.out.println(" default output is 'plain text'. 0-1 of " + output + " is allowed.");
            System.out.println(" " + view + "=" + argsToHelp(knownViews));
            System.out.println(" default view is 'all'. 0-N of " + view + " is allowed.");
            System.out.println(" job pointers are numbers. If zero or negative, then it is 0 for last one, -1 for one beofre last ...");
            System.out.println(" When using even number of build pointers, you can use " + fillSwitch + " switch to consider them as rows");
            System.out.println(" Another strange argument is " + keepFailedSwitch + " which will include failed/aborted/not-existing builds/dirs during listing.");
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
            if (!isNumber(opt)) {
                //not u number.. is known?
                if (opt.startsWith("-") && !arrayContains(switches, opt)) {
                    System.err.println("WARNING unknown param " + opt);

                }
            }
        }
    }

    private static final String output = "-output";
    private static final String view = "-view";
    private static final String fillSwitch = "-fill";
    private static final String keepFailedSwitch = "-keep-failed";

    private static final String[] switches = {output, view, fillSwitch, keepFailedSwitch};

    static final String output_html = "html";
    static final String output_color = "color";
    private static final String[] knownOutputs = new String[]{output_color, output_html};

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

    private static final String[] knownViews = new String[]{
        view_info_summary, view_info_summary_suites, view_info_problems, view_info_hidevalues,
        view_diff_summary, view_diff_summary_suites, view_diff_details, view_diff_list,
        view_hide_positives, view_hide_negatives, view_hide_misses, view_hide_totals,
        view_diff, view_info
    };

    public Options parse() {
        Options result = new Options();
        for (String arg : base) {
            if (arg.startsWith(output + "=")) {
                String output_type = arg.split("=")[1];
                if (!arrayContains(knownOutputs, output_type)) {
                    throw new RuntimeException("uknown arg for " + output + " - " + output_type);
                }
                result.setOutputType(output_type);
            } else if (arg.startsWith(view + "=")) {
                String nextView = arg.split("=")[1];
                if (!arrayContains(knownViews, nextView)) {
                    throw new RuntimeException("uknown arg for " + view + " - " + nextView);
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
            throw new RuntimeException("No main argument. At elast one Directory is expected");
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

        jenkinsDir = System.getProperty("jenkins_home");
        if (jenkinsDir == null) {
            jenkinsDir = System.getenv("JENKINS_HOME");
        }
        if (!argsAreDirs) {
            if (jenkinsDir == null) {
                throw new RuntimeException("You are working in jenkins jobs mode, but non -Djenkins_home nor $JENKINS_HOME is specified");
            }
            jobsDir = new File(jenkinsDir, "jobs");
            possibleJobs = jobsDir.list();
            Arrays.sort(possibleJobs);
            String jobName = null;
            File jobDir;
            File buildsDir = null;
            Integer latestBuild = null;
            if (result.isFill()) {
                int i = -1;
                while (true) {
                    i++;
                    if (i >= mainArgs.size()) {
                        break;
                    }
                    String arg = mainArgs.get(i);
                    if (!isNumber(arg)) {
                        jobName = arg;
                        checkJob(jobName);
                        jobDir = new File(jobsDir, jobName);
                        buildsDir = new File(jobDir, "builds");
                        latestBuild = getLatestBuildId(buildsDir);
                        continue;
                    }
                    if (jobName == null) {
                        throw new RuntimeException("You are tying to specify build " + arg + " but not have no job specified ahead.");
                    }
                    int from = Integer.valueOf(arg);
                    i++;
                    arg = mainArgs.get(i);
                    if (!isNumber(arg)) {
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
                            result.add(new File(buildsDir, String.valueOf(x)));
                        }
                    } else {
                        for (int x = from; x <= to; x++) {
                            result.add(new File(buildsDir, String.valueOf(x)));
                        }
                    }
                }
            } else {
                for (int i = 0; i < mainArgs.size(); i++) {
                    String arg = mainArgs.get(i);
                    if (!isNumber(arg)) {
                        jobName = arg;
                        checkJob(jobName);
                        jobDir = new File(jobsDir, jobName);
                        buildsDir = new File(jobDir, "builds");
                        latestBuild = getLatestBuildId(buildsDir);
                        continue;
                    }
                    if (jobName == null) {
                        throw new RuntimeException("You are tying to specify build " + arg + " but not have no job specified ahead.");
                    }
                    int jobId = Integer.valueOf(mainArgs.get(i));
                    if (jobId <= 0) {
                        jobId = latestBuild + jobId;
                    }
                    jobId = sanitize(jobId);
                    result.add(new File(buildsDir, String.valueOf(jobId)));
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

    private int sanitize(int jobId) {
        if (jobId <= 0) {
            return 1;
        }
        return jobId;
    }

    public boolean isNumber(String s) {
        try {
            Integer.valueOf(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isJob(String jobName) {
        return arrayContains(possibleJobs, jobName);
    }

    private void checkJob(String jobName) {
        if (!isJob(jobName)) {
            System.out.println("Possible jobs");
            for (String jobs : possibleJobs) {
                System.out.println(jobs);
            }
            throw new RuntimeException("Unknown job `" + jobName + "`");
        }
    }
}
