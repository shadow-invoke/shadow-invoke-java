package io.shadowstack;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import java.util.UUID;

@Data
@ToString
public class InvocationContext implements AutoCloseable {
    private static final ThreadLocal<String> CURRENT_INVOCATION_ID = new ThreadLocal<>();
    private final String contextId;
    private transient final boolean didSetInvocationId;

    public InvocationContext(String contextId) {
        this.contextId = contextId;
        CURRENT_INVOCATION_ID.set(this.contextId);
        this.didSetInvocationId = true;
    }

    @JsonIgnore
    public boolean isValid() {
        return this.contextId != null && this.contextId.length() > 0;
    }

    public InvocationContext() {
        if(CURRENT_INVOCATION_ID.get() == null) { // this is the first context instantiation in the call chain
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