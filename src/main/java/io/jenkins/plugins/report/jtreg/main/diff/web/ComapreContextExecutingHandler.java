/*
 * The MIT License
 *
 * Copyright 2015 user.
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
package io.jenkins.plugins.report.jtreg.main.diff.web;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ComapreContextExecutingHandler extends ContextExecutingHandler {

    public ComapreContextExecutingHandler(File targetProgram) throws IOException {
        super(targetProgram);
    }

    @Override
    protected String loadDifTemplate() throws IOException {
        return loadTemplate("/io/jenkins/plugins/report/jtreg/main/web/comp.html");
    }

    @Override
    protected String pritnHelp() throws UnsupportedEncodingException {
        return "note:\n"
                + "--path path_to_the_jenkins_jobs_directory is already preset, no need to  set it\n"
                + "examples:\n"
                + "--compare --query  \"tck jp19 ojdkLatest~rpms * * release sdk * aarch64 beaker x11 defaultgc ignorecp lnxagent *\" --history 5  --nvr \"java-latest-openjdk-19.0.2.0.7-1.rolling"
                + ".el8\"\n"
                + "--compare --query  \"tck jp19 ojdkLatest~rpms * * release sdk * aarch64 beaker x11 defaultgc ignorecp lnxagent *\" --history 1\n"
                + "When launched from here, web, the  `\" or * or { or } you have to use \\\" and \\* and \\{ and \\}` is NOT CORRECT\n"
                + "html wrapper not yet finished, you ust check SOURCE (ctrl+u) for anyviable thing\n";
    }
}
