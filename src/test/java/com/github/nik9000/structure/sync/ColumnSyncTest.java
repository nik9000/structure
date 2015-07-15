package com.github.nik9000.structure.sync;

import static com.github.nik9000.structure.StringMapExample.MAX_DEPTH;
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
        StructuredDataSync sync = new Structure.Sync(new Bytes.OutputStreamByteSync(bytes), fields,
                MAX_DEPTH);
        sync = new BothStructuredDataSync(sync, new Column.Sync(column));
        StringMap.sync(sync, example.testData());
        System.err.printf("%30s:  %35s %-30s %s\n", example.description(),
                Arrays.toString(bytes.toByteArray()), fields, column);

        StringMap.Sync map = new StringMap.Sync();
        sync = map;
        // sync = new PrintingStructuredDataSync(sync);
        Bytes.Source bytesSource = new Bytes.InputStreamByteSource(new ByteArrayInputStream(
                bytes.toByteArray()));
        Structure.Rebuilder rebuilder = new Structure.Rebuilder(column.reader(), fields,
                bytesSource, sync, MAX_DEPTH);
        rebuilder.sync();
        assertEquals(example.testData(), map.root());
    }

    public static class PrintingStructuredDataSync extends AbstractTracingStructuredDataSync {
        public PrintingStructuredDataSync(StructuredDataSync delegate) {
            super(delegate);
        }

        @Override
        protected void trace(String method, Object arg) {
            System.err.print(method);
            System.err.print('(');
            if (arg != null) {
                System.err.print(arg);
            }
            System.err.println(')');
        }
    }
}
