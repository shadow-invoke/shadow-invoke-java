package io.shadowstack;

import io.shadowstack.invocations.Invocation;
import io.shadowstack.invocations.InvocationContext;
import io.shadowstack.invocations.InvocationKey;
import io.shadowstack.invocations.sources.InvocationParameters;
import io.shadowstack.invocations.sources.InvocationSource;
import io.shadowstack.invocations.destinations.InvocationDestination;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;

/**
 * A InvocationDestination/Source for unit testing that saves to an in-memory cache.
 */
@Slf4j
public class InMemoryInvocationDestination implements InvocationSource, InvocationDestination {
    // Context GUID -> Key Hash -> Ordered Recordings
    private final Map<String, Map<String, Queue<Invocation>>> CONTEXT_TO_KEY_TO_RECORDINGS = new HashMap<>();
    private final Function<List<Invocation>, Boolean> callback;

    public InMemoryInvocationDestination(Function<List<Invocation>, Boolean> callback) {
        this.callback = callback;
    }

    @Override
    public List<Invocation> send(List<Invocation> invocations) {
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
        return invocations;
    }

    @Override
    public Invocation retrieve(InvocationParameters parameters) {
        String contextGuid = parameters.getContext();
        if(CONTEXT_TO_KEY_TO_RECORDINGS.containsKey(contextGuid) &&
           CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).containsKey(parameters.getHash())) {
            // Pops it off the queue and returns. Next call will get next instance.
            return CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).get(parameters.getHash()).poll();
        }
        return null;
    }
}
