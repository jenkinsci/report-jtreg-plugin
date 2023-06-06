package io.jenkins.plugins.report.jtreg.main.comparator.arguments;

import io.jenkins.plugins.report.jtreg.main.comparator.HelpMessage;
import io.jenkins.plugins.report.jtreg.main.comparator.Options;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByQuery;
import io.jenkins.plugins.report.jtreg.main.comparator.jobs.JobsByRegex;
import io.jenkins.plugins.report.jtreg.main.diff.formatters.ColorFormatter;
import io.jenkins.plugins.report.jtreg.main.diff.formatters.HtmlFormatter;

public class ArgumentsParsing {
    // parses the given arguments and returns instance of Options
    public static Options parse(String[] arguments) {
        Options options = new Options();

        if (arguments.length >= 1) {
            // setting up the available jobs providers
            JobsByQuery jobsByQuery = new JobsByQuery();
            JobsByRegex jobsByRegex = new JobsByRegex();

            for (int i = 0; i < arguments.length; i++) {
                String[] splitArg = arguments[i].split("=");
                // delete all leading - characters
                String currentArg = splitArg[0].replaceAll("^-+", "--");

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
                    if (i + 1 <= arguments.length) {
                        options.setJobsPath(arguments[++i]);
                    } else {
                        throw new RuntimeException("Expected path to jobs after --path.");
                    }

                } else if (currentArg.equals(ArgumentsDeclaration.nvrArg.getName())) {
                    // --nvr
                    if (i + 1 <= arguments.length) {
                        options.setNvrQuery(arguments[++i]);
                    } else {
                        throw new RuntimeException("Expected NVR after --nvr.");
                    }

                } else if (currentArg.equals(ArgumentsDeclaration.historyArg.getName())) {
                    // --history
                    if (i + 1 <= arguments.length) {
                        options.setNumberOfBuilds(Integer.parseInt(arguments[++i]));
                    } else {
                        throw new RuntimeException("Expected number of builds after --history.");
                    }

                } else if (currentArg.equals(ArgumentsDeclaration.skipFailedArg.getName())) {
                    // --skip-failed
                    if (splitArg.length == 2 && splitArg[1].equals("false")) {
                        options.setSkipFailed(false);
                    }

                } else if (currentArg.equals(ArgumentsDeclaration.forceArg.getName())) {
                    // --force
                    options.setForceVague(true);

                } else if (currentArg.equals(ArgumentsDeclaration.onlyVolatileArg.getName())) {
                    // --only-volatile
                    if (splitArg.length == 2 && splitArg[1].equals("true")) {
                        options.setOnlyVolatile(true);
                    }

                } else if (currentArg.equals(ArgumentsDeclaration.exactTestsArg.getName())) {
                    // --exact-tests
                    if (i + 1 <= arguments.length) {
                        options.setExactTestsRegex(arguments[++i]);
                    } else {
                        throw new RuntimeException("Expected the exact tests regex after --exact-tests.");
                    }

                } else if (currentArg.equals(ArgumentsDeclaration.formattingArg.getName())) {
                    // --formatting
                    if (splitArg.length == 2 && splitArg[1].equals("color")) {
                        options.setFormatter(new ColorFormatter(System.out));
                    } else if (splitArg.length == 2 && splitArg[1].equals("html")) {
                        options.setFormatter(new HtmlFormatter(System.out));
                    } else if (splitArg.length == 2 && !splitArg[1].equals("plain")) {
                        throw new RuntimeException("Unexpected formatting specified.");
                    }

                } else if (currentArg.equals(ArgumentsDeclaration.useDefaultBuildArg.getName())) {
                    // --use-default-build
                    if (splitArg.length == 2 && splitArg[1].equals("true")) {
                        options.setUseDefaultBuild(true);
                    }

                // parsing arguments of the jobs providers
                } else if (jobsByQuery.getSupportedArgs().contains(currentArg) || jobsByRegex.getSupportedArgs().contains(currentArg)) {
                    // add a jobs provider to options, if there is none
                    if (options.getJobsProvider() == null) {
                        if (jobsByQuery.getSupportedArgs().contains(currentArg)) {
                            options.setJobsProvider(jobsByQuery);
                        } else if (jobsByRegex.getSupportedArgs().contains(currentArg)){
                            options.setJobsProvider(jobsByRegex);
                        }
                    }
                    // check if the argument is compatible with current jobs provider
                    if (options.getJobsProvider().getSupportedArgs().contains(currentArg)) {
                        if (i + 1 <= arguments.length) {
                            options.getJobsProvider().parseArguments(currentArg, arguments[++i]);
                        } else {
                            throw new RuntimeException("Expected a value after " + currentArg + ".");
                        }
                    } else {
                        throw new RuntimeException("Cannot combine arguments from multiple job providers.");
                    }

                // unknown argument
                } else {
                    throw new RuntimeException("Unknown argument " + currentArg + ", run with --help for info.");
                }

            }
        } else {
            throw new RuntimeException("Expected some arguments.");
        }

        // check for basic errors
        if (options.getOperation() == null && !options.isPrintVirtual()) {
            throw new RuntimeException("Expected some operation (--list, --enumerate, --compare, --print or --virtual).");
        }
        if (options.getJobsPath() == null) {
            throw new RuntimeException("Expected path to jobs directory (--path).");
        }

        // add the info about forcing vague queries to the current jobs provider
        if (options.isForceVague()) {
            options.getJobsProvider().parseArguments(ArgumentsDeclaration.forceArg.getName(), null);
        }

        return options;
    }
}