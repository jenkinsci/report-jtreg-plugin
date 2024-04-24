package io.jenkins.plugins.report.jtreg.arguments;

import io.jenkins.plugins.report.jtreg.CommonOptions;
import io.jenkins.plugins.report.jtreg.formatters.ColorFormatter;
import io.jenkins.plugins.report.jtreg.formatters.HtmlFormatter;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonArgParser {
    protected CommonOptions options;
    protected String[] arguments;

    public CommonArgParser(CommonOptions options, String[] arguments) {
        this.options = options;
        this.arguments = arguments;
    }

    public CommonOptions parseAndGetOptions() {
        parseCommonArguments();
        return options;
    }

    protected void parseCommonArguments() {
        if (arguments.length == 0) {
            throw new RuntimeException("Expected some arguments.");
        }

        List<String> unparsedArgs = new ArrayList<>();

        for (int i = 0; i < arguments.length; i++) {
            // delete all leading - characters
            String currentArg = arguments[i].replaceAll("^-+", "--");

            // parse the arguments
            if (currentArg.equals(CommonArgDeclaration.helpArg.getName()) || currentArg.equals("--h")) {
                // --help
                options.setDie(true);
                return;

            } else if (currentArg.equals(CommonArgDeclaration.formattingArg.getName())) {
                // --formatting
                String formatting = getArgumentValue(i++);
                if (formatting.equals("color") || formatting.equals("colour")) {
                    options.setFormatter(new ColorFormatter(System.out));
                } else if (formatting.equals("html")) {
                    options.setFormatter(new HtmlFormatter(System.out));
                } else if (!formatting.equals("plain")) {
                    throw new RuntimeException("Unexpected formatting specified.");
                }

            } else if (currentArg.equals(CommonArgDeclaration.pathArg.getName())) {
                // --path
                options.setJobsPath(getArgumentValue(i++));

            } else if (currentArg.equals(CommonArgDeclaration.cutTraceArg.getName())) {
                // --cut-trace
                String[] values = getArgumentValue(i++).split(":");
                if (values.length != 2) {
                    throw new RuntimeException("Wrong value format for " + CommonArgDeclaration.cutTraceArg.getName() + " argument.");
                } else {
                    if (values[0].equalsIgnoreCase("head")) {
                        options.setSubstringSide(CommonOptions.Side.Head);
                    } else if (values[0].equalsIgnoreCase("headeach")) {
                        options.setSubstringSide(CommonOptions.Side.HeadEach);
                    } else if (values[0].equalsIgnoreCase("tail")) {
                        options.setSubstringSide(CommonOptions.Side.Tail);
                    } else if (values[0].equalsIgnoreCase("taileach")) {
                        options.setSubstringSide(CommonOptions.Side.TailEach);
                    } else {
                        throw new RuntimeException("Wrong value for side of stack trace, only 'head', 'headEach', 'tail' or 'tailEach' are allowed.");
                    }
                    options.setSubstringLength(Integer.parseInt(values[1]));
                }

            } else if (currentArg.equals(CommonArgDeclaration.exactTestsArg.getName())) {
                // --exact-tests
                options.setExactTestsRegex(getArgumentValue(i++));

            } else if (currentArg.equals(CommonArgDeclaration.jenkinsUrlArg.getName())) {
                // --jenkins-url
                options.setJenkinsUrl(getArgumentValue(i++));
            } else {
                unparsedArgs.add(currentArg);
            }
        }

        this.arguments = unparsedArgs.toArray(new String[0]);
    }

    protected String getArgumentValue(int i) {
        if (i + 1 < arguments.length && !arguments[i + 1].matches("^--.*")) {
            return arguments[i + 1];
        } else {
            throw new RuntimeException("Expected some value after " + arguments[i] + " argument.");
        }
    }
}
