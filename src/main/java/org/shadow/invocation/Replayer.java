package org.shadow.invocation;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.shadow.exception.ReplayException;
import org.shadow.filtering.ObjectFilter;

import java.lang.reflect.Method;
import java.time.Instant;

@Slf4j
public class Replayer <T> implements MethodInterceptor {
    private final Class<T> cls;
    private Instant timestamp = null;
    private Record record = null;
    private boolean priorOnly = false;
    private ObjectFilter objectFilter;

    public Replayer(Class<T> cls) {
        this.cls = cls;
    }

    public Replayer<T> filteringWith(ObjectFilter filter) {
        this.objectFilter = filter;
        return this;
    }

    public Replayer<T> atTime(Instant timestamp) {
        this.priorOnly = false;
        this.timestamp = timestamp;
        return this;
    }

    public Replayer<T> atTimeBefore(Instant timestamp) {
        this.priorOnly = true;
        this.timestamp = timestamp;
        return this;
    }

    public Replayer<T> retrievingFrom(Record record) {
        this.record = record;
        return this;
    }

    public T start() throws ReplayException {
        if(this.cls == null) {
            throw new ReplayException("Replayer created with null class.");
        }
        if(this.record == null) {
            throw new ReplayException("Replayer started with null record.");
        }
        if(this.objectFilter == null) {
            throw new ReplayException("Replayer started with null filter.");
        }
        if(this.timestamp == null) {
            log.warn("Replayer started with null timestamp. Defaulting to now.");
            this.timestamp = Instant.now();
        }
        return (T) Enhancer.create(this.cls, this);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Recording.InvocationKey key = new Recording.InvocationKey(
                method,
                this.cls,
                this.objectFilter.filterAsEvaluatedCopy(args),
                this.timestamp
        );
        Recording recording = this.record.getNearest(key, this.priorOnly);
        return (recording != null)? recording.getReferenceResult() : null;
    }
}
