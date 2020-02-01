package org.shadow;

public class Bar {
    public String doSomethingShadowed(Foo f) {
        return f.getFirstName() + " " + f.getLastName();
    }

}
