package org.shadow.invocation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.QueueUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.lang.reflect.Method;
import java.util.*;

@Data
@Slf4j
public class Recording {
    // TODO: Make the size configurable
    public static final Queue<Recording> QUEUE = QueueUtils.synchronizedQueue(new CircularFifoQueue<>(1024));
    private final String invocationKey;
    private final Object[] referenceArguments;
    private final Object referenceResult;
    private final Object[] evaluatedArguments;
    private final Object evaluatedResult;

    public Recording(Object invocationTarget, Method invokedMethod, Object[] referenceArguments, Object referenceResult,
                     Object[] evaluatedArguments, Object evaluatedResult) {
        this.invocationKey = invocationTarget.getClass().getCanonicalName() + "." + invokedMethod.getName();
        this.referenceArguments = referenceArguments;
        this.referenceResult = referenceResult;
        this.evaluatedArguments = evaluatedArguments;
        this.evaluatedResult = evaluatedResult;
    }
}
