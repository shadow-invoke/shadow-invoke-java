package org.shadow;

import org.shadow.field.Filter;

public class Fluently {
    private Fluently() {}

    public static Filter.Builder noise() {
        return new Filter.Builder((obj) -> obj); // ignore and note exclusion
    }

    public static Filter.Builder secret() {
        return new Filter.Builder((obj) -> Redacted.valueOf(obj.getClass())); // redact and note exclusion
    }
}
