package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.arguments.ComparatorArgDeclaration;
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
            "    " + ComparatorArgDeclaration.pathArg.getName() + ComparatorArgDeclaration.pathArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.pathArg.getHelp() + "\n" +
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
            "    " + ComparatorArgDeclaration.printArg.getName() + "\n" +
            "                  " + ComparatorArgDeclaration.printArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.enumerateArg.getName() + "\n" +
            "                  " + ComparatorArgDeclaration.enumerateArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.listArg.getName() + "\n" +
            "                  " + ComparatorArgDeclaration.listArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.compareArg.getName() + "\n" +
            "                  " + ComparatorArgDeclaration.compareArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.virtualArg.getName() + "\n" +
            "                  " + ComparatorArgDeclaration.virtualArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.compareTracesArg.getName() + "\n" +
            "                  " + ComparatorArgDeclaration.compareTracesArg.getHelp() + "\n" +
            "\n" +
            "    Other arguments:\n" +
            "    " + ComparatorArgDeclaration.helpArg.getName() + "\n" +
            "                  " + ComparatorArgDeclaration.helpArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.skipFailedArg.getName() + ComparatorArgDeclaration.skipFailedArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.skipFailedArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.historyArg.getName() + ComparatorArgDeclaration.historyArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.historyArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.onlyVolatileArg.getName() + ComparatorArgDeclaration.onlyVolatileArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.onlyVolatileArg.getHelp() + "\n" +"                  " + ComparatorArgDeclaration.historyArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.finalColumns.getName() + ComparatorArgDeclaration.finalColumns.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.finalColumns.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.forceArg.getName() + "\n" +
            "                  " + ComparatorArgDeclaration.forceArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.formattingArg.getName() + ComparatorArgDeclaration.formattingArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.formattingArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.exactTestsArg.getName() + ComparatorArgDeclaration.exactTestsArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.exactTestsArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.useDefaultBuildArg.getName() + ComparatorArgDeclaration.useDefaultBuildArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.useDefaultBuildArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.hidePassesArg.getName() + ComparatorArgDeclaration.hidePassesArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.hidePassesArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.setReferentialArg.getName() + ComparatorArgDeclaration.setReferentialArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.setReferentialArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.cutTraceArg.getName() + ComparatorArgDeclaration.cutTraceArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.cutTraceArg.getHelp() + "\n" +
            "\n" +
            "    Links arguments:\n" +
            "        Some html outputs may contain links\n" +
            "        If you want to point them to reasonable locations, set following stubs:\n" +
            "    " + ComparatorArgDeclaration.jenkinsUrlArg.getName() + ComparatorArgDeclaration.jenkinsUrlArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.jenkinsUrlArg.getHelp() + "\n" +
            "\n" +
            "    Dynamic arguments:\n" +
            "        Another type of arguments you can use are dynamic arguments. They are used\n" +
            "        for further filtering the jobs/builds to compare by any value in their config\n" +
            "        files.\n" +
            "        You must define the value to look for in a config file. The general syntax is:\n" +
            "          --X \"configFileName:whatAreYouLookingFor:queryToFindIt\"\n" +
            "            Instead of --X, you use one of these arguments:\n" +
            "    " + ComparatorArgDeclaration.buildConfigFindArg.getName() + ComparatorArgDeclaration.buildConfigFindArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.buildConfigFindArg.getHelp() + "\n" +
            "    " + ComparatorArgDeclaration.jobConfigFindArg.getName() + ComparatorArgDeclaration.jobConfigFindArg.getUsage() + "\n" +
            "                  " + ComparatorArgDeclaration.jobConfigFindArg.getHelp() + "\n" +
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
            "        " + ComparatorArgDeclaration.buildConfigFindArg.getName() + " " + "\"changelog.xml:nvr:/build/nvr\"\n" +
            "        Then you can use this argument: --nvr \"java-17-openjdk.*\" to filter only builds,\n" +
            "        that have changelog.xml file in the build directory, and the value on XPath /build/nvr\n" +
            "        matches java-17-openjdk.*.\n" +
            "    Additional info:\n" +
            "        When taking the build results, the tool defaults to using the results from build.xml\n" +
            "        file with /build/result XPath, but you can set it to different value with\n" +
            "        " + ComparatorArgDeclaration.buildConfigFindArg.getName() + "argument. (The whatAreYouLookingFor part of the syntax\n" +
            "        should be set to \"result\".)\n";
}
