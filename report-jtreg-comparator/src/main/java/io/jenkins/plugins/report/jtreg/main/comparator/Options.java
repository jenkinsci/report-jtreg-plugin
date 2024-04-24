package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.CommonOptions;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Options extends CommonOptions {
    private Operations operation;
    private int numberOfBuilds;
    private boolean forceVague;
    private boolean onlyVolatile;
    private boolean useDefaultBuild;
    private JobsProvider jobsProvider;
    private boolean printVirtual;
    private boolean hidePasses;
    private final Map<String, Configuration> configurations;
    private String referentialJobName;
    private int referentialBuildNumber;

    public Options() {
        this.numberOfBuilds = 1;
        this.forceVague = false;
        this.onlyVolatile = false;
        this.useDefaultBuild = false;
        this.jobsProvider = null;
        this.printVirtual = false;
        this.hidePasses = false;
        this.configurations = new HashMap<>();
        this.referentialJobName = null;
        this.referentialBuildNumber = -1;
        // default configuration for getting job results
        Configuration resultConfig = new Configuration("build.xml", "/build/result", Locations.Build);
        resultConfig.setValue("{SUCCESS,UNSTABLE}");
        addConfiguration("result", resultConfig);
    }

    public Operations getOperation() {
        return operation;
    }

    public void setOperation(Operations operation) {
        this.operation = operation;
    }

    public int getNumberOfBuilds() {
        return numberOfBuilds;
    }

    public void setNumberOfBuilds(int numberOfBuilds) {
        this.numberOfBuilds = numberOfBuilds;
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

    public void setHidePasses(boolean hidePasses) {
        this.hidePasses = hidePasses;
    }

    public boolean isHidePasses() {
        return hidePasses;
    }

    public void setPrintVirtual(boolean printVirtual) {
        this.printVirtual = printVirtual;
    }

    public String getReferentialJobName() {
        return referentialJobName;
    }

    public void setReferentialJobName(String referentialJobName) {
        this.referentialJobName = referentialJobName;
    }

    public int getReferentialBuildNumber() {
        return referentialBuildNumber;
    }

    public void setReferentialBuildNumber(int referentialBuildNumber) {
        this.referentialBuildNumber = referentialBuildNumber;
    }

    public Configuration getConfiguration(String whatToFind) {
        return configurations.get(whatToFind);
    }

    public Map<String, Configuration> getAllConfigurations() {
        return configurations;
    }

    public void addConfiguration(String whatToFind, Configuration configuration) {
        this.configurations.put(whatToFind, configuration);
    }

    // enum of all available operations
    public enum Operations {
        List, Enumerate, Compare, Print, TraceCompare
    }

    public enum Locations {
        Build, Job
    }

    public static class Configuration {
        private final String configFileName;
        private final String findQuery;
        private final Locations location;
        private String value;

        public Configuration(String configFileName, String findQuery, Locations location) {
            this.configFileName = configFileName;
            this.findQuery = findQuery;
            this.location = location;
            this.value = null;
        }

        public String getConfigFileName() {
            return configFileName;
        }
        public String getFindQuery() {
            return findQuery;
        }
        public Locations getLocation() {
            return location;
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

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}