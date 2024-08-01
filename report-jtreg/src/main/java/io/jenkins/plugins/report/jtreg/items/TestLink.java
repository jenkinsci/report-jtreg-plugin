package io.jenkins.plugins.report.jtreg.items;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class TestLink extends AbstractDescribableImpl<TestLink> {
    private String label;
    private String basePage;
    private String spliterator;
    private String arguments;

    @DataBoundConstructor
    public TestLink(String label, String spliterator, String arguments) {
        this.label = label;
        this.spliterator = spliterator;
        this.arguments = arguments;
    }

    public String getLabel() {
        return label;
    }

    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
    }

    public String getBasePage() {
        return basePage;
    }

    @DataBoundSetter
    public void setBasePage(String basePage) {
        this.basePage = basePage;
    }

    public String getSpliterator() {
        return spliterator;
    }

    @DataBoundSetter
    public void setSpliterator(String spliterator) {
        this.spliterator = spliterator;
    }

    public String getArguments() {
        return arguments;
    }

    @DataBoundSetter
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TestLink> {
        public String getDisplayName() { return "Link next to a failed test"; }
    }
}
