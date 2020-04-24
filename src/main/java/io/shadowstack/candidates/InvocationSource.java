package io.shadowstack.candidates;

import io.shadowstack.exceptions.InvocationSourceException;
import io.shadowstack.Invocation;
import io.shadowstack.InvocationContext;
import io.shadowstack.InvocationKey;

public interface InvocationSource {
    Invocation retrieve(InvocationKey key, InvocationContext context) throws InvocationSourceException;
}
