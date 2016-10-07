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
package hudson.plugins.report.jck.main.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * This class is very simple files providing handler. If it is known file in
 * declared root, then it is returned. If it is directory, listing is returned,
 * otherwise 404.
 */
public class ContextExecutingHandler implements HttpHandler {

    private final File jenkinsHome;
    private final File targetProgram;
    private final File jenkinsJobs;
    private final String template;

    public ContextExecutingHandler(File jenkinsHome, File targetProgram) throws IOException {
        this.jenkinsHome = jenkinsHome;
        this.jenkinsJobs = new File(jenkinsHome, "jobs");
        this.targetProgram = targetProgram;
        this.template = loadDifTempalte();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        //moving result toseparate thread is increasing performance by 1000%
        RequestRunner rr = new RequestRunner(t);
        new Thread(rr).start();
    }

    private String loadDifTempalte() throws IOException {
        return loadTemplate("/hudson/plugins/report/jck/main/web/diff.html");
    }

    private String loadTemplate(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(path), "utf-8"))) {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    return sb.toString();
                }
                sb.append(line + "\n");
            }
        }
    }

    private class RequestRunner implements Runnable {

        private final HttpExchange t;

        public RequestRunner(HttpExchange t) {
            this.t = t;
        }

        @Override
        public void run() {
            try {
                runImpl();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void runImpl() throws IOException {
            String requestedFile = t.getRequestURI().getPath();
            String params = t.getRequestURI().getQuery();
            List<String> parsedParams = new ArrayList<>();
            if (params != null) {
                params = params.replace("generated-part=", "");
                params = params.replace("custom-part=", "");
                String[] ps = params.split("&+");
                for (String p : ps) {
                    String pp = URLDecoder.decode(p, "utf-8").trim();
                    if (!pp.isEmpty()) {
                        parsedParams.add(pp);
                    }
                }
            }
            parsedParams.add(0, targetProgram.getAbsolutePath());
            t.sendResponseHeaders(200, 0);
            try (OutputStreamWriter wos = new OutputStreamWriter(t.getResponseBody(), "utf-8")) {
                wos.write(processTemplate(template));
                ProcessWrapper pw = new ProcessWrapper(wos, parsedParams.toArray(new String[parsedParams.size()]));
                pw.run();
                wos.write("            </div>\n"
                        + "            <pre id=\"err\" style=\"border:solid;\">\n"
                        + pw.getErrorResult()
                        + "            </pre>\n"
                        + "        </body>\n"
                        + "        </html>"
                );
                wos.flush();
            }

        }

        private String processTemplate(String template) {
            return template;
        }

    }

}
