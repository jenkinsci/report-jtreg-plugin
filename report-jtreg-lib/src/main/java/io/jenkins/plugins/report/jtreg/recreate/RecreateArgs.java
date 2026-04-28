package io.jenkins.plugins.report.jtreg.recreate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import io.jenkins.plugins.report.jtreg.writers.WriterKinds;

public class RecreateArgs implements ExportArgs{

    private final List<String> aargs;

    public RecreateArgs(String[] args) {
        this.aargs = Collections.unmodifiableList((Arrays.asList(args)));
        if (containsSwitch("-h") || containsSwitch("-help")) {
            System.out.println("From  Recreate class you must specify `jtreg` or `jck` keyword");
            System.out.println("From  RecreateJckReportSummaries and RecreateJtregReportSummaries classes it may be omitted");
            System.out.println("This tool operates from CWD, whcihc should be build dir or job dir. In case of job dir it process all builds");
            System.out.println("-url <url> will allow generation of links in plaintext results pointing to <url>/job/<job>/...");
            System.out.println("-add-files <f1,..> f can be coma separated list of files which should be backuped too. Like build.xml,changelog.xml");
            System.out.println("-archives <regex> tool uses quite free recognition to find results in archive dir. Set your own regex instead of it to restrict it");
            System.out.println("-out <path>    save the files to a tmp-like <out> directory instead of original dir");
            System.out.println("               reason for <out-path> is the mandatory generation in two steps");
            System.out.println("-no-restore    If -out is specified, by default the original files will be restored.");
            System.out.println("               Setting this flag will disable it, and will keep freshly generated files");
            System.out.println("-nvr-db <path> from <out-path> will copy the results to path/displayName/job/buildId");
            System.out.println("               if -out <out-path> is not set, tmp is created, used, and dropped");
            System.out.println("-job-db <path> from <out-path> will copy the results to path/job/buildId");
            System.out.println("               if -out <out-path> is not set, tmp is created, used, and dropped");
            System.out.println("-kinds <k1,..> k can be coma separated list of any of PLAIN, JSON, PROPERTIES");
            System.out.println("               PLAIN will regenerate plaintexts, JSON jsons and PROPERTIES properties. Default is JSON,PLAIN,PROPERTIES");
            System.out.println("               You can not regenerate PLAIN or PROPERTIES without jsons already in place");
            System.out.println("               Everything is always backup-ed and exported. This is really about what is regenerated");
            System.out.println("               Special value is NONE, which willc ause to regenerate nothing, and just export (if set)");
            System.out.println("                                                                               ");
            System.out.println(" !!!!!!!!      if -out <out-path> is not set, tmp is created, used, and dropped for -nvr-db/-job-db");
            System.out.println("                                                                               ");
            System.out.println("Actually the current impl for cases with  <out-path> is, that:");
            System.out.println("      the backup is done");
            System.out.println("      new files are generated");
            System.out.println("      files are moved to out-path and copied as needed");
            System.out.println("      backup is restored and backup-timestamp.zip is removed");
            System.out.println("      nvr-db, job-db and add-files are substitued by config if called from jenkins via plugin");
            throw new RuntimeException("help requested");
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

    public String getNvrDb() {
        return get("-nvr-db");
    }

    public String getJobDb() {
        return get("-job-db");
    }

    public String getArchivesRegex() {
        return get("-archives");
    }

    public List<String> getAdditionalFiles() {
        List<String> r = new ArrayList<>();
        String orig = get("-add-files");
        if (orig != null) {
            for (String file : orig.split(",")) {
                r.add(file);
            }
        }
        return Collections.unmodifiableList(r);
    }

    public List<WriterKinds> getKinds() {
        List<WriterKinds> r = new ArrayList<>();
        String orig = get("-kinds");
        if (orig != null) {
            for (String kind : orig.split(",")) {
                r.add(WriterKinds.valueOf(kind.toUpperCase()));
            }
        }
        return Collections.unmodifiableList(r);
    }

    public boolean isNoRestore() {
        return containsSwitch("-no-restore");
    }

    private String get(String s) {
        for (int i = 0; i < aargs.size() - 1; i++) {
            String arg = aargs.get(i);
            if (compareSwitches(s, arg)) {
                return aargs.get(i + 1);
            }
        }
        return null;
    }

    private boolean containsSwitch(String s) {
        for (int i = 0; i < aargs.size() - 1; i++) {
            String arg = aargs.get(i);
            if (compareSwitches(s, arg)) {
                return true;
            }
        }
        return false;
    }

    private static boolean compareSwitches(String s, String arg) {
        return arg.replaceAll("^-+", "-").equals(s.replaceAll("^-+", "-"));
    }
}
