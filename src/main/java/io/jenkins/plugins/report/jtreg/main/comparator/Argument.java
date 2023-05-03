package io.jenkins.plugins.report.jtreg.main.comparator;

import java.util.Optional;

public class Argument {
    private final String argumentName;
    private final String helpMessage;

    private final String argumentFormat;

    public Argument(String argumentName, String helpMessage, Optional<String> argumentFormat) {
        this.argumentName = argumentName;
        this.helpMessage = helpMessage;
        this.argumentFormat = argumentFormat.orElse("");
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
