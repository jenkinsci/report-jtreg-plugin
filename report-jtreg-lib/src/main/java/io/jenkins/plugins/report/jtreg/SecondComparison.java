package io.jenkins.plugins.report.jtreg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SecondComparison {

    public interface SecondComparisonUrlProvider {
        String getSecondComparisonUrl();
    }


    private static SecondComparison instance;
    private final SecondComparisonUrlProvider provider;
    private List<String> cache = new ArrayList<>();
    private Date cachedAt = new Date(0);
    private static final long CACHE_TIMEOUT = 60 * 60 * 1000;

    SecondComparison(SecondComparisonUrlProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null");
        }
        this.provider = provider;
    }

    public static synchronized SecondComparison getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SecondComparison has not yet been initialized");
        }
        return instance;
    }

    public static synchronized SecondComparison getOrCreateInstance(SecondComparisonUrlProvider provider) {
        //the provider returning null ro empty is ok, it is causing the instance to be practically disabled
        //it is also expected that the provider behavior can change in time
        if (instance == null) {
            instance = new SecondComparison(provider);
        }
        return instance;
    }

    public synchronized List<String> getList() {
        if (isReadingCurrentlyAllowed()) {
            if (shouldRefresh()) {
                cache = read();
                cachedAt = new Date();
                if (cache.isEmpty()) {
                    cache = null;
                }
                return cache;
            } else {
                return cache;
            }
        } else {
            return null;
        }
    }

    private boolean isReadingCurrentlyAllowed() {
        return provider.getSecondComparisonUrl() != null && !provider.getSecondComparisonUrl().isBlank();
    }

    private boolean shouldRefresh() {
        return new Date().getTime() > (cachedAt.getTime() + CACHE_TIMEOUT);
    }

    private  List<String> read() {
        return readImpl(provider.getSecondComparisonUrl());
    }

    private static List<String> readImpl(String surl) {
        try {
            URL url = new URL(surl);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                return reader.lines().filter(s -> !s.isBlank()).map(s->s.trim()).collect(Collectors.toList());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }




}
