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

import com.google.gson.GsonBuilder;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.report.jck.model.Suite;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import static hudson.plugins.report.jck.Constants.REPORT_JSON;

public class JckReportPublisher extends Recorder {

    private String reportFileGlob;

    @DataBoundConstructor
    public JckReportPublisher(String reportFileGlob) {
        this.reportFileGlob = reportFileGlob;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        List<Suite> report = parseAndStoreSummary(build);
        if (report.stream().anyMatch(s -> s.getReport() != null && (s.getReport().getTestsError() != 0 || s.getReport().getTestsFailed() != 0))) {
            build.setResult(Result.UNSTABLE);
        }

        JckReportAction action = new JckReportAction(build);
        build.addAction(action);
        return true;
    }

    private List<Suite> parseAndStoreSummary(AbstractBuild<?, ?> build) throws IOException, InterruptedException {
        List<Suite> report = build.getWorkspace().act(new JckReportParserCallable(reportFileGlob));
        File jsonFile = new File(build.getRootDir(), REPORT_JSON);
        try (Writer out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(jsonFile)), StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(report, out);
        }
        return report;
    }

    @Override
    public BuildStepDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @DataBoundSetter
    public void setReportFileGlob(String reportFileGlob) {
        this.reportFileGlob = reportFileGlob;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public String getDisplayName() {
            return "JCK Report";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }

}
