package io.jenkins.plugins.report.jtreg.main.comparator;

public class Options {
    private Operations operation;
    private String jobsPath;
    private String queryString;
    private String nvrQuery;
    private String test;
    private int numberOfBuilds;
    private boolean skipFailed;
    private boolean showNvrs;

    public Options() {
        this.queryString = "";
        this.nvrQuery = "";
        this.numberOfBuilds = 1;
        this.skipFailed = true;
        this.showNvrs = false;
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

    public String getNvrQuery() {
        return nvrQuery;
    }

    public void setNvrQuery(String nvr) {
        this.nvrQuery = nvr;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
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

    public boolean isShowNvrs() {
        return showNvrs;
    }

    public void setShowNvrs(boolean showNvrs) {
        this.showNvrs = showNvrs;
    }

    // enum of all available operations
    public enum Operations {
        List, Enumerate, Compare
    }
}