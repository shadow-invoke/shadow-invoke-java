package org.shadow.converting;

import lombok.*;
import org.shadow.Baz;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Foo2 {
    private String first;
    private String last;
    private int age;
    private String timestamp;
    private Baz baz;
}
