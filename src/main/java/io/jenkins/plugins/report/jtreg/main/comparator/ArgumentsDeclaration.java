package io.jenkins.plugins.report.jtreg.main.comparator;

import java.util.Optional;

public final class ArgumentsDeclaration {
    public static final Argument helpArg = new Argument("--help", "Print this help message.", Optional.empty());
    public static final Argument listArg = new Argument("--list", "Print a table of matched jobs with their builds and the tests that failed in the build.", Optional.empty());
    public static final Argument enumerateArg = new Argument("--enumerate", "Print lists of all variants of jobs (that match the rest of arguments).", Optional.empty());
    public static final Argument compareArg = new Argument("--compare", "Print a table of all failed tests (of matched job builds) and the builds where they failed.", Optional.empty());
    public static final Argument printArg = new Argument("--print", "Print all jobs and their builds that match the rest of arguments, without actually doing any operation on the builds or tests.", Optional.empty());
    public static final Argument pathArg = new Argument("--path", "A system path to a directory with your jenkins jobs.", Optional.of(" <path/to/jenkins/jobs>"));
    public static final Argument queryArg = new Argument("--query", "A query string to filter the jobs (the syntax is described below).", Optional.of(" <querystring>"));
    public static final Argument nvrArg = new Argument("--nvr", "To specify what builds to take (only builds with specified NVRs). The syntax is described below.", Optional.of(" <nvrquery>"));
    public static final Argument historyArg = new Argument("--history", "To specify the maximum number of builds to look in.", Optional.of(" <number>"));
    public static final Argument skipFailedArg = new Argument("--skip-failed", "Specify whether the comparator should skip failed tests (only take successful and unstable) or take all. The default value is true.", Optional.of("=<true/false>"));
    public static final Argument forceArg = new Argument("--force", "Used for forcing vague requests, that could potentially take a long time.", Optional.empty());
    public static final Argument exactLengthArg = new Argument("--exact-length", "Since job names can have different lengths and sometimes we only need to match jobs with exact name length (number of element in the job name), this specifies the exact length.", Optional.of(" <number>"));
    public static final Argument onlyVolatileArg = new Argument("--only-volatile", "Specify true to show only non stable tests with the arguments list and compare (shows only tests, that are NOT failed everywhere). The default value is false.", Optional.of("=<true/false>"));
}
