package org.shadow.invoke.core.filtering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterBuilder {
    // filtering(
    //      noise().from(Foo.class).named("timestamp", "firstName"),
    //      secrets().from(Baz.class).annotated(Secret.class)
    // )
    private Map<Class<?>, Map<FilterReason, List<String>>> filters = new HashMap<>();
}
