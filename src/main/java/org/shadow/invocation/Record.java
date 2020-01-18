package org.shadow.invocation;

import org.apache.commons.collections4.QueueUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.shadow.invocation.transmission.Transmitter;
import org.shadow.schedule.Throttle;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * The record of recordings recorded by recorders. Say that fast three times.
 */
public enum Record {
    INSTANCE;

    // TODO: Make the size configurable using something like cache(max(1024)) or caching(ttl(1, HOUR))
    private final Queue<Recording> QUEUE = QueueUtils.synchronizedQueue(new CircularFifoQueue<>(1024));
    private final Map<String, Throttle> invocationKeyToThrottle = new HashMap<>();
    private final Map<String, Transmitter> invocationKeyToTransmitter = new HashMap<>();
    // TODO: Use combination of ScheduledExecutorService and plain ExecutorService to periodically
    //       fire off a batch of transmissions, timing them out after some interval.

    protected void submit(Recording recording, Recorder recorder) {
        String invocationKey = recording.getInvocationKey();
        invocationKeyToThrottle.putIfAbsent(invocationKey, recorder.getThrottle());
        invocationKeyToTransmitter.putIfAbsent(invocationKey, recorder.getTransmitter());
        QUEUE.offer(recording);
    }
}
