package org.shadow.invocation;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.shadow.exception.ReplayException;

import java.lang.reflect.Method;
import java.time.Instant;

/**
 * Foo output = replay(Bar.class).atTime(timestamp).retrievingFrom(recorder).start().myMethod(input1, input2, input3);
 */
@Slf4j
public class Replayer <T> implements MethodInterceptor {
    private final Class<T> cls;
    private Instant timestamp = null;
    private Record record = null;

    public Replayer(Class<T> cls) {
        this.cls = cls;
    }

    public Replayer<T> atTime(Instant timestamp) {
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
        if(this.timestamp == null) {
            log.warn("Replayer started with null timestamp. Defaulting to now.");
            this.timestamp = Instant.now();
        }
        return (T) Enhancer.create(this.cls, this);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Recording.InvocationKey key = new Recording.InvocationKey(method, obj, args, this.timestamp);
        return this.record.get(key);
    }
}
