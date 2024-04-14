package io.jenkins.plugins.report.jtreg.main.comparator.arguments;

public final class ArgumentsDeclaration {
  //public static final Argument exampleArg = new Argument("--argument-name", "The help message of this argument that will be shown with --help.", "<format/usage of argument showed with --help>")
  public static final Argument helpArg = new Argument("--help", "Print this help message.", "");
  public static final Argument listArg = new Argument("--list", "Print a table of matched jobs with their builds and the tests that failed in the build.", "");
  public static final Argument enumerateArg = new Argument("--enumerate", "Print lists of all variants of jobs (that match the rest of arguments).", "");
  public static final Argument compareArg = new Argument("--compare", "Print a table of all failed tests (of matched job builds) and the builds where they failed.", "");
  public static final Argument printArg = new Argument("--print", "Print all jobs and their builds that match the rest of arguments, without actually doing any operation on the builds or tests.", "");
  public static final Argument virtualArg = new Argument("--virtual", "Print a table of all matched jobs' builds and their result (e.g. SUCCESS, UNSTABLE, etc.). Can be used as standalone operation or combined with any other operation. Probably should be run with --skip-failed=false switch.", "");
  public static final Argument pathArg = new Argument("--path", "A system path to a directory with your jenkins jobs. This argument is mandatory", " <path/to/jenkins/jobs>");
  public static final Argument jenkinsUrlArg = new Argument("--jenkins-url", "An url pointing to yours jenkins instance to construct urls to", " <https://some.jenkins:1234>");
  public static final Argument historyArg = new Argument("--history", "To specify the maximum number of builds to look in.", " <number>");
  public static final Argument skipFailedArg = new Argument("--skip-failed", "Specify whether the comparator should skip failed tests (only take successful and unstable) or take all. The default value is true.", " <true/false>");
  public static final Argument forceArg = new Argument("--force", "Used for forcing vague requests, that could potentially take a long time.", "");
  public static final Argument onlyVolatileArg = new Argument("--only-volatile", "Specify true to show only non stable tests with the arguments list and compare (shows only tests, that are NOT failed everywhere). The default value is false.", " <true/false>");
  public static final Argument exactTestsArg = new Argument("--exact-tests", "Specify (with regex) the exact tests to show only. The rest of tests will be ignored.", " <regex>");
  public static final Argument formattingArg = new Argument("--formatting", "Specify the output formatting (plain, color or html). The default is plain.", " <plain/color/html>");
  public static final Argument useDefaultBuildArg = new Argument("--use-default-build", "If set to true and no matching build with given criteria was found, the tool will use the latest (default) build instead. Default value is false.", " <true/false>");
  public static final Argument buildConfigFindArg = new Argument("--build-config-find", "Argument used for declaring dynamic arguments. Looks for the specified config file inside the BUILD directory.", " configFileName:whatAreYouLookingFor:queryToFindIt");
  public static final Argument jobConfigFindArg = new Argument("--job-config-find", "Argument used for declaring dynamic arguments. Looks for the specified config file inside the JOB directory.", " configFileName:whatAreYouLookingFor:queryToFindIt");
  public static final Argument hidePassesArg = new Argument("--hide-passes", "If set to true, when printing the compare table, only builds with at least one failed test will be shown, passed tests will be hidden to make the table smaller. Set to false by default.", " <true/false>");
  public static final Argument compareTracesArg = new Argument("--compare-traces", "Print a table of all failed tests (of matched job builds) with the similarities (in percentages, calculated with Levenshtein distance) of the stack traces to the referential build's (by default the left-most build in each table row) stack trace.", "");
  public static final Argument setReferentialArg = new Argument("--set-referential", "Use with --compare-traces argument, if you want to set a different referential build than the left-most one in a row. Rows where referential build passed (the test is successful) will be skipped.", "<jobName:buildNumber>");
  public static final Argument cutTraceArg = new Argument("--cut-trace", "Since Levenshtein distance consumes a lot of memory, long stack traces need to be cut. There are 4 options to cut them with this argument: head - concat all outputs and cut N characters from start, headEach - if any test output is longer than N cut from the start from each, tail/tailEach - similar as head, but cut from the end. Default is tailEach and 5000 characters.", "<head/headEach/tail/tailEach:numberOfChars>");
}
