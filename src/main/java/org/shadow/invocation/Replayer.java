package org.shadow.invocation;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.shadow.exception.ReplayException;
import org.shadow.filtering.ObjectFilter;

import java.lang.reflect.Method;

@Slf4j
public class Replayer <T> implements MethodInterceptor {
    private final Class<T> cls;
    private String contextId = null;
    private Record record = null;
    private ObjectFilter objectFilter;

    public Replayer(Class<T> cls) {
        this.cls = cls;
    }

    public Replayer<T> filteringWith(ObjectFilter filter) {
        this.objectFilter = filter;
        return this;
    }

    public Replayer<T> retrievingFrom(Record record) {
        this.record = record;
        return this;
    }

    public T forContextId(String contextId) throws ReplayException {
        this.contextId = contextId;
        if(this.cls == null) {
            throw new ReplayException("Replayer created with null class.");
        }
        if(this.record == null) {
            throw new ReplayException("Replayer started with null record.");
        }
        if(this.objectFilter == null) {
            throw new ReplayException("Replayer started with null filter.");
        }
        if(this.contextId == null) {
            throw new ReplayException("Replayer started with null context ID.");
        }
        return (T) Enhancer.create(this.cls, this);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        // Top-level caller is responsible for setting a context for replays.
        // To this end, it will receive a GUID in the shadowing request.
        try(Recording.InvocationContext context = new Recording.InvocationContext(this.contextId)) {
            Recording.InvocationKey key = new Recording.InvocationKey(
                    method,
                    this.cls,
                    this.objectFilter.filterAsEvaluatedCopy(args)
            );
            Recording recording = this.record.get(key, context);
            return (recording != null) ? recording.getReferenceResult() : null;
        }
    }
}
