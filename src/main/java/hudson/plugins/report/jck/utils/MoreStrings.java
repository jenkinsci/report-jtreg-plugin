package hudson.plugins.report.jck.utils;

public class MoreStrings {

    private MoreStrings() {
    }

    public static int compareStrings(String s1, String s2) {
        if (s1 != null) {
            // if second is null - first one is bigger:
            if (s2 == null) {
                return 1;
            }
            // none of them is null, comparing:
            return s1.compareTo(s2);
        }
        // first and second are null - equal:
        if (s2 == null) {
            return 0;
        }
        // if we are here - first is null, second is not:
        return -1;
    }

}
