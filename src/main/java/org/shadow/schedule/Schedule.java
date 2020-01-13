package org.shadow.schedule;

import org.shadow.invocation.Recording;

public interface Schedule {
    public boolean accept(Recording recording);
}
