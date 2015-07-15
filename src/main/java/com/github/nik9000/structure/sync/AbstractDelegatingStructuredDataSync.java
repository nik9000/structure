package com.github.nik9000.structure.sync;

import com.github.nik9000.structure.StructuredDataSync;


public abstract class AbstractDelegatingStructuredDataSync implements StructuredDataSync {
    private final StructuredDataSync delegate;

    public AbstractDelegatingStructuredDataSync(StructuredDataSync delegate) {
        this.delegate = delegate;
    }

    @Override
    public void value(Object o) {
        delegate.value(o);
    }

    @Override
    public void startList() {
        delegate.startList();
    }

    @Override
    public void endList() {
        delegate.endList();
    }

    @Override
    public void startObject() {
        delegate.startObject();
    }

    @Override
    public void endObject() {
        delegate.endObject();
    }

    @Override
    public void field(String name) {
        delegate.field(name);
    }
}
