package io.jenkins.plugins.report.jtreg.formatters;

import org.apache.commons.collections.list.UnmodifiableList;

import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * todo, replace dummy content by formatting + text?
 * todo replace by interface and have formatter based cell?
 *
 * todo, rmeove the  SuppressWarnings once all ewuals and hashes are identified and fixed
 */
public class JtregPluginServicesCell {

    private final List<JtregPluginServicesLinkWithTooltip> content;

    protected JtregPluginServicesCell(List<JtregPluginServicesLinkWithTooltip> content) {
        if (content == null) {
            throw new NullPointerException("content can not be null");
        }
        this.content = Collections.unmodifiableList(content);
    }

    public String renderCell() {
        StringBuilder sb = new StringBuilder();
        for(JtregPluginServicesLinkWithTooltip item: content){
            if (item == null) {
                sb.append("");
            } else {
                sb.append(item.render()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public String getCellContent() {
        StringBuilder sb = new StringBuilder();
        for(JtregPluginServicesLinkWithTooltip item: content){
            sb.append(item.getText()).append(" ");
        }
        return sb.toString().trim();
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
