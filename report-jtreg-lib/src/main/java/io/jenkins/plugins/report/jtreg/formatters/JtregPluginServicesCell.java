package io.jenkins.plugins.report.jtreg.formatters;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * todo, replace dummy content by formatting + text?
 * todo replace by interface and have formatter based cell?
 *
 * todo, rmeove the  SuppressWarnings once all ewuals and hashes are identified and fixed
 */
public class JtregPluginServicesCell {

    private final String dummyContent;

    protected JtregPluginServicesCell(String dummyContent) {
        this.dummyContent = dummyContent;
    }

    public String renderCell() {
        return dummyContent;
    }

    public String getCellContent() {
        return dummyContent;
    }

    public String toString() {
        throw  new RuntimeException("remove usage in favour of renderCell() (or getCellContent)");
    }

    public int cellWidth() {
        return getCellContent().length();
    }



    /**
     * Replace by hashCode once properly found
     * @return
     */
    public int contetntHashCode() {
        return  getCellContent().hashCode();
    }
    public int hashCode() {
        throw  new RuntimeException("remove usage in favour of contetntHashCode()");
    }

    /**
     * Replace by hashCode once properly found
     * @return
     */
    public boolean contentEquals(Object obj) {
        return  getCellContent().equals(obj);
    }

    @SuppressFBWarnings({"EQ_UNUSUAL"})
    public boolean equals(Object obj) {
        throw  new RuntimeException("remove usage in favour of contentEquals()");
    }


    public boolean contentMatches(String s) {
        return getCellContent().matches(s);
    }
}
