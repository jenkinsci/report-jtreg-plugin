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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import io.jenkins.plugins.report.jtreg.main.diff.cmdline.Arguments;
import io.jenkins.plugins.report.jtreg.main.diff.cmdline.JobsRecognition;

/**
 *
 * This class is very simple files providing handler. If it is known file in
 * declared root, then it is returned. If it is directory, listing is returned,
 * otherwise 404.
 */
public abstract class ContextExecutingHandler implements HttpHandler {

    private final File targetProgram;
    private final String template;

    public ContextExecutingHandler(File targetProgram) throws IOException {
        this.targetProgram = targetProgram;
        this.template = loadDifTemplate();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        //moving result toseparate thread is increasing performance by 1000%
        RequestRunner rr = new RequestRunner(t);
        new Thread(rr).start();
    }

    protected abstract String loadDifTemplate() throws IOException;

    public static String loadTemplate(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ContextExecutingHandler.class.getResourceAsStream(path), "utf-8"))) {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    return sb.toString();
                }
                sb.append(line).append("\n");
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
            if (checkForBedChars(parsedParams, t)) {
                return;
            }
            t.sendResponseHeaders(200, 0);
            try (BufferedWriter wos = new BufferedWriter(new OutputStreamWriter(t.getResponseBody(), "utf-8"))) {
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

        private String processTemplate(final String template) throws UnsupportedEncodingException {
            String r = template;
            r = r.replaceAll("(?s)<!--help-->.*<!--helpEnd-->", pritnHelp());
            StringBuilder views = new StringBuilder();
            for (int i = 0; i < Arguments.knownViews.size(); i++) {
                String string = Arguments.knownViews.get(i);
                views.append("<option value=\"view").append(i + 1).append("\" ").append(setSelected(string, Arguments.bestViews)).append(">").append(string).append("</option>").append("\n");

            }
            r = multilineReplaceMark("views", r, views);

            views = new StringBuilder();
            int maxI=0;
            for (int i = 0; i < Arguments.knownOutputs.size(); i++) {
                maxI=i;
                String string = Arguments.knownOutputs.get(i);
                views.append("<option value=\"output").append(i + 1).append("\"  ").append(setSelected(string, Arguments.output_html)).append("  >").append(string).append("</option>").append("\n");

            }
            //special case, nothing for plaintext
            views.append("<option value=\"output").append(maxI + 2).append("\" >").append("").append("</option>").append("\n");
            r = multilineReplaceMark("outputs", r, views);

            views = new StringBuilder();
            for (String string : Arguments.knownBoolSwitches) {
                views.append("  <input onclick=\"generateComand();\" type=\"checkbox\" class=\"switch\" id=\"").append(string).append("\" value=\"").append(string).append("\">").append(string).append("</input><br/>\n");
            }
            r = multilineReplaceMark("swithces", r, views);

            views = new StringBuilder();
            String[] jobs = JobsRecognition.jobsRecognition().getPossibleJobs();
            for (int i = 0; i < jobs.length; i++) {
                String string = jobs[i];
                views.append("<option value=\"job").append(i + 1).append("\">").append(string).append("</option>").append("\n");

            }
            r = multilineReplaceMark("jobs", r, views);

            return r;
        }

        private String multilineReplaceMark(String mark, String orig, StringBuilder views) {
            String r = orig.replaceAll("(?s)<!--" + mark + "-->.*<!--" + mark + "End-->", views.toString());
            return r;
        }

        private String setSelected(String string, String wonted) {
            if (string.equals(wonted)) {
                return "selected";
            } else {
                return "";
            }
        }
        private String setSelected(String string, List<String> wonted) {
            if (wonted.contains(string)) {
                return "selected";
            } else {
                return "";
            }
        }

    }

    private static boolean isEven(int i) {
        return i % 2 == 0;
    }

    /**
     * this is caunting escapes only for quotes, so actually can be missleading a bit, but toward not allowing more, thus correct
     * I think this still can be fooled byescaping over several params
     * @param parsedParams
     * @param t
     * @return
     * @throws IOException
     */
    static boolean checkForBedChars(List<String> parsedParams, HttpExchange t) throws IOException {
        char q1 = '"';
        char q2 = '\'';
        char escapeChar = '\\';
        for (String param : parsedParams) {
            if (param.contains("eval") || param.contains("exec")) {
                if (t != null) {
                    sayBayBay(t);
                }
                return true;
            }
            int currentQuote = -1;
            int escapesCounter = 0;
            for (int i = 0; i < param.length(); i++) {
                char investigatedChar = param.charAt(i);
                if (investigatedChar == escapeChar) {
                    escapesCounter++;
                } else {
                    if (isEven(escapesCounter) && currentQuote == -1 && (investigatedChar == q1 || investigatedChar == q2)) {
                        currentQuote = investigatedChar;
                    } else {
                        if (investigatedChar == currentQuote && isEven(escapesCounter)) {
                            currentQuote = -1;
                        } else {
                            if (currentQuote == -1 && (investigatedChar == ';' || investigatedChar == '&' || investigatedChar == '|'
                                    || investigatedChar == '>' || investigatedChar == '<')) {
                                if (t != null) {
                                    sayBayBay(t);
                                }
                                return true;
                            }
                        }
                    }
                    escapesCounter = 0;
                }
            }
        }
        return false;
    }

    private static void sayBayBay(HttpExchange t) throws IOException {
        t.sendResponseHeaders(505, 0);
        t.getResponseBody().write("nice try, but if you do better, you will beat me".getBytes("ascii"));
        t.getResponseBody().flush();
        t.getResponseBody().close();
    }

    protected abstract String pritnHelp() throws UnsupportedEncodingException;

}
