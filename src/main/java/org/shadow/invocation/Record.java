package org.shadow.invocation;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.shadow.exception.RecordingException;
import org.shadow.invocation.Recording.InvocationContext;
import org.shadow.invocation.Recording.InvocationKey;

import java.util.List;

/**
 * A record of recordings recorded by a recorder. Say that fast three times.
 */
@Slf4j
public abstract class Record implements Subscriber<List<Recording>> {
    private Subscription subscription = null;
    @Getter private int batchSize = 1;

    public abstract void put(List<Recording> recordings) throws RecordingException;

    public abstract Recording get(InvocationKey key, InvocationContext context) throws RecordingException;

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
        try {
            this.put(recordings);
        } catch (RecordingException e) {
            log.error(String.format("While calling put() on %s", recordings), e);
        }
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
