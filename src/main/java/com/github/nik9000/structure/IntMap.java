package com.github.nik9000.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of the StructuredDataSync for Map<Integer, Object>.
 */
public final class IntMap {
    public static void sync(StructuredDataSync sync, Map<Integer, ? extends Object> map) {
        sync.startObject();
        for (Map.Entry<Integer, ? extends Object> e : map.entrySet()) {
            sync.field(e.getKey());
            sync(sync, e.getValue());
        }
        sync.end();
    }

    public static void sync(StructuredDataSync sync, List<? extends Object> list) {
        sync.startList();
        for (Object value : list) {
            sync(sync, value);
        }
        sync.end();
    }

    @SuppressWarnings("unchecked")
    public static void sync(StructuredDataSync sync, Object value) {
        if (value instanceof List) {
            sync(sync, (List<Object>) value);
            return;
        }
        if (value instanceof Map) {
            sync(sync, (Map<Integer, Object>) value);
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
        public void field(int id) {
            target.field(id);
        }

        @Override
        public void end() {
            if (target.previous == null) {
                throw new IllegalStateException("End called when there is nothing to end");
            }
            target = target.previous;
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

            public abstract void field(int id);

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
            public void field(int id) {
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
            public void field(int id) {
                throw new IllegalStateException("field called on a list - must be in an object");
            }

            @Override
            public void value(Object v) {
                target.add(v);
            }
        }

        private class ObjectTarget extends Target {
            private final Map<Integer, Object> target = new HashMap<>();
            private int field = -1;

            public ObjectTarget(Target previous) {
                super(previous);
            }

            @Override
            public Object wraps() {
                return target;
            }

            @Override
            public void field(int id) {
                if (field >= 0) {
                    throw new IllegalStateException(
                            "field called without first setting the value of the last field");
                }
                if (id < 0) {
                    throw new IllegalArgumentException("fields must be greater than 0 but got:  "
                            + id);
                }
                field = id;
            }

            @Override
            public void value(Object v) {
                if (field < 0) {
                    throw new IllegalStateException("value called without first calling field");
                }
                target.put(field, v);
                field = -1;
            }
        }
    }
}
