package io.shadowstack.invocation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import io.shadowstack.exception.RecordingException;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

@Data
@Slf4j
@ToString
@EqualsAndHashCode
public class Recording {
    private final InvocationKey invocationKey;
    private final InvocationContext invocationContext;
    private final Object[] referenceArguments;
    private final Object referenceResult;
    private final Object[] evaluatedArguments;
    private final Object evaluatedResult;

    public Recording(Class<?> invocationTarget, Method invokedMethod, InvocationContext invocationContext,
                     Object[] referenceArguments, Object referenceResult, Object[] evaluatedArguments, Object evaluatedResult) {
        this.invocationKey = new InvocationKey(invokedMethod, invocationTarget, evaluatedArguments);
        this.invocationContext = invocationContext;
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
    @Slf4j
    @ToString
    @AllArgsConstructor
    public static class InvocationKey {
        private final static ObjectMapper MAPPER = new ObjectMapper();
        private final Method invokedMethod;
        private final Class<?> invocationTarget;
        private final Object[] evaluatedArguments;

        /**
         * Generate a unique key from the SHA256 hash of all invocation key fields except for the timestamp.
         */
        public String generateHash() throws RecordingException {
            StringBuilder builder = new StringBuilder();
            builder.append(this.getInvocationTarget().getCanonicalName());
            builder.append(',');
            builder.append(this.getInvokedMethod().getName());
            for(Object obj : this.getEvaluatedArguments()) {
                try {
                    builder.append(',');
                    builder.append(MAPPER.writeValueAsString(obj));
                } catch (JsonProcessingException e) {
                    log.error(String.format("While serializing %s", obj), e);
                    throw new RecordingException(e);
                }
            }
            String hash = DigestUtils.sha256Hex(builder.toString());
            log.info(String.format("Generated hash %s for key %s.", hash, this));
            return hash;
        }
    }

    @Data
    @ToString
    public static class InvocationContext implements AutoCloseable {
        private static final ThreadLocal<String> CURRENT_INVOCATION_ID = new ThreadLocal<>();
        private final Instant timeStamp;
        private final String contextId;
        private transient final boolean didSetInvocationId;

        public InvocationContext(String contextId) {
            this.timeStamp = Instant.now();
            this.contextId = contextId;
            CURRENT_INVOCATION_ID.set(this.contextId);
            this.didSetInvocationId = true;
        }

        public InvocationContext() {
            this.timeStamp = Instant.now();
            if(CURRENT_INVOCATION_ID.get() == null) { // this is the context in the call chain
                CURRENT_INVOCATION_ID.set(UUID.randomUUID().toString());
                this.didSetInvocationId = true;
            }
            else {
                this.didSetInvocationId = false;
            }
            this.contextId = CURRENT_INVOCATION_ID.get();
        }

        @Override
        public void close() throws Exception {
            if(this.didSetInvocationId) {
                CURRENT_INVOCATION_ID.set(null);
            }
        }
    }
}
