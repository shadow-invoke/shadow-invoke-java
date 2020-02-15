package org.shadow.invocation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Record for unit testing that saves to an in-memory cache.
 */
@Slf4j
public class InMemoryRecord extends Record {
    private final static ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.addMixIn(Recording.InvocationKey.class, InvocationKeySerializationMixin.class);
    }
    private Map<String, List<Recording>> hashToRecordings = new HashMap<>();

    @Override
    public void put(List<Recording> recordings) {
        if(recordings != null && !recordings.isEmpty()) {
            for(Recording recording : recordings) {
                try {
                    String hash = generateHash(recording.getInvocationKey());
                    hashToRecordings.putIfAbsent(hash, new ArrayList<>());
                    hashToRecordings.get(hash).add(recording);
                } catch (JsonProcessingException e) {
                    log.error("While putting " + recording, e);
                }
            }
        }
    }

    @Override
    public List<Recording> get(Recording.InvocationKey key) {
        try {
            return hashToRecordings.get(generateHash(key));
        } catch (JsonProcessingException e) {
            log.error("While getting " + key, e);
            return null;
        }
    }

    @Override
    public Recording getNearest(Recording.InvocationKey key, boolean priorOnly) {
        if(key == null || key.getTimestamp() == null) {
            log.error("Null key or timestamp.");
            return null;
        }
        List<Recording> recordings = this.get(key);
        Recording nearest = null;
        if(recordings != null && !recordings.isEmpty()) {
            Duration min = Duration.ofSeconds(Long.MAX_VALUE, 999_999_999);
            for(Recording recording : recordings) {
                if(recording.getInvocationKey().getTimestamp() != null) {
                    Duration diff = Duration.between(recording.getInvocationKey().getTimestamp(), key.getTimestamp());
                    if(!diff.isNegative() || !priorOnly) {
                        diff = diff.abs();
                        if (diff.compareTo(min) < 0) {
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
    public static String generateHash(Recording.InvocationKey key) throws JsonProcessingException {
        return DigestUtils.sha3_256Hex(MAPPER.writeValueAsString(key));
    }

    public static abstract class InvocationKeySerializationMixin {
        @JsonIgnore abstract Instant getTimeStamp();
    }
}
