/*
 * The MIT License
 *
 * Copyright 2015-2023 report-jtreg plugin contributors
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
package io.jenkins.plugins.report.jtreg;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import java.util.Set;

public class ReportAction extends AbstractReportAction {

    public ReportAction(AbstractBuild<?, ?> build) {
        super(build);
    }

    @Override
    public String getUrlName() {
        return "java-reports";
    }

    @Override
    protected String getReportSuffix() {
        return "Reports";
    }

    @Override
    protected Action createProjectAction(Job<?, ?> job, Set<String> prefixes) {
        return new ReportProjectAction(job, prefixes);
    }

    @Override
    protected BuildReportExtended getRealTarget(PreviousBuilds reports) {
        return reports.getLastStableUnstableBuild();
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
