package org.shadow.field;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.shadow.ReflectiveAccess;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Predicate;

@Data
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Filter {
    private final Predicate<Field> selector;
    private final Function<Object, Object> generateEvaluatedMember;
    private final Function<Object, Object> generateReferenceMember;
    private final Class<?> target;

    public void filterAsEvaluatedCopy(Object obj, Field fld) {
        filterWith(obj, fld, this.generateEvaluatedMember);
    }

    public void filterAsReferenceCopy(Object obj, Field fld) {
        filterWith(obj, fld, this.generateReferenceMember);
    }

    private void filterWith(Object obj, Field fld, Function<Object, Object> action) {
        if(obj == null) return;
        Class<?> cls = obj.getClass();
        if(cls.equals(this.target)) {
            Object member = ReflectiveAccess.getMember(obj, fld);
            if ((member != null) && this.selector.test(fld)) {
                member = action.apply(member);
                ReflectiveAccess.setMember(obj, member, fld);
            }
        }
    }

    public static class Builder {
        private Predicate<Field> selector;
        private Function<Object, Object> generateEvaluatedMember;
        private Function<Object, Object> generateReferenceMember;
        private Class<?> target;

        public Builder(Function<Object, Object> generateEvaluated, Function<Object, Object> generateReference) {
            this.generateEvaluatedMember = generateEvaluated;
            this.generateReferenceMember = generateReference;
        }

        public Builder from(Class<?> target) {
            this.target = target;
            return this;
        }

        public Builder where(Predicate<Field> selector) {
            this.selector = selector;
            return this;
        }

        public Filter build() {
            return new Filter(this.selector, this.generateEvaluatedMember, this.generateReferenceMember, this.target);
        }
    }
}
