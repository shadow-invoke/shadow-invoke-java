package org.shadow.converting;

import com.google.common.reflect.TypeToken;

@SuppressWarnings("ALL")
public interface Converter<F, T> {
    T convert(F from);

    default public TypeToken<F> getFromTypeToken() {
        return new TypeToken<F>(getClass()) {};
    }

    default public TypeToken<T> getToTypeToken() {
        return new TypeToken<T>(getClass()) {};
    }
}
