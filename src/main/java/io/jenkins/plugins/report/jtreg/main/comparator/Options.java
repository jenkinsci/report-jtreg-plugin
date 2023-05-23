package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.formatters.Formatters;
import io.jenkins.plugins.report.jtreg.main.comparator.formatters.PlainFormatter;

public class Options {
    private Operations operation;
    private String jobsPath;
    private String queryString;
    private String regexString;
    private String nvrQuery;
    private int numberOfBuilds;
    private boolean skipFailed;
    private boolean forceVagueQuery;
    private int exactJobLength;
    private boolean onlyVolatile;
    private String exactTestsRegex;
    private Formatters formatter;

    public Options() {
        this.queryString = "";
        this.regexString = "";
        this.nvrQuery = "";
        this.numberOfBuilds = 1;
        this.skipFailed = true;
        this.forceVagueQuery = false;
        this.exactJobLength = -1; // negative means the length does not matter
        this.onlyVolatile = false;
        this.exactTestsRegex = ".*";
        this.formatter = new PlainFormatter(System.out);
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

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getRegexString() {
        return regexString;
    }

    public void setRegexString(String queryString) {
        this.regexString = queryString;
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

    public boolean isForceVagueQuery() {
        return forceVagueQuery;
    }

    public void setForceVagueQuery(boolean forceVagueQuery) {
        this.forceVagueQuery = forceVagueQuery;
    }

    public int getExactJobLength() {
        return exactJobLength;
    }

    public void setExactJobLength(int exactJobLength) {
        this.exactJobLength = exactJobLength;
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

    public Formatters getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatters formatter) {
        this.formatter = formatter;
    }

    // enum of all available operations
    public enum Operations {
        List, Enumerate, Compare, Print, Virtual
    }
}