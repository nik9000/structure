package com.github.nik9000.structure.sync;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.github.nik9000.structure.Bytes;
import com.github.nik9000.structure.ListMultimapColumnRowWriter;
import com.github.nik9000.structure.StringMapExample;
import com.github.nik9000.structure.Structure;
import com.github.nik9000.structure.StructuredDataSync;

public class ColumnSyncTest extends StringMapExample.ParameterizedTest {
    @Test
    public void roundTrip() {
        assumeThat(example.testData(), instanceOf(Map.class));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Structure.FieldResolver fields = new Structure.SimpleFieldResolver();
        ListMultimapColumnRowWriter column = new ListMultimapColumnRowWriter();
        StructuredDataSync sync = new BothStructuredDataSync(new Structure.Sync(
                new Bytes.OutputStreamByteSync(bytes), fields), new Column.Sync(column));
        StringMap.sync(sync, example.testData());
        System.err.printf("%30s:  %30s %-30s %s\n", example.description(),
                Arrays.toString(bytes.toByteArray()),
                fields, column);

        StringMap.Sync map = new StringMap.Sync();
        Structure.Rebuilder rebuilder = new Structure.Rebuilder(column.reader(), fields,
                new Bytes.InputStreamByteSource(new ByteArrayInputStream(bytes.toByteArray())), map);
        rebuilder.sync();
        assertEquals(example.testData(), map.root());
    }
}
