package com.exactpro.task.interfaces;

/**
 * This is a main interface, which I need to implement.
 * Implementing this interface allows an object to be serialized or deserialized.
 *
 * @author Exactpro
 * @version 1.0
 * @see com.exactpro.task.logic.SuperEncoderImpl
 */
public interface SuperEncoder {
    /**
     * Serialize any object, which implements {@link java.io.Serializable}
     *
     * @param anyBean object, which need to serialize
     */
    byte[] serialize(Object anyBean);

    /**
     * Deserialize any object, which implements {@link java.io.Serializable}
     *
     * @param data byte array, which need to deserialize to object
     */
    Object deserialize(byte[] data);
}
