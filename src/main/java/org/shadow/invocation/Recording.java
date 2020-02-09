package org.shadow.invocation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.lang.reflect.Method;

@Data
@Slf4j
@ToString
public class Recording {
    private final static ObjectMapper MAPPER = new ObjectMapper();
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

    public String generateUniqueKey() throws JsonProcessingException {
        return DigestUtils.sha3_256Hex(MAPPER.writeValueAsString(this.invocationKey));
    }

    /**
     * A unique key for a particular invocation recording.
     * Recordings are considered equivalent if their target
     * class, method, and relevant/evaluated inputs are equal.
     */
    @AllArgsConstructor
    private static class InvocationKey {
        private Method invokedMethod;
        private Object invocationTarget;
        private Object[] evaluatedArguments;
    }
}
