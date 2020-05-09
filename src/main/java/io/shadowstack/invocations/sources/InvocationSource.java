package io.shadowstack.invocations.sources;

import io.shadowstack.exceptions.InvocationSourceException;
import io.shadowstack.invocations.Invocation;
import io.shadowstack.invocations.InvocationContext;
import io.shadowstack.invocations.InvocationKey;

public interface InvocationSource {
    Invocation retrieve(InvocationKey key, InvocationContext context) throws InvocationSourceException;
}
