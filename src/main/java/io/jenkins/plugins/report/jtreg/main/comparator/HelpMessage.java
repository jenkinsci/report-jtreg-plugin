package io.jenkins.plugins.report.jtreg.main.comparator;

final public class HelpMessage {
    public static final String HELP_MESSAGE =
           //80-characters-long-comment------------------------------------------------------
            "Test Variant Comparator Usage: java -cp <classpaths> <mainclass> [arguments...]\n" +
            "\n" +
            "Where mainclass is:\n" +
            "    io.jenkins.plugins.report.jtreg.main.comparator.VariantComparator\n" +
            "\n" +
            "And arguments include:\n" +
            "\n" +
            "    These two arguments are mandatory:\n" +
            "    --path <path/to/jenkins/jobs>\n" +
            "                  A system path to a directory with your jenkins jobs.\n" +
            "    --query <querystring>\n" +
            "                  A query string to filter the jobs (the syntax is described\n" +
            "                  below).\n" +
            "\n" +
            "    You need to choose ONE operation from these:\n" +
            "    --print       Print all jobs and their builds that match the rest of\n" +
            "                  arguments, without actually doing any operation on the builds\n" +
            "                  or tests.\n" +
            "    --enumerate   Print lists of all variants of jobs (that match the rest of\n" +
            "                  arguments).\n" +
            "    --list        Print a table of matched jobs with their builds and the tests\n" +
            "                  that failed in the build.\n" +
            "    --compare     Print a table of all failed tests (of matched job builds) and\n" +
            "                  the builds where they failed.\n" +
            "\n" +
            "    Other arguments:\n" +
            "    --help        Print this help message.\n" +
            "    --skip-failed=<true/false>\n" +
            "                  Specify whether the comparator should skip failed tests\n" +
            "                  (only take successful and unstable) or take all. The\n" +
            "                  default value is true.\n" +
            "    --nvr <nvrquery>\n" +
            "                  To specify what builds to take (only builds with specified\n" +
            "                  NVRs). The syntax is described below\n" +
            "    --history <number>\n" +
            "                  To specify the maximum number of builds to look in.\n";
}
