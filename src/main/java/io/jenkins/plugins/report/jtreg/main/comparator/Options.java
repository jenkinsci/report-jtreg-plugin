package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsProvider;
import io.jenkins.plugins.report.jtreg.main.diff.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.main.diff.formatters.PlainFormatter;

public class Options {
    private Operations operation;
    private String jobsPath;
    private String nvrQuery;
    private int numberOfBuilds;
    private boolean skipFailed;
    private boolean forceVague;
    private boolean onlyVolatile;
    private String exactTestsRegex;
    private Formatter formatter;
    private JobsProvider jobsProvider;

    public Options() {
        this.nvrQuery = "";
        this.numberOfBuilds = 1;
        this.skipFailed = true;
        this.forceVague = false;
        this.onlyVolatile = false;
        this.exactTestsRegex = ".*";
        this.formatter = new PlainFormatter(System.out);
        this.jobsProvider = null;
    }

    public Operations getOperation() {
        return operation;
    }

    public void setOperation(Operations operation) {
        this.operation = operation;
    }

    public String getJobsPath() {
        return jobsPath;
    }

    public void setJobsPath(String jobsPath) {
        this.jobsPath = jobsPath;
    }

    public String getNvrQuery() {
        return nvrQuery;
    }

    public void setNvrQuery(String nvr) {
        this.nvrQuery = nvr;
    }

    public int getNumberOfBuilds() {
        return numberOfBuilds;
    }

    public void setNumberOfBuilds(int numberOfBuilds) {
        this.numberOfBuilds = numberOfBuilds;
    }

    public boolean isSkipFailed() {
        return skipFailed;
    }

    public void setSkipFailed(boolean skipFailed) {
        this.skipFailed = skipFailed;
    }

    public boolean isForceVague() {
        return forceVague;
    }

    public void setForceVague(boolean forceVagueQuery) {
        this.forceVague = forceVagueQuery;
    }

    public boolean isOnlyVolatile() {
        return onlyVolatile;
    }

    public void setOnlyVolatile(boolean onlyVolatile) {
        this.onlyVolatile = onlyVolatile;
    }

    public String getExactTestsRegex() {
        return exactTestsRegex;
    }

    public void setExactTestsRegex(String exactTestsRegex) {
        this.exactTestsRegex = exactTestsRegex;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public JobsProvider getJobsProvider() {
        return jobsProvider;
    }

    public void setJobsProvider(JobsProvider jobsProvider) {
        this.jobsProvider = jobsProvider;
    }

    // enum of all available operations
    public enum Operations {
        List, Enumerate, Compare, Print, Virtual
    }
}