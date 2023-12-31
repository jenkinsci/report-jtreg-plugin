package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsProvider;
import io.jenkins.plugins.report.jtreg.formatters.Formatter;
import io.jenkins.plugins.report.jtreg.formatters.PlainFormatter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    private boolean useDefaultBuild;
    private JobsProvider jobsProvider;
    private boolean printVirtual;
    private boolean die;
    private final Map<String, Configuration> configurations;

    public Options() {
        this.nvrQuery = "";
        this.numberOfBuilds = 1;
        this.skipFailed = true;
        this.forceVague = false;
        this.onlyVolatile = false;
        this.exactTestsRegex = ".*";
        this.formatter = new PlainFormatter(System.out);
        this.useDefaultBuild = false;
        this.jobsProvider = null;
        this.printVirtual = false;
        this.die = false;
        this.configurations = new HashMap<>();
    }

    public void setDie(boolean die) {
        this.die = die;
    }

    public boolean isDie() {
        return die;
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

    public boolean isUseDefaultBuild() {
        return useDefaultBuild;
    }

    public void setUseDefaultBuild(boolean useDefaultBuild) {
        this.useDefaultBuild = useDefaultBuild;
    }

    public boolean isPrintVirtual() {
        return printVirtual;
    }

    public void setPrintVirtual(boolean printVirtual) {
        this.printVirtual = printVirtual;
    }

    public Configuration getConfiguration(String whatToFind) {
        return configurations.get(whatToFind);
    }

    public void addConfiguration(String whatToFind, Configuration configuration) {
        this.configurations.put(whatToFind, configuration);
    }

    // enum of all available operations
    public enum Operations {
        List, Enumerate, Compare, Print
    }

    public enum Locations {
        Build, Job
    }

    public static class Configuration {
        private final String configFileName;
        private final String findQuery;
        private final Locations location;

        public Configuration(String configFileName, String findQuery, Locations location) {
            this.configFileName = configFileName;
            this.findQuery = findQuery;
            this.location = location;
        }

        public String getConfigFileName() {
            return configFileName;
        }
        public String getFindQuery() {
            return findQuery;
        }
        public File findConfigFile(File buildDir) {
            // This method takes a build directory as an argument.
            // If the user used --job-config-find, the method returns the config file in the job directory.
            if (location == Locations.Build) {
                return new File(buildDir, configFileName);
            } else {
                // go to the job directory from the build directory (.../job-dir/builds/1/...)
                return new File(buildDir.getParentFile().getParentFile(), configFileName);
            }
        }
    }
}