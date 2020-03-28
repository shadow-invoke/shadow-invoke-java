package io.shadowstack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import io.shadowstack.filtering.Noise;
import io.shadowstack.filtering.Secret;

import java.time.LocalDateTime;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Foo {
    private String firstName;
    @Secret
    private String lastName;
    private int age;
    @Noise
    private LocalDateTime timestamp;
    private Baz baz;
}
