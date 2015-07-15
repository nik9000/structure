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
        private PathStack path;

        public Rebuilder(RowReader reader, FieldResolver resolver, Source source,
                StructuredDataSync sync, int maxDepth) {
            this.reader = reader;
            this.resolver = resolver;
            this.source = source;
            this.sync = sync;
            path = new PathStack(".", maxDepth);
        }

        public void sync() {
            read = readVInt();
            if (read == START_OBJECT) {
                read = readVInt();
            }
            syncObject();
        }

        private void syncObject() {
            sync.startObject();
            while (source.hasNext()) {
                switch (read) {
                case START_OBJECT:
                    throw new IllegalStateException(
                            "Its invalid to start an object outside of a field.");
                case START_LIST:
                    throw new IllegalStateException(
                            "Its invalid to start a list outside of a field.");
                case VALUE_LIST:
                    throw new IllegalStateException(
                            "Its invalid to start a list outside of a field.");
                case END:
                    sync.endObject();
                    if (source.hasNext()) {
                        read = readVInt();
                    }
                    return;
                default:
                    String field = resolver.resolve(read - FIRST_FIELD);
                    path.push(field);
                    sync.field(field);
                    read = readVInt();
                    syncValue(false);
                    path.pop();
                }
            }
        }

        private void syncValue(boolean listMode) {
            switch (read) {
            case START_OBJECT:
                read = readVInt();
                syncObject();
                return;
            case START_LIST:
                read = readVInt();
                sync.startList();
                while (read != END) {
                    syncValue(true);
                }
                sync.endList();
                return;
            case VALUE_LIST:
                if (!source.hasNext()) {
                    throw new Bytes.IOException(
                            "Invalid value list - its missing its length. Its likely the data was cut off.");
                }
                read = readVInt();
                sync.startList();
                String column = path.toPath();
                for (int i = 0; i < read; i++) {
                    sync.value(reader.next(column));
                }
                sync.endList();
                read = readVInt();
                break;
            case END:
                if (listMode) {
                    return;
                }
            default:
                sync.value(reader.next(path.toPath()));
                if (listMode) {
                    read = readVInt();
                }
            }
        }

        /**
         * Read a VInt.
         */
        private int readVInt() {
            if (!source.hasNext()) {
                return END;
            }
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
        private static final int IN_OBJECT = -1;
        private static final int IN_COMPLEX_LIST = -2;
        private final Bytes.Sync b;
        private final FieldResolver fieldResolver;
        /**
         * Date for what is being worked on.
         * <ul>
         * <li>If the top is >= 0 that means we're working on a value list.
         * <li>If the top == IN_OBJECT that means we're working on an object.
         * <li>If the top == IN_COMPLEX_LIST that means we're working on a
         * complex list _and_ the value right under the top will be the field
         * that that list is for.
         * </ul>
         */
        private final int[] objectStates;
        private int currentObjectState = 0;
        private int lastField;

        public Sync(Bytes.Sync b, FieldResolver fieldResolver, int maxDepth) {
            // TODO its acceptable to strip the leading START_OBJECT
            // TODO its acceptable to strip all trailing ENDs
            this.b = b;
            this.fieldResolver = fieldResolver;
            objectStates = new int[maxDepth * 2];
            objectStates[currentObjectState] = -1;
        }

        @Override
        public void value(Object o) {
            switch (objectStates[currentObjectState]) {
            case IN_OBJECT:
                // In an object, nothing to do
                return;
            case IN_COMPLEX_LIST:
                writeField(objectStates[currentObjectState - 1]);
                return;
            default:
                assert objectStates[currentObjectState] >= 0 : "Unexpected state";
                objectStates[currentObjectState] += 1;
            }
        }

        @Override
        public void startList() {
            prepateForComplexValue();
            // Default all lists to value lists unless we see otherwise
            pushState(0);
        }

        @Override
        public void endList() {
            switch (objectStates[currentObjectState]) {
            case IN_OBJECT:
                throw new IllegalStateException("Expected to be in a list but was in an object");
            case IN_COMPLEX_LIST:
                b.put(END);
                currentObjectState -= 2;
                return;
            default:
                assert objectStates[currentObjectState] >= 0 : "Unexpected state";
                b.put(VALUE_LIST);
                writeVInt(objectStates[currentObjectState]);
                currentObjectState -= 1;
                return;
            }
        }

        @Override
        public void startObject() {
            prepateForComplexValue();
            pushState(-1);
            b.put(START_OBJECT);
        }

        @Override
        public void endObject() {
            if (objectStates[currentObjectState] != IN_OBJECT) {
                throw new IllegalStateException("Expected to be in an object but was in a list");
            }
            currentObjectState -= 1;
            b.put(END);
            return;
        }

        @Override
        public void field(String name) {
            if (objectStates[currentObjectState] != IN_OBJECT) {
                throw new IllegalStateException("Expected to be in an object but was in a list");
            }
            lastField = fieldResolver.resolve(name);
            writeField(lastField);
        }

        private void writeVInt(int i) {
            // Implementation pretty shamelessly stolen from Lucene.
            while ((i & ~0x7F) != 0) {
                b.put((byte) ((i & 0x7f) | 0x80));
                i >>>= 7;
            }
            b.put((byte) i);
        }

        private void writeField(int field) {
            writeVInt(FIRST_FIELD + field);
        }

        /**
         * If we're working on a value list convert it to a complex list so it
         * can hold a complex value.
         */
        private void prepateForComplexValue() {
            switch (objectStates[currentObjectState]) {
            case IN_OBJECT:
            case IN_COMPLEX_LIST:
                break;
            default:
                assert objectStates[currentObjectState] >= 0 : "Unexpected state";
                convertValueListIntoComplexList();
            }
        }

        private void convertValueListIntoComplexList() {
            b.put(START_LIST);
            for (int i = 0; i < objectStates[currentObjectState]; i++) {
                writeField(lastField);
            }
            objectStates[currentObjectState] = lastField;
            pushState(-2);
        }

        private void pushState(int newState) {
            currentObjectState += 1;
            if (currentObjectState >= objectStates.length) {
                throw new IllegalStateException(
                        "Ran out of state storage space. Build with greater maxObjectDepth");
            }
            objectStates[currentObjectState] = newState;

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
