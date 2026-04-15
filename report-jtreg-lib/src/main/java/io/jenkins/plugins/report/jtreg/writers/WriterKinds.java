package io.jenkins.plugins.report.jtreg.writers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum WriterKinds {
    PLAIN, JSON, PROPERTIES, NONE;

    public static final List<WriterKinds> ALL = Collections.unmodifiableList(Arrays.asList(WriterKinds.values()));

}
