package io.jenkins.plugins.report.jtreg;

import io.jenkins.plugins.report.jtreg.model.Suite;
import io.jenkins.plugins.report.jtreg.model.SuiteTestChanges;
import io.jenkins.plugins.report.jtreg.model.SuitesWithResults;
import io.jenkins.plugins.report.jtreg.model.UrlsProvider;
import io.jenkins.plugins.report.jtreg.model.UrlsProviderPlugin;

import java.util.List;

public class BuildReportExtendedPluginFactory extends BuildReportExtendedFactory {

    private static final UrlsProvider urlsProvider = new UrlsProviderPlugin();
    private final String endpoint;

    public BuildReportExtendedPluginFactory(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public BuildReportExtended createBuildReportExtended(int buildNumber, String buildName, int passed, int failed,
                                                               int error, List<Suite> suites, List<String> addedSuites,
                                                               List<String> removedSuites, List<SuiteTestChanges> testChanges,
                                                               int total, int notRun, SuitesWithResults allTests, String job,
                                                               long timestamp, long duration,
                                                               int comparedAgainstBuildNumber, String comparedAgainstBuildName, long comparedAgainstStart, long comparedAgainstDuration) {
        return new BuildReportExtendedPlugin(buildNumber, buildName, passed, failed, error, suites, addedSuites, removedSuites,
                testChanges, total, notRun, allTests, job, timestamp, duration, comparedAgainstBuildNumber, comparedAgainstBuildName, comparedAgainstStart, comparedAgainstDuration, urlsProvider, endpoint);
    }
}
