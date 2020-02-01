package org.shadow.invocation.transmission;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.shadow.invocation.Recording;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class Transmitter implements Subscriber<Recording> {
    public static final int DEFAULT_BATCH_SIZE = 10;
    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(NUM_CORES);
    private List<Recording> pending = new ArrayList<>(DEFAULT_BATCH_SIZE);
    private Subscription subscription = null;
    @Getter private int batchSize = DEFAULT_BATCH_SIZE;

    public abstract void transmit(Collection<Recording> recordings);

    private void sendPending(int threshold) {
        if(this.pending.size() > threshold) {
            try {
                this.transmit(new ArrayList<>(this.pending)); // send a copy since we clear the source
            } catch (Throwable t) {
                this.onError(t);
                log.error(String.format("Failed to transmit %s", this.pending), t);
            } finally {
                this.pending.clear();
            }
        }
        if(this.subscription != null) {
            this.subscription.request(1L);
        }
    }

    public Transmitter withBatchSize(int size) {
        this.batchSize = size;
        sendPending(0); // send now
        this.pending = new ArrayList<>(size);
        return this;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        sendPending(0); // send now
    }

    @Override
    public void onNext(Recording recording) {
        this.pending.add(recording);
        this.sendPending(this.batchSize - 1);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error(this.getClass().getCanonicalName(), throwable);
    }

    @Override
    public void onComplete() {
        this.subscription = null;
        sendPending(0);
    }
}
