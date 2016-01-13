package hudson.plugins.report.jck.model;

import lombok.Value;

@Value
public class TestOutput implements Comparable<TestOutput> {

    String name;
    String value;

    @Override
    public int compareTo(TestOutput o) {
        if (this == o) {
            return 0;
        }
        if (o == null) {
            return -1;
        }
        return name.compareTo(o.getName());
    }

}
