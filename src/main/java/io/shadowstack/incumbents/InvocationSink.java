package io.shadowstack.incumbents;

import io.shadowstack.Invocation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;

/**
 * A sink for Invocations as they are either shadowed or recorded. Can either be a remote or local sink.
 */
@Slf4j
public class InvocationSink implements Subscriber<List<Invocation>> {
    private final InvocationDestination destination;
    private Subscription subscription = null;
    @Getter private int batchSize = 1;

    public InvocationSink(InvocationDestination destination) {
        this.destination = destination;
    }

    public InvocationSink withBatchSize(int size) {
        this.batchSize = size;
        if(this.batchSize < 1) {
            String fmt = "InvocationSink got bad batch size %d. Setting to 1.";
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
    public void onNext(List<Invocation> invocations) {
        try {
            this.destination.send(invocations);
        } catch (Throwable t) {
            log.error(String.format("While sending %s to %s.", invocations, this.destination), t);
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
