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

public class StringMapExample {
    /**
     * Maximum depth required to round trip all examples.
     */
    public static int MAX_DEPTH = 20;

    public static List<StringMapExample> EXAMPLES;
    static {
        ImmutableList.Builder<StringMapExample> b = ImmutableList.builder();
        b.add(e("just value", "cat"));
        b.add(e("just list", ImmutableList.of("cat", "dog")));
        b.add(e("empty", ImmutableMap.of()));
        b.add(e("single entry", ImmutableMap.of("animal", "cat")));
        b.add(e("two entries", ImmutableMap.of("first", "cat", "second", "dog")));
        b.add(e("empty list", ImmutableMap.of("empty", ImmutableList.of())));
        b.add(e("single entry list", ImmutableMap.of("animal", ImmutableList.of("cat"))));
        b.add(e("two entry list", ImmutableMap.of("animal", ImmutableList.of("cat", "dog"))));
        b.add(e("two level", ImmutableMap.of("animal", ImmutableMap.of("good", "cat"))));
        b.add(e("two level two item", ImmutableMap.of("animal", //
                ImmutableMap.of("good", "cat", "bad", "dog"))));
        b.add(e("two level with trailing field", ImmutableMap.of( //
                "animal", ImmutableMap.of("good", "cat"), //
                "action", "run")));
        b.add(e("two level with trailing object", ImmutableMap.of( //
                "animal", ImmutableMap.of("good", "cat"), //
                "action", ImmutableMap.of("flies", "bird"))));
        b.add(e("two level list",
                ImmutableMap.of("animal", ImmutableMap.of("good", ImmutableList.of("cat")))));
        b.add(e("list of objects", ImmutableMap.of("animal", //
                ImmutableList.of(ImmutableMap.of("good", "cat"), ImmutableMap.of("bad", "dog")))));
        b.add(e("list of lists", ImmutableMap.of("animal", //
                ImmutableList.of(ImmutableList.of("cat", "dog"), ImmutableList.of("bird")))));
        b.add(e("list of value and then list", ImmutableMap.of("animal", //
                ImmutableList.of("bird", ImmutableList.of("cat", "dog")))));
        b.add(e("list of list and then value", ImmutableMap.of("animal", //
                ImmutableList.of(ImmutableList.of("cat", "dog"), "bird"))));
        EXAMPLES = b.build();
    }

    /**
     * Just a convenient builder.
     */
    private static StringMapExample e(String description, Object map) {
        return new StringMapExample(description, map);
    }

    private final String description;
    private final Object testData;

    public StringMapExample(String description, Object testData) {
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
         * 
         * @RunWith(Parameters.class).
         */
        @Parameters(name = "{0}")
        public static Collection<Object[]> parameters() {
            List<Object[]> parameters = new ArrayList<>();
            for (StringMapExample e : EXAMPLES) {
                parameters.add(new Object[] { e.description(), e });
            }
            return parameters;
        }

        @Parameter(0)
        public String description;
        @Parameter(1)
        public StringMapExample example;

    }
}
