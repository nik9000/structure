package com.github.nik9000.structure;

import static org.apache.lucene.util.RamUsageEstimator.NUM_BYTES_OBJECT_REF;

import org.apache.lucene.util.ArrayUtil;

public class PathStack {
    private static final int DEFAULT_INITIAL_DEPTH = 5;
    /**
     * Paths built here. Initial capacity of 64 because 16 and 32 just seemed a
     * bit small.
     */
    private final StringBuilder pathBuilder = new StringBuilder(64);
    private final String separator;
    private String[] entries;
    private int nextEntry;

    public PathStack(String separator) {
        this(separator, DEFAULT_INITIAL_DEPTH);
    }

    public PathStack(String separator, int initialDepth) {
        this.separator = separator;
        entries = new String[ArrayUtil.oversize(initialDepth, NUM_BYTES_OBJECT_REF)];
    }

    /**
     * Push a path part onto the path.
     */
    public void push(String entry) {
        if (nextEntry >= entries.length) {
            String[] backup = entries;
            entries = new String[ArrayUtil.oversize(nextEntry, NUM_BYTES_OBJECT_REF)];
            System.arraycopy(backup, 0, entries, 0, backup.length);
        }
        entries[nextEntry] = entry;
        nextEntry++;
    }

    public void pop() {
        // Its simpler to just ignore pops that are invalid
        if (nextEntry > 0) {
            nextEntry -= 1;
        }
    }

    public String toPath() {
        switch (nextEntry) {
        case 0:
            return "";
        case 1:
            return entries[0] == null ? "" : entries[0];
        default:
            // Intentionally fall out
        }
        pathBuilder.setLength(0);
        pathBuilder.append(entries[0]);
        for (int i = 1; i < nextEntry; i++) {
            pathBuilder.append(separator).append(entries[i]);
        }
        return pathBuilder.toString();
    }

    @Override
    public String toString() {
        return toPath();
    }
}
