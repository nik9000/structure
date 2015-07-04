package com.github.nik9000.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.nik9000.structure.StructuredDataSync.FieldValueSync;
import com.github.nik9000.structure.StructuredDataSync.ListValuesSync;
import com.github.nik9000.structure.StructuredDataSync.ValueSync;

public final class IntMap {
    public static void walk(StructuredDataSync sync, Map<Integer, ? extends Object> map) {
        for (Map.Entry<Integer, ? extends Object> e : map.entrySet()) {
            sync(sync.field(e.getKey()), e.getValue());
        }
        sync.endObject();
    }

    public static void walk(ListValuesSync sync, List<? extends Object> list) {
        for (Object value : list) {
            sync(sync, value);
        }
        sync.endList();
    }

    @SuppressWarnings("unchecked")
    public static void sync(ValueSync sync, Object value) {
        if (value instanceof List) {
            walk(sync.startList(), (List<Object>) value);
            return;
        }
        if (value instanceof Map) {
            walk(sync.startObject(), (Map<Integer, Object>) value);
            return;
        }
        sync.value(value);
    }

    public static class Sync implements StructuredDataSync {
        private final Map<Integer, Object> map = new HashMap<Integer, Object>();

        public Map<Integer, Object> map() {
            return map;
        }

        @Override
        public FieldValueSync field(int id) {
            return new IntMapFieldValueSync(map, id);
        }

        @Override
        public void endObject() {
        }
    }

    private static class IntMapFieldValueSync extends AbstractIntMapValueSync implements
            FieldValueSync {
        private final Map<Integer, Object> map;
        private final int field;

        public IntMapFieldValueSync(Map<Integer, Object> map, int field) {
            this.map = map;
            this.field = field;
        }

        @Override
        public void value(Object o) {
            map.put(field, o);
        }
    }

    private static class IntMapListValuesSync extends AbstractIntMapValueSync implements
            ListValuesSync {
        private final List<Object> list = new ArrayList<>();

        public List<Object> list() {
            return list;
        }

        @Override
        public void value(Object o) {
            list.add(o);
        }

        @Override
        public void endList() {
        }
    }

    private static abstract class AbstractIntMapValueSync implements ValueSync {
        @Override
        public abstract void value(Object o);

        @Override
        public ListValuesSync startList() {
            IntMapListValuesSync sync = new IntMapListValuesSync();
            value(sync.list());
            return sync;
        }

        @Override
        public StructuredDataSync startObject() {
            Sync sync = new Sync();
            value(sync.map());
            return sync;
        }
    }
}
