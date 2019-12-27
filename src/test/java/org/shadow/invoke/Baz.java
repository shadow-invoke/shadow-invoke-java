package org.shadow.invoke;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class Baz {
    private String title;
    private double salary;
    private int id;
}
