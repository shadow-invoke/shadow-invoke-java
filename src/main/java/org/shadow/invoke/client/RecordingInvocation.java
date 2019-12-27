package org.shadow.invoke.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.ClassUtils;
import org.shadow.invoke.core.DefaultValues;
import org.shadow.invoke.core.FieldFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Slf4j
public class RecordingInvocation implements MethodInterceptor {
    private final Object originalInstance;
    private final Map<Class<?>, Set<String>> redactedFields;

    protected RecordingInvocation() {
        this(null, new HashMap<>());
    }

    protected RecordingInvocation(Object instance, Map<Class<?>, Set<String>> redactedFields) {
        this.originalInstance = instance;
        this.redactedFields = redactedFields;
    }

    public static RecordingInvocation record(Object recordedInstance) {
        if(recordedInstance == null) {
            throw new IllegalArgumentException("Can't create a recording invocation from a null instance.");
        }
        return new RecordingInvocation(recordedInstance, new HashMap<>());
    }

    public RecordingInvocation redacting(FieldFilter... filters) {
        if(filters != null && filters.length > 0) {
            for(FieldFilter f : filters) {
                this.redact(f);
            }
        } else {
            String message = "Bad redact filters passed to recording invocation for {}: {}";
            String className = this.originalInstance.getClass().getSimpleName();
            log.warn(String.format(message, className, filters));
        }
        return this;
    }

    public RecordingInvocation redact(FieldFilter filter) {
        if(filter != null && filter.isValid()) {
            this.redactedFields.put(filter.getFilteredClass(), filter.getFilteredFields());
        } else {
            String message = "Bad redacting field filter passed to shadow invocation for {}: {}";
            String className = this.originalInstance.getClass().getSimpleName();
            log.warn(String.format(message, className, filter));
        }
        return this;
    }

    public <T> T invoke(Class<T> cls) {
        if(cls == null || this.originalInstance == null || !cls.isInstance(this.originalInstance)) {
            String message = "Invalid combination of class {} and original instance {}. Returning null.";
            log.warn(String.format(message, cls, this.originalInstance));
            return null;
        }
        T s = (T)Enhancer.create(cls, this);
        return s;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String className = this.originalInstance.getClass().getSimpleName();
        log.info("called " + method.getName() + " in " + className + " with the following arguments:");
        Object result = null;
        try {
            for (Object obj : args) {
                StringBuilder builder = new StringBuilder();
                this.formJson(builder, obj, 0);
                log.info(builder.toString());
            }
            result = method.invoke(this.originalInstance, args);
            log.info("...and returning:");
            StringBuilder builder = new StringBuilder();
            this.formJson(builder, result, 0);
            log.info(builder.toString());
        } catch(Throwable t) {
            log.error("While intercepting", t);
        }
        return result;
    }

    protected void formJson(StringBuilder builder, Object obj, int level) throws IllegalAccessException {
        if(obj != null && level < 5) {
            builder.append('{');
            Class<?> cls = obj.getClass();
            for(Field fld : cls.getDeclaredFields()) {
                if(fld.getDeclaringClass().equals(cls)) {
                    fld.setAccessible(true);
                    builder.append("\"");
                    builder.append(fld.getName());
                    builder.append("\":");
                    Set<String> redactions = this.redactedFields.get(cls);
                    if(redactions != null && redactions.contains(fld.getName())) {
                        log.info("Redacting " + fld.getName());
                        DefaultValues.appendFromField(fld, builder);
                    } else if(ClassUtils.isPrimitiveOrWrapper(fld.getType())) {
                        builder.append(fld.get(obj));
                    } else if(fld.getType().equals(String.class)) {
                        builder.append("\"\"");
                    } else {
                        formJson(builder, fld.get(obj), level + 1);
                    }
                }
            }
            builder.append('}');
        }
    }

    protected boolean shouldSkip(Field fld, Object obj) {
        if(obj == null || fld == null) return true;
        Class<?> cls = obj.getClass();
        if(!cls.equals(fld.getDeclaringClass())) return true;
        Set<String> redactions = this.redactedFields.get(cls);
        if(redactions == null || redactions.isEmpty()) return false;
        return redactions.contains(fld.getName());
    }
}
