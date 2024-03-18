package io.jenkins.plugins.report.jtreg.main.comparator.arguments;

import io.jenkins.plugins.report.jtreg.Constants;
import io.jenkins.plugins.report.jtreg.main.comparator.HelpMessage;
import io.jenkins.plugins.report.jtreg.main.comparator.Options;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.DefaultProvider;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByQuery;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByRegex;
import io.jenkins.plugins.report.jtreg.formatters.ColorFormatter;
import io.jenkins.plugins.report.jtreg.formatters.HtmlFormatter;

import java.util.HashMap;
import java.util.Map;

public class ArgumentsParsing {
    // parses the given arguments and returns instance of Options
    public static Options parse(String[] arguments) {
        Options options = new Options();

        Map<String, String> otherArgs = new HashMap<>(); // a list for unmatched (for now) arguments

        if (arguments.length == 0) {
            throw new RuntimeException("Expected some arguments.");
        }

        for (int i = 0; i < arguments.length; i++) {
            // delete all leading - characters
            String currentArg = arguments[i].replaceAll("^-+", "--");

            if (!currentArg.matches("^--.*")) {
                throw new RuntimeException("Unknown argument " + arguments[i] + ", did you forget the leading hyphens (--)?");
            }

            // parsing the arguments:
            if (currentArg.equals(ArgumentsDeclaration.helpArg.getName()) || currentArg.equals("--h")) {
                // --help
                System.out.print(HelpMessage.HELP_MESSAGE);
                Options tmpO = new Options();
                tmpO.setDie(true);
                return tmpO;
            } else if (currentArg.equals(ArgumentsDeclaration.listArg.getName())) {
                // --list
                if (options.getOperation() != null) {
                    throw new RuntimeException("Cannot combine --list with other operations.");
                }
                options.setOperation(Options.Operations.List);

            } else if (currentArg.equals(ArgumentsDeclaration.enumerateArg.getName())) {
                // --enumerate
                if (options.getOperation() != null) {
                    throw new RuntimeException("Cannot combine --enumerate with other operations.");
                }
                options.setOperation(Options.Operations.Enumerate);

            } else if (currentArg.equals(ArgumentsDeclaration.compareArg.getName())) {
                // --compare
                if (options.getOperation() != null) {
                    throw new RuntimeException("Cannot combine --compare with other operations.");
                }
                options.setOperation(Options.Operations.Compare);

            } else if (currentArg.equals(ArgumentsDeclaration.printArg.getName())) {
                // --print
                if (options.getOperation() != null) {
                    throw new RuntimeException("Cannot combine --print with other operations.");
                }
                options.setOperation(Options.Operations.Print);

            } else if (currentArg.equals(ArgumentsDeclaration.virtualArg.getName())) {
                // --virtual
                options.setPrintVirtual(true);

            } else if (currentArg.equals(ArgumentsDeclaration.pathArg.getName())) {
                // --path
                options.setJobsPath(getArgumentValue(arguments, i++));

            } else if (currentArg.equals(ArgumentsDeclaration.historyArg.getName())) {
                // --history
                options.setNumberOfBuilds(Integer.parseInt(getArgumentValue(arguments, i++)));

            } else if (currentArg.equals(ArgumentsDeclaration.skipFailedArg.getName())) {
                // --skip-failed
                if (!Boolean.parseBoolean(getArgumentValue(arguments, i++))) {
                    // if skip failed = false, any result value is taken, so .*
                    options.getConfiguration("result").setValue(".*");
                }

            } else if (currentArg.equals(ArgumentsDeclaration.forceArg.getName())) {
                // --force
                options.setForceVague(true);

            } else if (currentArg.equals(ArgumentsDeclaration.onlyVolatileArg.getName())) {
                // --only-volatile
                options.setOnlyVolatile(Boolean.parseBoolean(getArgumentValue(arguments, i++)));

            } else if (currentArg.equals(ArgumentsDeclaration.exactTestsArg.getName())) {
                // --exact-tests
                options.setExactTestsRegex(getArgumentValue(arguments, i++));

            } else if (currentArg.equals(ArgumentsDeclaration.formattingArg.getName())) {
                // --formatting
                String formatting = getArgumentValue(arguments, i++);
                if (formatting.equals("color") || formatting.equals("colour")) {
                    options.setFormatter(new ColorFormatter(System.out));
                } else if (formatting.equals("html")) {
                    options.setFormatter(new HtmlFormatter(System.out));
                } else if (!formatting.equals("plain")) {
                    throw new RuntimeException("Unexpected formatting specified.");
                }

            } else if (currentArg.equals(ArgumentsDeclaration.useDefaultBuildArg.getName())) {
                // --use-default-build
                options.setUseDefaultBuild(Boolean.parseBoolean(getArgumentValue(arguments, i++)));

            } else if (currentArg.equals(ArgumentsDeclaration.hidePassesArg.getName())) {
                // --hide-passes
                options.setHidePasses(Boolean.parseBoolean(getArgumentValue(arguments, i++)));

            } else if (currentArg.equals(ArgumentsDeclaration.buildConfigFindArg.getName()) ||
                    currentArg.equals(ArgumentsDeclaration.jobConfigFindArg.getName())) {
                // --X-config-find
                String[] values = getArgumentValue(arguments, i++).split(":");
                Options.Configuration configuration = createConfiguration(values, currentArg);
                // (whatToFind, configuration)
                if (options.getConfiguration(values[1]) == null) {
                    options.addConfiguration(values[1], configuration);
                } else {
                    throw new RuntimeException("The configuration for " + values[1] + " already exists.");
                }

            // parsing arguments of the jobs providers
            } else if (JobsByQuery.getSupportedArgsStatic().contains(currentArg) || JobsByRegex.getSupportedArgsStatic().contains(currentArg)) {
                // add a jobs provider to options, if there is none
                if (options.getJobsProvider() == null) {
                    if (JobsByQuery.getSupportedArgsStatic().contains(currentArg)) {
                        options.setJobsProvider(new JobsByQuery());
                    } else if (JobsByRegex.getSupportedArgsStatic().contains(currentArg)){
                        options.setJobsProvider(new JobsByRegex());
                    }
                }
                // check if the argument is compatible with current jobs provider
                if (options.getJobsProvider().getSupportedArgs().contains(currentArg)) {
                    options.getJobsProvider().parseArguments(currentArg, getArgumentValue(arguments, i++));

                } else {
                    throw new RuntimeException("Cannot combine arguments from multiple job providers.");
                }

            } else {
                if (!arguments[i + 1].matches("^--.*")) {
                    // these arguments can be dynamic arguments, that were not defined yet
                    // putting them into the map and checking later
                    otherArgs.put(currentArg, getArgumentValue(arguments, i++));
                } else {
                    // only arguments with some value can now be legal, so if no value was present, throw
                    throw new RuntimeException("Unknown argument " + currentArg + ", run with --help for info.");
                }
            }
        }

        for (Map.Entry<String, String> entry : otherArgs.entrySet()) {
            // check if the argument is one of the dynamic arguments
            String strippedArg = entry.getKey().replaceAll("-", "");

            Options.Configuration configuration = options.getConfiguration(strippedArg);
            if (configuration != null) {
                configuration.setValue(entry.getValue());
            } else {
                // unknown argument
                throw new RuntimeException("Unknown argument " + entry.getKey() + ", run with --help for info.");
            }
        }

        // check for basic errors
        if (options.getOperation() == null && !options.isPrintVirtual()) {
            throw new RuntimeException("Expected some operation (--list, --enumerate, --compare, --print or --virtual).");
        }
        if (options.getJobsPath() == null) {
            throw new RuntimeException("Expected path to jobs directory (--path).");
        }

        // if no provider was set, fallback to default provider
        if (options.getJobsProvider() == null) {
            options.setJobsProvider(new DefaultProvider());
        }

        // add the info about forcing vague queries to the current jobs provider
        if (options.isForceVague()) {
            options.getJobsProvider().parseArguments(ArgumentsDeclaration.forceArg.getName(), null);
        }

        return options;
    }

    private static Options.Configuration createConfiguration(String[] values, String currentArg) {
        Options.Configuration configuration;
        // checks the number of items it got from the splitting of the value
        if (values.length == 3) { // filename:whatToFind:findQuery
            if (currentArg.equals(ArgumentsDeclaration.buildConfigFindArg.getName())) {
                configuration = new Options.Configuration(values[0], values[2], Options.Locations.Build);
            } else {
                configuration = new Options.Configuration(values[0], values[2], Options.Locations.Job);
            }
        } else {
            throw new RuntimeException("Unknown number of parameters with the --config-find argument.");
        }
        return configuration;
    }

    private static String getArgumentValue(String[] arguments, int i) {
        if (i + 1 < arguments.length && !arguments[i + 1].matches("^--.*")) {
            return arguments[i + 1];
        } else {
            throw new RuntimeException("Expected some value after " + arguments[i] + " argument.");
        }
    }
}