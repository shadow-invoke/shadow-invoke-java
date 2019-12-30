package org.shadow.invoke.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.shadow.invoke.core.FieldFilter;
import org.shadow.invoke.core.Recordings;
import java.lang.reflect.Method;
import java.util.*;

@Data
@Slf4j
@EqualsAndHashCode
public class RecordingInvocation implements MethodInterceptor {
    private final Object originalInstance;
    private final List<FieldFilter> redactedFields;
    private int maxObjectGraphDepth = 10;

    protected RecordingInvocation() {
        this(null, new ArrayList<>());
    }

    protected RecordingInvocation(Object instance, List<FieldFilter> redactedFields) {
        this.originalInstance = instance;
        this.redactedFields = redactedFields;
    }

    public static RecordingInvocation record(Object recordedInstance) {
        if(recordedInstance == null) {
            throw new IllegalArgumentException("Can't create a recording invocation from a null instance.");
        }
        return new RecordingInvocation(recordedInstance, new ArrayList<>());
    }

    public RecordingInvocation redacting(FieldFilter... filters) {
        if(filters != null && filters.length > 0) {
            for(FieldFilter f : filters) {
                this.redact(f);
            }
        } else {
            String message = "Bad redact filters passed to recording invocation for %s: %s";
            String className = this.originalInstance.getClass().getSimpleName();
            String filtersString = Arrays.toString(filters);
            log.warn(String.format(message, className, filtersString));
        }
        return this;
    }

    public RecordingInvocation redact(FieldFilter filter) {
        if(filter != null && filter.isValid()) {
            this.redactedFields.add(filter);
        } else {
            String message = "Bad redacting field filter passed to shadow invocation for %s: %s";
            String className = "null";
            if(this.originalInstance != null && this.originalInstance.getClass() != null) {
                className = this.originalInstance.getClass().getSimpleName();
            }
            log.warn(String.format(message, className, filter));
        }
        return this;
    }

    public RecordingInvocation toObjectGraphDepth(int depth) {
        this.maxObjectGraphDepth = depth;
        return this;
    }

    public <T> T invoke(Class<T> cls) {
        if(cls == null || !cls.isInstance(this.originalInstance)) {
            String message = "Invalid combination of class %s and original instance %s. Returning null.";
            String className = (cls != null)? cls.getSimpleName() : "null";
            log.warn(String.format(message, className, this.originalInstance.getClass().getSimpleName()));
            return null;
        }
        return (T)Enhancer.create(cls, this);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Object result = method.invoke(this.originalInstance, args);
        try {
            startNewRecording(result, method, args);
        } catch(Throwable t) {
            String message = "While intercepting recorded invocation. Method=%s, Args=%s, Object=%s.";
            String passed = Arrays.toString(args);
            String className = this.originalInstance.getClass().getSimpleName();
            log.error(String.format(message, method.getName(), passed, className), t);
        }
        return result;
    }

    protected void startNewRecording(Object output, Method method, Object[] inputs) {
        Recordings.INSTANCE.createAndSave(
                inputs, output, method,
                this.maxObjectGraphDepth,
                this.redactedFields,
                new ArrayList<>()
        );
    }
}
