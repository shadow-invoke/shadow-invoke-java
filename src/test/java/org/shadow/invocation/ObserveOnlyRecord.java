package org.shadow.invocation;

import org.shadow.invocation.Recording.InvocationContext;
import org.shadow.invocation.Recording.InvocationKey;

public abstract class ObserveOnlyRecord extends Record {
    @Override
    public Recording get(InvocationKey key, InvocationContext context) {
        return null;
    }
}
