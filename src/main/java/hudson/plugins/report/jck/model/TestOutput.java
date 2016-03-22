package hudson.plugins.report.jck.model;

public class TestOutput implements Comparable<TestOutput> {

    private final String name;
    private final String value;

    public TestOutput(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

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
