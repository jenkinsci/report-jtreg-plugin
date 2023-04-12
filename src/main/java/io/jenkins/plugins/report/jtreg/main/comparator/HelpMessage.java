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
            "                  a system path to a directory with your jenkins jobs\n" +
            "    --query <querystring>\n" +
            "                  a query string to filter the jobs (The syntax is described\n" +
            "                  below.)\n" +
            "\n" +
            "    You need to choose ONE operation from these:\n" +
            "    --print       Print all jobs and their builds that match the rest of\n" +
            "                  arguments, without actually doing any operation on the builds\n" +
            "                  or tests.\n" +
            "    --enumerate   Print lists of all variants of jobs (that match the rest of\n" +
            "                  arguments).\n" +
            "    --list\n" +
            "    --compare\n";
}
