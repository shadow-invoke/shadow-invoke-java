package org.shadow.invoke.core;

import org.junit.Test;
import org.shadow.invoke.Baz;

import java.util.Set;

import static org.junit.Assert.*;

public class FieldFilterTest {

    @Test
    public void from() {
        FieldFilter filter = FieldFilter.from(Baz.class).fields("title", "salary");
        assertTrue(filter.getFilteredClass().equals(Baz.class));
        assertTrue(filter.getFilteredFields().contains("title"));
        assertTrue(filter.getFilteredFields().contains("salary"));
        assertFalse(filter.getFilteredFields().contains("id"));
    }
}