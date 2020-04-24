package io.shadowstack.incumbents;

import io.shadowstack.Invocation;
import io.shadowstack.exceptions.InvocationSinkException;

import java.util.List;

public class ReplayServiceSink extends InvocationSink {
    private String host;

    @Override
    public void record(List<Invocation> invocations) throws InvocationSinkException {
        // TODO
    }

    public ReplayServiceSink at(String host) {
        this.host = host;
        return this;
    }
}
