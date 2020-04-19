package io.shadowstack.candidates;

import io.shadowstack.exceptions.InvocationSourceException;
import io.shadowstack.incumbents.Invocation;
import io.shadowstack.incumbents.InvocationContext;
import io.shadowstack.incumbents.InvocationKey;

public interface InvocationSource {
    Invocation retrieve(InvocationKey key, InvocationContext context) throws InvocationSourceException;
}
