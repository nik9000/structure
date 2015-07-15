package com.github.nik9000.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

public class ByteTest {
    @Test
    public void someBytes() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        Bytes.OutputStreamByteSync sync = new Bytes.OutputStreamByteSync(b);
        
        sync.put((byte) 42);
        sync.put((byte) 12);
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            sync.put((byte) i);
        }

        Bytes.InputStreamByteSource source = new Bytes.InputStreamByteSource(
                new ByteArrayInputStream(b.toByteArray()));
        assertTrue(source.hasNext());
        assertEquals(source.next(), (byte) 42);
        assertTrue(source.hasNext());
        assertEquals(source.next(), (byte) 12);
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            assertTrue(source.hasNext());
            assertEquals(source.next(), (byte) i);
        }
    }

    @Test
    public void empty() {
        assertFalse(new Bytes.InputStreamByteSource(new ByteArrayInputStream(new byte[] {}))
                .hasNext());
    }
}
