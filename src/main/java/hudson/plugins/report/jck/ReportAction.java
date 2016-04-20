/*
 * The MIT License
 *
 * Copyright 2016 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.report.jck;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerProxy;

public class ReportAction implements Action, StaplerProxy, SimpleBuildStep.LastBuildAction {

    private final AbstractBuild<?, ?> build;
    private final Set<String> prefixes = new HashSet<>();

    public ReportAction(AbstractBuild<?, ?> build) {
        if (build == null) {
            throw new IllegalArgumentException("Build cannot be null");
        }
        this.build = build;
    }

    public void addPrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be empty");
        }
        prefixes.add(prefix);
    }

    @Override
    public String getIconFileName() {
        return "graph.png";
    }

    @Override
    public String getDisplayName() {
        return prefixes.stream()
                .sequential()
                .map(s -> s.toUpperCase())
                .collect(Collectors.joining(", ", "", " Reports"));
    }

    @Override
    public String getUrlName() {
        return "java-reports";
    }

    @Override
    public BuildReportExtended getTarget() {
        try {
            AbstractReportPublisher settings = getAbstractReportPublisher(build.getProject().getPublishersList());
            BuildReportExtended report = new BuildSummaryParser(prefixes, settings).parseBuildReportExtended(build);
            return report;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        Job<?, ?> job = build.getParent();
        return Collections.singleton(new ReportProjectAction(job, prefixes));
    }

    public static  AbstractReportPublisher getAbstractReportPublisher(DescribableList<Publisher, Descriptor<Publisher>> publishersList) {
        for (Publisher publisher : publishersList) {
            if (publisher instanceof  AbstractReportPublisher){
                return (AbstractReportPublisher)publisher;
            }
        }
        return null;
    }

}
