package org.shadow.field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.shadow.ReflectiveAccess;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@Data
@Slf4j
@AllArgsConstructor
public class Filter implements Function<Object, Object> {
    private final Predicate<Field> selector;
    private final Function<Object, Object> action;
    private final Class<?> target;
    private final Map<String, Set<String>> history;

    public Object apply(Object obj) {
        if(obj == null) return null;
        if(obj.getClass().equals(this.target)) {
            Class<?> cls = obj.getClass();
            for(Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                Object member = ReflectiveAccess.getMember(obj, field);
                if(this.selector.test(field)) {
                    ReflectiveAccess.setMember(obj, this.action.andThen(this::apply).apply(member), field);
                    String key = cls.getCanonicalName();
                    // TODO: Record history only on first run somehow
                    history.putIfAbsent(key, new HashSet<>());
                    history.get(key).add(field.getName());
                } else {
                    ReflectiveAccess.setMember(obj, this.apply(member), field);
                }
            }
        }
        return obj;
    }

    public static class Builder {
        private Function<Object, Object> action;
        private Class<?> target;

        public Builder(Function<Object, Object> action) {
            this.action = action;
        }

        public Builder from(Class<?> target) {
            this.target = target;
            return this;
        }

        public Filter where(Predicate<Field> selector) {
            return new Filter(selector, this.action, this.target, new HashMap<>());
        }
    }
}
