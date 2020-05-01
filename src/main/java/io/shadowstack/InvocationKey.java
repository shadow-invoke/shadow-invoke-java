package io.shadowstack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.lang.reflect.Method;

/**
 * A unique key for a particular invocation recording.
 * Recordings are considered equivalent if their target
 * class, method, and evaluated/filtered inputs are equal.
 */
@Data
@Slf4j
@ToString
public class InvocationKey {
    private final static ObjectMapper MAPPER = new ObjectMapper();
    private final String targetMethodName;
    private final String targetClassName;
    private final String invocationHash;

    public InvocationKey(Method invokedMethod, Object[] evaluatedArguments) {
        this.targetMethodName = invokedMethod.getName();
        this.targetClassName = invokedMethod.getDeclaringClass().getCanonicalName();
        this.invocationHash = generateHash(this.targetMethodName, this.targetClassName, evaluatedArguments);
    }

    public boolean isValid() {
        return this.targetMethodName != null && this.targetMethodName.length() > 0 &&
               this.targetClassName != null && this.targetClassName.length() > 0;
    }

    /**
     * Generate a unique key from the SHA256 hash of all invocation key fields.
     */
    private static String generateHash(String targetMethodName, String targetClassName, Object[] evaluatedArguments) {
        StringBuilder builder = new StringBuilder();
        builder.append(targetClassName);
        builder.append(',');
        builder.append(targetMethodName);
        for(Object obj : evaluatedArguments) {
            try {
                builder.append(',');
                builder.append(MAPPER.writeValueAsString(obj));
            } catch (JsonProcessingException e) {
                log.error(String.format("While serializing %s. Returned hash will be null.", obj), e);
                return null;
            }
        }
        return DigestUtils.sha256Hex(builder.toString());
    }
}