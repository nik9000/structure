package com.github.nik9000.structure.sync.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.util.BytesRef;

import com.github.nik9000.structure.sync.Column.RowWriter;

/**
 * Really RowWriter implementation targeting Lucene.
 */
public class LuceneRowWriter implements RowWriter {
    private final Document doc;

    public LuceneRowWriter(Document doc) {
        this.doc = doc;
    }

    @Override
    public void write(String column, Object value) {
        doc.add(storedField(column, value));
    }

    private StoredField storedField(String column, Object value) {
        // TODO this is nasty
        if (value instanceof byte[]) {
            return new StoredField(column, (byte[]) value);
        }
        if (value instanceof BytesRef) {
            return new StoredField(column, (BytesRef) value);
        }
        if (value instanceof Double) {
            return new StoredField(column, (Double) value);
        }
        if (value instanceof Float) {
            return new StoredField(column, (Float) value);
        }
        if (value instanceof Integer) {
            return new StoredField(column, (Integer) value);
        }
        if (value instanceof Long) {
            return new StoredField(column, (Long) value);
        }
        if (value instanceof String) {
            return new StoredField(column, (String) value);
        }
        throw new IllegalArgumentException(
                "Can't store something of this type in a Lucene stored field:  " + value.getClass());
    }
}
