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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hudson.model.Run;
import hudson.plugins.report.jck.model.Suite;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static hudson.plugins.report.jck.Constants.REPORT_JSON;

public class BuildSummaryParser {

    public JckReport parseReport(Run<?, ?> build) throws Exception {
        List<Suite> suites = parseBuildSummary(build);
        int total = 0;
        int failed = 0;
        int error = 0;

        for (Suite suite : suites) {
            total += suite.getReport().getTestsTotal();
            failed += suite.getReport().getTestsFailed();
            error += suite.getReport().getTestsError();
        }

        return new JckReport(Integer.toString(build.getNumber()), total, failed, error, suites);
    }

    private List<Suite> parseBuildSummary(Run<?, ?> build) throws Exception {
        File reportFile = new File(build.getRootDir(), REPORT_JSON);
        if (reportFile.exists() && reportFile.isFile() && reportFile.canRead()) {
            try (Reader in = new InputStreamReader(new BufferedInputStream(new FileInputStream(reportFile)), StandardCharsets.UTF_8)) {
                List<Suite> list = new Gson().fromJson(in, new TypeToken<List<Suite>>() {
                }.getType());
                return list;
            }
        }
        throw new IllegalStateException("Build does not contain JCK report summary");
    }

}
