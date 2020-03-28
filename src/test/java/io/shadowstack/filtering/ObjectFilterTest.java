package io.shadowstack.filtering;

import io.shadowstack.DefaultValue;
import io.shadowstack.Fluently;
import org.junit.Test;
import io.shadowstack.BaseTest;
import io.shadowstack.Baz;
import io.shadowstack.Foo;

import static org.junit.Assert.assertEquals;

public class ObjectFilterTest extends BaseTest {
    @Test
    public void testToObjectDepth() {
        ObjectFilter filter = Fluently.filter(
                Fluently.noise().from(Foo.class),
                Fluently.secrets().from(Foo.class),
                Fluently.noise().from(Baz.class),
                Fluently.secrets().from(Baz.class)
        ).toObjectDepth(1);
        Foo filtered = (Foo)filter.filterAsEvaluatedCopy(foo);
        assertEquals(foo.getFirstName(), filtered.getFirstName());
        assertEquals(DefaultValue.of(String.class), filtered.getLastName());
        assertEquals(foo.getBaz().getTitle(), filtered.getBaz().getTitle());
        // Fields that should be filtered
        assertEquals(DefaultValue.of(Long.class), filtered.getBaz().getId());
        assertEquals(DefaultValue.of(Float.class), filtered.getBaz().getHeight());
        filter.toObjectDepth(0);
        filtered = (Foo)filter.filterAsEvaluatedCopy(foo);
        assertEquals(foo.getFirstName(), filtered.getFirstName());
        assertEquals(DefaultValue.of(String.class), filtered.getLastName());
        // Fields that should no longer be filtered
        assertEquals(foo.getBaz().getId(), filtered.getBaz().getId());
        assertEquals(foo.getBaz().getHeight(), filtered.getBaz().getHeight());
    }
}
