package com.github.nik9000.structure;

import java.util.List;

public class ForkingStructuredDataSync implements StructuredDataSync {
    private List<StructuredDataSync> syncs;

    public ForkingStructuredDataSync(List<StructuredDataSync> syncs) {
        this.syncs = syncs;
    }

}
