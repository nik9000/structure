package com.github.nik9000.structure.sync.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import com.github.nik9000.structure.sync.Column.RowReader;

/**
 * Really RowReader implementation targeting Lucene.
 */
public class LuceneRowReader implements RowReader {
    public static RowReader fromDocument(Document doc) {
        return new LuceneRowReader(doc.getFields());
    }

    private final Map<String, Iterator<IndexableField>> fields = new HashMap<>();

    public LuceneRowReader(Iterable<IndexableField> fieldsFromDocument) {
        Map<String, List<IndexableField>> fieldLists = new HashMap<>();
        for (IndexableField field : fieldsFromDocument) {
            if (!field.fieldType().stored()) {
                continue;
            }
            List<IndexableField> namedFields = fieldLists.get(field.name());
            if (namedFields == null) {
                namedFields = new ArrayList<>();
                fieldLists.put(field.name(), namedFields);
            }
            namedFields.add(field);
        }
        for (Map.Entry<String, List<IndexableField>> field : fieldLists.entrySet()) {
            fields.put(field.getKey(), field.getValue().iterator());
        }
    }

    @Override
    public Object next(String column) {
        Iterator<IndexableField> fieldItr = fields.get(column);
        if (fieldItr == null || !fieldItr.hasNext()) {
            return null;
        }
        IndexableField field = fieldItr.next();
        return getValue(field);
    }

    protected Object getValue(IndexableField field) {
        Object v = field.numericValue();
        if (v != null) {
            return v;
        }
        v = field.stringValue();
        if (v != null) {
            return v;
        }
        return field.binaryValue();
    }
}
