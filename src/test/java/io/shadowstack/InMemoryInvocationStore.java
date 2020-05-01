package io.shadowstack;

import io.shadowstack.candidates.InvocationSource;
import io.shadowstack.exceptions.InvocationSinkException;
import io.shadowstack.exceptions.InvocationSourceException;
import io.shadowstack.incumbents.InvocationSink;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;

/**
 * A InvocationSink for unit testing that saves to an in-memory cache.
 */
@Slf4j
public class InMemoryInvocationStore extends InvocationSink implements InvocationSource {
    // Context GUID -> Key Hash -> Ordered Recordings
    private final Map<String, Map<String, Queue<Invocation>>> CONTEXT_TO_KEY_TO_RECORDINGS = new HashMap<>();
    private final Function<List<Invocation>, Boolean> callback;

    public InMemoryInvocationStore(Function<List<Invocation>, Boolean> callback) {
        this.callback = callback;
    }

    @Override
    public void record(List<Invocation> invocations) throws InvocationSinkException {
        if(invocations != null && !invocations.isEmpty()) {
            for(Invocation invocation : invocations) {
                String contextGuid = invocation.getInvocationContext().getContextId();
                String keyHash = invocation.getInvocationKey().getInvocationHash();
                CONTEXT_TO_KEY_TO_RECORDINGS.putIfAbsent(contextGuid, new HashMap<>());
                CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).putIfAbsent(keyHash, new LinkedList<>());
                CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).get(keyHash).offer(invocation);
            }
        }
        if(this.callback != null) {
            this.callback.apply(invocations);
        }
    }

    @Override
    public Invocation retrieve(InvocationKey key, InvocationContext context) throws InvocationSourceException {
        if(key == null || context == null) {
            String msg = String.format("Bad context (%s) or key (%s)", key, context);
            throw new InvocationSourceException(msg);
        }
        String contextGuid = context.getContextId();
        if(CONTEXT_TO_KEY_TO_RECORDINGS.containsKey(contextGuid) &&
           CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).containsKey(key.getInvocationHash())) {
            // Pops it off the queue and returns. Next call will get next instance.
            return CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).get(key.getInvocationHash()).poll();
        }
        return null;
    }
}
