package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.Constants;

public class Arguments {
    // parses the given arguments and returns instance of Options
    public static Options parse(String[] arguments) {
        Options options = new Options();

        if (arguments.length >= 1) {
            for (int i = 0; i < arguments.length; i++) {
                String currentArgument = arguments[i];
                boolean hadHyphen = false; // for enforcing at least one hyphen

                // delete all leading - characters
                while (currentArgument.charAt(0) == '-') {
                    hadHyphen = true;
                    currentArgument = currentArgument.substring(1);
                }
                if (!hadHyphen) {
                    throw new RuntimeException("Unknown argument " + currentArgument + ", did you forget the leading hyphens to the argument (--)?");
                }

                switch (currentArgument.toLowerCase()) {
                    case "h":
                    case "help":
                        System.out.println("Test Variant Comparer Usage:");
                        System.exit(0);
                        break;
                    case "list":
                        if (options.getOperation() != null) {
                            throw new RuntimeException("Cannot combine --list with other operations.");
                        }
                        options.setOperation(Options.Operations.List);
                        break;
                    case "enumerate":
                        if (options.getOperation() != null) {
                            throw new RuntimeException("Cannot combine --enumerate with other operations.");
                        }
                        options.setOperation(Options.Operations.Enumerate);
                        break;
                    case "compare":
                        if (options.getOperation() != null) {
                            throw new RuntimeException("Cannot combine --compare with other operations.");
                        }
                        options.setOperation(Options.Operations.Compare);
                        break;
                    case "print":
                        if (options.getOperation() != null) {
                            throw new RuntimeException("Cannot combine --print with other operations.");
                        }
                        options.setOperation(Options.Operations.Print);
                        break;
                    case "path":
                        if (i + 1 <= arguments.length) {
                            options.setJobsPath(arguments[++i]);
                        } else {
                            throw new RuntimeException("Expected path to jobs after --path.");
                        }
                        break;
                    case "query":
                        if (i + 1 <= arguments.length) {
                            options.setQueryString(arguments[++i]);
                        } else {
                            throw new RuntimeException("Expected query string after --query.");
                        }
                        break;
                    case "nvr":
                        if (i + 1 <= arguments.length) {
                            options.setNvrQuery(arguments[++i]);
                        } else {
                            throw new RuntimeException("Expected NVR after --nvr.");
                        }
                        break;
                    case "history":
                        if (i + 1 <= arguments.length) {
                            options.setNumberOfBuilds(Integer.parseInt(arguments[++i]));
                        } else {
                            throw new RuntimeException("Expected number of builds after --history.");
                        }
                        break;
                    case "skip-failed=true":
                    case "skip-failed=false":
                        if (currentArgument.split("=")[1].equals("false")) {
                            options.setSkipFailed(false);
                        }
                        break;
                    case "force":
                        options.setForceVagueQuery(true);
                        break;
                    default:
                        throw new RuntimeException("Unknown argument " + currentArgument + ", run with --help for info.");
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
        if ((numOfAsterisks > Constants.VAGUE_QUERY_THRESHOLD  || options.getQueryString().equals(""))&& !options.isForceVagueQuery()) {
            throw new RuntimeException("The query string is too vague (too many *), run with --force to continue anyway.");
        }

        return options;
    }
}