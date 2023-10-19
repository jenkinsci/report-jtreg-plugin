package io.jenkins.plugins.report.jtreg;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.List;

public class ComparatorLinksGroup extends AbstractDescribableImpl<ComparatorLinksGroup> {
    private String jobMatchRegex;
    private List<LinkToComparator> links;

    @DataBoundConstructor
    public ComparatorLinksGroup(String jobMatchRegex, List<LinkToComparator> links) {
        this.jobMatchRegex = jobMatchRegex;
        this.links = links;
    }

    public String getJobMatchRegex() {
        return jobMatchRegex;
    }
    @DataBoundSetter
    public void setJobMatchRegex(String jobMatchRegex) {
        this.jobMatchRegex = jobMatchRegex;
    }

    public List<LinkToComparator> getLinks() {
        return links;
    }
    @DataBoundSetter
    public void setLinks(List<LinkToComparator> links) {
        this.links = links;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ComparatorLinksGroup> {
        public String getDisplayName() { return "Comparator links"; }
    }

}
