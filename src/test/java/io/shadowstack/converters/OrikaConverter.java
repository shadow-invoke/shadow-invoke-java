package io.shadowstack.converters;

import ma.glasnost.orika.BoundMapperFacade;

public class OrikaConverter<F, T> implements Converter<F, T> {
    private final BoundMapperFacade<F, T> orikaMapper;

    public OrikaConverter(BoundMapperFacade<F, T> orikaMapper) {
        this.orikaMapper = orikaMapper;
    }

    @Override
    public T convert(F from) {
        return this.orikaMapper.map(from);
    }
}
