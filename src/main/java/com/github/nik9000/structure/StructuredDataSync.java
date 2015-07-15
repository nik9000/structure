package com.github.nik9000.structure;

/**
 * Sync for structured data.
 */
public interface StructuredDataSync {
    /**
     * Set the value of a field or add a value to a list.
     */
    void value(Object o);

    /**
     * Start a new list at the current position. Fill out the list by calling
     * value or any of the start menthods over and over again.
     */
    void startList();

    /**
     * End the current list.
     */
    void endList();

    /**
     * Start a new object at the current position. Fill out the object by
     * calling field and then value or any of the start methods and then calling
     * field again, etc.
     */
    void startObject();

    /**
     * End the current object.
     */
    void endObject();

    /**
     * Start a new field in the current object. After calling this you must call
     * value, startObject, or endObject.
     */
    void field(String name);
}
