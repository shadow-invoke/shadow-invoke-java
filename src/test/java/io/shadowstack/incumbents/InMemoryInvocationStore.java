package io.shadowstack.incumbents;

import io.shadowstack.candidates.InvocationSource;
import io.shadowstack.exceptions.InvocationKeyException;
import io.shadowstack.exceptions.InvocationSinkException;
import io.shadowstack.exceptions.InvocationSourceException;
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
    public void shadow(List<Invocation> invocations) throws InvocationSinkException {
        throw new InvocationSinkException("Not implemented");
    }

    @Override
    public void record(List<Invocation> invocations) throws InvocationSinkException {
        if(invocations != null && !invocations.isEmpty()) {
            for(Invocation invocation : invocations) {
                String contextGuid = invocation.getInvocationContext().getContextId();
                String keyHash = null;
                try {
                    keyHash = invocation.getInvocationKey().generateHash();
                } catch (InvocationKeyException e) {
                    throw new InvocationSinkException(e);
                }
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
        String keyHash = null;
        try {
            keyHash = key.generateHash();
        } catch (InvocationKeyException e) {
            throw new InvocationSourceException(e);
        }
        if(CONTEXT_TO_KEY_TO_RECORDINGS.containsKey(contextGuid) &&
           CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).containsKey(keyHash)) {
            // Pops it off the queue and returns. Next call will get next instance.
            return CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).get(keyHash).poll();
        }
        return null;
    }
}
