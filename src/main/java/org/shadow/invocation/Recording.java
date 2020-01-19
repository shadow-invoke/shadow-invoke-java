package org.shadow.invocation;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Data
@Slf4j
@ToString
public class Recording {
    private final String invocationKey;
    private final Object[] referenceArguments;
    private final Object referenceResult;
    private final Object[] evaluatedArguments;
    private final Object evaluatedResult;

    public Recording(Object invocationTarget, String namespace, Method invokedMethod, Object[] referenceArguments,
                     Object referenceResult,Object[] evaluatedArguments, Object evaluatedResult) {
        if(namespace != null) {
            this.invocationKey = namespace + "." +
                                 invocationTarget.getClass().getCanonicalName() + "." +
                                 invokedMethod.getName();
        } else {
            this.invocationKey = invocationTarget.getClass().getCanonicalName() + "." + invokedMethod.getName();
        }
        this.referenceArguments = referenceArguments;
        this.referenceResult = referenceResult;
        this.evaluatedArguments = evaluatedArguments;
        this.evaluatedResult = evaluatedResult;
    }
}
