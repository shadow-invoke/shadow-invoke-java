package org.shadow.invocation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A Record for unit testing that saves to an in-memory cache.
 */
@Slf4j
public class InMemoryRecord extends Record {
    private final Map<String, List<Recording>> hashToRecordings = new HashMap<>();
    private final Function<List<Recording>, Boolean> callback;

    public InMemoryRecord(Function<List<Recording>, Boolean> callback) {
        this.callback = callback;
    }

    @Override
    public void put(List<Recording> recordings) {
        if(recordings != null && !recordings.isEmpty()) {
            for(Recording recording : recordings) {
                String hash = generateHash(recording.getInvocationKey());
                log.info(String.format("Storing object %s at key %s", recording, hash));
                hashToRecordings.putIfAbsent(hash, new ArrayList<>());
                hashToRecordings.get(hash).add(recording);
            }
        }
        if(this.callback != null) {
            this.callback.apply(recordings);
        }
    }

    @Override
    public List<Recording> get(Recording.InvocationKey key) {
        String hash = generateHash(key);
        List<Recording> recordings = hashToRecordings.get(hash);
        log.info(String.format("Retrieving objects %s at key %s", recordings, hash));
        return recordings;
    }

    @Override
    public Recording getNearest(Recording.InvocationKey key) {
        if(key == null || key.getTimestamp() == null) {
            log.error("Null key or call timestamp.");
            return null;
        }
        List<Recording> recordings = this.get(key);
        Recording nearest = null;
        if(recordings != null && !recordings.isEmpty()) {
            Duration min = Duration.ofSeconds(Long.MAX_VALUE, 999_999_999);
            for(Recording recording : recordings) {
                if(recording.getInvocationKey().getTimestamp() != null) {
                    Duration diff = Duration.between(recording.getInvocationKey().getTimestamp(), key.getTimestamp());
                    if(diff.isNegative() || diff.isZero()) { // looking for the nearest recording occurring before the call timestamp
                        diff = diff.abs();
                        if (diff.compareTo(min) <= 0) {
                            min = diff;
                            nearest = recording;
                        }
                    }
                }
            }
        }
        return nearest;
    }

    /**
     * Generate a unique key from the SHA256 hash of all invocation key fields except for the timestamp.
     */
    public static String generateHash(Recording.InvocationKey key) {
        StringBuilder builder = new StringBuilder();
        builder.append(key.getInvokedMethod().getName());
        builder.append(',');
        for(Object obj : key.getEvaluatedArguments()) {
            builder.append(obj.toString());
        }
        builder.append(',');
        builder.append(key.getInvocationTarget().getCanonicalName());
        String hash = DigestUtils.sha256Hex(builder.toString());
        log.info(String.format("Generated hash %s for key %s.", hash, key));
        return hash;
    }
}
