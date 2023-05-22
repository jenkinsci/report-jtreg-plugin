package io.jenkins.plugins.report.jtreg.main.comparator.arguments;

public final class ArgumentsDeclaration {
  //public static final Argument exampleArg = new Argument("--argument-name", "The help message of this argument that will be shown with --help.", "<format/usage of argument showed with --help>")
    public static final Argument helpArg = new Argument("--help", "Print this help message.", "");
    public static final Argument listArg = new Argument("--list", "Print a table of matched jobs with their builds and the tests that failed in the build.", "");
    public static final Argument enumerateArg = new Argument("--enumerate", "Print lists of all variants of jobs (that match the rest of arguments).", "");
    public static final Argument compareArg = new Argument("--compare", "Print a table of all failed tests (of matched job builds) and the builds where they failed.", "");
    public static final Argument printArg = new Argument("--print", "Print all jobs and their builds that match the rest of arguments, without actually doing any operation on the builds or tests.", "");
    public static final Argument pathArg = new Argument("--path", "A system path to a directory with your jenkins jobs.", " <path/to/jenkins/jobs>");
    public static final Argument queryArg = new Argument("--query", "A query string to filter the jobs (the syntax is described below).", " <querystring>");
    public static final Argument nvrArg = new Argument("--nvr", "To specify what builds to take (only builds with specified NVRs). The syntax is described below.", " <nvrquery>");
    public static final Argument historyArg = new Argument("--history", "To specify the maximum number of builds to look in.", " <number>");
    public static final Argument skipFailedArg = new Argument("--skip-failed", "Specify whether the comparator should skip failed tests (only take successful and unstable) or take all. The default value is true.", "=<true/false>");
    public static final Argument forceArg = new Argument("--force", "Used for forcing vague requests, that could potentially take a long time.", "");
    public static final Argument exactLengthArg = new Argument("--exact-length", "Since job names can have different lengths and sometimes we only need to match jobs with exact name length (number of element in the job name), this specifies the exact length.", " <number>");
    public static final Argument onlyVolatileArg = new Argument("--only-volatile", "Specify true to show only non stable tests with the arguments list and compare (shows only tests, that are NOT failed everywhere). The default value is false.", "=<true/false>");
    public static final Argument exactTestsArg = new Argument("--exact-tests", "Specify (with regex) the exact tests to show only. The rest of tests will be ignored.", " <regex>");
   public static final Argument formattingArg = new Argument("--formatting", "Specify the output formatting. The default is plain", "=<plain>");
}
