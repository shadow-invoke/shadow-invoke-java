package org.shadow.invoke.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class RedactedFields {
    private RedactedFields() {}

    public static boolean shouldRedactMembers(Field field) {
        return (redactedValueOf(field.getType()) == null) && !Modifier.isStatic(field.getModifiers());
    }

    public static Object redactedValueOf(Class<?> cls) {
        if(cls != null) {
            if(isInteger(cls)) {
                return (int)-1;
            } else if(isShort(cls)) {
                return (short)-1;
            } else if(isDouble(cls)) {
                return -1.0D;
            } else if(isBoolean(cls)) {
                return false;
            } else if(isFloat(cls)) {
                return -1.0F;
            } else if(isCharacter(cls)) {
                return '*';
            } else if(isString(cls)) {
                return "*****";
            } else if(isByte(cls)) {
                return (byte)0;
            } else if(isLong(cls)) {
                return -1L;
            }
        }
        return null;
    }

    private static boolean isInteger(Class<?> cls) {
        return cls.equals(int.class) || cls.equals(Integer.class);
    }

    private static boolean isDouble(Class<?> cls) {
        return cls.equals(double.class) || cls.equals(Double.class);
    }

    private static boolean isLong(Class<?> cls) {
        return cls.equals(long.class) || cls.equals(Long.class);
    }

    private static boolean isBoolean(Class<?> cls) {
        return cls.equals(boolean.class) || cls.equals(Boolean.class);
    }

    private static boolean isShort(Class<?> cls) {
        return cls.equals(short.class) || cls.equals(Short.class);
    }

    private static boolean isFloat(Class<?> cls) {
        return cls.equals(float.class) || cls.equals(Float.class);
    }

    private static boolean isByte(Class<?> cls) {
        return cls.equals(byte.class) || cls.equals(Byte.class);
    }

    private static boolean isCharacter(Class<?> cls) {
        return cls.equals(char.class) || cls.equals(Character.class);
    }

    private static boolean isString(Class<?> cls) {
        return cls.equals(String.class);
    }
}
