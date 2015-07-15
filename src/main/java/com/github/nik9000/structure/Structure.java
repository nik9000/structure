package com.github.nik9000.structure;

import java.util.HashMap;
import java.util.Map;

import com.github.nik9000.structure.Bytes.Source;
import com.github.nik9000.structure.sync.Column;
import com.github.nik9000.structure.sync.Column.RowReader;

public class Structure {
    /**
     * Start a new object.
     */
    private static final byte START_OBJECT = 0;
    /**
     * Start a new list.
     */
    private static final byte START_LIST = 1;
    /**
     * End the current list or object.
     */
    private static final byte END = 2;
    /**
     * Special case identifier for a list of values. Represented as this value
     * and then a count of the number of values to read.
     */
    private static final byte VALUE_LIST = 3;
    /**
     * Offset for all fields.
     */
    private static final int FIRST_FIELD = VALUE_LIST + 1;

    /**
     * Rebuilds a row from a column store into structured data.
     *
     */
    public static class Rebuilder {
        private final Column.RowReader reader;
        private final FieldResolver resolver;
        private final Bytes.Source source;
        private final StructuredDataSync sync;
        private int read;
        private String path = "";

        public Rebuilder(RowReader reader, FieldResolver resolver, Source source,
                StructuredDataSync sync) {
            this.reader = reader;
            this.resolver = resolver;
            this.source = source;
            this.sync = sync;
        }

        public void sync() {
            read = readVInt();
            if (read == START_OBJECT) {
                read = readVInt();
            }
            sync.startObject();
            while (true) {
                if (syncFieldInObject()) {
                    if (!source.hasNext()) {
                        break;
                    }
                    read = readVInt();
                }
            }
        }

        private boolean syncFieldInObject() {
            switch (read) {
            case START_OBJECT:
                throw new IllegalStateException(
                        "Its invalid to start an object outside of a field.");
            case START_LIST:
                throw new UnsupportedOperationException("Complex lists not yet supported");
            case VALUE_LIST:
                throw new IllegalStateException("Its invalid to start a list outside of a field.");
            case END:
                sync.endObject();
                int index = path.lastIndexOf('.', path.length() - 2);
                if (index < 0) {
                    path = "";
                } else {
                    path = path.substring(0, index);
                }
                return true;
            default:
                String field = resolver.resolve(read - FIRST_FIELD);
                sync.field(field);
                read = readVInt();
                return syncFieldValue(field);
            }
        }

        private boolean syncFieldValue(String field) {
            switch (read) {
            case START_OBJECT:
                path += field + '.';
                sync.startObject();
                return true;
            case START_LIST:
                throw new UnsupportedOperationException("Complex lists not yet supported");
            case VALUE_LIST:
                int size = readVInt();
                sync.startList();
                String column = column(field);
                for (int i = 0; i < size; i++) {
                    sync.value(reader.next(column));
                }
                sync.endList();
                return true;
            case END:
                sync.value(reader.next(column(field)));
                return false;
            default:
                sync.value(reader.next(column(field)));
                return false;
            }
        }

        /**
         * Get the column name for the field at the current path.
         */
        private String column(String field) {
            return path + field;
        }

        /**
         * Read a VInt.
         */
        private int readVInt() {
            // Implementation shamelessly stolen from Lucene.
            byte b = source.next();
            if (b >= 0) {
                return b;
            }
            try {
                int i = b & 0x7F;
                b = source.next();
                i |= (b & 0x7F) << 7;
                if (b >= 0) {
                    return i;
                }
                b = source.next();
                i |= (b & 0x7F) << 14;
                if (b >= 0) {
                    return i;
                }
                b = source.next();
                i |= (b & 0x7F) << 21;
                if (b >= 0) {
                    return i;
                }
                b = source.next();
                // Warning: the next ands use 0x0F / 0xF0 - beware copy/paste
                // errors:
                i |= (b & 0x0F) << 28;
                if ((b & 0xF0) == 0) {
                    return i;
                }
            } catch (Bytes.IOException e) {
                throw new Bytes.IOException(
                        "Byte stream terminates in the middle of a vInt. Its likely been cut off.",
                        e);
            }
            throw new Bytes.IOException("Invalid vInt detected (too many bits)");
        }
    }

    public static class Sync implements StructuredDataSync {
        private final Bytes.Sync b;
        private final FieldResolver fieldResolver;
        /**
         * If >= 0 it means we're in a simple list. If == -1 it means we're in
         * an object. If == -2 it means we're in a complex list.
         */
        private int listCount = -1;

        public Sync(Bytes.Sync b, FieldResolver fieldResolver) {
            // TODO its acceptable to strip the leading START_OBJECT
            // TODO its acceptable to strip all trailing ENDs
            this.b = b;
            this.fieldResolver = fieldResolver;
        }

        @Override
        public void value(Object o) {
            if (listCount >= 0) {
                listCount++;
            }
        }

        @Override
        public void startList() {
            if (listCount >= 0) {
                throw new UnsupportedOperationException("No complex lists yet");
            }
            b.put(VALUE_LIST);
            listCount = 0;
        }

        @Override
        public void endList() {
            if (listCount >= 0) {
                writeVInt(listCount);
            }
        }

        @Override
        public void startObject() {
            if (listCount >= 0) {
                throw new UnsupportedOperationException("No complex lists yet");
            }
            b.put(START_OBJECT);
        }

        @Override
        public void endObject() {
            b.put(END);
        }

        @Override
        public void field(String name) {
            writeVInt(FIRST_FIELD + fieldResolver.resolve(name));
        }

        private void writeVInt(int i) {
            // Implementation pretty shamelessly stolen from Lucene.
            while ((i & ~0x7F) != 0) {
                b.put((byte) ((i & 0x7f) | 0x80));
                i >>>= 7;
            }
            b.put((byte) i);
        }
    }

    public interface FieldResolver {
        int resolve(String name);

        String resolve(int id);
    }

    /**
     * Simple, unsynchronized, probably toy implementation of FieldResolver.
     */
    public static class SimpleFieldResolver implements FieldResolver {
        private final Map<String, Integer> fieldsByName = new HashMap<>();
        private final Map<Integer, String> fieldsById = new HashMap<>();
        private int lastId = -1;

        @Override
        public int resolve(String name) {
            Integer id = fieldsByName.get(name);
            if (id != null) {
                return id;
            }
            lastId++;
            fieldsByName.put(name, lastId);
            fieldsById.put(lastId, name);
            return lastId;
        }

        @Override
        public String resolve(int id) {
            String field = fieldsById.get(id);
            if (field == null) {
                throw new IllegalArgumentException("Unknown field:  " + id);
            }
            return field;
        }

        @Override
        public String toString() {
            return fieldsByName.toString();
        }
    }
}
