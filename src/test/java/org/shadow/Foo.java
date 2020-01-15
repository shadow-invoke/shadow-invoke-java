package org.shadow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class Foo {
    private String firstName;
    private String lastName;
    private int age;
    private LocalDateTime timestamp;
    private Baz baz;
}
