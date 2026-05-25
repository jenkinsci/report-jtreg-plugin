package io.jenkins.plugins.report.jtreg;

import java.util.List;
import java.util.function.Predicate;

public class PreviousBuilds {

    public static final String EXACT = "-exact";

    public static Predicate<String> getAllPredicate() {
        return s -> true;
    }

    public static Predicate<String> createPredicate(List<String> displayNamesToFind) {
        return new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return displayNamesToFind.contains(s);
            }
        };
    }

    public static Predicate<String> createPredicate() {
        return createPredicate(SecondComparison.getInstance().getList());
    }

    private final BuildReportExtended lastStableUnstableBuild;
    private final BuildReportExtended lastNamedStableUnstableBuild;

    public PreviousBuilds(BuildReportExtended lastStableUnstableBuild, BuildReportExtended lastNamedStableUnstableBuild) {
        this.lastStableUnstableBuild = lastStableUnstableBuild;
        this.lastNamedStableUnstableBuild = lastNamedStableUnstableBuild;
    }

    public BuildReportExtended getLastNamedStableUnstableBuild() {
        return lastNamedStableUnstableBuild;
    }

    public BuildReportExtended getLastStableUnstableBuild() {
        return lastStableUnstableBuild;
    }

    public BuildReportExtended[] getBuilds() {
        return new BuildReportExtended[]{lastStableUnstableBuild, lastNamedStableUnstableBuild};
    }

    public String[] getResolutions() {
        return new String[]{"latest stable or unstable", "exact, specified (released, latest stable...) build"};
    }

    public String[] getSuffixes() {
        return new String[]{"", EXACT};
    }


}
