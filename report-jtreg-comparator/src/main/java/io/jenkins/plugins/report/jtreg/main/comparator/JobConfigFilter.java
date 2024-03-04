package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.ConfigFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JobConfigFilter {
    private List<File> jobs;
    private final Map<String, Options.Configuration> configs;
    public JobConfigFilter(List<File> jobs, Map<String, Options.Configuration> configs) {
        this.jobs = jobs;
        this.configs = configs;
    }

    public void filterJobs() {
        List<File> filteredJobs = new ArrayList<>();
        for (File job : jobs) {
            boolean correct = true;
            for(Map.Entry<String, Options.Configuration> entry : configs.entrySet()) {
                if (entry.getValue().getLocation() != Options.Locations.Job) {
                    continue;
                }

                File config = new File(job, entry.getValue().getConfigFileName());
                String desiredValue = entry.getValue().getValue();
                String valueInConfig = new ConfigFinder(config, entry.getKey(), entry.getValue().getFindQuery()).findInConfig();

                if (valueInConfig == null) {
                    if (!desiredValue.equals(".*")) {
                        correct = false;
                    }
                } else if (desiredValue == null || desiredValue.isEmpty()) {
                    correct = true;
                } else if (desiredValue.charAt(0) == '{') {
                    // match multiple values
                    if (desiredValue.charAt(desiredValue.length() - 1) != '}') {
                        throw new RuntimeException("Expected closing } in the --" + entry.getKey() + "  value.");
                    }

                    String[] values = desiredValue.substring(1, desiredValue.length() - 1).split(",");
                    correct = Arrays.stream(values).anyMatch(valueInConfig::matches);
                } else {
                    correct = valueInConfig.matches(desiredValue);
                }

                if (!correct) {
                    break;
                }
            }
            // only add correct (matched) job
            if (correct) {
                filteredJobs.add(job);
            }
        }
        // replace jobs list with the filtered list
        jobs = filteredJobs;
    }

    public List<File> getJobs() {
        return jobs;
    }
}
