package io.jenkins.plugins.report.jtreg.main.comparator.arguments;

import io.jenkins.plugins.report.jtreg.Constants;
import io.jenkins.plugins.report.jtreg.main.comparator.HelpMessage;
import io.jenkins.plugins.report.jtreg.main.comparator.Options;

public class ArgumentsParsing {
    // parses the given arguments and returns instance of Options
    public static Options parse(String[] arguments) {
        Options options = new Options();

        if (arguments.length >= 1) {
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
                    System.exit(0);

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

                } else if (currentArg.equals(ArgumentsDeclaration.pathArg.getName())) {
                    // --path
                    if (i + 1 <= arguments.length) {
                        options.setJobsPath(arguments[++i]);
                    } else {
                        throw new RuntimeException("Expected path to jobs after --path.");
                    }

                } else if (currentArg.equals(ArgumentsDeclaration.queryArg.getName())) {
                    // --query
                    if (i + 1 <= arguments.length) {
                        options.setQueryString(arguments[++i]);
                    } else {
                        throw new RuntimeException("Expected query string after --query.");
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
                    options.setForceVagueQuery(true);

                } else if (currentArg.equals(ArgumentsDeclaration.exactLengthArg.getName())) {
                    // --exact-length
                    if (i + 1 <= arguments.length) {
                        options.setExactJobLength(Integer.parseInt(arguments[++i]));
                    } else {
                        throw new RuntimeException("Expected the exact job length after --exact-length.");
                    }

                } else if (currentArg.equals(ArgumentsDeclaration.onlyVolatileArg.getName())) {
                    // --only-volatile
                    if (splitArg.length == 2 && splitArg[1].equals("true")) {
                        options.setOnlyVolatile(true);
                    }

                } else {
                    throw new RuntimeException("Unknown argument " + currentArg + ", run with --help for info.");
                }

            }
        } else {
            throw new RuntimeException("Expected some arguments.");
        }

        // check for basic errors
        if (options.getOperation() == null) {
            throw new RuntimeException("Expected some operation (--list, --enumerate, --compare or --print).");
        }
        if (options.getJobsPath() == null) {
            throw new RuntimeException("Expected path to jobs directory (--path).");
        }

        // check if the query string is too vague
        int numOfAsterisks = options.getQueryString().length() - options.getQueryString().replace("*", "").length();
        int lengthOfQuery = options.getQueryString().split("\\s+").length;
        if ((lengthOfQuery < Constants.VAGUE_QUERY_LENGTH_THRESHOLD ||
                (numOfAsterisks != 0 && (double)numOfAsterisks / (double)lengthOfQuery > Constants.VAGUE_QUERY_THRESHOLD))
                && !options.isForceVagueQuery()) {
            throw new RuntimeException("The query string is too vague (too many * or short query), run with --force to continue anyway.");
        }

        return options;
    }
}