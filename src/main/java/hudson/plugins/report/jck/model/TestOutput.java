package hudson.plugins.report.jck.model;

import java.util.Objects;

public class TestOutput implements Comparable<TestOutput>, java.io.Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestOutput that = (TestOutput) o;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "TestOutput{" + "name='" + name + '\'' + ", value='" + value + '\'' + '}';
    }
}
