package io.jenkins.plugins.report.jtreg;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ConfigItem extends AbstractDescribableImpl<ConfigItem> {
    private String configFileName;
    private String whatToFind;
    private String findQuery;
    private String configLocation;

    @DataBoundConstructor
    public ConfigItem(String configFileName, String whatToFind, String findQuery, String configLocation) {
        // check if the user is trying to escape the current directory with ../
        this.configFileName = configFileName;
        this.whatToFind = whatToFind;
        this.findQuery = findQuery;
        this.configLocation = configLocation;
    }

    public String getConfigFileName() {
        return configFileName;
    }
    @DataBoundSetter
    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getWhatToFind() {
        return whatToFind;
    }
    @DataBoundSetter
    public void setWhatToFind(String whatToFind) {
        this.whatToFind = whatToFind;
    }
    public String getFindQuery() {
        return findQuery;
    }
    @DataBoundSetter
    public void setFindQuery(String findQuery) {
        this.findQuery = findQuery;
    }
    public String getConfigLocation() {
        return configLocation;
    }
    @DataBoundSetter
    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    // --config-find filename:whatToFind:findQuery
    public String generateConfigArgument() {
        if (configLocation.equals("build")) {
            return "--build-config-find " + configFileName + ":" + whatToFind + ":" + findQuery;
        } else {
            return "--job-config-find " + configFileName + ":" + whatToFind + ":" + findQuery;
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ConfigItem> {
        public String getDisplayName() { return "Item in config file"; }
    }
}
