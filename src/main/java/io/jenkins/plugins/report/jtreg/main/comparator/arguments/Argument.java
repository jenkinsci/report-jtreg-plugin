package io.jenkins.plugins.report.jtreg.main.comparator.arguments;

public class Argument {
    private final String argumentName; // the name of the argument (example: --path)
    private final String helpMessage; // the help for the argument that will be showed in help message
    private final String argumentFormat; // the "format/usage" of the argument that will be showed in the help message (example: <path/to/jobs> <number> etc.)

    public Argument(String argumentName, String helpMessage, String argumentFormat) {
        this.argumentName = argumentName;
        this.helpMessage = helpMessage;
        this.argumentFormat = argumentFormat;
    }

    public String getName() {
        return argumentName;
    }

    public String getHelp() {
        return helpMessage;
    }

    public String getUsage() {
        return argumentFormat;
    }
}
