package com.github.nik9000.structure.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.nik9000.structure.StructuredDataSync;

/**
 * Simple implementation of the StructuredDataSync for Map<Integer, Object>.
 */
public final class StringMap {
    public static void sync(StructuredDataSync sync, Map<String, ? extends Object> map) {
        sync.startObject();
        for (Map.Entry<String, ? extends Object> e : map.entrySet()) {
            sync.field(e.getKey());
            sync(sync, e.getValue());
        }
        sync.endObject();
    }

    public static void sync(StructuredDataSync sync, List<? extends Object> list) {
        sync.startList();
        for (Object value : list) {
            sync(sync, value);
        }
        sync.endList();
    }

    @SuppressWarnings("unchecked")
    public static void sync(StructuredDataSync sync, Object value) {
        if (value instanceof List) {
            sync(sync, (List<Object>) value);
            return;
        }
        if (value instanceof Map) {
            sync(sync, (Map<String, Object>) value);
            return;
        }
        sync.value(value);
    }

    public static class Sync implements StructuredDataSync {
        private final Target rootTarget = new RootTarget();
        private Target target = rootTarget;

        public Object root() {
            return rootTarget.wraps();
        }

        @Override
        public void value(Object o) {
            if (o instanceof Map || o instanceof List) {
                throw new IllegalArgumentException(
                        "Value must not be a Map or List. That wouldn't be fair.");
            }
            target.value(o);
        }

        @Override
        public void startObject() {
            push(new ObjectTarget(target));
        }

        @Override
        public void startList() {
            push(new ListTarget(target));
        }

        @Override
        public void field(String name) {
            target.field(name);
        }

        @Override
        public void endList() {
            end();
        }

        @Override
        public void endObject() {
            end();
        }

        private void end() {
            if (target.previous != null) {
                target = target.previous;
            }
        }

        private void push(Target nextTarget) {
            target.value(nextTarget.wraps());
            target = nextTarget;
        }

        private abstract class Target {
            public final Target previous;

            public Target(Target previous) {
                this.previous = previous;
            }

            public abstract Object wraps();

            public abstract void field(String name);

            public abstract void value(Object v);
        }

        private class RootTarget extends Target {
            private Object wraps;

            public RootTarget() {
                super(null);
            }

            @Override
            public Object wraps() {
                return wraps;
            }

            @Override
            public void field(String name) {
                throw new IllegalStateException("field called on the root - call startObject first");
            }

            @Override
            public void value(Object v) {
                if (wraps != null) {
                    throw new IllegalStateException("Attempted to set the value of the root twice");
                }
                wraps = v;
            }
        }

        private class ListTarget extends Target {
            private final List<Object> target = new ArrayList<>();

            public ListTarget(Target previous) {
                super(previous);
            }

            @Override
            public Object wraps() {
                return target;
            }

            @Override
            public void field(String name) {
                throw new IllegalStateException("field called on a list - must be in an object");
            }

            @Override
            public void value(Object v) {
                target.add(v);
            }
        }

        private class ObjectTarget extends Target {
            private final Map<String, Object> target = new HashMap<>();
            private String fieldName;

            public ObjectTarget(Target previous) {
                super(previous);
            }

            @Override
            public Object wraps() {
                return target;
            }

            @Override
            public void field(String name) {
                if (fieldName != null) {
                    throw new IllegalStateException(
                            "field called without first setting the value of the last field");
                }
                fieldName = name;
            }

            @Override
            public void value(Object v) {
                if (fieldName == null) {
                    throw new IllegalStateException("value called without first calling field");
                }
                target.put(fieldName, v);
                fieldName = null;
            }
        }
    }
}
