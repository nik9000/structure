package com.github.nik9000.structure.sync;

import com.github.nik9000.structure.PathStack;
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
        /**
         * Load the next value in this column.
         */
        Object next(String column);
    }

    /**
     * Syncs the structured data to a column store. This implementation plays
     * fast and loose with lists - it ignores calls to startList and endList
     * entirely and multiple calls to value between calls to startObject and
     * field are considered lists.
     */
    public static class Sync implements StructuredDataSync {
        private static enum State {
            OBJECT, LIST;
        }

        private final RowWriter writer;
        private final State[] states;
        private int currentState = -1;
        private PathStack path;

        public Sync(RowWriter writer, int maxDepth) {
            this.writer = writer;
            path = new PathStack(".");
            states = new State[maxDepth];
        }

        @Override
        public void value(Object o) {
            writer.write(path.toPath(), o);
            popPathIfAppropriate();
        }

        @Override
        public void startList() {
            pushState(State.LIST);
        }

        @Override
        public void endList() {
            popState();
            popPathIfAppropriate();
        }

        @Override
        public void startObject() {
            pushState(State.OBJECT);
        }

        @Override
        public void endObject() {
            popState();
            popPathIfAppropriate();
        }

        @Override
        public void field(String name) {
            path.push(name);
        }

        private void pushState(State state) {
            currentState += 1;
            if (currentState >= states.length) {
                throw new IllegalStateException("Too much depth. Construct with greater maxDepth.");
            }
            states[currentState] = state;
        }

        private void popState() {
            if (currentState < -1) {
                throw new IllegalStateException("No more states to pop");
            }
            currentState -= 1;
        }

        private void popPathIfAppropriate() {
            if (currentState >= 0 && states[currentState] == State.OBJECT) {
                path.pop();
            }
        }
    }
}
