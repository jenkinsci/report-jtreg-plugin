package io.jenkins.plugins.report.jtreg.main.recreate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecreateArgs {

    private final List<String> aargs;

    public RecreateArgs(String[] args) {
        this.aargs = Collections.unmodifiableList((Arrays.asList(args)));
    }

    public boolean isJtreg() {
        return contains("jtreg");
    }

    public boolean isJck() {
        return contains("jck");
    }

    private boolean contains(String s) {
        if (aargs.contains(s)) {
            return true;
        }
        return false;
    }

    public String getUrl() {
        return get("-url");
    }

    private String get(String s) {
        for(int i = 0; i < aargs.size() - 1; i++) {
            String arg = aargs.get(i);
            if (arg.replaceAll("^-+","-").equals(s.replaceAll("^-+","-"))){
                return aargs.get(i+1);
            }
        }
        return null;
    }
}
