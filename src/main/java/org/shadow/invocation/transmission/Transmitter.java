package org.shadow.invocation.transmission;

import io.reactivex.rxjava3.core.Observer;
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
public abstract class Transmitter implements Observer<Recording> {
    public abstract void transmit(Recording recordings);

    @Override
    public void onNext(Recording recording) {
        this.transmit(recording);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error(this.getClass().getCanonicalName(), throwable);
    }

    @Override
    public void onComplete() {
    }
}
