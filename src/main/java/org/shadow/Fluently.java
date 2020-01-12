package org.shadow;

import org.shadow.field.Filter;
import org.shadow.invocation.Scribe;
import org.shadow.schedule.PercentageSchedule;
import org.shadow.schedule.Schedule;
import org.shadow.schedule.TimeSchedule;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Fluently {
    private Fluently() {}

    public static Schedule percent(int percent) {
        return new PercentageSchedule((double)percent);
    }

    public static Schedule percent(double percent) {
        return new PercentageSchedule(percent);
    }

    public static Schedule every(long timeDuration, TimeUnit timeUnit) {
        return new TimeSchedule(timeDuration, timeUnit);
    }

    public static Schedule every(TimeUnit timeUnit) {
        return every(1L, timeUnit);
    }

    public static Predicate<Field> named(String... names) {
        // TODO: Make more efficient using actual class and hashset.
        return field -> {
            if(field == null || names == null || names.length == 0) return false;
            return Arrays.stream(names).anyMatch(field.getName()::equals);
        };
    }

    public static Predicate<Field> annotated(Class<? extends Annotation> cls) {
        return field -> {
            if(field == null || cls == null) return false;
            return field.getDeclaredAnnotation(cls) == null;
        };
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
