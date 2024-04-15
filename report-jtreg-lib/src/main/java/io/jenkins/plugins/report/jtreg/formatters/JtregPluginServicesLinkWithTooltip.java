package io.jenkins.plugins.report.jtreg.formatters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
<!DOCTYPE html>
<html>
<style>
.tooltip {
  position: relative;
  display: inline-block;
}

.tooltip .tooltiptext {
  visibility: hidden;
  width: 120px;
  background-color: grey;
  color: #fff;
  text-align: center;
  border-radius: 6px;
  padding: 5px 0;


  position: absolute;
          z-index: 1;
          top: 100%;
          left: 50%;
          margin-left: -60px;
          }

          .tooltip:hover .tooltiptext {
          visibility: visible;
          }
</style>
<body>


<div class="tooltip">
<a href="blah1">Hover over me1</a>
<span class="tooltiptext">
  <a href="blah2">Tooltip text2</a>
  Tooltip text3
  <a href="blah4">Tooltip text4</a>
</span>
</div>
<div class="tooltip">
Hover over me5
<span class="tooltiptext">
  Tooltip text3
  <a href="blah4">Tooltip text4</a>
  Tooltip text3
</span>
</div>
</body>
</html>
*/

public class JtregPluginServicesLinkWithTooltip {
    private final String text;
    private final String link;
    private final boolean tooltipLineBreaks;

    /**
     * https://www.w3schools.com/css/css_tooltip.asp
     *
     * styles must be present in the file
     */
    private final List<JtregPluginServicesLinkWithTooltip> tooltips;

    public JtregPluginServicesLinkWithTooltip(String text) {
        this(text, null, null);
    }

    public JtregPluginServicesLinkWithTooltip(String text, String link, List<JtregPluginServicesLinkWithTooltip> tooltips) {
        this(text, link, tooltips, false);
    }

    public JtregPluginServicesLinkWithTooltip(String text, String link, List<JtregPluginServicesLinkWithTooltip> tooltips,
            boolean tooltipLineBreaks) {
        if (text == null) {
            this.text = "";
        } else {
            this.text = text;
        }
        if (link == null) {
            this.link = "";
        } else {
            this.link = link;
        }
        if (tooltips == null) {
            this.tooltips = Collections.unmodifiableList(new ArrayList<>(0));
        } else {
            this.tooltips = Collections.unmodifiableList(tooltips);
        }
        this.tooltipLineBreaks = tooltipLineBreaks;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        if (!tooltips.isEmpty()) {
            sb.append("<div class=\"tooltip\">\n");
        }
        if (link.isEmpty()) {
            sb.append(text);
        } else {
            sb.append("<a href=\"" + link + "\">" + text + "</a>\n");
        }
        if (!tooltips.isEmpty()) {
            sb.append("<span class=\"tooltiptext\">\n");
            for (JtregPluginServicesLinkWithTooltip toolstip : tooltips) {
                sb.append(toolstip.render());
                if (tooltipLineBreaks) {
                    sb.append("<br/>\n");
                } else {
                    sb.append(" ");
                }
            }
            sb.append("<span>\n");
        }
        if (!tooltips.isEmpty()) {
            sb.append("<div>\n");
        }
        return sb.toString();
    }

    public String getText() {
        return text;
    }

    public JtregPluginServicesLinkWithTooltip toPlain() {
        return new JtregPluginServicesLinkWithTooltip(getText(), null, null, false);
    }

    public static List<JtregPluginServicesLinkWithTooltip> toPlainList(List<JtregPluginServicesLinkWithTooltip> rich) {
        return rich.stream().map(a->a.toPlain()).collect(Collectors.toList());

    }
}
