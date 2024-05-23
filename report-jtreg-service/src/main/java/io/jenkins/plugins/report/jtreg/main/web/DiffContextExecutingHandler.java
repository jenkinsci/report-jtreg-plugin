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
package io.jenkins.plugins.report.jtreg.main.web;

import io.jenkins.plugins.report.jtreg.main.diff.DiffHelp;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class DiffContextExecutingHandler extends ContextExecutingHandler {

    public DiffContextExecutingHandler(File targetProgram) throws IOException {
        super(targetProgram);
    }

    @Override
    protected String loadDifTemplate() throws IOException {
        return loadTemplate("/io/jenkins/plugins/report/jtreg/main/web/diff.html");
    }

    @Override
    protected String pritnHelp() throws UnsupportedEncodingException {
        return sanitizeHtml(DiffHelp.HELP_MESSAGE) + "\n" +
                "note:\n" +
                "    --path path_to_the_jenkins_jobs_directory is already preset, no need to  set it\n" +
                "examples:\n" +
                "    --trace-from a-jenkins-job:210 --trace-to a-jenkins-job:211 --diff-format inline --formatting html\n" +
                "    --trace-from first-job:105 --trace-to second-job:166 --exact-tests .*compilation.* --cut-trace headEach:2500 --formatting html\n";
    }
}
