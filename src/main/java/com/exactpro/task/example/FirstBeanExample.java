package com.exactpro.task.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
@NoArgsConstructor
public class FirstBeanExample implements Serializable {
    private String string;
    private BigDecimal bigDecimal;
    private Integer integer;
    private List<SecondBeanExample> list;
    //private FirstBeanExample firstBeanExample;
}
