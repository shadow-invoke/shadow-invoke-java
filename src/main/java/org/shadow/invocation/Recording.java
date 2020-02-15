package org.shadow.invocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.time.Instant;

@Data
@Slf4j
@ToString
@EqualsAndHashCode
public class Recording {
    private final InvocationKey invocationKey;
    private final Object[] referenceArguments;
    private final Object referenceResult;
    private final Object[] evaluatedArguments;
    private final Object evaluatedResult;

    public Recording(Object invocationTarget, Method invokedMethod, Object[] referenceArguments,
                     Object referenceResult, Object[] evaluatedArguments, Object evaluatedResult) {
        this.invocationKey = new InvocationKey(invokedMethod, invocationTarget, evaluatedArguments);
        this.referenceArguments = referenceArguments;
        this.referenceResult = referenceResult;
        this.evaluatedArguments = evaluatedArguments;
        this.evaluatedResult = evaluatedResult;
    }

    /**
     * A unique key for a particular invocation recording.
     * Recordings are considered equivalent if their target
     * class, method, and evaluated/filtered inputs are equal.
     * The time stamp allows a consumer to find recordings nearest
     * to their time of interest. For example, when requesting a replay
     * of an invocation having occurred at or near a particular moment in time.
     */
    @Data
    @ToString
    @AllArgsConstructor
    @EqualsAndHashCode(exclude = {"timestamp"})
    public static class InvocationKey {
        private final Method invokedMethod;
        private final Object invocationTarget;
        private final Object[] evaluatedArguments;
        private final Instant timestamp;

        public InvocationKey(Method invokedMethod, Object invocationTarget, Object[] evaluatedArguments) {
            this.invocationTarget = invocationTarget;
            this.invokedMethod = invokedMethod;
            this.evaluatedArguments = evaluatedArguments;
            this.timestamp = Instant.now();
        }
    }
}
