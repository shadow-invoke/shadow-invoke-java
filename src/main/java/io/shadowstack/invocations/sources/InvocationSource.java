package io.shadowstack.invocations.sources;

import io.shadowstack.invocations.Invocation;

public interface InvocationSource {
    Invocation retrieve(InvocationParameters parameters);
}
