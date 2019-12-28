package org.shadow.invoke.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodProxy;
import org.shadow.invoke.core.FieldFilter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Slf4j
public class ShadowingInvocation extends RecordingInvocation {
    private final Map<Class<?>, Set<String>> ignoredFields;

    protected ShadowingInvocation(Object instance, Map<Class<?>, Set<String>> redactedFields, Map<Class<?>, Set<String>> ignoredFields) {
        super(instance, redactedFields);
        this.ignoredFields = ignoredFields;
    }

    public static ShadowingInvocation shadow(Object shadowedInstance) {
        if(shadowedInstance == null) {
            throw new IllegalArgumentException("Can't create a shadowing invocation from a null instance.");
        }
        return new ShadowingInvocation(shadowedInstance, new HashMap<>(), new HashMap<>());
    }

    public ShadowingInvocation ignoring(FieldFilter... filters) {
        if(filters != null && filters.length > 0) {
            for(FieldFilter f : filters) {
                this.ignore(f);
            }
        } else {
            String message = "Bad ignore filters passed to shadow invocation for {}: {}";
            String className = this.getOriginalInstance().getClass().getSimpleName();
            log.warn(String.format(message, className, filters));
        }
        return this;
    }

    public ShadowingInvocation ignore(FieldFilter filter) {
        if(filter != null && filter.isValid()) {
            if(!this.ignoredFields.containsKey(filter.getFilteredClass())) {
                this.ignoredFields.put(filter.getFilteredClass(), new HashSet<>());
            }
            this.ignoredFields.get(filter.getFilteredClass()).addAll(filter.getFilteredFields());
        } else {
            String message = "Bad ignoring field filter passed to shadow invocation for {}: {}";
            String className = this.getOriginalInstance().getClass().getSimpleName();
            log.warn(String.format(message, className, filter));
        }
        return this;
    }

    @Override
    protected boolean shouldSkip(Field fld, Object obj) {
        if(super.shouldSkip(fld, obj)) return true;
        Set<String> ignores = this.ignoredFields.get(obj.getClass());
        if(ignores == null || ignores.isEmpty()) return false;
        return ignores.contains(fld.getName());
    }
}
