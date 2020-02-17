package org.shadow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.shadow.filtering.Noise;
import org.shadow.filtering.Secret;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Baz {
    private String title;
    private double salary;
    @Secret
    private Float height;
    @Noise
    private Long id;
    private Map<TimeUnit, Task> taskTime;
}
