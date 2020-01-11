package org.shadow.schedule;

import org.shadow.invocation.Transcript;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class TimeSchedule implements Schedule {
    private final TimeUnit timeUnit;
    private final long timeDuration;
    private long startEpochMillis;

    public TimeSchedule(long timeDuration, TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        this.timeDuration = timeDuration;
        this.startEpochMillis = Instant.now().toEpochMilli();
    }

    @Override
    public boolean accept(Transcript transcript) {
        long endEpochMillis = this.startEpochMillis + this.timeUnit.toMillis(this.timeDuration);
        if(Instant.now().toEpochMilli() > endEpochMillis) {
            // TODO: An obvious bug here is that if accept() is called less often than
            //       the given duration, this end-time will fall behind. All that means
            //       is that accept will always return true, which for now is close
            //       enough to what one would expect. A more robust implementation
            //       might use a buffer of time-stamped transcripts and a timer.
            this.startEpochMillis = endEpochMillis;
            return true;
        }
        return false;
    }
}
