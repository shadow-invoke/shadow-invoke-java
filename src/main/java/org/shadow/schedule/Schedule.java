package org.shadow.schedule;

import org.shadow.invocation.Transcript;

public interface Schedule {
    public boolean accept(Transcript transcript);
}
