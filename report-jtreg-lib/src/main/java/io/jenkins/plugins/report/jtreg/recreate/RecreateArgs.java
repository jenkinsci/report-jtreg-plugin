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
            System.out.println("-out <path>    will copy the results to custom directory");
            System.out.println("-nvr-db <path> will copy the results to path/displayName/job/buildId");
            System.out.println("-job-db <path> will copy the results to path/job/buildId");
            System.out.println("-no-restore    by default the original files will be restored.");
            System.out.println("               Setting this flag will disable it, and will keep freshly generated files");
            System.out.println("-kinds <k1,..> can be coma separated list of any of PLAIN, JSON, PROPERTIES");
            System.out.println("               PLAIN will regenerate plaintexts, JSON jsons and PROPERTIES properties. Default is JSON,PLAIN,PROPERTIES");
            System.out.println("               You can not regenerate PLAIN or PROPERTIES without jsons already in place");
            System.out.println("               Everything is always backup-ed and exported. This is really about what is regenerated");
            System.out.println("               Special value is NONE, which willc ause to regenerate nothing, and just export (if set)");
            System.out.println("                                                                               ");
            System.out.println("Because regeneration regenerates also the jsons, which are crucial to the operation of plugin, the export works like this:");
            System.out.println("      the backup is done");
            System.out.println("      new files are generated");
            System.out.println("      copied to the out/nvr/job external locations");
            System.out.println("      backup is restored and backup-timestamp.zip is removed (unless forbidden by -no-restore)");
            System.out.println("      nvr-db, job-db and add-files are substituted by config if called from jenkins via plugin");
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
        String orig = get("-add-files");
        return splitImpl(orig);
    }

    private static List<String> splitImpl(String s) {
        List<String> r = new ArrayList<>();
        if (s != null) {
            for (String file : s.split(",")) {
                r.add(file.trim());
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

    /*
     * Faking  main method arguments for reuse from plugin
     */


    private static String sanitize(String a) {
        if (a == null) {
            return null;
        }
        if (a.isBlank()) {
            return null;
        }
        return a.trim();
    }


    public static String getOut(String targetFolders) {
        List<String> count = splitImpl(targetFolders);
        for (String c : count) {
            if (c.startsWith("out-dir:")) {
                return c.substring("out-dir:".length());
            }
        }
        return null;

    }

    public static String getNvrDb(String targetFolders) {
        if (sanitize(targetFolders) == null) {
            return null;
        }
        List<String> count = splitImpl(targetFolders);
        if (count.size() == 1) {
            return targetFolders + "/nvr-db";
        } else {
            for (String c : count) {
                if (c.startsWith("nvr-db:")) {
                    return c.substring("nvr-db:".length());
                }
            }
            return null;
        }
    }

    public static String getJobDb(String targetFolders) {
        if (sanitize(targetFolders) == null) {
            return null;
        }
        List<String> count = splitImpl(targetFolders);
        if (count.size() == 1) {
            return targetFolders + "/job-db";
        } else {
            for (String c : count) {
                if (c.startsWith("job-db:")) {
                    return c.substring("job-db:".length());
                }
            }
            return null;
        }
    }

    public static RecreateArgs fromJenkins(String additionalFiles, String targetFolders, String prefix, String rootUrl, String kinds) {
        List<String> aargs = new ArrayList<>();
        aargs.add(prefix);
        if (rootUrl != null && !rootUrl.isBlank()) {
            aargs.add("-url");
            aargs.add(rootUrl);
        }
        if (additionalFiles != null && !additionalFiles.isBlank()) {
            aargs.add("-add-files");
            aargs.add(additionalFiles);
        }
        String outDir = getOut(targetFolders);
        if (outDir != null && !outDir.isBlank()) {
            aargs.add("-out");
            aargs.add(outDir);
        }
        String nvrDir = getNvrDb(targetFolders);
        if (nvrDir != null && !nvrDir.isBlank()) {
            aargs.add("-nvr-db");
            aargs.add(nvrDir);
        }
        String jobDir = getJobDb(targetFolders);
        if (jobDir != null && !jobDir.isBlank()) {
            aargs.add("-job-db");
            aargs.add(jobDir);
        }
        if (kinds != null && !kinds.isBlank()) {
            aargs.add("-kinds");
            aargs.add(kinds);
        }
        //probably not needed as there is no zip.. but...
        aargs.add("-no-restore");
        return new RecreateArgs(aargs.toArray(new String[0]));
    }
}
