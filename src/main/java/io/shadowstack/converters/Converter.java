package io.shadowstack.converters;

public interface Converter<F, T> {
    T convert(F from);
}
