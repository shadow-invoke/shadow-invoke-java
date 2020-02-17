package org.shadow.filtering;

import com.rits.cloning.Cloner;
import org.shadow.DefaultValue;
import org.shadow.ReflectiveAccess;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ObjectFilter {
    private static final Cloner CLONER = new Cloner();
    private int objectDepth = 10; // to filter recursively
    private final FieldFilter[] constituentFieldFilters;

    public ObjectFilter(FieldFilter[] constituentFieldFilters) {
        this.constituentFieldFilters = constituentFieldFilters;
    }

    public ObjectFilter toObjectDepth(int objectDepth) {
        this.objectDepth = objectDepth;
        return this;
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
            for(FieldFilter fieldFilter : this.constituentFieldFilters) {
                if(isEvaluated) {
                    obj = fieldFilter.filterAsEvaluatedCopy(obj, field);
                } else {
                    obj = fieldFilter.filterAsReferenceCopy(obj, field);
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
