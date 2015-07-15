package com.github.nik9000.structure.sync;

import com.github.nik9000.structure.StructuredDataSync;

public abstract class AbstractTracingStructuredDataSync extends
        AbstractDelegatingStructuredDataSync {
    public AbstractTracingStructuredDataSync(StructuredDataSync delegate) {
        super(delegate);
    }

    protected abstract void trace(String method, Object arg);

    @Override
    public void value(Object o) {
        trace("value", o);
        super.value(o);
    }

    @Override
    public void startObject() {
        trace("startObject", null);
        super.startObject();
    }

    @Override
    public void endObject() {
        trace("endObject", null);
        super.endObject();
    }

    @Override
    public void startList() {
        trace("startList", null);
        super.startList();
    }

    @Override
    public void endList() {
        trace("endList", null);
        super.endList();
    }

    @Override
    public void field(String name) {
        trace("field", name);
        super.field(name);
    }
}
