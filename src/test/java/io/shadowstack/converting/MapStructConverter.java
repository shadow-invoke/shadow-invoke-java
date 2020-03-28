package io.shadowstack.converting;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import io.shadowstack.Foo;

@Mapper
public interface MapStructConverter extends Converter<Foo, Foo2> {
    MapStructConverter INSTANCE = Mappers.getMapper(MapStructConverter.class);

    @Mapping(target = "first", source = "firstName")
    @Mapping(target = "last", source = "lastName")
    Foo2 convert(Foo from);
}
