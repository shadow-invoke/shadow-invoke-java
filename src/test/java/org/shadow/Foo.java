package org.shadow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.shadow.field.Noise;
import org.shadow.field.Secret;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class Foo {
    private String firstName;
    @Secret
    private String lastName;
    private int age;
    @Noise
    private LocalDateTime timestamp;
    private Baz baz;
}
