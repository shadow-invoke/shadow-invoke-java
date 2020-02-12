package org.shadow.invocation;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;

/**
 * A record of recordings recorded by a recorder. Say that fast three times.
 */
@Slf4j
public abstract class Record implements Subscriber<List<Recording>> {
    private Subscription subscription = null;
    @Getter private int batchSize = 1;

    public abstract void put(List<Recording> recordings);

    public abstract List<Recording> get(Recording.InvocationKey key);

    /***
     * Get a recording matching the key which is nearest the timestamp in the key (can be before or after).
     * @param key The key of the recording, with timestamp.
     * @param priorOnly Does the retrieved recording need to have occurred strictly before the timestamp?
     * @return A recording matching the given key, or null if none found.
     */
    public abstract Recording getNearest(Recording.InvocationKey key, boolean priorOnly);

    public Record withBatchSize(int size) {
        this.batchSize = size;
        if(this.batchSize < 1) {
            String fmt = "Record got bad batch size %d. Setting to 1.";
            log.warn(String.format(fmt, this.batchSize));
            this.batchSize = 1;
        }
        return this;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(this.batchSize);
    }

    @Override
    public void onNext(List<Recording> recordings) {
        this.put(recordings);
        this.subscription.request(this.batchSize + 1);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error(this.getClass().getCanonicalName(), throwable);
    }

    @Override
    public void onComplete() {
        this.subscription = null;
    }
}
