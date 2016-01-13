package hudson.plugins.report.jck.model;

import java.util.List;
import lombok.Value;

import static hudson.plugins.report.jck.utils.MoreStrings.compareStrings;

@Value
public class Test implements Comparable<Test> {

    String name;
    String status;
    String statusLine;
    List<TestOutput> outputs;

    @Override
    public int compareTo(Test o) {
        return compareStrings(name, o.getName());
    }

}
