package org.shadow;

import org.shadow.field.Filter;
import org.shadow.invocation.Scribe;
import org.shadow.schedule.PercentageSchedule;
import org.shadow.schedule.Schedule;
import org.shadow.schedule.TimeSchedule;

import java.util.concurrent.TimeUnit;

public class Fluently {
    private Fluently() {}

    public static Schedule percent(int percent) {
        return new PercentageSchedule((double)percent);
    }

    public static Schedule percent(double percent) {
        return new PercentageSchedule(percent);
    }

    public static Schedule every(TimeUnit timeUnit) {
        return new TimeSchedule(1L, timeUnit);
    }

    public static Schedule hourly() {
        return every(TimeUnit.HOURS);
    }

    public static Filter.Builder noise() {
        return new Filter.Builder((obj) -> obj); // ignore and note exclusion
    }

    public static Filter.Builder secret() {
        return new Filter.Builder((obj) -> Redacted.valueOf(obj.getClass())); // redact and note exclusion
    }

    public static Scribe transcribe(Object target) {
        return new Scribe(target);
    }
}
