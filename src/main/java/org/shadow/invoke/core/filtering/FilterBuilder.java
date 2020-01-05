package org.shadow.invoke.core.filtering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterBuilder {
    // filtering(
    //      noise().from(Foo.class).named("timestamp", "firstName"),
    //      secrets().from(Baz.class).annotated(Secret.class)
    // )
    // client:  class -> fields -> action + isExcluded
    // service: class -> excluded fields
    // mock:    instance + (class -> excluded fields) + mapping -> new I/O
    private Map<Class<?>, Map<FilterReason, List<String>>> filters = new HashMap<>();
}
