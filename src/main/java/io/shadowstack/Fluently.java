package io.shadowstack;

import io.shadowstack.candidates.CandidateService;
import io.shadowstack.candidates.InvocationReplayer;
import io.shadowstack.candidates.registrars.CandidateRegistrar;
import io.shadowstack.candidates.registrars.RestCandidateRegistrar;
import io.shadowstack.invocations.destinations.InvocationDestination;
import io.shadowstack.invocations.destinations.ReplayingRestInvocationDestination;
import io.shadowstack.invocations.destinations.ShadowingRestInvocationDestination;
import io.shadowstack.invocations.sources.InvocationSource;
import io.shadowstack.invocations.sources.RestInvocationSource;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import io.shadowstack.filters.FieldFilter;
import io.shadowstack.filters.Noise;
import io.shadowstack.filters.ObjectFilter;
import io.shadowstack.filters.Secret;
import io.shadowstack.incumbents.InvocationRecorder;
import io.shadowstack.throttles.Percentage;
import io.shadowstack.throttles.Rate;
import io.shadowstack.throttles.Throttle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@UtilityClass
public class Fluently {
    public static Throttle percent(double percent) {
        return new Percentage(percent);
    }

    public static Rate rate(int max) {
        return new Rate(max);
    }

    public static Throttle every(long timeDuration, TimeUnit timeUnit) {
        return new Rate(1).per(timeDuration, timeUnit);
    }

    public static Predicate<Field> named(String... names) {
        return new NamesPredicate(names);
    }

    private static class NamesPredicate implements Predicate<Field> {
        private final Set<String> nameSet = new HashSet<>();

        public NamesPredicate(String... names) {
            this.nameSet.addAll(Arrays.asList(names));
        }

        @Override
        public boolean test(Field field) {
            return field != null && this.nameSet.contains(field.getName());
        }
    }

    public static Predicate<Field> annotated(Class<? extends Annotation> cls) {
        return field -> {
            if(field == null || cls == null) return false;
            return field.getDeclaredAnnotation(cls) != null;
        };
    }

    public static ObjectFilter filter(FieldFilter.Builder... fieldFilterBuilders) {
        return new ObjectFilter(Arrays.stream(fieldFilterBuilders).map(FieldFilter.Builder::build).toArray(FieldFilter[]::new));
    }

    public static FieldFilter.Builder noise() {
        // Noise is ignored, meaning that it gets scrubbed in the evaluated
        // object copies but kept unaltered in the reference object copies.
        return new FieldFilter.Builder(
                (obj) -> DefaultValue.of(obj.getClass()),
                (obj) -> obj
        ).where(annotated(Noise.class));
    }

    public static FieldFilter.Builder secrets() {
        // Secrets are redacted, meaning that they're scrubbed in both the
        // evaluated object copies and the reference object copies.
        return new FieldFilter.Builder(
                (obj) -> DefaultValue.of(obj.getClass()),
                (obj) -> DefaultValue.of(obj.getClass())
        ).where(annotated(Secret.class));
    }

    public static InvocationRecorder record(Object target) {
        return new InvocationRecorder(target);
    }

    public static <T> InvocationReplayer<T> replay(Class<T> cls) {
        return new InvocationReplayer<>(cls);
    }

    public static CandidateService.CandidateServiceBuilder candidate(Object candidateInstance) {
        return CandidateService.builder().candidateInstance(candidateInstance);
    }

    @AllArgsConstructor
    private static class DestinationClientBuilder {
        private String host;

        public InvocationDestination replaying() {
            return ReplayingRestInvocationDestination.createClient(this.host);
        }

        public InvocationDestination shadowing() {
            return ShadowingRestInvocationDestination.createClient(this.host);
        }
    }

    public static DestinationClientBuilder destination(String host) {
        return new DestinationClientBuilder(host);
    }

    public static InvocationSource source(String host) {
        return RestInvocationSource.createClient(host);
    }

    public static CandidateRegistrar registrar(String host) {
        return RestCandidateRegistrar.createClient(host);
    }
}
