/*
 * The MIT License
 *
 * Copyright 2016
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
package hudson.plugins.report.jck.main.cmdline;

import hudson.plugins.report.jck.main.formatters.ColorFormatter;
import hudson.plugins.report.jck.main.formatters.HtmlFormatter;
import hudson.plugins.report.jck.main.formatters.PlainFormatter;
import hudson.plugins.report.jck.main.formatters.BasicFormatter;
import hudson.plugins.report.jck.main.formatters.Formatter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class Options {

    private boolean skipFailed;
    private final List<File> dirsToWork;
    private boolean fill = false;
    private String output_type;
    private final List<String> views;
    private BasicFormatter formatter;
    private PrintStream stream;

    public Options() {
        this.views = new ArrayList<>();
        this.dirsToWork = new ArrayList<>();
    }

    public List<File> getDirsToWork() {
        return dirsToWork;
    }

    public boolean hideMisses() {
        return views.contains(Arguments.view_hide_misses);
    }

    public boolean hideTotals() {
        return views.contains(Arguments.view_hide_totals);
    }

    public boolean hideNegatives() {
        return views.contains(Arguments.view_hide_negatives);
    }

    public boolean hidePositives() {
        return views.contains(Arguments.view_hide_positives);
    }

    public boolean viewInfoSummary() {
        return views.isEmpty() || views.contains(Arguments.view_info_summary) || views.contains(Arguments.view_info);
    }

    public boolean viewInfoSummarySuites() {
        return views.isEmpty() || views.contains(Arguments.view_info_summary_suites) || views.contains(Arguments.view_info);
    }

    public boolean viewInfoProblems() {
        return views.isEmpty() || views.contains(Arguments.view_info_problems) || views.contains(Arguments.view_info);
    }

    public boolean viewDiffSummary() {
        return views.isEmpty() || views.contains(Arguments.view_diff_summary) || views.contains(Arguments.view_diff);
    }

    public boolean viewDiffSummarySuites() {
        return views.isEmpty() || views.contains(Arguments.view_diff_summary_suites) || views.contains(Arguments.view_diff);
    }

    public boolean viewDiffDetails() {
        return views.isEmpty() || views.contains(Arguments.view_diff_details) || views.contains(Arguments.view_diff);
    }

    public boolean viewDiffList() {
        return views.isEmpty() || views.contains(Arguments.view_diff_list) || views.contains(Arguments.view_diff);
    }

    void setOutputType(String output_type) {
        this.output_type = output_type;
    }

    void addView(String nextView) {
        views.add(nextView);
    }

    public boolean isInfo() {
        return (this.viewInfoProblems() || this.viewInfoSummary() || this.viewInfoSummarySuites() || views.contains(Arguments.view_info));
    }

    public boolean isDiff() {
        return this.viewDiffDetails() || this.viewDiffList() || this.viewDiffSummary() || this.viewDiffSummarySuites() || views.contains(Arguments.view_diff);
    }

    void setFill(boolean b) {
        fill = b;
    }

    public boolean isFill() {
        return fill;
    }

    void setSkipFailed(boolean b) {
        skipFailed = b;
    }

    public boolean isSkipFailed() {
        return skipFailed;
    }

    void add(File file) {
        if (skipFailed) {
            if (!file.exists()
                    || !new File(file, "log").exists()
                    || tail(new File(file, "log")).matches(".*(ABORTED|FAILURE).*")) {
                System.err.println("File " + file + " excluded -  skipping of failed builds allowed, and it seems to be fialed build");
            } else {
                dirsToWork.add(file);
                System.err.println("Added " + file + " !");
            }
        } else {
            dirsToWork.add(file);
            System.err.println("Added " + file + " !");
        }
    }

    //maybe linux only, not utf8 valid solution... nto much tested, just copypasted and worked
    private String tail(File file) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;

                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }

                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null) {
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Formatter getFormatter() {
        if (formatter == null) {
            if (output_type.equals(Arguments.output_color)) {
                formatter = new ColorFormatter(stream);
            } else if (output_type.equals(Arguments.output_html)) {
                formatter = new HtmlFormatter(stream);
            } else {
                formatter = new PlainFormatter(stream);
            }
        }
        return formatter;

    }
    
    public void setStream(PrintStream o){
        this.stream=o;
    }
    
    public PrintStream getStream(){
        return stream;
    }
}
