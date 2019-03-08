package com.exactpro.task.logic;

import com.exactpro.task.exceptions.CyclicException;
import com.exactpro.task.exceptions.CyclicExceptionInCollections;
import com.exactpro.task.interfaces.SuperEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * <p> The main class, which implements interface {@code SuperEncoder}. It contains the logger for monitoring operations.
 * </p>
 *
 * @author Max
 * @version 1.0
 */
public class SuperEncoderImpl implements SuperEncoder {
    private static final Logger LOGGER = LogManager.getLogger(SuperEncoderImpl.class.getName());

    private static ArrayList<String> listOfAllNamesOfCollections = new ArrayList<>();

    private int positionInByteArray = 0;

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
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {

            String className = anyBean.getClass().getName();
            dataOutputStream.writeUTF(className);
            Field[] allClassFields = anyBean.getClass().getDeclaredFields();
            for (Field field : allClassFields) {
                field.setAccessible(true);
                dataOutputStream.writeUTF(field.getName());
                String typeName = field.getType().getName();
                Object beanField = field.get(anyBean);
                if (beanField != null) {
                    dataOutputStream.writeUTF(typeName);
                    writeFieldWithValue(dataOutputStream, beanField);
                } else {
                    dataOutputStream.writeUTF("null");
                }
            }

            dataOutputStream.flush();

        } catch (IOException | IllegalAccessException e) {
            throw new RuntimeException("Can't write in stream ", e);
        }

        LOGGER.info(anyBean.getClass() + " was successfully serialized!");
        return byteArrayOutputStream.toByteArray();
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

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        try {
            dataInputStream.skipBytes(positionInByteArray);
        } catch (IOException e) {
            throw new RuntimeException("Can't skip bytes ", e);
        }

        Object rebuildClass = null;

        try {
            String className = dataInputStream.readUTF();
            Class<?> clazz = Class.forName(className);
            rebuildClass = clazz.newInstance();
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't initialize parameter for deserialize", e);
        }

        int countFieldInClasses = rebuildClass.getClass().getDeclaredFields().length;

        for (int i = 0; i < countFieldInClasses; i++) {
            String fieldName = null;
            String typeName = null;
            try {
                fieldName = dataInputStream.readUTF();
                typeName = dataInputStream.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Field field = rebuildClass.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                String fullTypeName = field.getAnnotatedType().getType().getTypeName();
                field.set(rebuildClass, readFieldWithValue(data, byteArrayInputStream, dataInputStream, typeName, rebuildClass, field, fullTypeName));
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Can't get a field ", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Can't set a field ", e);
            }
        }

        checkNullReferenceObject(rebuildClass);
        LOGGER.info("Deserialization succeeded!");
        return rebuildClass;
    }

    private Object readFieldWithValue(byte[] data, ByteArrayInputStream byteArrayInputStream, DataInputStream dataInputStream, String typeName, Object rebuildClass, Field field, String fullTypeName) {
        try {
            switch (typeName) {
                case "byte":
                case "java.lang.Byte":
                    return dataInputStream.readByte();
                case "short":
                case "java.lang.Short":
                    return dataInputStream.readShort();
                case "int":
                case "java.lang.Integer":
                    return dataInputStream.readInt();
                case "long":
                case "java.lang.Long":
                    return dataInputStream.readLong();
                case "float":
                case "java.lang.Float":
                    return dataInputStream.readFloat();
                case "double":
                case "java.lang.Double":
                    return dataInputStream.readDouble();
                case "boolean":
                case "java.lang.Boolean":
                    return dataInputStream.readBoolean();
                case "java.lang.Character":
                    return dataInputStream.readChar();
                case "java.lang.String":
                    return dataInputStream.readUTF();
                case "java.math.BigDecimal":
                    return new BigDecimal(dataInputStream.readUTF());
                case "java.time.Instant":
                    long epochSecond = dataInputStream.readLong();
                    long nanoSecond = dataInputStream.readLong();
                    return Instant.ofEpochSecond(epochSecond, nanoSecond);
                case "java.util.List":
                    return readList(data, byteArrayInputStream, dataInputStream, rebuildClass, field, fullTypeName);
                case "java.util.Set":
                    return readSet(data, byteArrayInputStream, dataInputStream, rebuildClass, field, fullTypeName);
                case "java.util.Map":
                    return readMap(data, byteArrayInputStream, dataInputStream, rebuildClass, field, fullTypeName);
                case "null":
                    return null;
                default:
                    setPositionInByteArray(byteArrayInputStream);
                    int positionBeforeDeserialize = positionInByteArray;
                    Object rebuildField = deserialize(data);
                    dataInputStream.skip(positionInByteArray - positionBeforeDeserialize);
                    return rebuildField;
            }
        } catch (IOException e) {
            try {
                field.set(rebuildClass, null);
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
            throw new RuntimeException("Can't read field with value");
        }
    }

    private void setPositionInByteArray(ByteArrayInputStream byteArrayInputStream) {
        Field pos = null;
        try {
            pos = byteArrayInputStream.getClass().getDeclaredField("pos");
            pos.setAccessible(true);
            positionInByteArray = (Integer) pos.get(byteArrayInputStream);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Can't set position "+ pos + e);
        }
    }

    private Object readList(byte[] data, ByteArrayInputStream byteArrayInputStream, DataInputStream dataInputStream, Object rebuildClass, Field field, String fullTypeName) {
        String className = null;
        int listSize = 0;
        try {
            className = dataInputStream.readUTF();
            listSize = dataInputStream.readInt();
        } catch (IOException e) {
            throw new RuntimeException("Can't initialize parameter for List ", e);
        }

        String typeName = fullTypeName.substring(fullTypeName.indexOf('<') + 1, fullTypeName.indexOf('>'));
        String typeValue = typeName.indexOf('<') != -1 ? typeName.substring(0, typeName.indexOf('<')) : typeName;

        List<Object> list = createConcreteList(className);
        for (int i = 0; i < listSize; i++) {
            list.add(readFieldWithValue(data, byteArrayInputStream, dataInputStream, typeValue, rebuildClass, field, typeName));
        }
        return list;
    }

    private Object readSet(byte[] data, ByteArrayInputStream byteArrayInputStream, DataInputStream dataInputStream, Object rebuildClass, Field field, String fullTypeName) {
        String className = null;
        int setSize = 0;
        try {
            className = dataInputStream.readUTF();
            setSize = dataInputStream.readInt();
        } catch (IOException e) {
            throw new RuntimeException("Can't initialize parameter for Set ", e);
        }

        String typeName = fullTypeName.substring(fullTypeName.indexOf('<') + 1, fullTypeName.indexOf('>'));
        String typeValue = typeName.indexOf('<') != -1 ? typeName.substring(0, typeName.indexOf('<')) : typeName;

        Set<Object> set = createConcreteSet(className);
        for (int i = 0; i < setSize; i++) {
            set.add(readFieldWithValue(data, byteArrayInputStream, dataInputStream, typeValue, rebuildClass, field, typeName));
        }
        return set;
    }

    private Object readMap(byte[] data, ByteArrayInputStream byteArrayInputStream, DataInputStream dataInputStream, Object rebuildClass, Field field, String fullTypeName) {
        String className = null;
        int mapSize = 0;
        try {
            className = dataInputStream.readUTF();
            mapSize = dataInputStream.readInt();
        } catch (IOException e) {
            throw new RuntimeException("Can't initialize parameter for Map ", e);
        }

        String[] typeName = fullTypeName.substring(fullTypeName.indexOf('<') + 1, fullTypeName.lastIndexOf('>')).split(", ");

        String typeKey = typeName[0].indexOf('<') != -1 ? typeName[0].substring(0, typeName[0].indexOf('<')) : typeName[0];
        String typeValue = typeName[1].indexOf('<') != -1 ? typeName[1].substring(0, typeName[1].indexOf('<')) : typeName[1];

        Map<Object, Object> map = createConcreteMap(className);
        for (int i = 0; i < mapSize; i++) {
            Object key = readFieldWithValue(data, byteArrayInputStream, dataInputStream, typeKey, rebuildClass, field, typeName[0]);
            Object value = readFieldWithValue(data, byteArrayInputStream, dataInputStream, typeValue, rebuildClass, field, typeName[1]);
            map.put(key, value);
        }
        return map;
    }

    private List<Object> createConcreteList(String className) {
        switch (className) {
            case "java.util.ArrayList": {
                return new ArrayList<>();
            }
            case "java.util.LinkedList": {
                return new LinkedList<>();
            }
            case "java.util.Stack": {
                return new Stack<>();
            }
        }
        throw new RuntimeException("Illegal name of concrete List " + className);
    }

    private Set<Object> createConcreteSet(String className) {
        switch (className) {
            case "java.util.HashSet":
                return new HashSet<>();
            case "java.util.TreeSet":
                return new TreeSet<>();
        }
        throw new NullPointerException("Illegal name of concrete Set " + className);
    }

    private Map<Object, Object> createConcreteMap(String className) {
        switch (className) {
            case "java.util.TreeMap":
                return new TreeMap<>();
            case "java.util.HashMap":
                return new HashMap<>();
        }
        throw new RuntimeException("Illegal name of concrete Map " + className);
    }

    private void writeFieldWithValue(DataOutputStream dataOutputStream, Object beanField) {
        try {
            if (beanField instanceof Byte) {
                dataOutputStream.writeByte((byte) beanField);
            } else if (beanField instanceof Short) {
                dataOutputStream.writeShort((short) beanField);
            } else if (beanField instanceof Integer) {
                dataOutputStream.writeInt((int) beanField);
            } else if (beanField instanceof Long) {
                dataOutputStream.writeLong((long) beanField);
            } else if (beanField instanceof Float) {
                dataOutputStream.writeFloat((float) beanField);
            } else if (beanField instanceof Double) {
                dataOutputStream.writeDouble((double) beanField);
            } else if (beanField instanceof Instant) {
                Instant instant = (Instant) beanField;
                writeFieldWithValue(dataOutputStream, instant.getEpochSecond());
                writeFieldWithValue(dataOutputStream, (long) instant.getNano());
            } else if (beanField instanceof Character) {
                dataOutputStream.writeChar((Character) beanField);
            } else if (beanField instanceof String) {
                dataOutputStream.writeUTF((String) beanField);
            } else if (beanField instanceof BigDecimal) {
                dataOutputStream.writeUTF(beanField.toString());
            } else if (beanField instanceof Boolean) {
                dataOutputStream.writeBoolean((Boolean) beanField);
            } else if (beanField instanceof List) {
                writeList(dataOutputStream, beanField);
            } else if (beanField instanceof Set) {
                writeSet(dataOutputStream, beanField);
            } else if (beanField instanceof Map) {
                writeMap(dataOutputStream, beanField);
            } else
                dataOutputStream.write(serialize(beanField));
        } catch (IOException e) {
            throw new RuntimeException("Can't write bean field " + beanField.getClass(), e);
        }
    }

    private void writeList(DataOutputStream dataOutputStream, Object beanField) {
        List<Object> list = (List<Object>) beanField;

        try {
            dataOutputStream.writeUTF(beanField.getClass().getName());
            dataOutputStream.writeInt(list.size());
        } catch (IOException e) {
            throw new RuntimeException("Can't write List ", e);
        }

        for (Object value : list) {
            writeFieldWithValue(dataOutputStream, value);
        }
    }

    private void writeSet(DataOutputStream dataOutputStream, Object beanField) {
        Set<Object> set = (Set<Object>) beanField;

        try {
            dataOutputStream.writeUTF(beanField.getClass().getName());
            dataOutputStream.writeInt(set.size());
        } catch (IOException e) {
            throw new RuntimeException("Can't write Set ", e);
        }

        for (Object value : set) {
            writeFieldWithValue(dataOutputStream, value);
        }
    }

    private void writeMap(DataOutputStream dataOutputStream, Object beanField) {
        Map<Object, Object> map = (Map<Object, Object>) beanField;

        try {
            dataOutputStream.writeUTF(beanField.getClass().getName());
            dataOutputStream.writeInt(map.size());
        } catch (IOException e) {
            throw new RuntimeException("Can't write Map ", e);
        }

        for (Map.Entry<Object, Object> item : map.entrySet()) {
            writeFieldWithValue(dataOutputStream, item.getKey());
            writeFieldWithValue(dataOutputStream, item.getValue());
        }
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

    private void checkNullSizeAndReference(byte[] data) {
        if (data == null || data.length == 0) {
            throw new NullPointerException();
        } else {
            LOGGER.info("Byte array isn't empty.");
        }
    }
}