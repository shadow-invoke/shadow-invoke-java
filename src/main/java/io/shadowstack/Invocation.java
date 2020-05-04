package io.shadowstack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;

@Data
@Slf4j
@ToString
@EqualsAndHashCode
public class Invocation {
    private final InvocationKey invocationKey;
    private final InvocationContext invocationContext;
    /**
     * Reference objects contain as much unfiltered data as permitted by the source.
     * For example, a field marked @Secret will be redacted from a reference object,
     * but a field marked @Noise will not. A reference object, as the name suggests,
     * is used to examine the state of an argument or result; not to evaluate its
     * correctness compared to another object.
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

    public Invocation(Method invokedMethod, InvocationContext invocationContext, Object[] referenceArguments,
                      Object referenceResult, Object[] evaluatedArguments, Object evaluatedResult) {
        this.invocationKey = new InvocationKey(invokedMethod, evaluatedArguments);
        this.invocationContext = invocationContext;
        this.referenceArguments = referenceArguments;
        this.referenceResult = referenceResult;
        this.evaluatedArguments = evaluatedArguments;
        this.evaluatedResult = evaluatedResult;
    }

    @JsonIgnore
    public boolean isValid() {
        return this.invocationContext != null && this.invocationContext.isValid() &&
               this.invocationKey != null && this.invocationKey.isValid() &&
               this.evaluatedResult != null && this.referenceResult != null &&
               this.evaluatedArguments != null && this.referenceArguments != null;
    }
}
