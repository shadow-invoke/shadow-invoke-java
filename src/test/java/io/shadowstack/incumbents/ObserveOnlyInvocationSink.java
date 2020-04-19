package io.shadowstack.incumbents;

public abstract class ObserveOnlyInvocationSink extends InvocationSink {
    @Override
    public Invocation get(InvocationKey key, InvocationContext context) {
        return null;
    }
}
