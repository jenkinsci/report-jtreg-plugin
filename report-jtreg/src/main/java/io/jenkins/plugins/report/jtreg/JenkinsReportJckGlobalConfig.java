package io.jenkins.plugins.report.jtreg;


import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;

@Extension
public class JenkinsReportJckGlobalConfig extends GlobalConfiguration {
    private static Logger logger = Logger.getLogger(JenkinsReportJckGlobalConfig.class.getName());

    String diffToolUrl;
    List<ComparatorLinksGroup> comparatorLinksGroups = new ArrayList<>();
    List<ConfigItem> configItems = new ArrayList<>();

    public static JenkinsReportJckGlobalConfig getInstance() {
        return GlobalConfiguration.all().get(JenkinsReportJckGlobalConfig.class);
    }

    public static String getGlobalDiffUrl() {
        return getInstance().getDiffToolUrl();
    }

    public static String getGlobalDiffUrlMissing() {
        return "Difftool url is not set";
    }

    public static boolean isGlobalDiffUrl() {
        return getInstance().isDiffToolUrlSet();
    }

    public boolean isDiffToolUrlSet() {
        return diffToolUrl != null && !diffToolUrl.trim().isEmpty();
    }

    public String getDiffToolUrl() {
        return diffToolUrl;
    }

    @DataBoundSetter
    public void setDiffToolUrl(String diffToolUrl) {
        this.diffToolUrl = diffToolUrl;
    }

    public static List<ComparatorLinksGroup> getGlobalComparatorLinksGroups() {
        return getInstance().getComparatorLinksGroups();
    }

    public List<ComparatorLinksGroup> getComparatorLinksGroups() {
        return comparatorLinksGroups;
    }

    @DataBoundSetter
    public void setComparatorLinksGroups(List<ComparatorLinksGroup> comparatorLinksGroups) {
        this.comparatorLinksGroups = comparatorLinksGroups;
    }

    public static List<ConfigItem> getGlobalConfigItems() {
        return getInstance().getConfigItems();
    }

    public List<ConfigItem> getConfigItems() {
        return configItems;
    }

    @DataBoundSetter
    public void setConfigItems(List<ConfigItem> configItems) {
        this.configItems = configItems;
    }

    @DataBoundConstructor
    public JenkinsReportJckGlobalConfig(String diffToolUrl, List<ComparatorLinksGroup> comparatorLinksGroups, List<ConfigItem> configItems) {
        this.diffToolUrl = diffToolUrl;
        this.comparatorLinksGroups = comparatorLinksGroups;
        this.configItems = configItems;
    }

    public JenkinsReportJckGlobalConfig() {
        load();
    }


    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return super.configure(req, json);
    }
}
