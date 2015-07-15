package com.github.nik9000.structure;

public class PathStack {
    private final String[] entries;
    private final String separator;
    /**
     * Starting capacity used for the StringBuilder for the path. Defaults to
     * something simple and ratchets upwards.
     */
    private int pathBuilderStartingCapacity = 40;
    private int nextEntry;

    public PathStack(String separator, int maxEntries) {
        this.separator = separator;
        entries = new String[maxEntries];
    }

    /**
     * Push a path part onto the path.
     */
    public void push(String entry) {
        if (nextEntry >= entries.length) {
            throw new IllegalStateException("Too many entries. Construct with more maxEntries");
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

    public String top() {
        if (nextEntry <= 0) {
            return null;
        }
        return entries[nextEntry - 1];
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
        StringBuilder b = new StringBuilder(pathBuilderStartingCapacity);
        b.append(entries[0]);
        for (int i = 1; i < nextEntry; i++) {
            b.append(separator).append(entries[i]);
        }
        pathBuilderStartingCapacity = Math.max(pathBuilderStartingCapacity, b.capacity());
        return b.toString();
    }

    @Override
    public String toString() {
        if (nextEntry == 0) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        b.append('"').append(entries[0]).append('"');
        for (int i = 1; i < nextEntry; i++) {
            b.append(separator).append('"').append(entries[i]).append('"');
        }
        return b.toString();
    }
}
