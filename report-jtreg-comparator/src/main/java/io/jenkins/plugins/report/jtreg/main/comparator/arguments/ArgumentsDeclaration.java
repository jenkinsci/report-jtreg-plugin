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
  public static final Argument compareTracesArg = new Argument("--compare-traces", "TODO", ""); // TODO add description and add to help
  public static final Argument setReferentialArg = new Argument("--set-referential", "TODO", "<jobName:buildNumber>"); // TODO add description and add to help
  public static final Argument cutTraceArg = new Argument("--cut-trace", "TODO", "<head/headEach/tail/tailEach:numberOfChars>"); // TODO add description and add to help
}
