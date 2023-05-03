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
            "    " + ArgumentsDeclared.pathArg.getName() + ArgumentsDeclared.pathArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclared.pathArg.getHelp() + "\n" +
            "    " + ArgumentsDeclared.queryArg.getName() + ArgumentsDeclared.queryArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclared.queryArg.getHelp() + "\n" +
            "\n" +
            "    You need to choose ONE operation from these:\n" +
            "    " + ArgumentsDeclared.printArg.getName() + "\n" +
            "                  " + ArgumentsDeclared.printArg.getHelp() + "\n" +
            "    " + ArgumentsDeclared.enumerateArg.getName() + "\n" +
            "                  " + ArgumentsDeclared.enumerateArg.getHelp() + "\n" +
            "    " + ArgumentsDeclared.listArg.getName() + "\n" +
            "                  " + ArgumentsDeclared.listArg.getHelp() + "\n" +
            "    " + ArgumentsDeclared.compareArg.getName() + "\n" +
            "                  " + ArgumentsDeclared.compareArg.getHelp() + "\n" +
            "\n" +
            "    Other arguments:\n" +
            "    " + ArgumentsDeclared.helpArg.getName() + "\n" +
            "                  " + ArgumentsDeclared.helpArg.getHelp() + "\n" +
            "    " + ArgumentsDeclared.skipFailedArg.getName() + ArgumentsDeclared.skipFailedArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclared.skipFailedArg.getHelp() + "\n" +
            "    " + ArgumentsDeclared.nvrArg.getName() + ArgumentsDeclared.nvrArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclared.nvrArg.getHelp() + "\n" +
            "    " + ArgumentsDeclared.historyArg.getName() + ArgumentsDeclared.historyArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclared.historyArg.getHelp() + "\n" +
            "    " + ArgumentsDeclared.exactLengthArg.getName() + ArgumentsDeclared.exactLengthArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclared.exactLengthArg.getHelp() + "\n" +
            "    " + ArgumentsDeclared.onlyVolatileArg.getName() + ArgumentsDeclared.onlyVolatileArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclared.onlyVolatileArg.getHelp() + "\n" +
            "\n" +
            "Query string syntax:\n" +
            "    The tool splits every job name by . or - characters and compares each\n" +
            "    split part with the query string. The query string consists of N parts\n" +
            "    separated by spaces (or other whitespace) and each of these parts\n" +
            "    corresponds with 1st..Nth part of the split job name.\n" +
            "    Example with explanation:\n" +
            "     \"jtreg~full jp17 * {f37,el8} !aarch64 !{fastdebug,slowdebug} * * *\"\n" +
            "        jtreg~full - specifies that the job's first part should be exactly\n" +
            "                     jtreg~full.\n" +
            "        jp17 - specifies that the job's second part should be exactly jp17.\n" +
            "        * - asterisk is a powerful wildcard that matches everything, so in\n" +
            "            this example, the job's parts on the 3rd, 7th, 8th and 9th don't\n" +
            "            matter = the tool takes everything on these positions.\n" +
            "            To stop the tool from draining system resources by looking at\n" +
            "            all jobs, if more than half of the query elements are asterisks,\n" +
            "            or the query has less than 4 parts, you must combine it with the\n" +
            "            \"" + ArgumentsDeclared.forceArg.getName() + "\" switch.\n" +
            "        {f37,el8} - this is a set of possible matches, so the jobs's part on\n" +
            "                    4th position can be either f37 or el8. There can me as\n" +
            "                    many elements as you want, but they must be split by\n" +
            "                    commas with no spaces between them.\n" +
            "        !aarch64 - matches everything, BUT aarch64.\n" +
            "        !{fastdebug,slowdebug} - matches everything, but the elements in\n" +
            "                                 the set.\n" +
            "\n" +
            "NVR query syntax:\n" +
            "    The syntax of the NVR query is simple: it either takes an asterisk (*)\n" +
            "    all NVRs, or it takes a RegEx of a single NVR it should match, or\n" +
            "    multiple RegExes in a set ({}) with same rules as the query string set.\n";
}
