package io.jenkins.plugins.report.jtreg.main.comparator.arguments;

import io.jenkins.plugins.report.jtreg.arguments.CommonArgParser;
import io.jenkins.plugins.report.jtreg.main.comparator.Options;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.DefaultProvider;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByQuery;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByRegex;

import java.util.HashMap;
import java.util.Map;

public class ComparatorArgParser extends CommonArgParser {
    public ComparatorArgParser(Options options, String[] arguments) {
        super(options, arguments);
    }

    public Options parseAndGetOptions() {
        parseCommonArguments();
        if (!options.isDie()) {
            parseComparatorArguments();
        }
        return (Options) options;
    }

    private void parseComparatorArguments() {
        Options localOptions = (Options) options; // create cast local options

        Map<String, String> otherArgs = new HashMap<>(); // a list for unmatched (for now) arguments

        for (int i = 0; i < arguments.length; i++) {
            // delete all leading - characters
            String currentArg = arguments[i].replaceAll("^-+", "--");

            if (!currentArg.matches("^--.*")) {
                throw new RuntimeException("Unknown argument " + arguments[i] + ", did you forget the leading hyphens (--)?");
            }

            // parsing the arguments:
            if (currentArg.equals(ComparatorArgDeclaration.listArg.getName())) {
                // --list
                if (localOptions.getOperation() != null) {
                    throw new RuntimeException("Cannot combine --list with other operations.");
                }
                localOptions.setOperation(Options.Operations.List);

            } else if (currentArg.equals(ComparatorArgDeclaration.enumerateArg.getName())) {
                // --enumerate
                if (localOptions.getOperation() != null) {
                    throw new RuntimeException("Cannot combine --enumerate with other operations.");
                }
                localOptions.setOperation(Options.Operations.Enumerate);

            } else if (currentArg.equals(ComparatorArgDeclaration.compareArg.getName())) {
                // --compare
                if (localOptions.getOperation() != null) {
                    throw new RuntimeException("Cannot combine --compare with other operations.");
                }
                localOptions.setOperation(Options.Operations.Compare);

            } else if (currentArg.equals(ComparatorArgDeclaration.printArg.getName())) {
                // --print
                if (localOptions.getOperation() != null) {
                    throw new RuntimeException("Cannot combine --print with other operations.");
                }
                localOptions.setOperation(Options.Operations.Print);

            } else if (currentArg.equals(ComparatorArgDeclaration.compareTracesArg.getName())) {
                // --compare-traces
                if (localOptions.getOperation() != null) {
                    throw new RuntimeException("Cannot combine --compare-traces with other operations.");
                }
                localOptions.setOperation(Options.Operations.TraceCompare);

            } else if (currentArg.equals(ComparatorArgDeclaration.virtualArg.getName())) {
                // --virtual
                localOptions.setPrintVirtual(true);

            } else if (currentArg.equals(ComparatorArgDeclaration.historyArg.getName())) {
                // --history
                localOptions.setNumberOfBuilds(Integer.parseInt(getArgumentValue(i++)));

            } else if (currentArg.equals(ComparatorArgDeclaration.skipFailedArg.getName())) {
                // --skip-failed
                if (!Boolean.parseBoolean(getArgumentValue(i++))) {
                    // if skip failed = false, any result value is taken, so .*
                    localOptions.getConfiguration("result").setValue(".*");
                }

            } else if (currentArg.equals(ComparatorArgDeclaration.forceArg.getName())) {
                // --force
                localOptions.setForceVague(true);

            } else if (currentArg.equals(ComparatorArgDeclaration.onlyVolatileArg.getName())) {
                // --only-volatile
                localOptions.setOnlyVolatile(Boolean.parseBoolean(getArgumentValue(i++)));

            } else if (currentArg.equals(ComparatorArgDeclaration.useDefaultBuildArg.getName())) {
                // --use-default-build
                localOptions.setUseDefaultBuild(Boolean.parseBoolean(getArgumentValue(i++)));

            } else if (currentArg.equals(ComparatorArgDeclaration.hidePassesArg.getName())) {
                // --hide-passes
                localOptions.setHidePasses(Boolean.parseBoolean(getArgumentValue(i++)));

            } else if (currentArg.equals(ComparatorArgDeclaration.setReferentialArg.getName())) {
                // --set-referential
                String[] values = getArgumentValue(i++).split(":");
                if (values.length != 2) {
                    throw new RuntimeException("Wrong value format for " + ComparatorArgDeclaration.setReferentialArg.getName() + " argument.");
                } else {
                    localOptions.setReferentialJobName(values[0]);
                    localOptions.setReferentialBuildNumber(Integer.parseInt(values[1]));
                }

            } else if (currentArg.equals(ComparatorArgDeclaration.buildConfigFindArg.getName()) ||
                    currentArg.equals(ComparatorArgDeclaration.jobConfigFindArg.getName())) {
                // --X-config-find
                String[] values = getArgumentValue(i++).split(":");
                Options.Configuration configuration = createConfiguration(values, currentArg);
                // (whatToFind, configuration)
                if (localOptions.getConfiguration(values[1]) == null) {
                    localOptions.addConfiguration(values[1], configuration);
                } else {
                    throw new RuntimeException("The configuration for " + values[1] + " already exists.");
                }

            // parsing arguments of the jobs providers
            } else if (JobsByQuery.getSupportedArgsStatic().contains(currentArg) || JobsByRegex.getSupportedArgsStatic().contains(currentArg)) {
                // add a jobs provider to options, if there is none
                if (localOptions.getJobsProvider() == null) {
                    if (JobsByQuery.getSupportedArgsStatic().contains(currentArg)) {
                        localOptions.setJobsProvider(new JobsByQuery());
                    } else if (JobsByRegex.getSupportedArgsStatic().contains(currentArg)){
                        localOptions.setJobsProvider(new JobsByRegex());
                    }
                }
                // check if the argument is compatible with current jobs provider
                if (localOptions.getJobsProvider().getSupportedArgs().contains(currentArg)) {
                    localOptions.getJobsProvider().parseArguments(currentArg, getArgumentValue(i++));

                } else {
                    throw new RuntimeException("Cannot combine arguments from multiple job providers.");
                }

            } else {
                if (!arguments[i + 1].matches("^--.*")) {
                    // these arguments can be dynamic arguments, that were not defined yet
                    // putting them into the map and checking later
                    otherArgs.put(currentArg, getArgumentValue(i++));
                } else {
                    // only arguments with some value can now be legal, so if no value was present, throw
                    throw new RuntimeException("Unknown argument " + currentArg + ", run with --help for info.");
                }
            }
        }

        for (Map.Entry<String, String> entry : otherArgs.entrySet()) {
            // check if the argument is one of the dynamic arguments
            String strippedArg = entry.getKey().replaceAll("-", "");

            Options.Configuration configuration = localOptions.getConfiguration(strippedArg);
            if (configuration != null) {
                configuration.setValue(entry.getValue());
            } else {
                // unknown argument
                throw new RuntimeException("Unknown argument " + entry.getKey() + ", run with --help for info.");
            }
        }

        // check for basic errors
        if (localOptions.getOperation() == null && !localOptions.isPrintVirtual()) {
            throw new RuntimeException("Expected some operation (use --help for the set of available operations).");
        }
        if (localOptions.getJobsPath() == null) {
            throw new RuntimeException("Expected path to jobs directory (--path).");
        }

        // if no provider was set, fallback to default provider
        if (localOptions.getJobsProvider() == null) {
            localOptions.setJobsProvider(new DefaultProvider());
        }

        // add the info about forcing vague queries to the current jobs provider
        if (localOptions.isForceVague()) {
            localOptions.getJobsProvider().parseArguments(ComparatorArgDeclaration.forceArg.getName(), null);
        }

        this.options = localOptions;
    }

    private Options.Configuration createConfiguration(String[] values, String currentArg) {
        Options.Configuration configuration;
        // checks the number of items it got from the splitting of the value
        if (values.length == 3) { // filename:whatToFind:findQuery
            if (currentArg.equals(ComparatorArgDeclaration.buildConfigFindArg.getName())) {
                configuration = new Options.Configuration(values[0], values[2], Options.Locations.Build);
            } else {
                configuration = new Options.Configuration(values[0], values[2], Options.Locations.Job);
            }
        } else {
            throw new RuntimeException("Unknown number of parameters with the --config-find argument.");
        }
        return configuration;
    }
}