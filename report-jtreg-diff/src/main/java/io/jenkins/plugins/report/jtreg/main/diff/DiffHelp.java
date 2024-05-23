package io.jenkins.plugins.report.jtreg.main.diff;

public final class DiffHelp {
    public static final String HELP_MESSAGE =
            "This is a basic tool for creating a diff between two stack traces in different formats.\n" +
                    "\n" +
                    "You can use these arguments:\n" +
                    DiffArgDeclaration.helpArg.getName() + "\n" +
                    "    " + DiffArgDeclaration.helpArg.getHelp() + "\n" +
                    DiffArgDeclaration.pathArg.getName() + DiffArgDeclaration.pathArg.getUsage() + "\n" +
                    "    " + DiffArgDeclaration.pathArg.getHelp() + "\n" +
                    DiffArgDeclaration.formattingArg.getName() + DiffArgDeclaration.formattingArg.getUsage() + "\n" +
                    "    " + DiffArgDeclaration.formattingArg.getHelp() + "\n" +
                    DiffArgDeclaration.diffFormatArg.getName() + DiffArgDeclaration.diffFormatArg.getUsage() + "\n" +
                    "    " + DiffArgDeclaration.diffFormatArg.getHelp() + "\n" +
                    DiffArgDeclaration.traceFromArg.getName() + DiffArgDeclaration.traceFromArg.getUsage() + "\n" +
                    "    " + DiffArgDeclaration.traceFromArg.getHelp() + "\n" +
                    DiffArgDeclaration.traceToArg.getName() + DiffArgDeclaration.traceToArg.getUsage() + "\n" +
                    "    " + DiffArgDeclaration.traceToArg.getHelp() + "\n" +
                    DiffArgDeclaration.exactTestsArg.getName() + DiffArgDeclaration.exactTestsArg.getUsage() + "\n" +
                    "    " + DiffArgDeclaration.exactTestsArg.getHelp() + "\n" +
                    DiffArgDeclaration.cutTraceArg.getName() + DiffArgDeclaration.cutTraceArg.getUsage() + "\n" +
                    "    " + DiffArgDeclaration.cutTraceArg.getHelp() + "\n";
}
