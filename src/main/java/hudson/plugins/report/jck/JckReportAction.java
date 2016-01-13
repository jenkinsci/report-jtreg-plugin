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

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Job;
import java.util.Collection;
import java.util.Collections;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerProxy;

public class JckReportAction implements Action, StaplerProxy, SimpleBuildStep.LastBuildAction {

    private final AbstractBuild<?, ?> build;

    public JckReportAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    @Override
    public String getIconFileName() {
        return "graph.png";
    }

    @Override
    public String getDisplayName() {
        return "JCK Report";
    }

    @Override
    public String getUrlName() {
        return "jck";
    }

    @Override
    public JckReport getTarget() {
        try {
            JckReport report = new BuildSummaryParser().parseReport(build);
            return report;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        Job<?, ?> job = build.getParent();
        if (/* getAction(Class) produces a StackOverflowError */!Util.filter(job.getActions(), JckReportProjectAction.class).isEmpty()) {
            // JENKINS-26077: someone like XUnitPublisher already added one
            return Collections.emptySet();
        }
        return Collections.singleton(new JckReportProjectAction(job));
    }

}
