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

    @DataBoundConstructor
    public ConfigItem(String configFileName, String whatToFind, String findQuery) {
        this.configFileName = configFileName;
        this.whatToFind = whatToFind;
        this.findQuery = findQuery;
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
    public void setFindQuery(String prefix) {
        this.findQuery = prefix;
    }

    // --config-find filename:whatToFind:findQuery
    public String generateConfigArgument() {
        return "--config-find " + configFileName + ":" + whatToFind + ":" + findQuery;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ConfigItem> {
        public String getDisplayName() { return "Item in config file"; }
    }
}
