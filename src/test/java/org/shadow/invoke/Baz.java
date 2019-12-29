package org.shadow.invoke;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class Baz {
    private String title;
    private double salary;
    private Float height;
    private Long id;
    private Map<TimeUnit, Task> taskTime;
}
