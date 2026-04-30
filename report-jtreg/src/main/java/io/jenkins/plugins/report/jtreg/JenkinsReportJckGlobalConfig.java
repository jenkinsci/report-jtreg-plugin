package io.jenkins.plugins.report.jtreg;


import io.jenkins.plugins.report.jtreg.items.ComparatorLinksGroup;
import io.jenkins.plugins.report.jtreg.items.ConfigItem;
import io.jenkins.plugins.report.jtreg.items.TestLink;
import io.jenkins.plugins.report.jtreg.writers.WriterKinds;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;

@Extension
public class JenkinsReportJckGlobalConfig extends GlobalConfiguration {
    private static Logger logger = Logger.getLogger(JenkinsReportJckGlobalConfig.class.getName());

    String toolsUrl;
    String additionalFilesToCopy;
    String targetFolders;
    String kinds;
    List<ComparatorLinksGroup> comparatorLinksGroups;
    List<ConfigItem> configItems;
    List<TestLink> testLinks;

    public static JenkinsReportJckGlobalConfig getInstance() {
        return GlobalConfiguration.all().get(JenkinsReportJckGlobalConfig.class);
    }

    public static String getGlobalDiffUrl() {
        return getInstance().getToolsUrl();
    }

    public static String getGlobalDiffUrlMissing() {
        return "Difftool url is not set";
    }

    public static boolean isGlobalDiffUrl() {
        return getInstance().isDiffToolUrlSet();
    }

    public boolean isDiffToolUrlSet() {
        return toolsUrl != null && !toolsUrl.trim().isEmpty();
    }

    public String getToolsUrl() {
        return toolsUrl;
    }

    @DataBoundSetter
    public void setToolsUrl(String toolsUrl) {
        this.toolsUrl = toolsUrl;
    }

    public String getAdditionalFilesToCopy() {
        return additionalFilesToCopy;
    }

    @DataBoundSetter
    public void setAdditionalFilesToCopy(String additionalFilesToCopy) {
        this.additionalFilesToCopy = additionalFilesToCopy;
    }

    public static String getGlobalAdditionalFilesToCopy() {
        return getInstance().getAdditionalFilesToCopy();
    }

    public String getTargetFolders() {
        return targetFolders;
    }

    @DataBoundSetter
    public void setTargetFolders(String targetFolders) {
        this.targetFolders = targetFolders;
    }

    public static String getGlobalTargetFolders() {
        return getInstance().getTargetFolders();
    }

    public String getKinds() {
        return kinds;
    }

    @DataBoundSetter
    public void setKinds(String kinds) {
        this.kinds = kinds;
    }

    public static String getGlobalKinds() {
        return getInstance().getKinds();
    }

    /**
     * Validates the kinds field to ensure it contains only valid WriterKinds values
     * and that no value contains spaces after trimming.
     * @param value the comma-separated list of kinds
     * @return FormValidation result
     */
    public FormValidation doCheckKinds(@QueryParameter String value) {
        if (value == null || value.trim().isEmpty()) {
            return FormValidation.ok();
        }

        String[] parts = value.split(",");
        Set<String> validKinds = Arrays.stream(WriterKinds.values())
                .filter(k -> k != WriterKinds.NONE)
                .map(Enum::name)
                .collect(Collectors.toSet());
        
        Set<String> invalidKinds = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        Set<String> seen = new HashSet<>();
        Set<String> withSpaces = new HashSet<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            
            // Check for spaces after trimming
            if (trimmed.contains(" ")) {
                withSpaces.add(trimmed);
            }
            
            String upperTrimmed = trimmed.toUpperCase();
            
            if (!validKinds.contains(upperTrimmed)) {
                invalidKinds.add(trimmed);
            }
            
            if (!seen.add(upperTrimmed)) {
                duplicates.add(upperTrimmed);
            }
        }

        if (!withSpaces.isEmpty()) {
            return FormValidation.error("Values must not contain spaces: " + String.join(", ", withSpaces));
        }

        if (!invalidKinds.isEmpty()) {
            return FormValidation.error("Invalid kinds: " + String.join(", ", invalidKinds) +
                    ". Valid values are: " + String.join(", ", validKinds));
        }

        if (!duplicates.isEmpty()) {
            return FormValidation.warning("Duplicate kinds found: " + String.join(", ", duplicates));
        }

        return FormValidation.ok();
    }

    /**
     * Validates the additionalFilesToCopy field to ensure no value contains spaces after trimming.
     * @param value the comma-separated list of file paths
     * @return FormValidation result
     */
    public FormValidation doCheckAdditionalFilesToCopy(@QueryParameter String value) {
        if (value == null || value.trim().isEmpty()) {
            return FormValidation.ok();
        }

        String[] parts = value.split(",");
        Set<String> withSpaces = new HashSet<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            
            // Check for spaces after trimming
            if (trimmed.contains(" ")) {
                withSpaces.add(trimmed);
            }
        }

        if (!withSpaces.isEmpty()) {
            return FormValidation.error("File paths must not contain spaces: " + String.join(", ", withSpaces));
        }

        return FormValidation.ok();
    }

    /**
     * Validates the targetFolders field to ensure:
     * - No value contains spaces after trimming
     * - If multiple folders are specified, they must be prefixed with nvr-db:, job-db:, or out-dir:
     * - Warns if any specified folder does not exist
     * @param value the comma-separated list of target folders
     * @return FormValidation result
     */
    public FormValidation doCheckTargetFolders(@QueryParameter String value) {
        if (value == null || value.trim().isEmpty()) {
            return FormValidation.ok();
        }

        String[] parts = value.split(",");
        Set<String> withSpaces = new HashSet<>();
        Set<String> nonExistentFolders = new HashSet<>();
        int folderCount = 0;
        boolean hasPrefixed = false;
        boolean hasUnprefixed = false;

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            
            folderCount++;
            
            // Check for spaces after trimming
            if (trimmed.contains(" ")) {
                withSpaces.add(trimmed);
                continue; // Skip further checks for this entry
            }
            
            // Check for prefix
            String folderPath = trimmed;
            if (trimmed.startsWith("nvr-db:") || trimmed.startsWith("job-db:") || trimmed.startsWith("out-dir:")) {
                hasPrefixed = true;
                folderPath = trimmed.substring(trimmed.indexOf(':') + 1);
            } else {
                hasUnprefixed = true;
            }
            
            // Check if folder exists
            File folder = new File(folderPath);
            if (!folder.exists()) {
                nonExistentFolders.add(folderPath);
            }
        }

        if (!withSpaces.isEmpty()) {
            return FormValidation.error("Folder paths must not contain spaces: " + String.join(", ", withSpaces));
        }

        if (folderCount > 1 && hasUnprefixed) {
            return FormValidation.error("When specifying multiple target folders, all must be prefixed with nvr-db:, job-db:, or out-dir:");
        }

        if (!nonExistentFolders.isEmpty()) {
            return FormValidation.warning("The following folders do not exist: " + String.join(", ", nonExistentFolders));
        }

        return FormValidation.ok();
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

    public static List<TestLink> getGlobalTestLinks() {
        return getInstance().getTestLinks();
    }

    public List<TestLink> getTestLinks() {
        return testLinks;
    }

    @DataBoundSetter
    public void setTestLinks(List<TestLink> testLinks) {
        this.testLinks = testLinks;
    }

    @DataBoundConstructor
    public JenkinsReportJckGlobalConfig(String toolsUrl, String additionalFilesToCopy, String targetFolders, String kinds, List<ComparatorLinksGroup> comparatorLinksGroups, List<ConfigItem> configItems, List<TestLink> testLinks) {
        this.toolsUrl = toolsUrl;
        this.additionalFilesToCopy = additionalFilesToCopy;
        this.targetFolders = targetFolders;
        this.kinds = kinds;
        this.comparatorLinksGroups = comparatorLinksGroups;
        this.configItems = configItems;
        this.testLinks = testLinks;
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
