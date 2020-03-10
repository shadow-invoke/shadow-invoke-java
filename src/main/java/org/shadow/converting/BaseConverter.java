package org.shadow.converting;

import com.google.common.reflect.TypeToken;

public abstract class BaseConverter<F, T> implements Converter<F, T> {
    private final TypeToken<F> fromTypeToken = new TypeToken<F>(getClass()) {};
    private final TypeToken<T> toTypeToken = new TypeToken<T>(getClass()) {};

    @Override
    public TypeToken<F> getFromTypeToken() {
        return this.fromTypeToken;
    }

    @Override
    public TypeToken<T> getToTypeToken() {
        return this.toTypeToken;
    }
}
