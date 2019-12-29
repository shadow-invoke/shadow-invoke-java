package org.shadow.invoke.core;

import com.rits.cloning.Cloner;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Data
@Slf4j
@ToString
@EqualsAndHashCode
public class InvocationRecord {
    private static final int MAX_LEVELS = 10; // TODO: Make this configurable
    private static final Cloner CLONER = new Cloner();
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
        this.inputs = new ArrayList<>(inputs.size());
        inputs.forEach(i -> {
            Object copy = CLONER.deepClone(i);
            this.redactFields(copy, 0);
            this.inputs.add(copy);
        });
        Object copy = CLONER.deepClone(output);
        this.redactFields(copy, 0);
        this.output = copy;
        this.method = method;
    }

    private void redactFields(Object obj, int level) {
        if(obj == null) return;
        if(level > MAX_LEVELS) {
            String message = "Recursive redacting exceeded limit of %d. Current member class: %s.";
            log.warn(String.format(message, MAX_LEVELS, obj.getClass().getSimpleName()));
            return;
        }
        Class<?> cls = obj.getClass();
        for(Field fld : cls.getDeclaredFields()) {
            if(fld.getDeclaringClass().equals(cls)) {
                fld.setAccessible(true);
                Set<String> redactions = this.redactedFields.get(cls);
                if (redactions != null && redactions.contains(fld.getName())) {
                    setMember(obj, RedactedValue.of(fld), fld);
                } else if(!ClassUtils.isPrimitiveOrWrapper(fld.getType())) {
                    redactFields(getMember(obj, fld), level + 1);
                }
            }
        }
    }

    private void setMember(Object parent, Object member, Field field) {
        try {
            field.set(parent, member);
        } catch (IllegalAccessException e) {
            String message = "While setting field {} of {}";
            log.error(String.format(message, field, parent.getClass()), e);
        }
    }

    private Object getMember(Object parent, Field field) {
        try {
            return field.get(parent);
        } catch (IllegalAccessException e) {
            String message = "While getting field {} of {}";
            log.error(String.format(message, field, parent.getClass()), e);
        }
        return null;
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
