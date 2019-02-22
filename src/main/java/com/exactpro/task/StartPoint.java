package com.exactpro.task;

import com.exactpro.task.example.FirstBeanExample;
import com.exactpro.task.example.SecondBeanExample;
import com.exactpro.task.logic.SuperEncoderImpl;

import java.math.BigDecimal;
import java.time.Instant;

public class StartPoint {
    public static void main(String[] args) {
        SuperEncoderImpl encoder = new SuperEncoderImpl();
        FirstBeanExample firstBeanExample = new FirstBeanExample("S", new BigDecimal("0.1499999"), 123);
        SecondBeanExample secondBeanExample = new SecondBeanExample(123456789L, Instant.now());

        byte[] dataForFirstObject = encoder.serialize(firstBeanExample);
        byte[] dataForSecondObject = encoder.serialize(secondBeanExample);

        FirstBeanExample returnFirstObject = (FirstBeanExample) encoder.deserialize(dataForFirstObject);
        System.out.println(returnFirstObject.getInteger() + " " + returnFirstObject.getBigDecimal() + " " + returnFirstObject.getString());

        SecondBeanExample returnSecondObject = (SecondBeanExample) encoder.deserialize(dataForSecondObject);
        System.out.println(returnSecondObject.getInstant() + " " + returnSecondObject.getALong());
    }
}
