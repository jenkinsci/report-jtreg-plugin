package io.jenkins.plugins.report.jtreg.main.diff;

import io.jenkins.plugins.report.jtreg.arguments.Argument;
import io.jenkins.plugins.report.jtreg.arguments.CommonArgDeclaration;

public class DiffArgDeclaration extends CommonArgDeclaration {
  public static final Argument traceFromArg = new Argument("--trace-from", "It creates a diff between two stack traces and prints it. You need to specify the job name and build number. Needs to be combined with --trace-to for the second build and --exact-tests for the set of tests to create the diff on.", " jobName:buildNumber");
  public static final Argument traceToArg = new Argument("--trace-to", "Specify the second build to create the diff, see --trace-from.", " jobName:buildNumber");
  public static final Argument diffFormatArg = new Argument("--diff-format", "You can choose the format the diff of two stack traces will have. The supported formats are standard patch, inline comparison and side by side view. Not all are supported on all formatters though (html formatter supports all). The default is patch.", " <patch/inline/sidebyside>");
}
