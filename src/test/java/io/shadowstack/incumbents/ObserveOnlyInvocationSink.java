package io.shadowstack.incumbents;

import io.shadowstack.exceptions.InvocationSinkException;

import java.util.List;

public abstract class ObserveOnlyInvocationSink extends InvocationSink {
    @Override
    public void shadow(List<Invocation> invocations) throws InvocationSinkException {
        throw new InvocationSinkException("Not implemented");
    }
}
