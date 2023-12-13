package io.jenkins.plugins.report.jtreg.model;

import java.io.Serializable;

public interface UrlsProvider extends Serializable {

    String getDiffServer();
    String getCompServer();

}
