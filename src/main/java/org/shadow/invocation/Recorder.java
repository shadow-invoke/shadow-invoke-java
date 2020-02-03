package org.shadow.invocation;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.shadow.field.Filter;
import org.shadow.invocation.transmission.Transmitter;
import org.shadow.throttling.Throttle;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Recorder implements MethodInterceptor, FlowableEmitter<Recording> {
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final Set<Subscriber<? super Recording>> listeners = new HashSet<>();
    private final List<Subscription> subscriptions = new ArrayList<>();
    private final Flowable<Recording> flowable;
    private Filter[] filters;
    private FilteringCloner filteringCloner;
    @Getter private final Object originalInstance;
    @Getter private Throttle throttle = null;
    @Getter private int objectDepth = 10;

    public Recorder(Object originalInstance) {
        this(originalInstance, 1);
    }

    public Recorder(Object originalInstance, int batchSize) {
        this.originalInstance = originalInstance;
        this.flowable = Flowable.create(this);
    }

    public Recorder filteringOut(Filter.Builder... filters) {
        this.filters = Arrays.stream(filters).map(Filter.Builder::build).toArray(Filter[]::new);
        this.filteringCloner = new FilteringCloner(this.objectDepth, this.filters);
        return this;
    }

    public Recorder toDepth(int objectDepth) {
        this.objectDepth = objectDepth;
        this.filteringCloner = new FilteringCloner(this.objectDepth, this.filters);
        return this;
    }

    public Recorder throttlingTo(Throttle throttle) {
        this.throttle = throttle;
        return this;
    }

    public Recorder sendingTo(Transmitter... transmitters) {
        if(transmitters != null && transmitters.length > 0) {
            Arrays.stream(transmitters).forEach(t -> {
                    //this.subscriptions.add(this.subject.subscribe(t));
            });
        }
        return this;
    }

    public <T> T proxyingAs(Class<T> cls) {
        if(cls == null || !cls.isInstance(this.originalInstance)) {
            String message = "Invalid combination of class %s and original instance %s. Returning null.";
            String className = (cls != null)? cls.getSimpleName() : "null";
            log.warn(String.format(message, className, this.originalInstance.getClass().getSimpleName()));
            return null;
        }
        return (T)Enhancer.create(cls, this);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] arguments, MethodProxy proxy) throws Throwable {
        Object result = method.invoke(this.originalInstance, arguments);
        try {
            Recording recording = new Recording(this.originalInstance, method,
                                                this.filteringCloner.filterAsReferenceCopy(arguments),
                                                this.filteringCloner.filterAsReferenceCopy(result),
                                                this.filteringCloner.filterAsEvaluatedCopy(arguments),
                                                this.filteringCloner.filterAsEvaluatedCopy(result));
            if(this.getThrottle() == null || !this.getThrottle().reject()) {
                this.onNext(recording);
            }
        } catch(Throwable t) {
            String message = "While intercepting recorded invocation. Method=%s, Args=%d, Object=%s.";
            String className = this.originalInstance.getClass().getSimpleName();
            log.error(String.format(message, method.getName(), arguments.length, className), t);
        }
        return result;
    }

    @Override
    public void onNext(@NonNull Recording value) {

    }

    @Override
    public void onError(@NonNull Throwable error) {

    }

    @Override
    public void onComplete() {

    }
}
