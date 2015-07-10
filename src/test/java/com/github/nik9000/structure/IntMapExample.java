package com.github.nik9000.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class IntMapExample {
    public static List<IntMapExample> EXAMPLES;
    static {
        ImmutableList.Builder<IntMapExample> b = ImmutableList.builder();
        b.add(e("just value", "cat"));
        b.add(e("just list", ImmutableList.of("cat", "dog")));
        b.add(e("empty", ImmutableMap.of()));
        b.add(e("single entry", ImmutableMap.of(1, "cat")));
        b.add(e("two entries", ImmutableMap.of(1, "cat", 2, "dog")));
        b.add(e("empty list", ImmutableMap.of(1, ImmutableList.of())));
        b.add(e("single entry list", ImmutableMap.of(1, ImmutableList.of("cat"))));
        b.add(e("two entry list", ImmutableMap.of(1, ImmutableList.of("cat", "dog"))));
        b.add(e("two level", ImmutableMap.of(1, ImmutableMap.of(1, "cat"))));
        b.add(e("two level list", ImmutableMap.of(1, ImmutableMap.of(1, ImmutableList.of("cat")))));
        b.add(e("list of objects", ImmutableMap.of(1, //
                ImmutableList.of(ImmutableMap.of(1, "cat"), ImmutableMap.of(1, "dog")))));
        b.add(e("list of lists", ImmutableMap.of(1, //
                ImmutableList.of(ImmutableList.of("cat", "dog"), ImmutableList.of("bird")))));
        EXAMPLES = b.build();
    }

    /**
     * Just a convenient builder.
     */
    private static IntMapExample e(String description, Object map) {
        return new IntMapExample(description, map);
    }

    private final String description;
    private final Object testData;

    public IntMapExample(String description, Object testData) {
        this.description = description;
        this.testData = testData;
    }

    public String description() {
        return description;
    }

    public Object testData() {
        return testData;
    }

    @RunWith(Parameterized.class)
    public static class ParameterizedTest {
        /**
         * Turns EXAMPLES into a list compatible with
         * @RunWith(Parameters.class).
         */
        @Parameters(name = "{0}")
        public static Collection<Object[]> parameters() {
            List<Object[]> parameters = new ArrayList<>();
            for (IntMapExample e : EXAMPLES) {
                parameters.add(new Object[] { e.description(), e });
            }
            return parameters;
        }

        @Parameter(0)
        public String description;
        @Parameter(1)
        public IntMapExample example;

    }
}
