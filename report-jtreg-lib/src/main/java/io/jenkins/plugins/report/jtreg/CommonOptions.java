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

    /**
     * FIXME; this is terribly hackish an incredibnly wrong
     * But considering the nature of our CGI wrapper, th "self" url is not bubblibng down
     * unless it would be passed in similarly as jenkins url.
     * <p>
     * Already port is out of scope here:(
     *
     * @return nonsense
     */
    public String getSelfUrl() {
        final String wrongGuessedDefaultPort = "9090";
        if (jenkinsUrl != null) {
            if (jenkinsUrl.contains(":")) {
                return jenkinsUrl.replaceAll(":8080.*", ":" + wrongGuessedDefaultPort);
            } else {
                return jenkinsUrl + ":" + wrongGuessedDefaultPort;
            }
        } else {
            return "http://localhost:" + wrongGuessedDefaultPort;
        }
    }

    public String getDiffUrl() {
        return getSelfUrl() + Constants.DIFF_BACKEND;
    }

    public String getComparatorUrl() {
        return getSelfUrl() + Constants.COMPARATOR_BACKEND;
    }


    public enum Side {
        Head, HeadEach, Tail, TailEach
    }
}