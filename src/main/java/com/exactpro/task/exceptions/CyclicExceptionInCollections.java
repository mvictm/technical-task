package com.exactpro.task.exceptions;

/**
 * Exception, which is generated when an object contains a collection, which has a generic type like an object.
 *
 * @author Max
 * @version 1.0
 */
public class CyclicExceptionInCollections extends Exception {
    public CyclicExceptionInCollections() {
        super("Serializable class has a collection, which uses the same generic type as a class.");
    }
}
