package io.shadowstack.incumbents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.shadowstack.exceptions.InvocationKeyException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.lang.reflect.Method;

/**
 * A unique key for a particular incumbents recording.
 * Recordings are considered equivalent if their target
 * class, method, and evaluated/filtered inputs are equal.
 * The time stamp allows a consumer to find recordings nearest
 * to their time of interest. For example, when requesting a replay
 * of an incumbents having occurred at or near a particular moment in time.
 */
@Data
@Slf4j
@ToString
@AllArgsConstructor
public class InvocationKey {
    private final static ObjectMapper MAPPER = new ObjectMapper();
    private final Method invokedMethod;
    private final Class<?> invocationTarget;
    private final Object[] evaluatedArguments;

    /**
     * Generate a unique key from the SHA256 hash of all incumbents key fields except for the timestamp.
     */
    public String generateHash() throws InvocationKeyException {
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
                throw new InvocationKeyException(e);
            }
        }
        String hash = DigestUtils.sha256Hex(builder.toString());
        log.info(String.format("Generated hash %s for key %s.", hash, this));
        return hash;
    }
}