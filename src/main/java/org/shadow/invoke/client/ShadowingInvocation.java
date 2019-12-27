package org.shadow.invoke.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodProxy;
import org.shadow.invoke.core.FieldFilter;
import java.lang.reflect.Method;
import java.util.HashMap;
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
            this.ignoredFields.put(filter.getFilteredClass(), filter.getFilteredFields());
        } else {
            String message = "Bad ignoring field filter passed to shadow invocation for {}: {}";
            String className = this.getOriginalInstance().getClass().getSimpleName();
            log.warn(String.format(message, className, filter));
        }
        return this;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String className = this.getOriginalInstance().getClass().getSimpleName();
        log.info("called " + method.getName() + " in " + className + " with the following arguments:");
        Object result = null;
        try {
            for (Object obj : args) {
                StringBuilder builder = new StringBuilder();
                this.formJson(builder, obj, 0);
                log.info(builder.toString());
            }
            result = method.invoke(this.getOriginalInstance(), args);
            log.info("...and returning:");
            StringBuilder builder = new StringBuilder();
            this.formJson(builder, result, 0);
            log.info(builder.toString());
        } catch(Throwable t) {
            log.error("While intercepting", t);
        }
        return result;
    }
}
