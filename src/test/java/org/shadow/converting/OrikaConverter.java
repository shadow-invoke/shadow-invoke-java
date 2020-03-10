package org.shadow.converting;

import com.google.common.reflect.TypeToken;
import ma.glasnost.orika.BoundMapperFacade;

public class OrikaConverter<F, T> implements Converter<F, T> {
    private final TypeToken<F> fromTypeToken;
    private final TypeToken<T> toTypeToken;
    private final BoundMapperFacade<F, T> orikaMapper;

    public OrikaConverter(BoundMapperFacade<F, T> orikaMapper) {
        this.orikaMapper = orikaMapper;
        fromTypeToken = new TypeToken<F>(orikaMapper.getClass()) {};
        toTypeToken = new TypeToken<T>(orikaMapper.getClass()) {};
    }

    @Override
    public T convert(F from) {
        return this.orikaMapper.map(from);
    }

    @Override
    public TypeToken<F> getFromTypeToken() {
        return this.fromTypeToken;
    }

    @Override
    public TypeToken<T> getToTypeToken() {
        return this.toTypeToken;
    }
}
