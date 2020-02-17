package org.shadow.filtering;

import org.junit.Test;
import org.shadow.BaseTest;
import org.shadow.Baz;
import org.shadow.DefaultValue;
import org.shadow.Foo;

import static org.junit.Assert.assertEquals;
import static org.shadow.Fluently.*;

public class ObjectFilterTest extends BaseTest {
    @Test
    public void testToObjectDepth() {
        ObjectFilter filter = filter(
                noise().from(Foo.class),
                secrets().from(Foo.class),
                noise().from(Baz.class),
                secrets().from(Baz.class)
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
