package com.github.nik9000.structure;

public interface StructuredDataSync {
    /**
     * Enter the field identified by id.
     */
    FieldValueSync field(int id);

    void endObject();

    interface ValueSync {
        void value(Object o);

        StructuredDataSync startObject();

        ListValuesSync startList();
    }

    interface FieldValueSync extends ValueSync {
    }

    interface ListValuesSync extends ValueSync {
        void endList();
    }
}
