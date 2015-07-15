package com.github.nik9000.structure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.nik9000.structure.sync.Column;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Emulates a column store using an ArrayListMultimap.
 */
public class ListMultimapColumnRowWriter implements Column.RowWriter {
    private final ListMultimap<String, Object> map = ArrayListMultimap.create();

    @Override
    public void write(String column, Object value) {
        map.put(column, value);
    }

    public Column.RowReader reader() {
        return new Reader(map);
    }

    @Override
    public String toString() {
        return map.toString();
    }

    private static class Reader implements Column.RowReader {
        private final ListMultimap<String, Object> map;
        private final Map<String, Iterator<Object>> itrs;

        public Reader(ListMultimap<String, Object> map) {
            this.map = map;
            itrs = new HashMap<>(map.size());
        }

        @Override
        public Object next(String column) {
            Iterator<Object> itr = itrs.get(column);
            if (itr == null) {
                List<Object> list = map.get(column);
                if (list == null) {
                    itr = Collections.emptyList().iterator();
                } else {
                    itr = list.iterator();
                }
                itrs.put(column, itr);
            }
            if (itr.hasNext()) {
                return itr.next();
            }
            return null;
        }
    }
}
