package org.shadow.converting;

public interface Converter<F, T> {
    T convert(F from);
}
