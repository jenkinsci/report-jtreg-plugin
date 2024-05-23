package io.jenkins.plugins.report.jtreg.main.diff;

import io.jenkins.plugins.report.jtreg.CommonOptions;
import io.jenkins.plugins.report.jtreg.arguments.CommonArgParser;
import io.jenkins.plugins.report.jtreg.formatters.BasicFormatter;

public class DiffArgParser extends CommonArgParser {
    public DiffArgParser(CommonOptions options, String[] arguments) {
        super(options, arguments);
    }

    public DiffInfo parseAndGetOptions() {
        parseCommonArguments();
        if (!options.isDie()) {
            parseTraceDiffArguments();
        }
        return (DiffInfo) options;
    }

    private void parseTraceDiffArguments() {
        DiffInfo localDiffInfo = (DiffInfo) options; // create cast local options

        for (int i = 0; i < arguments.length; i++) {
            // delete all leading - characters
            String currentArg = arguments[i].replaceAll("^-+", "--");

            if (!currentArg.matches("^--.*")) {
                throw new RuntimeException("Unknown argument " + arguments[i] + ", did you forget the leading hyphens (--)?");
            }

            // parsing the arguments:
            if (currentArg.equals(DiffArgDeclaration.traceFromArg.getName())) {
                // --trace-from
                String[] values = getArgumentValue(i++).split(":");
                if (values.length != 2) {
                    throw new RuntimeException("Wrong value format for " + DiffArgDeclaration.traceFromArg.getName() + " argument.");
                } else {
                    localDiffInfo.setBuildOne(values[0], values[1]);
                }

            } else if (currentArg.equals(DiffArgDeclaration.traceToArg.getName())) {
                // --trace-to
                String[] values = getArgumentValue(i++).split(":");
                if (values.length != 2) {
                    throw new RuntimeException("Wrong value format for " + DiffArgDeclaration.traceToArg.getName() + " argument.");
                } else {
                    localDiffInfo.setBuildTwo(values[0], values[1]);
                }

            } else if (currentArg.equals(DiffArgDeclaration.diffFormatArg.getName())) {
                // --diff-format
                String format = getArgumentValue(i++).toLowerCase();
                if (format.equals("inline")) {
                    localDiffInfo.setTypeOfDiff(BasicFormatter.TypeOfDiff.INLINE);
                } else if (format.equals("sidebyside")) {
                    localDiffInfo.setTypeOfDiff(BasicFormatter.TypeOfDiff.SIDEBYSIDE);
                } else if (!format.equals("patch")) {
                    throw new RuntimeException("Unexpected diff format specified, expected patch, inline or sidebyside.");
                }

            } else {
                // unknown argument
                throw new RuntimeException("Unknown argument " + currentArg + ", run with --help for info.");
            }
        }

        this.options = localDiffInfo;
    }
}
