package io.jenkins.plugins.report.jtreg.main.recreate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.plugins.report.jtreg.model.Suite;

public class Recreate {

    protected final RecreateArgs args;

    public Recreate(RecreateArgs args) {
        this.args = args;
    }

    public static void main(String[] aargs) throws Exception {
        RecreateArgs args = new RecreateArgs(aargs);
        new Recreate(args).doWork();
    }

    private void doWork() throws Exception {
        if (args.isJtreg()) {
            new RecreateJtregReportSummaries(args).work();
        } else if (args.isJck()) {
            new RecreateJckReportSummaries(args).work();
        } else {
            throw new IllegalStateException("jtreg or jck keyword required");
        }
    }

    boolean isResultsArchive(String s) {
        return true;
    }

    String getPrefix() {
        return null;
    }

    @SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    void work() throws Exception {
        if (ReportSummaryUtil.isBuildDir(new File("."))) {
            recreate(new File(".").getCanonicalFile().toPath());
        } else {
            Path cwd = Paths.get(".").toAbsolutePath().normalize();
            if (cwd.resolve("builds").toFile().exists()) {
                cwd = cwd.resolve("builds");
            }
            try (Stream<Path> dirsStream = Files.list(cwd)) {
                dirsStream.sequential().filter(d -> !Files.isSymbolicLink(d)).forEach(this::recreate);
            }
        }
    }


    @SuppressFBWarnings(value = {"REC_CATCH_EXCEPTION", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = " npe of spotbugs sucks")
    private void recreate(Path buildPath) {
        if (!ReportSummaryUtil.isBuildDir(buildPath.toFile())) {
            return;
        }
        System.err.println("Processing: " + buildPath);
        //note, what archives, is defined in subclasses
        final List<Path> archives = ReportSummaryUtil.findArchives(buildPath, this);
        try (Stream<Path> tckReportsStream = archives.stream()) {
            List<Suite> suitesList = tckReportsStream.sequential().filter(p -> isResultsArchive(p.toString())).map(this::recreateImpl).filter(s -> s != null).collect(Collectors.toList());
            ReportSummaryUtil.backupAndStoreSummaries(getPrefix(), suitesList, buildPath, args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    Suite recreateImpl(Path path) {
        return null;
    }

}