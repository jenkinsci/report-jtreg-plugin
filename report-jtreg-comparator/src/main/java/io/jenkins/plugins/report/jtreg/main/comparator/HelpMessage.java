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
            "    You can choose one of these arguments to filter the jobs by their name\n" +
            "    (but they are not mandatory to use):\n" +
            "    " + JobsByQuery.queryArg.getName() + JobsByQuery.queryArg.getUsage() + "\n" +
            "                  " + JobsByQuery.queryArg.getHelp() + "\n" +
            "    Query string syntax:\n" +
            JobsByQuery.queryStringUsage +
            "\n" +
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
            "    " + ArgumentsDeclaration.compareTracesArg.getName() + "\n" +
            "                  " + ArgumentsDeclaration.compareTracesArg.getHelp() + "\n" +
            "\n" +
            "    Other arguments:\n" +
            "    " + ArgumentsDeclaration.helpArg.getName() + "\n" +
            "                  " + ArgumentsDeclaration.helpArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.skipFailedArg.getName() + ArgumentsDeclaration.skipFailedArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.skipFailedArg.getHelp() + "\n" +
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
            "    " + ArgumentsDeclaration.hidePassesArg.getName() + ArgumentsDeclaration.hidePassesArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.hidePassesArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.setReferentialArg.getName() + ArgumentsDeclaration.setReferentialArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.setReferentialArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.cutTraceArg.getName() + ArgumentsDeclaration.cutTraceArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.cutTraceArg.getHelp() + "\n" +
            "\n" +
            "    Dynamic arguments:\n" +
            "        Another type of arguments you can use are dynamic arguments. They are used\n" +
            "        for further filtering the jobs/builds to compare by any value in their config\n" +
            "        files.\n" +
            "        You must define the value to look for in a config file. The general syntax is:\n" +
            "          --X \"configFileName:whatAreYouLookingFor:queryToFindIt\"\n" +
            "            Instead of --X, you use one of these arguments:\n" +
            "    " + ArgumentsDeclaration.buildConfigFindArg.getName() + ArgumentsDeclaration.buildConfigFindArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.buildConfigFindArg.getHelp() + "\n" +
            "    " + ArgumentsDeclaration.jobConfigFindArg.getName() + ArgumentsDeclaration.jobConfigFindArg.getUsage() + "\n" +
            "                  " + ArgumentsDeclaration.jobConfigFindArg.getHelp() + "\n" +
            "            configFileName is the name of the config file in the chosen working directory.\n" +
            "            whatAreYouLookingFor is the name of the config value you are looking for.\n" +
            "                You later use this name as an argument for the filtering itself.\n" +
            "            queryToFindIt is the query, to find the value in the config file. Currently,\n" +
            "                XPath is supported for XML files, Json Query for JSON files and plain value\n" +
            "                for properties files.\n" +
            "        Now, you can proceed to the filtering itself. For that, you use the whatAreYouLooking\n" +
            "        part from the definition as an argument and this syntax:\n" +
            "            It either takes RegEx to match the value with or multiple RegExes\n" +
            "            in curly brackets, separated by commas. (e.g. {nvr1.*,nvr2.*})\n" +
            "        Example:\n" +
            "        " + ArgumentsDeclaration.buildConfigFindArg.getName() + " " + "\"changelog.xml:nvr:/build/nvr\"\n" +
            "        Then you can use this argument: --nvr \"java-17-openjdk.*\" to filter only builds,\n" +
            "        that have changelog.xml file in the build directory, and the value on XPath /build/nvr\n" +
            "        matches java-17-openjdk.*.\n" +
            "    Additional info:\n" +
            "        When taking the build results, the tool defaults to using the results from build.xml\n" +
            "        file with /build/result XPath, but you can set it to different value with\n" +
            "        " + ArgumentsDeclaration.buildConfigFindArg.getName() + "argument. (The whatAreYouLookingFor part of the syntax\n" +
            "        should be set to \"result\".)\n";
}
