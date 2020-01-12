package org.shadow.field;

import lombok.AccessLevel;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Filter implements Function<Object, Object> {
    /**
     * TODO: Merge filters so they don't all run sequentially.
     *       Can ignored fields be set to redacted values, but
     *       set aside in a mapping from path to value? These
     *       would of course also need to be filtered.
     */
    private final Predicate<Field> selector;
    private final Function<Object, Object> action;
    private final Class<?> target;
    private final Map<String, Set<String>> history;
    private final int depth; // depth of recursion when filtering

    public Object apply(Object obj) {
        return apply(obj, 0);
    }

    private Object apply(Object obj, int level) {
        if(obj == null) return null;
        Class<?> cls = obj.getClass();
        boolean inTargetClass = cls.equals(this.target);
        for(Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            Object member = ReflectiveAccess.getMember(obj, field);
            if(inTargetClass && (member != null) && this.selector.test(field)) {
                member = this.action.apply(member);
                String key = cls.getCanonicalName();
                // TODO: Transcript history only on first run somehow
                history.putIfAbsent(key, new HashSet<>());
                history.get(key).add(field.getName());
            }
            if(level < this.depth && areMembersFilterable(field)){
                member = this.apply(member, level + 1);
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
        private int depth = 15;

        public Builder(Function<Object, Object> action) {
            this.action = action;
        }

        public Builder from(Class<?> target) {
            this.target = target;
            return this;
        }

        public Builder toDepth(int depth) {
            this.depth = depth;
            return this;
        }

        public Filter where(Predicate<Field> selector) {
            return new Filter(selector, this.action, this.target, new HashMap<>(), this.depth);
        }
    }
}
