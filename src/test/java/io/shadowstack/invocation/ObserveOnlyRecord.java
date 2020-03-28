package io.shadowstack.invocation;

import io.shadowstack.invocation.Recording.InvocationContext;
import io.shadowstack.invocation.Recording.InvocationKey;

public abstract class ObserveOnlyRecord extends Record {
    @Override
    public Recording get(InvocationKey key, InvocationContext context) {
        return null;
    }
}
