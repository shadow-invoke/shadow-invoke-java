package org.shadow;

import org.shadow.field.Filter;
import org.shadow.field.Noise;
import org.shadow.field.Secret;
import org.shadow.invocation.Recorder;
import org.shadow.schedule.Percentage;
import org.shadow.schedule.Schedule;
import org.shadow.schedule.Time;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Fluently {
    private Fluently() {}

    public static Schedule percent(int percent) {
        return new Percentage((double)percent);
    }

    public static Schedule percent(double percent) {
        return new Percentage(percent);
    }

    public static Schedule every(long timeDuration, TimeUnit timeUnit) {
        return new Time(timeDuration, timeUnit);
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
        // Noise is ignored, meaning that it gets scrubbed in the evaluated
        // object copies but kept unaltered in the reference object copies.
        return new Filter.Builder(
                (obj) -> DefaultValue.of(obj.getClass()),
                (obj) -> obj
        ).where(annotated(Noise.class));
    }

    public static Filter.Builder secrets() {
        // Secrets are redacted, meaning that they're scrubbed in both the
        // evaluated object copies and the reference object copies.
        return new Filter.Builder(
                (obj) -> DefaultValue.of(obj.getClass()),
                (obj) -> DefaultValue.of(obj.getClass())
        ).where(annotated(Secret.class));
    }

    public static Recorder record(Object target) {
        return new Recorder(target);
    }
}
