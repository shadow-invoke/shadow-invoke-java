package org.shadow.invoke.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;

@Data
@Slf4j
@ToString
@EqualsAndHashCode
public class InvocationRecord {
    // Fields excluded for consideration which are NOT transmitted or stored
    private final Map<Class<?>, Set<String>> redactedFields;
    // Fields excluded for consideration which ARE transmitted and stored
    private final Map<Class<?>, Set<String>> ignoredFields;
    private final List<Object> inputs;
    private final Object output;
    private final Method method;

    public InvocationRecord(List<FieldFilter> redacted, List<FieldFilter> ignored, List<Object> inputs,
                            Object output, Method method) {
        this.redactedFields = new HashMap<>();
        populateMap(this.redactedFields, redacted);
        this.ignoredFields = new HashMap<>();
        populateMap(this.ignoredFields, ignored);
        // TODO: Add field redaction
        this.inputs = inputs;
        this.output = output;
        this.method = method;
    }

    private static void populateMap(final Map<Class<?>, Set<String>> map, final List<FieldFilter> filters) {
        if(filters == null) {
            log.warn("Tried to populate an invocation recording with a null filter list.");
            return;
        }
        filters.forEach(f -> {
            if(!map.containsKey(f.getFilteredClass())) {
                map.put(f.getFilteredClass(), f.getFilteredFields());
            } else {
                map.get(f.getFilteredClass()).addAll(f.getFilteredFields());
            }
        });
    }
}
