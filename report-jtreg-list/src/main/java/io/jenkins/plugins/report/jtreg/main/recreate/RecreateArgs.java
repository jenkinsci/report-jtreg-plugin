package io.jenkins.plugins.report.jtreg.main.recreate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecreateArgs {

    private final List<String> aargs;

    public RecreateArgs(String[] args) {
        this.aargs = Collections.unmodifiableList((Arrays.asList(args)));
        if (containsSwitch("-h") || containsSwitch("--help")) {
            System.out.println("From "+Recreate.class.getName()+" class you must specify `jtreg` or `jck` keyword");
            System.out.println("From "+RecreateJckReportSummaries.class.getName()+" and " + RecreateJtregReportSummaries.class.getName()+" classes it may be ommited");
            System.out.println("-url <url> will allow generation of links in plaintext results pointing to <url>/job/<job>/...");
            System.out.println("-originals NOT_YET_IMPLENMENED - will restore the unpacked xml files as needed");
            System.out.println("-out <path>    save the files to a tmp-like <out> directory instead of original dir");
            System.out.println("               reason for <out-path> is the mandatory generation in two steps");
            System.out.println("-no-restore    If -out is specified, by default the original files will be restored.");
            System.out.println("               Settin this flag will disable it, and will keep freshly generated files");
            System.out.println("-nvr-db <path> from <out-path> will copy the results to path/displayName/job/buildId");
            System.out.println("               if -out <out-path> is not set, tmp is created, used, and dropped");
            System.out.println("-job-db <path> from <out-path> will copy the results to path/job/buildId");
            System.out.println("               if -out <out-path> is not set, tmp is created, used, and dropped");
            System.out.println("Actually the current impl for cases with  <out-path> is, that:");
            System.out.println("      the backup is done");
            System.out.println("      new files are generated");
            System.out.println("      files are moved to out-path and copied as needed");
            System.out.println("      backup is restored and backup-timestamp.zip is removed");
            System.exit(1);
        }
    }

    public boolean isJtreg() {
        return contains("jtreg");
    }

    public boolean isJck() {
        return contains("jck");
    }

    private boolean contains(String s) {
        if (aargs.contains(s)) {
            return true;
        }
        return false;
    }

    public String getUrl() {
        return get("-url");
    }

    public String getOut() {
        return get("-out");
    }

    public boolean isNoRestore() {
        return containsSwitch("-no-restore");
    }

    private String get(String s) {
        for(int i = 0; i < aargs.size() - 1; i++) {
            String arg = aargs.get(i);
            if (compareSwitches(s, arg)){
                return aargs.get(i+1);
            }
        }
        return null;
    }

    private boolean containsSwitch (String s) {
        for(int i = 0; i < aargs.size() - 1; i++) {
            String arg = aargs.get(i);
            if (compareSwitches(s, arg)){
                return true;
            }
        }
        return false;
    }

    private static boolean compareSwitches(String s, String arg) {
        return arg.replaceAll("^-+", "-").equals(s.replaceAll("^-+", "-"));
    }
}
