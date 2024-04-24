package io.jenkins.plugins.report.jtreg.main.comparator.jobs;

import io.jenkins.plugins.report.jtreg.arguments.Argument;
import io.jenkins.plugins.report.jtreg.main.comparator.arguments.ComparatorArgDeclaration;

import java.io.File;
import java.util.ArrayList;

public class JobsByRegex implements JobsProvider {
    public static final Argument regexArg = new Argument("--regex", "A regular expression to filter the jobs by their names.", " <regex>");


    private String regexString;
    private boolean forceVague;
    private ArrayList<File> jobsInDir;
    private ArrayList<File> matchedJobs;

    public JobsByRegex() {
        this.regexString = "";
        this.forceVague = false;
        this.jobsInDir = new ArrayList<>();
        this.matchedJobs = new ArrayList<>();
    }

    public static ArrayList<String> getSupportedArgsStatic() {
        ArrayList<String> supportedArgs = new ArrayList<>();
        supportedArgs.add(regexArg.getName());
        return supportedArgs;
    }

    public ArrayList<String> getSupportedArgs() {
        return getSupportedArgsStatic();
    }

    public void parseArguments(String argument, String value) {
        if (argument.equals(regexArg.getName())) {
            this.regexString = value;
        } else if (argument.equals(ComparatorArgDeclaration.forceArg.getName())) {
            this.forceVague = true;
        } else {
            throw new RuntimeException("JobsByRegex got an unexpected argument.");
        }
    }

    public void addJobs(ArrayList<File> jobsInDir) {
        this.jobsInDir = jobsInDir;
    }

    public void filterJobs() {
        // check if the regex is not too vague (has only .*)
        if (this.regexString.matches("^(\\.\\*)*$") && !this.forceVague) {
            throw new RuntimeException("The regular expression is too vague (contains only .*), run with --force to continue anyway.");
        }

        this.matchedJobs = new ArrayList<>();
        for (File job : this.jobsInDir) {
            if (job.getName().matches(this.regexString)) {
                this.matchedJobs.add(job);
            }
        }
    }

    public ArrayList<File> getJobs() {
        return matchedJobs;
    }

}
