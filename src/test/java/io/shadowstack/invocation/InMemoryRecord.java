package io.shadowstack.invocation;

import lombok.extern.slf4j.Slf4j;
import io.shadowstack.exception.RecordingException;
import io.shadowstack.invocation.Recording.InvocationContext;
import io.shadowstack.invocation.Recording.InvocationKey;

import java.util.*;
import java.util.function.Function;

/**
 * A Record for unit testing that saves to an in-memory cache.
 */
@Slf4j
public class InMemoryRecord extends Record {
    // Context GUID -> Key Hash -> Ordered Recordings
    private final Map<String, Map<String, Queue<Recording>>> CONTEXT_TO_KEY_TO_RECORDINGS = new HashMap<>();
    private final Function<List<Recording>, Boolean> callback;

    public InMemoryRecord(Function<List<Recording>, Boolean> callback) {
        this.callback = callback;
    }

    @Override
    public void put(List<Recording> recordings) throws RecordingException {
        if(recordings != null && !recordings.isEmpty()) {
            for(Recording recording : recordings) {
                String contextGuid = recording.getInvocationContext().getContextId();
                String keyHash = recording.getInvocationKey().generateHash();
                CONTEXT_TO_KEY_TO_RECORDINGS.putIfAbsent(contextGuid, new HashMap<>());
                CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).putIfAbsent(keyHash, new LinkedList<>());
                CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).get(keyHash).offer(recording);
            }
        }
        if(this.callback != null) {
            this.callback.apply(recordings);
        }
    }

    @Override
    public Recording get(InvocationKey key, InvocationContext context) throws RecordingException {
        if(key == null || context == null) {
            String msg = String.format("Bad context (%s) or key (%s)", key, context);
            throw new RecordingException(msg);
        }
        String contextGuid = context.getContextId();
        String keyHash = key.generateHash();
        if(CONTEXT_TO_KEY_TO_RECORDINGS.containsKey(contextGuid) &&
           CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).containsKey(keyHash)) {
            // Pops it off the queue and returns. Next call will get next instance.
            return CONTEXT_TO_KEY_TO_RECORDINGS.get(contextGuid).get(keyHash).poll();
        }
        return null;
    }
}
