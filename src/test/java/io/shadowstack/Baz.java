package io.shadowstack;

import lombok.*;
import io.shadowstack.filters.Noise;
import io.shadowstack.filters.Secret;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Baz {
    private String title;
    private double salary;
    @Secret
    private Float height;
    @Noise
    private Long id;
    private Map<TimeUnit, Title> taskTime;
}
