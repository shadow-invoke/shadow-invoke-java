package io.shadowstack.candidates;

import io.shadowstack.invocations.Invocation;
import io.shadowstack.invocations.InvocationContext;
import io.shadowstack.invocations.InvocationKey;
import io.shadowstack.invocations.sources.InvocationParameters;
import io.shadowstack.invocations.sources.InvocationSource;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import io.shadowstack.exceptions.InvocationReplayerException;
import io.shadowstack.filters.ObjectFilter;

import java.lang.reflect.Method;

@Slf4j
public class InvocationReplayer<T> implements MethodInterceptor {
    private final Class<T> cls;
    private String contextId = null;
    private InvocationSource invocationSource = null;
    private ObjectFilter objectFilter;

    public InvocationReplayer(Class<T> cls) {
        this.cls = cls;
    }

    public InvocationReplayer<T> filteringWith(ObjectFilter filter) {
        this.objectFilter = filter;
        return this;
    }

    public InvocationReplayer<T> retrievingFrom(InvocationSource invocationSource) {
        this.invocationSource = invocationSource;
        return this;
    }

    public T forContextId(String contextId) throws InvocationReplayerException {
        this.contextId = contextId;
        if(this.cls == null) {
            throw new InvocationReplayerException("InvocationReplayer created with null class.");
        }
        if(this.invocationSource == null) {
            throw new InvocationReplayerException("InvocationReplayer started with null invocationSource.");
        }
        if(this.objectFilter == null) {
            throw new InvocationReplayerException("InvocationReplayer started with null filter.");
        }
        if(this.contextId == null) {
            throw new InvocationReplayerException("InvocationReplayer started with null context ID.");
        }
        return (T) Enhancer.create(this.cls, this);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        // Top-level caller is responsible for setting a context for replays.
        // To this end, it will receive a GUID in the shadowing request.
        try(InvocationContext context = new InvocationContext(this.contextId)) {
            InvocationKey key = new InvocationKey(method, this.objectFilter.filterAsEvaluatedCopy(args));
            InvocationParameters parameters = new InvocationParameters(key, context);
            Invocation invocation = this.invocationSource.retrieve(parameters);
            // Using reference result here since replay should be as close to original as possible
            return (invocation != null) ? invocation.getReferenceResult() : null;
        }
    }
}
