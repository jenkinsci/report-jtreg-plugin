package io.jenkins.plugins.report.jtreg.arguments;

public class CommonArgDeclaration {
  //public static final Argument exampleArg = new Argument("--argument-name", "The help message of this argument that will be shown with --help.", "<format/usage of argument showed with --help>")
  public static final Argument helpArg = new Argument("--help", "Print this help message.", "");
  public static final Argument formattingArg = new Argument("--formatting", "Specify the output formatting (plain, color or html). The default is plain.", " <plain/color/html>");
  public static final Argument pathArg = new Argument("--path", "A system path to a directory with your jenkins jobs. This argument is mandatory", " <path/to/jenkins/jobs>");
  public static final Argument cutTraceArg = new Argument("--cut-trace", "For the operation to be quicker, long stack traces need to be cut. There are 4 options to cut them with this argument: head - concat all outputs and cut N characters from start, headEach - if any test output is longer than N cut from the start from each, tail/tailEach - similar as head, but cut from the end. Default is tailEach and 5000 characters.", " <head/headEach/tail/tailEach>:numberOfChars");
  public static final Argument exactTestsArg = new Argument("--exact-tests", "Specify (with regex) the exact tests to show only. The rest of tests will be ignored.", " <regex>");
  public static final Argument jenkinsUrlArg = new Argument("--jenkins-url", "An url pointing to yours jenkins instance to construct urls to", " <https://some.jenkins:1234>");
}
