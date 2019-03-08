package com.exactpro.task.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * This is a second JavaBean example. Project Lombok helps to create a Bean.
 * If you remove the comment line, program finishes with {@link com.exactpro.task.exceptions.CyclicExceptionInCollections}.
 *
 * @author Max
 * @version 1.0
 * @see lombok.Data
 * @see lombok.AllArgsConstructor
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecondBeanExample implements Serializable {
    private Long aLong;
    private Instant instant;
    //private Set<SecondBeanExample> set;
}
