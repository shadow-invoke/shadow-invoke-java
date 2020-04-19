package io.shadowstack.incumbents;

import lombok.Data;
import lombok.ToString;
import java.time.Instant;
import java.util.UUID;

@Data
@ToString
public class InvocationContext implements AutoCloseable {
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