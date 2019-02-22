package com.exactpro.task.logic;

import com.exactpro.task.exceptions.CyclicException;
import com.exactpro.task.exceptions.CyclicExceptionInCollections;
import com.exactpro.task.interfaces.SuperEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * <p> The main class, which implements interface {@code SuperEncoder}. It contains the logger for monitoring operations.
 *</p>
 * @author Max
 * @version 1.0
 */
public class SuperEncoderImpl implements SuperEncoder {
    private static final Logger LOGGER = LogManager.getLogger(SuperEncoderImpl.class.getName());

    private static ArrayList<String> listOfAllNamesOfCollections = new ArrayList<>();

    static {
        listOfAllNamesOfCollections.add("java.util.List");
        listOfAllNamesOfCollections.add("java.util.Set");
        listOfAllNamesOfCollections.add("java.util.Queue");
        listOfAllNamesOfCollections.add("java.util.Map");
    }


    /**
     * <p> This method serialize an object to a byte array. Firstly, it checks the object isn't {@code null}. If it is null, it
     * generates {@link NullPointerException}. After that, it checks cyclic reference. If it has a cyclic reference, it
     * generates {@link CyclicException} or {@link CyclicExceptionInCollections}.
     * Then the serialization begins. </p>
     *
     * @param anyBean Object, which need to serialize.
     * @return byte[] Byte array
     */
    @Override
    public byte[] serialize(Object anyBean) {
        checkNullReferenceObject(anyBean);
        checkCyclicReferences(anyBean);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(anyBean);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info(anyBean.getClass() + " was successfully serialized!");
        return byteArrayOutputStream.toByteArray();
    }

    private void checkNullReferenceObject(Object anyBean) {
        if (anyBean == null) {
            throw new NullPointerException();
        } else {
            LOGGER.info(anyBean.getClass() + " isn't null.");
        }
    }

    private void checkCyclicReferences(Object anyBean) {
        Field[] allClassFields = anyBean.getClass().getDeclaredFields();
        for (Field field : allClassFields) {
            if (anyBean.getClass().equals(field.getType())) {
                try {
                    throw new CyclicException();
                } catch (CyclicException e) {
                    e.printStackTrace();
                }
            }
            checkCyclicReferencesInCollections(anyBean, field);
        }
    }

    private void checkCyclicReferencesInCollections(Object anyBean, Field field) {
        for (String nameOfCollection : listOfAllNamesOfCollections) {
            if (field.getType().getName().equals(nameOfCollection)) {
                if (field.getGenericType().getTypeName().contains(anyBean.getClass().getName())) {
                    try {
                        throw new CyclicExceptionInCollections();
                    } catch (CyclicExceptionInCollections e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * <p> This method deserializes {@code byte []} to Object. Firstly, it checks array's size. If it is null, it generates
     * {@link NullPointerException}. After that deserialization begins. When it is completed, it checks if the object
     * isn't {@code null}. </p>
     *
     * @param data Byte array, which need to transform in object.
     * @return Object
     */
    @Override
    public Object deserialize(byte[] data) {
        checkNullSizeAndReference(data);
        Object object = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
            object = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        checkNullReferenceObject(object);
        LOGGER.info("Deserialization succeeded!");
        return object;
    }

    private void checkNullSizeAndReference(byte[] data) {
        if (data == null || data.length == 0) {
            throw new NullPointerException();
        } else {
            LOGGER.info("Byte array isn't empty.");
        }
    }
}