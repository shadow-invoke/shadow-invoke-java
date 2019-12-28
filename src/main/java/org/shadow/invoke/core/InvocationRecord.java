package org.shadow.invoke.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.reflect.Method;
import java.util.*;

@Data
@ToString
@EqualsAndHashCode
public class InvocationRecord {
    /**
     * Fields, by class, which are excluded from consideration. They will either
     * be ignored - in which case the values will still be included - or they
     * will be redacted - in which case the values will have been set to
     * default values.
     */
    private final Map<Class<?>, Set<String>> excludedFields = new HashMap<>();
    private final List<Object> inputs = new ArrayList<>();
    private Object result;
    private Method method;
    private String guid;
}
