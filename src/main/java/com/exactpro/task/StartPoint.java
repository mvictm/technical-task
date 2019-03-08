package com.exactpro.task;

import com.exactpro.task.example.FirstBeanExample;
import com.exactpro.task.example.SecondBeanExample;
import com.exactpro.task.logic.SuperEncoderImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StartPoint {
    public static void main(String[] args) {
        SuperEncoderImpl encoder = new SuperEncoderImpl();

        List<SecondBeanExample> list = new ArrayList<>();
        list.add(new SecondBeanExample());

        SecondBeanExample secondBeanExample = new SecondBeanExample(123456789L, Instant.now());
        FirstBeanExample firstBeanExample = new FirstBeanExample("S", new BigDecimal("0.1499999"), 123, list);

        byte[] dataForSecondObject = encoder.serialize(secondBeanExample);
        byte[] dataForFirstObject = encoder.serialize(firstBeanExample);

        SecondBeanExample returnSecondObject = (SecondBeanExample) encoder.deserialize(dataForSecondObject);
        System.out.println(returnSecondObject.getInstant() + " " + returnSecondObject.getALong());

        FirstBeanExample returnFirstObject = (FirstBeanExample) encoder.deserialize(dataForFirstObject);
        System.out.println(returnFirstObject.getInteger() + " " + returnFirstObject.getBigDecimal() + " " + returnFirstObject.getString() + " " + returnFirstObject.getList().size());
    }
}
