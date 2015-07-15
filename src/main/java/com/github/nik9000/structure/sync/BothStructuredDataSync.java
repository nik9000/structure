package com.github.nik9000.structure.sync;

import com.github.nik9000.structure.StructuredDataSync;


public class BothStructuredDataSync extends AbstractDelegatingStructuredDataSync {
    private final StructuredDataSync second;

    public BothStructuredDataSync(StructuredDataSync first, StructuredDataSync second) {
        super(first);
        this.second = second;
    }

    @Override
    public void value(Object o) {
        super.value(o);
        second.value(o);
    }

    @Override
    public void startList() {
        super.startList();
        second.startList();
    }

    @Override
    public void endList() {
        super.endList();
        second.endList();
    }

    @Override
    public void startObject() {
        super.startObject();
        second.startObject();
    }

    @Override
    public void endObject() {
        super.endObject();
        second.endObject();
    }

    @Override
    public void field(String name) {
        super.field(name);
        second.field(name);
    }
}
