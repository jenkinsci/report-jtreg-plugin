package io.jenkins.plugins.report.jtreg.main.comparator;

public class Arguments {
    // parses the given arguments and returns instance of Options
    public static Options parse(String[] arguments) {
        Options options = new Options();

        if (arguments.length >= 1) {
            for (int i = 0; i < arguments.length; i++) {
                switch (arguments[i].toLowerCase()) {
                    case "-h":
                    case "--help":
                        System.out.println("Test Variant Comparer Usage:");
                        System.exit(0);
                        break;
                    case "-l":
                    case "--list":
                        if (options.getOperation() != null) {
                            throw new RuntimeException("Cannot combine --list with other operations.");
                        }
                        options.setOperation(Options.Operations.List);
                        break;
                    case "-e":
                    case "--enumerate":
                        if (options.getOperation() != null) {
                            throw new RuntimeException("Cannot combine --enumerate with other operations.");
                        }
                        options.setOperation(Options.Operations.Enumerate);
                        break;
                    case "-c":
                    case "--compare":
                        if (options.getOperation() != null) {
                            throw new RuntimeException("Cannot combine --compare with other operations.");
                        }
                        options.setOperation(Options.Operations.Compare);
                        break;
                    case "-p":
                    case "--path":
                        if (i + 1 <= arguments.length) {
                            options.setJobsPath(arguments[++i]);
                        } else {
                            throw new RuntimeException("Expected path to jobs after -p.");
                        }
                        break;
                    case "-q":
                    case "--query":
                        if (i + 1 <= arguments.length) {
                            options.setQueryString(arguments[++i]);
                        } else {
                            throw new RuntimeException("Expected query string after -q.");
                        }
                        break;
                    case "-n":
                    case "--nvr":
                        if (i + 1 <= arguments.length) {
                            options.setNvrQuery(arguments[++i]);
                        } else {
                            throw new RuntimeException("Expected NVR after -n.");
                        }
                        break;
                    case "--test":
                        if (i + 1 <= arguments.length) {
                            options.setTest(arguments[++i]);
                        } else {
                            throw new RuntimeException("Expected test name after --test.");
                        }
                        break;
                    case "--history":
                        if (i + 1 <= arguments.length) {
                            options.setNumberOfBuilds(Integer.parseInt(arguments[++i]));
                        } else {
                            throw new RuntimeException("Expected number of builds after --history.");
                        }
                        break;
                    case "--show-nvrs=false":
                    case "--show-nvrs=true":
                        if (arguments[i].split("=")[1].equals("true")) {
                            options.setShowNvrs(true);
                        }
                        break;
                    case "--skip-failed=true":
                    case "--skip-failed=false":
                        if (arguments[i].split("=")[1].equals("false")) {
                            options.setSkipFailed(false);
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown argument " + arguments[i] + ", run with --help for info.");
                }
            }
        } else {
            throw new RuntimeException("Expected some arguments.");
        }

        // check for basic errors
        if (options.getOperation() == null) {
            throw new RuntimeException("Expected some operation (--list, --enumerate or --compare).");
        }
        if (options.getJobsPath() == null) {
            throw new RuntimeException("Expected path to jobs directory (-p).");
        }

        return options;
    }
}