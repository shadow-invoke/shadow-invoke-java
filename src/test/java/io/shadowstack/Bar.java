package io.shadowstack;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Bar {
    public String doSomethingShadowed(Foo f) {
        return f.getFirstName() + " " + f.getLastName();
    }

}
