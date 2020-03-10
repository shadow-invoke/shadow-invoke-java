package org.shadow.converting;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
public class Conversion<FC, TC> {
    @Getter
    private final Class<FC> from;
    @Getter
    private final Class<TC> to;
    private final Converter<FC, TC> converter;

    public TC convert(FC from) {
        return converter.convert(from);
    }

    public static class Builder {
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Initial<FI> {
            private Class<FI> from;

            public Initial(Class<FI> from) {
                this.from = from;
            }

            public <T> Final<FI, T> to(Class<T> to) {
                return new Final<>(this.from, to);
            }
        }

        @Slf4j
        @AllArgsConstructor
        public static class Final<FF, TF> {
            private Class<FF> from;
            private Class<TF> to;

            public Conversion<FF, TF> with(Converter<FF, TF> converter) {
                if (converter == null) {
                    log.warn("Null converter, returning null conversion.");
                    return null;
                }
                if (this.from == null || this.to == null) {
                    log.warn(String.format("Bad from class (%s) or to class (%s), returning null conversion.", this.from, this.to));
                    return null;
                }
                return new Conversion<>(this.from, this.to, converter);
            }
        }
    }
}
