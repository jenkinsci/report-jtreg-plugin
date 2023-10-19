package io.jenkins.plugins.report.jtreg;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class LinkToComparator extends AbstractDescribableImpl<LinkToComparator> {
    private String label;
    private String spliterator;
    private String comparatorArguments;

    @DataBoundConstructor
    public LinkToComparator(String label, String spliterator, String comparatorArguments) {
        this.label = label;
        this.spliterator = spliterator;
        this.comparatorArguments = comparatorArguments;
    }

    public String getLabel() {
        return label;
    }

    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
    }

    public String getSpliterator() {
        return spliterator;
    }

    @DataBoundSetter
    public void setSpliterator(String spliterator) {
        this.spliterator = spliterator;
    }

    public String getComparatorArguments() {
        return comparatorArguments;
    }

    @DataBoundSetter
    public void setComparatorArguments(String comparatorArguments) {
        this.comparatorArguments = comparatorArguments;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<LinkToComparator> {
        public String getDisplayName() {
            return "Link to comparator tool";
        }
    }
}
