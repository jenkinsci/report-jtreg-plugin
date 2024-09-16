package io.jenkins.plugins.report.jtreg.main.comparator;

import io.jenkins.plugins.report.jtreg.main.comparator.arguments.ComparatorArgDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class StackTraceCompareTest {

    @Test()
    void removeSetReferentialNull() {
        //Assertions.assertEquals(, result);
        Exception thrown = Assertions.assertThrows(
                Exception.class,
                () -> StackTraceCompare.removeReferentialFromArgs(null),
                "Expected Exception with null input"
        );
    }

    @Test
    void setReferential() {

        List<String> result1 = StackTraceCompare.removeReferentialFromArgs(new ArrayList<>());
        Assertions.assertEquals(new ArrayList<>(), result1);

        List<String> result2 = StackTraceCompare.removeReferentialFromArgs(Arrays.asList("Buenos Aires", "Córdoba", "La Plata"));
        Assertions.assertEquals(Arrays.asList("Buenos Aires", "Córdoba", "La Plata"), result2);

        List<String> result5 = StackTraceCompare.removeReferentialFromArgs(Arrays.asList("str", ComparatorArgDeclaration.setReferentialArg.getName(), "value", "Buenos Aires", "Córdoba", "La Plata"));
        Assertions.assertEquals(Arrays.asList("str", "Buenos Aires", "Córdoba", "La Plata"), result5);

        List<String> result4 = StackTraceCompare.removeReferentialFromArgs(Arrays.asList(ComparatorArgDeclaration.setReferentialArg.getName(), "value", "Buenos Aires", "Córdoba", "La Plata"));
        Assertions.assertEquals(Arrays.asList("Buenos Aires", "Córdoba", "La Plata"), result4);

        List<String> result3 = StackTraceCompare.removeReferentialFromArgs(Arrays.asList("Buenos Aires", "Córdoba", "La Plata", ComparatorArgDeclaration.setReferentialArg.getName(), "value"));
        Assertions.assertEquals(Arrays.asList("Buenos Aires", "Córdoba", "La Plata"), result3);
    }

    @Test()
    void removeSetReferentialMissingParameter() {
        List<String> result6 = StackTraceCompare.removeReferentialFromArgs(Arrays.asList("str", ComparatorArgDeclaration.setReferentialArg.getName(), "Buenos Aires", "Córdoba", "La Plata", ComparatorArgDeclaration.setReferentialArg.getName(), "value", "str2"));
        Assertions.assertEquals(Arrays.asList("str", "Córdoba", "La Plata", "str2"), result6);

        List<String> result5 = StackTraceCompare.removeReferentialFromArgs(Arrays.asList("str", ComparatorArgDeclaration.setReferentialArg.getName(), "Buenos Aires", "Córdoba", "La Plata"));
        Assertions.assertEquals(Arrays.asList("str", "Córdoba", "La Plata"), result5);

        List<String> result4 = StackTraceCompare.removeReferentialFromArgs(Arrays.asList(ComparatorArgDeclaration.setReferentialArg.getName(), "Buenos Aires", "Córdoba", "La Plata"));
        Assertions.assertEquals(Arrays.asList("Córdoba", "La Plata"), result4);

        IndexOutOfBoundsException thrown = Assertions.assertThrows(
                IndexOutOfBoundsException.class,
                () -> StackTraceCompare.removeReferentialFromArgs(Arrays.asList("Buenos Aires", "Córdoba", "La Plata", ComparatorArgDeclaration.setReferentialArg.getName())),
                "Expected Exception with parameter"
        );
    }

    @Test()
    void testChangeReferentialUrl() {
        String result = StackTraceCompare.addSetReferentialSwitch("buildName", "1234", Arrays.asList("--param1", "value1", "--param2", "value2"));
        Assertions.assertEquals("--param1+value1+--param2+value2+--set-referential+buildName:1234", result);
    }

    @Test
    void deleteSetReferentialUrl() {
        String result = StackTraceCompare.addSetReferentialSwitch("buildName", "1234", Arrays.asList("--param1", "value1", "--param2", "value2", ComparatorArgDeclaration.setReferentialArg.getName(), "value"));
        Assertions.assertEquals("--param1+value1+--param2+value2+--set-referential+buildName:1234", result);
    }

}