package org.shadow.invoke;

public class Bar {
    public String doSomethingShadowed(Foo f) {
        return f.getFirstName() + " " + f.getLastName();
    }

    public String doSomethingNotShadowed(Foo f) {
        return Integer.toString(f.getAge());
    }
}
