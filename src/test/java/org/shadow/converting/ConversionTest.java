package org.shadow.converting;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.shadow.BaseTest;
import org.shadow.Foo;

import static org.shadow.Fluently.from;

public class ConversionTest extends BaseTest {
    @Test
    public void testOrikaConverter() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.classMap(Foo.class, Foo2.class)
                .field("firstName", "first")
                .field("lastName", "last")
                .byDefault()
                .register();
        BoundMapperFacade<Foo, Foo2> orikaMapper = mapperFactory.getMapperFacade(Foo.class, Foo2.class);
        Conversion<Foo, Foo2> conversion = from(Foo.class).to(Foo2.class).with(new OrikaConverter<>(orikaMapper));
        Foo2 f2 = conversion.convert(this.foo);
        threadAssertEquals(f2.getAge(), this.foo.getAge());
        threadAssertEquals(f2.getFirst(), this.foo.getFirstName());
        threadAssertEquals(f2.getLast(), this.foo.getLastName());
        // Differing serialization formats can do different things with trailing zeros
        threadAssertEquals(
                StringUtils.stripEnd(f2.getTimestamp(), "0"),
                StringUtils.stripEnd(this.foo.getTimestamp().toString(), "0")
        );
        threadAssertEquals(f2.getBaz(), this.foo.getBaz());
    }

    @Test
    public void testMapStructConverter() {
        Conversion<Foo, Foo2> conversion = from(Foo.class).to(Foo2.class).with(MapStructConverter.INSTANCE);
        Foo2 f2 = conversion.convert(this.foo);
        threadAssertEquals(f2.getAge(), this.foo.getAge());
        threadAssertEquals(f2.getFirst(), this.foo.getFirstName());
        threadAssertEquals(f2.getLast(), this.foo.getLastName());
        // Differing serialization formats can do different things with trailing zeros
        threadAssertEquals(
                StringUtils.stripEnd(f2.getTimestamp(), "0"),
                StringUtils.stripEnd(this.foo.getTimestamp().toString(), "0")
        );
        threadAssertEquals(f2.getBaz(), this.foo.getBaz());
    }
}
