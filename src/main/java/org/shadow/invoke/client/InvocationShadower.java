package org.shadow.invoke.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.shadow.invoke.core.FieldFilter;
import org.shadow.invoke.core.InvocationCache;

import java.lang.reflect.Method;
import java.util.*;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class InvocationShadower extends InvocationRecorder {
    private final List<FieldFilter> ignoredFields;

    protected InvocationShadower(Object instance, List<FieldFilter> redactedFields, List<FieldFilter> ignoredFields) {
        super(instance, redactedFields);
        this.ignoredFields = ignoredFields;
    }

    public static InvocationShadower shadow(Object shadowedInstance) {
        if(shadowedInstance == null) {
            throw new IllegalArgumentException("Can't create a shadowing invocation from a null instance.");
        }
        return new InvocationShadower(shadowedInstance, new ArrayList<>(), new ArrayList<>());
    }

    public InvocationShadower ignoring(FieldFilter... filters) {
        if(filters != null && filters.length > 0) {
            for(FieldFilter f : filters) {
                this.ignore(f);
            }
        } else {
            String message = "Bad ignore filters passed to shadow invocation for %s: %s";
            String className = this.getOriginalInstance().getClass().getSimpleName();
            log.warn(String.format(message, className, Arrays.toString(filters)));
        }
        return this;
    }

    public InvocationShadower ignore(FieldFilter filter) {
        if(filter != null && filter.isValid()) {
            this.ignoredFields.add(filter);
        } else {
            String message = "Bad ignoring field filter passed to shadow invocation for %s: %s";
            String className = this.getOriginalInstance().getClass().getSimpleName();
            log.warn(String.format(message, className, filter));
        }
        return this;
    }

    @Override
    protected void startNewRecording(Object output, Method method, Object[] inputs) {
        InvocationCache.INSTANCE.createAndSave(
                inputs, output, method,
                this.getMaxObjectGraphDepth(),
                this.getRedactedFields(),
                this.ignoredFields);
    }
}
