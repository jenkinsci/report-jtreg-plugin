/*
 * The MIT License
 *
 * Copyright 2016 jvanek.
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
package io.jenkins.plugins.report.jtreg.main;

import com.sun.net.httpserver.HttpServer;

import io.jenkins.plugins.report.jtreg.main.diff.web.ComapreContextExecutingHandler;
import io.jenkins.plugins.report.jtreg.main.diff.web.DiffContextExecutingHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Service {

    public static void main(String... args) throws IOException {
        String file1 = "/home/tester/vm-shared/TckScripts/jenkins/custom_run_wrappers/diff_jobs_hydra.sh";
        String file2 = "/home/tester/vm-shared/TckScripts/jenkins/custom_run_wrappers/compare_jobs_hydra.sh";
        int port = 9090;
        if (args.length == 0) {
            souter("Warning! Running with defaults. Not recommended.");
            help();
        } else if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else if (args.length == 2) {
            file1 = args[0];
            file2 = args[1];
        } else if (args.length == 3) {
            file1 = args[0];
            file2 = args[1];
            port = Integer.parseInt(args[2]);
        } else {
            help();
            throw new RuntimeException("0,1,2 or 3 args expected. Is " + args.length);
        }
        String b1 = "/diff.html";
        String b2 = "/comp.html";
        HttpServer hs = HttpServer.create(new InetSocketAddress(port), 0);
        hs.createContext(b1, new DiffContextExecutingHandler(
                new File(file1)));
        hs.createContext(b2, new ComapreContextExecutingHandler(
                new File(file2)));
        hs.start();
        souter(b1 + " server started. Running at " + port + ". Terminate to end. Run for: " + file1);
        souter(b2 + " server started. Running at " + port + ". Terminate to end. Run for: " + file2);

    }

    public static void help() {
        souter("no argument, defaults");
        souter("one argument, changes port of service");
        souter("two  argument, set scriptable backends");
        souter("three  argument,first two sets scriptable backends, third is port");
        souter("more, error");
    }

    public static void souter(String s) {
        System.out.println(s);
        System.err.println(s);
    }

}
