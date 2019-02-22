package com.exactpro.task.example;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * This is the first JavaBean example. Project Lombok helps to create a Bean.
 * If you remove the comment line, program finishes with {@link com.exactpro.task.exceptions.CyclicException}.
 *
 * @author Max
 * @version 1.0
 * @see lombok.Data
 * @see lombok.AllArgsConstructor
 */

@Data
@AllArgsConstructor
public class FirstBeanExample implements Serializable {
    private String string;
    private BigDecimal bigDecimal;
    private Integer integer;
    //private FirstBeanExample firstBeanExample;
}
