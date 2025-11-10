package io.jenkins.plugins.report.jtreg;

import io.jenkins.plugins.report.jtreg.items.LinkToComparator;
import io.jenkins.plugins.report.jtreg.model.SuitesWithResults;
import io.jenkins.plugins.report.jtreg.model.UrlsProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildReportExtendedPluginTest {

    @Test
    void getMatchedComparatorLinksGroupsTest() {
        BuildReportExtendedPlugin bre = new BuildReportExtendedPlugin(0, "", 0, 0, 0, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), 0, 0,
                new SuitesWithResults(new ArrayList<>()),
                "tck-jp17-ojdk17~rpms-el8.aarch64-fastdebug.sdk-el8.aarch64.beaker-x11.defaultgc.fips.lnxagent.jfroff",
                new UrlsProvider() {
                    @Override
                    public String getListServer() {
                        return "http://mylocal" + Constants.LIST_BACKEND;
                    }

                    @Override
                    public String getCompServer() {
                        return "http://mylocal" + Constants.COMPARATOR_BACKEND;
                    }

                    @Override
                    public String getDiffServer() {
                        return "http://mylocal" + Constants.DIFF_BACKEND;
                    }
                });

        // arguments without any wildcard
        LinkToComparator ltc = new LinkToComparator("", "comparator", "", "[.-]", "--arguments\n--without\n--any\n--wildcard");
        String output = bre.createComparatorLinkUrl(ltc, false);
        assertEquals("http://mylocal/comp.html?generated-part=&custom-part=--arguments+--without+--any+--wildcard+", output);

        // arguments with valid wildcards
        ltc = new LinkToComparator("", "comparator", "", "[.-]", "--something-%{1}-something\n--%{N-2}\n--%{SPLIT}");
        output = bre.createComparatorLinkUrl(ltc, false);
        assertEquals("http://mylocal/comp.html?generated-part=&custom-part=--something-tck-something+--lnxagent+--%5B.-%5D+", output);

        // arguments with invalid wildcards (out of range of the job name)
        ltc = new LinkToComparator("", "comparator", "", "[.-]", "--%{20}");
        output = bre.createComparatorLinkUrl(ltc, false);
        assertEquals("http://mylocal/comp.html?generated-part=&custom-part=The+number+given+is+out+of+range+of+the+job+name%21+", output);

        ltc = new LinkToComparator("", "comparator", "", "[.-]", "--%{-1}");
        output = bre.createComparatorLinkUrl(ltc, false);
        assertEquals("http://mylocal/comp.html?generated-part=&custom-part=The+number+given+is+out+of+range+of+the+job+name%21+", output);

        ltc = new LinkToComparator("", "comparator", "", "[.-]", "--%{N-20}");
        output = bre.createComparatorLinkUrl(ltc, false);
        assertEquals("http://mylocal/comp.html?generated-part=&custom-part=The+number+given+is+out+of+range+of+the+job+name%21+", output);

        ltc = new LinkToComparator("", "comparator", "", "[.-]", "--%{N}");
        output = bre.createComparatorLinkUrl(ltc, false);
        assertEquals("http://mylocal/comp.html?generated-part=&custom-part=The+number+given+is+out+of+range+of+the+job+name%21+", output);

        ltc = new LinkToComparator("", "comparator", "", "[.-]", "--%{N+5}");
        output = bre.createComparatorLinkUrl(ltc, false);
        assertEquals("http://mylocal/comp.html?generated-part=&custom-part=The+number+given+is+out+of+range+of+the+job+name%21+", output);
    }
}
