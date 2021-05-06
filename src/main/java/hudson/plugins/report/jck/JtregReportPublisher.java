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

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.plugins.report.jck.parsers.JtregReportParser;
import hudson.plugins.report.jck.parsers.ReportParser;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.DataBoundConstructor;

public class JtregReportPublisher extends AbstractReportPublisher {

    @DataBoundConstructor
    public JtregReportPublisher(String reportFileGlob) {
        super(reportFileGlob);
    }

    public static final String sfxs = "zip,tar,tar.gz,tar.bz2,tar.xz";
    
    public static boolean isJtregArchive(String s){
        String[] ss = sfxs.split(",");
        for (String s1 : ss) {
            if (s.endsWith(s1)){
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected String defaultReportFileGlob() {
        return "glob:*.{"+sfxs+"}";
    }

    @Override
    protected ReportParser createReportParser() {
        return new JtregReportParser();
    }

    @Override
    protected String prefix() {
        return "jtreg";
    }

    @Override
    public BuildStepDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public String getDisplayName() {
            return "JTreg Report";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }

}
