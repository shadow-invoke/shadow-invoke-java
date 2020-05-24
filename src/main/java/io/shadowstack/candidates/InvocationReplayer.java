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
import java.time.Duration;
import java.time.Instant;

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

    public InvocationReplayer<T> forContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public T buildProxy() throws InvocationReplayerException {
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
        try(InvocationContext context = new InvocationContext(this.contextId))
        {
            Instant replayStart = Instant.now();
            InvocationKey key = new InvocationKey(method, this.objectFilter.filterAsEvaluatedCopy(args));
            InvocationParameters parameters = new InvocationParameters(key, context);
            Invocation invocation = this.invocationSource.retrieve(parameters);
            /**
             * When replaying an invocation, mimic the original caller's experience as closely as possible.
             *      1. Return the reference result instead of the evaluated result.
             *      2. Throw any exception thrown by the original call.
             *      3. If possible, take the same amount of time to return.
             */
            Duration replayDuration = Duration.between(replayStart, Instant.now());
            if(invocation.getCallDuration() != null) {
                Duration durationToWait = invocation.getCallDuration().minus(replayDuration);
                if(!durationToWait.isNegative()) {
                    Thread.sleep(durationToWait.toMillis());
                }
            }
            if(invocation.getExceptionThrown() != null) {
                throw invocation.getExceptionThrown();
            }
            return (invocation != null) ? invocation.getReferenceResult() : null;
        }
    }
}
