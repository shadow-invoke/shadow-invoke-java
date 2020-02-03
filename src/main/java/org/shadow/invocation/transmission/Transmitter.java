package org.shadow.invocation.transmission;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.shadow.invocation.Recording;

import java.util.List;

@Slf4j
public abstract class Transmitter implements Subscriber<List<Recording>> {
    private Subscription subscription = null;
    @Getter private int batchSize = 1;

    public abstract void transmit(List<Recording> recordings);

    public Transmitter withBatchSize(int size) {
        this.batchSize = size;
        if(this.batchSize < 1) {
            String fmt = "Transmitter got bad batch size %d. Setting to 1.";
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
        this.transmit(recordings);
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
