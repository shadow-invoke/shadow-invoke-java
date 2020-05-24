package io.shadowstack;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@ToString
@EqualsAndHashCode
public class Bar {
    public String doSomethingShadowed(Foo f) {
        return f.getFirstName() + " " + f.getLastName();
    }

    public String doSomethingBad(Foo f) {
        throw new NotImplementedException();
    }
}
