package org.shadow.invocation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.QueueUtils;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.shadow.invocation.transmission.TransmissionException;
import org.shadow.invocation.transmission.Transmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The record of recordings recorded by recorders. Say that fast three times.
 */
@Slf4j
public enum Record {
    INSTANCE;

    private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final int MAX_QUEUE_SIZE = 1024; // TODO: Make configurable
    // TODO: Make the size configurable using something like cache(max(1024)) or caching(ttl(1, HOUR))
    private final Map<String, Transmitter> invocationKeyToTransmitter = new HashMap<>();
    private final Map<String, Queue<Recording>> invocationKeyToPendingQueue = new HashMap<>();

    Record() {
        SCHEDULER.scheduleAtFixedRate(this::transmitPending, 0L, 1L, TimeUnit.MINUTES); // TODO: Make configurable
    }

    private void transmitPending() {
        for(String key : this.invocationKeyToPendingQueue.keySet()) {
            this.transmitPending(key);
        }
    }

    public void transmitPending(String invocationKey) {
        Queue<Recording> pending = this.invocationKeyToPendingQueue.get(invocationKey);
        Transmitter transmitter = this.invocationKeyToTransmitter.get(invocationKey);
        if(transmitter != null) {
            try {
                log.info(String.format("Transmitting %d recordings for key %s.", pending.size(), invocationKey));
                transmitter.transmit(pending.stream().collect(Collectors.toList()));
            } catch (TransmissionException e) {
                log.warn(String.format("While transmitting for key %s: ", invocationKey), e);
            }
        }
        pending.clear();
    }

    protected void submit(Recording recording, Recorder recorder) {
        if(recorder == null || recording == null) {
            log.warn("Null recorder or recording passed to submit");
            return;
        }
        if(recorder.getThrottle() == null || !recorder.getThrottle().reject()) {
            String invocationKey = recording.getInvocationKey();
            invocationKeyToTransmitter.putIfAbsent(invocationKey, recorder.getTransmitter());
            invocationKeyToPendingQueue.putIfAbsent(invocationKey, createQueue());
            invocationKeyToPendingQueue.get(invocationKey).offer(recording);
        }
    }

    private static Queue<Recording> createQueue() {
        return QueueUtils.synchronizedQueue(new CircularFifoQueue<>(MAX_QUEUE_SIZE));
    }
}
