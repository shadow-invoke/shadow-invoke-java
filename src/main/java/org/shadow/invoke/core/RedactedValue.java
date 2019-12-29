package org.shadow.invoke.core;

import java.lang.reflect.Field;

public class RedactedValue {
    private RedactedValue() {}

    public static Object of(Field field) {
        if(field != null) {
            Class<?> fldClazz = field.getType();
            if(fldClazz.equals(int.class)) {
                return (int)0;
            } else if(fldClazz.equals(short.class)) {
                return (short)0;
            } else if(fldClazz.equals(double.class)) {
                return 0.0D;
            } else if(fldClazz.equals(boolean.class)) {
                return false;
            } else if(fldClazz.equals(float.class)) {
                return 0.0F;
            } else if(fldClazz.equals(char.class)) {
                return '*';
            } else if(fldClazz.equals(String.class)) {
                return "*****";
            } else if(fldClazz.equals(byte.class)) {
                return (byte)0;
            } else if(fldClazz.equals(long.class)) {
                return 0L;
            }
        }
        return null;
    }
}
