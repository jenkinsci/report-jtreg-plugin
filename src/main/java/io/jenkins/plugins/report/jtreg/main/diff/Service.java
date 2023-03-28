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
package io.jenkins.plugins.report.jtreg.main.diff;

import com.sun.net.httpserver.HttpServer;
import io.jenkins.plugins.report.jtreg.main.diff.web.ContextExecutingHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Service {

    public static void main(String... args) throws IOException {
        String file = "/home/tester/vm-shared/TckScripts/jenkins/custom_run_wrappers/diff_jobs_hydra.sh";
        if (args.length>0) {
            file = args[0];
        }
        int port = 9090;
        if (args.length>1) {
            port = Integer.parseInt(args[1]);
        }
        HttpServer hs = HttpServer.create(new InetSocketAddress(port), 0);
        hs.createContext("/diff.html", new ContextExecutingHandler(
                new File(file)));
        hs.start();
        System.out.println("Diff server started. Running at " + port + ". Terminate to end. Run for: " + file);
        System.err.println("Diff server started. Running at " + port + ". Terminate to end. Run for: " + file);
    }

}
