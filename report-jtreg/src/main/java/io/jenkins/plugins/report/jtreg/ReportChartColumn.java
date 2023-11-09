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

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Project;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.jenkins.plugins.report.jtreg.model.BuildReport;
import io.jenkins.plugins.report.jtreg.model.BuildReportPlugin;
import org.kohsuke.stapler.DataBoundConstructor;

public class ReportChartColumn extends ListViewColumn {

    @DataBoundConstructor
    public ReportChartColumn() {
    }

    public List<BuildReportPlugin> getJckReport(Job<?, ?> job) {
        AbstractReportPublisher settings = ReportAction.getAbstractReportPublisher(((Project) job).getPublishersList());
        List<BuildReportPlugin> r = new BuildSummaryParserPlugin(Arrays.asList("jck", "jtreg"), settings).parseJobReports(job);
        try {
            ReportProjectAction.cacheSumms(job.getRootDir(), r);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return r;
    }

    public int getLatestResultFailures(final List<BuildReport> reports) {
        if (reports == null || reports.isEmpty()) {
            return 0;
        } else {
            BuildReport lastReport = reports.get(reports.size() - 1);
            return lastReport.getFailed() + lastReport.getError();
        }
    }

    public String generateChartName() {
        return "chart"+ UUID.randomUUID().toString().replace("-","");
    }

    @Extension
    public static final ReportChartColumnDescriptor DESCRIPTOR = new ReportChartColumnDescriptor();

    public static class ReportChartColumnDescriptor extends ListViewColumnDescriptor {

        @Override
        public boolean shownByDefault() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return "JCK and/or JTreg Results";
        }

    }

}
