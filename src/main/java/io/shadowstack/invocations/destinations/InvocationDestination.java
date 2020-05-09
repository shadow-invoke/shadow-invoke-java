package io.shadowstack.invocations.destinations;

import io.shadowstack.invocations.Invocation;

import java.util.List;

public interface InvocationDestination {
    /**
     * Send invocation instances to this destination.
     * @param invocations Invocations sent
     * @return Invocations saved (may be altered, e.g. with updated hashes)
     */
    List<Invocation> send(List<Invocation> invocations);
}
