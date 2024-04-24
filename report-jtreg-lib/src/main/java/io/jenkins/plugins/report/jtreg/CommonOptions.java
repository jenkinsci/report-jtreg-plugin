package io.jenkins.plugins.report.jtreg;

import io.jenkins.plugins.report.jtreg.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.formatters.PlainFormatter;

public class CommonOptions {
    private boolean die;
    private String jobsPath;
    private Formatter formatter;
    private Side substringSide;
    private int substringLength;
    private String exactTestsRegex;
    private String jenkinsUrl;

    public CommonOptions() {
        this.die = false;
        this.formatter = new PlainFormatter(System.out);
        this.substringSide = Side.TailEach;
        this.substringLength = 5000;
        this.exactTestsRegex = ".*";
    }

    public void setDie(boolean die) {
        this.die = die;
    }

    public boolean isDie() {
        return die;
    }

    public String getJobsPath() {
        return jobsPath;
    }

    public void setJobsPath(String jobsPath) {
        this.jobsPath = jobsPath;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public Side getSubstringSide() {
        return substringSide;
    }

    public void setSubstringSide(Side substringSide) {
        this.substringSide = substringSide;
    }

    public int getSubstringLength() {
        return substringLength;
    }

    public void setSubstringLength(int substringLength) {
        this.substringLength = substringLength;
    }

    public String getExactTestsRegex() {
        return exactTestsRegex;
    }

    public void setExactTestsRegex(String exactTestsRegex) {
        this.exactTestsRegex = exactTestsRegex;
    }

    public String getJenkinsUrl() {
        return jenkinsUrl;
    }

    public void setJenkinsUrl(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl;
    }

    public enum Side {
        Head, HeadEach, Tail, TailEach
    }
}