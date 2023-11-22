package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.arguments.ArgumentsDeclaration;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByQuery;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByRegex;

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
            "    " + JobsByQuery.queryArg.getName() + JobsByQuery.queryArg.getUsage() + "\n" +
            "                  " + JobsByQuery.queryArg.getHelp() + "\n" +
            "            " + JobsByQuery.exactLengthArg.getName() + JobsByQuery.exactLengthArg.getUsage() + "\n" +
            "                          " + JobsByQuery.exactLengthArg.getHelp() + "\n" +
            "    " + JobsByRegex.regexArg.getName() + JobsByRegex.regexArg.getUsage() + "\n" +
            "                  " + JobsByRegex.regexArg.getHelp() + "\n" +
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
            "    " + ArgumentsDeclaration.buildConfigFindArg.getName() + ArgumentsDeclaration.buildConfigFindArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.buildConfigFindArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.jobConfigFindArg.getName() + ArgumentsDeclaration.jobConfigFindArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.jobConfigFindArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.skipFailedArg.getName() + ArgumentsDeclaration.skipFailedArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.skipFailedArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.nvrArg.getName() + ArgumentsDeclaration.nvrArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.nvrArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.historyArg.getName() + ArgumentsDeclaration.historyArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.historyArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.onlyVolatileArg.getName() + ArgumentsDeclaration.onlyVolatileArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.onlyVolatileArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.forceArg.getName() + "\n" +
            "                  " + ArgumentsDeclaration.forceArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.formattingArg.getName() + ArgumentsDeclaration.formattingArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.formattingArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.exactTestsArg.getName() + ArgumentsDeclaration.exactTestsArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.exactTestsArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.useDefaultBuildArg.getName() + ArgumentsDeclaration.useDefaultBuildArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.useDefaultBuildArg.getHelp() + "\n" +
            "\n" +
            "Query string syntax:\n" +
            JobsByQuery.queryStringUsage +
            "\n" +
            "NVR query syntax:\n" +
            "    The syntax of the NVR query is simple: it either takes an asterisk (*)\n" +
            "    all NVRs, or it takes a RegEx of a single NVR it should match, or\n" +
            "    multiple RegExes in a set ({}) with same rules as the query string set.\n";
}
