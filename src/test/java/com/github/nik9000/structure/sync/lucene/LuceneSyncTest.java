package com.github.nik9000.structure.sync.lucene;

import static com.github.nik9000.structure.StringMapExample.MAX_DEPTH;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

import com.github.nik9000.structure.Bytes;
import com.github.nik9000.structure.StringMapExample;
import com.github.nik9000.structure.Structure;
import com.github.nik9000.structure.StructuredDataSync;
import com.github.nik9000.structure.sync.BothStructuredDataSync;
import com.github.nik9000.structure.sync.Column;
import com.github.nik9000.structure.sync.StringMap;

public class LuceneSyncTest extends StringMapExample.ParameterizedTest {
    @Test
    public void roundTrip() throws IOException {
        assumeThat(example.testData(), instanceOf(Map.class));
        Document doc = new Document();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Structure.FieldResolver fields = new Structure.SimpleFieldResolver();
        LuceneRowWriter column = new LuceneRowWriter(doc);
        StructuredDataSync sync = new Structure.Sync(new Bytes.OutputStreamByteSync(bytes), fields,
                MAX_DEPTH);
        sync = new BothStructuredDataSync(sync, new Column.Sync(column, MAX_DEPTH));
        StringMap.sync(sync, example.testData());
        column.write("_structure", bytes.toByteArray());

        try (IndexWriter writer = new IndexWriter(new RAMDirectory(), new IndexWriterConfig(
                new KeywordAnalyzer()))) {
            writer.addDocument(doc);
            try (IndexReader reader = DirectoryReader.open(writer, true)) {
                doc = reader.document(0);
                assertNotNull(doc);

                LuceneRowReader rowReader = new LuceneRowReader(doc);
                StringMap.Sync map = new StringMap.Sync();
                sync = map;
                // sync = new PrintingStructuredDataSync(sync);
                BytesRef structure = (BytesRef) rowReader.next("_structure");
                Bytes.Source bytesSource = new Bytes.BytesRefBytesSource(structure.bytes,
                        structure.offset, structure.length);
                Structure.Rebuilder rebuilder = new Structure.Rebuilder(rowReader, fields,
                        bytesSource, sync, MAX_DEPTH);
                rebuilder.sync();
                assertEquals(example.testData(), map.root());
            }
        }
    }
}
