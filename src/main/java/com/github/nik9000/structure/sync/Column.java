package com.github.nik9000.structure.sync;

import com.github.nik9000.structure.StructuredDataSync;

public class Column {
    /**
     * Wraps writing a single row to a column store.
     */
    public interface RowWriter {
        void write(String column, Object value);
    }

    /**
     * Wraps reading from a single row in a column store.
     */
    public interface RowReader {
        Object next(String column);
    }

    /**
     * Syncs the structured data to a column store. This implementation plays
     * fast and loose with lists - it ignores calls to startList and endList
     * entirely and multiple calls to value between calls to startObject and
     * field are considered lists.
     */
    public static class Sync implements StructuredDataSync {
        private final RowWriter writer;
        private String path = "";

        public Sync(RowWriter writer) {
            this.writer = writer;
        }

        @Override
        public void value(Object o) {
            if ("".equals(path)) {
                throw new IllegalStateException("Doesn't support bare values");
            }
            if (path.charAt(path.length() - 1) == '.') {
                throw new IllegalStateException("Field name not set");
            }
            writer.write(path, o);
        }

        @Override
        public void startList() {
        }

        @Override
        public void endList() {
        }

        @Override
        public void startObject() {
            if (!"".equals(path)) {
                path += '.';
            }
        }

        @Override
        public void endObject() {
            int index = path.lastIndexOf('.', path.length() - 2);
            if (index < 0) {
                path = "";
                return;
            }
            path = path.substring(0, index);
        }

        @Override
        public void field(String name) {
            if (path.endsWith(".")) {
                path += name;
                return;
            }
            int index = path.lastIndexOf('.');
            if (index < 0) {
                path = name;
                return;
            }
            path = path.substring(0, index) + name;
        }
    }
}
