package org.shadow.invocation;

import java.util.List;

public abstract class ObserveOnlyRecord extends Record {
    @Override
    public List<Recording> get(Recording.InvocationKey key) {
        return null;
    }

    @Override
    public Recording getNearest(Recording.InvocationKey key) {
        return null;
    }
}
