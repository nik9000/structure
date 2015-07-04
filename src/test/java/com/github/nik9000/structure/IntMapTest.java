package com.github.nik9000.structure;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IntMapTest extends IntMapExample.ParameterizedTest {
    @Test
    public void roundTrip() {
        IntMap.Sync sync = new IntMap.Sync();
        IntMap.walk(sync, example.map());
        assertEquals(example.map(), sync.map());
    }
}
