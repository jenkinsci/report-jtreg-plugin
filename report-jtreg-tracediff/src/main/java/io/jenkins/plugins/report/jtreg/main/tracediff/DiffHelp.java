package io.jenkins.plugins.report.jtreg.main.tracediff;

public final class DiffHelp {
    public static final String HELP_MESSAGE =
            "This is a basic tool for creating a diff between two stack traces in different formats.\n" +
                    "\n" +
                    "You can use these arguments:\n" +
                    TraceDiffArgDeclaration.helpArg.getName() + "\n" +
                    "    " + TraceDiffArgDeclaration.helpArg.getHelp() + "\n" +
                    TraceDiffArgDeclaration.pathArg.getName() + TraceDiffArgDeclaration.pathArg.getUsage() + "\n" +
                    "    " + TraceDiffArgDeclaration.pathArg.getHelp() + "\n" +
                    TraceDiffArgDeclaration.formattingArg.getName() + TraceDiffArgDeclaration.formattingArg.getUsage() + "\n" +
                    "    " + TraceDiffArgDeclaration.formattingArg.getHelp() + "\n" +
                    TraceDiffArgDeclaration.diffFormatArg.getName() + TraceDiffArgDeclaration.diffFormatArg.getUsage() + "\n" +
                    "    " + TraceDiffArgDeclaration.diffFormatArg.getHelp() + "\n" +
                    TraceDiffArgDeclaration.traceFromArg.getName() + TraceDiffArgDeclaration.traceFromArg.getUsage() + "\n" +
                    "    " + TraceDiffArgDeclaration.traceFromArg.getHelp() + "\n" +
                    TraceDiffArgDeclaration.traceToArg.getName() + TraceDiffArgDeclaration.traceToArg.getUsage() + "\n" +
                    "    " + TraceDiffArgDeclaration.traceToArg.getHelp() + "\n" +
                    TraceDiffArgDeclaration.exactTestsArg.getName() + TraceDiffArgDeclaration.exactTestsArg.getUsage() + "\n" +
                    "    " + TraceDiffArgDeclaration.exactTestsArg.getHelp() + "\n" +
                    TraceDiffArgDeclaration.cutTraceArg.getName() + TraceDiffArgDeclaration.cutTraceArg.getUsage() + "\n" +
                    "    " + TraceDiffArgDeclaration.cutTraceArg.getHelp() + "\n";
}
