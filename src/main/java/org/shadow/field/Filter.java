package org.shadow.field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.shadow.Redacted;
import org.shadow.ReflectiveAccess;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
        Class<?> cls = obj.getClass();
        boolean inTargetClass = cls.equals(this.target);
        for(Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            Object member = ReflectiveAccess.getMember(obj, field);
            if(inTargetClass && (member != null) && this.selector.test(field)) {
                member = this.action.apply(member);
                String key = cls.getCanonicalName();
                // TODO: Record history only on first run somehow
                history.putIfAbsent(key, new HashSet<>());
                history.get(key).add(field.getName());
            }
            if(areMembersFilterable(field)){
                // TODO: Control depth of recursion
                member = this.apply(member);
            }
            ReflectiveAccess.setMember(obj, member, field);
        }
        return obj;
    }

    private static boolean areMembersFilterable(Field field) {
        return (Redacted.valueOf(field.getType()) == null) && !Modifier.isStatic(field.getModifiers());
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
