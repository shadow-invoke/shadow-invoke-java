package org.shadow.field;

import com.rits.cloning.Cloner;
import org.shadow.DefaultValue;
import org.shadow.ReflectiveAccess;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FilteringCloner {
    private static final Cloner CLONER = new Cloner();
    private final int objectDepth; // to filter recursively
    private final Filter[] constituentFilters;

    public FilteringCloner(int objectDepth, Filter[] constituentFilters) {
        this.objectDepth = objectDepth;
        this.constituentFilters = constituentFilters;
    }

    public Object filterAsEvaluatedCopy(Object obj) {
        Object copy = CLONER.deepClone(obj);
        this.filter(copy, 0, true);
        return copy;
    }

    public Object filterAsReferenceCopy(Object obj) {
        Object copy = CLONER.deepClone(obj);
        this.filter(copy, 0, false);
        return copy;
    }

    public Object[] filterAsEvaluatedCopy(Object[] arguments) {
        Object[] copy = new Object[arguments.length];
        for (int i = 0; i < arguments.length; ++i) {
            copy[i] = this.filterAsEvaluatedCopy(arguments[i]);
        }
        return copy;
    }

    public Object[] filterAsReferenceCopy(Object[] arguments) {
        Object[] copy = new Object[arguments.length];
        for (int i = 0; i < arguments.length; ++i) {
            copy[i] = this.filterAsReferenceCopy(arguments[i]);
        }
        return copy;
    }

    private void filter(Object obj, int level, boolean isEvaluated) {
        if(obj == null) return;
        Class<?> cls = obj.getClass();
        for(Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            for(Filter filter : this.constituentFilters) {
                if(isEvaluated) {
                    obj = filter.filterAsEvaluatedCopy(obj, field);
                } else {
                    obj = filter.filterAsReferenceCopy(obj, field);
                }
            }
            boolean filterable = (DefaultValue.of(field.getType()) == null) && !Modifier.isStatic(field.getModifiers());
            if(level < this.objectDepth && filterable) {
                Object member = ReflectiveAccess.getMember(obj, field);
                this.filter(member, level + 1, isEvaluated);
            }
        }
    }
}
