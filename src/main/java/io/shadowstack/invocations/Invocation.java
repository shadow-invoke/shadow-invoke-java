package io.shadowstack.invocations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
import java.time.Duration;

@Data
@Slf4j
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Invocation {
    private final InvocationKey invocationKey;
    private final InvocationContext invocationContext;
    /**
     * Reference objects contain as much unfiltered data as permitted by the source.
     * For example, a field marked @Secret will be redacted from a reference object,
     * but a field marked @Noise will not. A reference object, as the name suggests,
     * is used to save the state of an argument or result for later reference; not
     * to evaluate its correctness compared to another object.
     */
    private final Object[] referenceArguments;
    private final Object referenceResult;
    /**
     * Evaluated objects contain only the data needed to compare them with another
     * result, as dictated by the source. For example, fields marked as @Secret
     * and @Noise will both be redacted from an evaluated object.
     */
    private final Object[] evaluatedArguments;
    private final Object evaluatedResult;
    /**
     * Replay will attempt to simulate both the response time of the recorded call,
     * as well as any exceptions thrown by it.
     */
    private final Throwable exceptionThrown;
    private final Duration callDuration;

    public Invocation(Method invokedMethod, InvocationContext invocationContext, Object[] referenceArguments,
                      Object referenceResult, Object[] evaluatedArguments, Object evaluatedResult,
                      Throwable exceptionThrown, Duration callDuration)
    {
        this(new InvocationKey(invokedMethod, evaluatedArguments), invocationContext, referenceArguments,
             referenceResult, evaluatedArguments, evaluatedResult, exceptionThrown, callDuration);
    }

    public Invocation(Method invokedMethod, InvocationContext invocationContext, Object[] referenceArguments,
                      Object referenceResult, Object[] evaluatedArguments, Object evaluatedResult)
    {
        this(new InvocationKey(invokedMethod, evaluatedArguments), invocationContext, referenceArguments,
                referenceResult, evaluatedArguments, evaluatedResult, null, null);
    }

    @JsonIgnore
    public boolean isValid() {
        return this.invocationContext != null && this.invocationContext.isValid() &&
               this.invocationKey != null && this.invocationKey.isValid() &&
               this.evaluatedResult != null && this.referenceResult != null &&
               this.evaluatedArguments != null && this.referenceArguments != null;
    }
}
