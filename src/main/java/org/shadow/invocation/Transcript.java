package org.shadow.invocation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Data
@Slf4j
public class Transcript {
    private String schemaVersion;
    private final String invocationKey;
    private final Object[] invocationArguments;
    private final Object invocationResult;

    public Transcript(Object invocationTarget, Method invokedMethod, Object[] invocationArguments, Object invocationResult) {
        this.invocationKey = invocationTarget.getClass().getCanonicalName() + "." + invokedMethod.getName();
        this.invocationArguments = invocationArguments;
        this.invocationResult = invocationResult;
    }
}
