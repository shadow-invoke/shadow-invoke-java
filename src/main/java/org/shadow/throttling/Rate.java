package org.shadow.throttling;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Rate implements Throttle {
    private TimeUnit timeUnit;
    private long timeDuration;
    private final int acceptsPerDuration;
    private int acceptedThisInterval;
    private long startEpochMillis;

    public Rate(int acceptsPerDuration) {
        this.acceptsPerDuration = acceptsPerDuration;
        this.acceptedThisInterval = 0;
    }

    public Rate per(long timeDuration, TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        this.timeDuration = timeDuration;
        this.startEpochMillis = Instant.now().toEpochMilli();
        return this;
    }

    @Override
    public boolean reject() {
        if(this.timeUnit == null) {
            log.warn("No time unit or duration was set for this rate throttling. Never accepting");
            return true;
        }
        long endEpochMillis = this.startEpochMillis + this.timeUnit.toMillis(this.timeDuration);
        if(Instant.now().toEpochMilli() > endEpochMillis) {
            // TODO: An obvious bug here is that if accept() is called less often than
            //       the given duration, this end-time will fall behind. All that means
            //       is that accept will always return true, which for now is close
            //       enough to what one would expect. A more robust implementation
            //       might use a buffer of time-stamped transcripts and a timer.
            this.startEpochMillis = endEpochMillis;
            this.acceptedThisInterval = 1;
            return false;
        } else if(this.acceptedThisInterval < this.acceptsPerDuration) {
            this.acceptedThisInterval++;
            return false;
        }
        return true;
    }
}
