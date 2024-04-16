package io.jenkins.plugins.report.jtreg.formatters;

import org.apache.commons.collections.list.UnmodifiableList;

import java.util.Collections;
import java.util.List;


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
        //calssical trim is much more powerfull,a nd is removing also colouring bash sequences
        return sb.toString().replaceAll("\\s+$","").replaceAll("^\\s+","");
    }

    public String getCellContent() {
        StringBuilder sb = new StringBuilder();
        for(JtregPluginServicesLinkWithTooltip item: content){
            sb.append(item.getText()).append(" ");
        }
        return sb.toString().trim();
    }

    public String toString() {
        return renderCell();
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
        return contetntHashCode();
    }

    /**
     * Replace by hashCode once properly found
     * @return
     */
    public boolean contentEquals(Object obj) {
        return  getCellContent().equals(obj);
    }

    public boolean equals(Object obj) {
        return contentEquals(obj);
    }

    public boolean contentMatches(String s) {
        return getCellContent().matches(s);
    }
}
