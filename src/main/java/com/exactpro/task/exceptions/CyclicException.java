package com.exactpro.task.exceptions;

/**
 * Exception, which is generated when object contains itself as a field.
 *
 * @author Max
 * @version 1.0
 */
public class CyclicException extends Exception {
    public CyclicException() {
        super("Serializable class has cyclic references");
    }
}
