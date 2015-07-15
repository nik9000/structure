package com.github.nik9000.structure.sync;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.nik9000.structure.StringMapExample;

public class StringMapTest extends StringMapExample.ParameterizedTest {
    @Test
    public void roundTrip() {
        StringMap.Sync sync = new StringMap.Sync();
        StringMap.sync(sync, example.testData());
        assertEquals(example.testData(), sync.root());
    }
}
