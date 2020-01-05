package org.shadow.invoke.core;

import com.rits.cloning.Cloner;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.shadow.Redacted;
import org.shadow.ReflectiveAccess;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Data
@Slf4j
@ToString
@EqualsAndHashCode
public class Invocation {
    private static final Cloner CLONER = new Cloner();
    // Fields excluded for consideration which are NOT transmitted or stored
    private final Map<Class<?>, Set<String>> redactedFields;
    // Fields excluded for consideration which ARE transmitted and stored
    private final Map<Class<?>, Set<String>> ignoredFields;
    private final List<Object> inputs;
    private final Object output;
    private final Method method;
    private final int maxRecursionLevels; // for applying redactions

    public Invocation(List<FieldFilter> redacted, List<FieldFilter> ignored, List<Object> inputs,
                      Object output, Method method, int maxRecursionLevels) {
        this.maxRecursionLevels = maxRecursionLevels;
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
        if(level > this.maxRecursionLevels) {
            if(this.maxRecursionLevels < 1) {
                String message = "Bad value for max recursion levels: %d";
                throw new IllegalArgumentException(String.format(message, this.maxRecursionLevels));
            }
            String message = "Recursive redacting exceeded limit of %d. Current member class: %s.";
            log.warn(String.format(message, this.maxRecursionLevels, obj.getClass().getSimpleName()));
            return;
        }
        Class<?> cls = obj.getClass();
        for(Field fld : cls.getDeclaredFields()) {
            if(fld.getDeclaringClass().equals(cls)) {
                fld.setAccessible(true);
                Set<String> redactions = this.redactedFields.get(cls);
                if (redactions != null && redactions.contains(fld.getName())) {
                    ReflectiveAccess.setMember(obj, Redacted.valueOf(fld.getType()), fld);
                } else if(shouldRecursivelyRedact(fld)) {
                    redactFields(ReflectiveAccess.getMember(obj, fld), level + 1);
                }
            }
        }
    }

    private boolean shouldRecursivelyRedact(Field field) {
        return this.redactedFields.containsKey(field.getType()) && Redacted.shouldRedactMembers(field);
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
