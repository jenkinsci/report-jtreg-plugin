package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.arguments.ArgumentsDeclaration;

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
            "    " + ArgumentsDeclaration.pathArg.getName() + ArgumentsDeclaration.pathArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.pathArg.getHelp() + "\n" +
            "\n" +
            "    You can choose one of these to filter the jobs by their name:\n" +
            "    " + ArgumentsDeclaration.queryArg.getName() + ArgumentsDeclaration.queryArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.queryArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.regexArg.getName() + ArgumentsDeclaration.regexArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.regexArg.getHelp() + "\n" +
            "\n" +
            "    You need to choose ONE operation from these:\n" +
            "    " + ArgumentsDeclaration.printArg.getName() + "\n" +
            "                  " + ArgumentsDeclaration.printArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.enumerateArg.getName() + "\n" +
            "                  " + ArgumentsDeclaration.enumerateArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.listArg.getName() + "\n" +
            "                  " + ArgumentsDeclaration.listArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.compareArg.getName() + "\n" +
            "                  " + ArgumentsDeclaration.compareArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.virtualArg.getName() + "\n" +
            "                  " + ArgumentsDeclaration.virtualArg.getHelp() + "\n" +
            "\n" +
            "    Other arguments:\n" +
            "    " + ArgumentsDeclaration.helpArg.getName() + "\n" +
            "                  " + ArgumentsDeclaration.helpArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.skipFailedArg.getName() + ArgumentsDeclaration.skipFailedArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.skipFailedArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.nvrArg.getName() + ArgumentsDeclaration.nvrArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.nvrArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.historyArg.getName() + ArgumentsDeclaration.historyArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.historyArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.exactLengthArg.getName() + ArgumentsDeclaration.exactLengthArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.exactLengthArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.onlyVolatileArg.getName() + ArgumentsDeclaration.onlyVolatileArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.onlyVolatileArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.forceArg.getName() + "\n" +
            "                  " + ArgumentsDeclaration.forceArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.formattingArg.getName() + ArgumentsDeclaration.formattingArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.formattingArg.getHelp() + "\n" +
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
            "            \"" + ArgumentsDeclaration.forceArg.getName() + "\" switch.\n" +
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
