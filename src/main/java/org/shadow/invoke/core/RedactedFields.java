package org.shadow.invoke.core;

import java.lang.reflect.Field;

public class RedactedFields {
    private RedactedFields() {}

    public static Object redactedValueOf(Field field) {
        if(field != null) {
            if(isInteger(field)) {
                return (int)0;
            } else if(isShort(field)) {
                return (short)0;
            } else if(isDouble(field)) {
                return 0.0D;
            } else if(isBoolean(field)) {
                return false;
            } else if(isFloat(field)) {
                return 0.0F;
            } else if(isCharacter(field)) {
                return '*';
            } else if(isString(field)) {
                return "*****";
            } else if(isByte(field)) {
                return (byte)0;
            } else if(isLong(field)) {
                return 0L;
            }
        }
        return null;
    }

    private static boolean isInteger(Field field) {
        return field.getType().equals(int.class) || field.getType().equals(Integer.class);
    }

    private static boolean isDouble(Field field) {
        return field.getType().equals(double.class) || field.getType().equals(Double.class);
    }

    private static boolean isLong(Field field) {
        return field.getType().equals(long.class) || field.getType().equals(Long.class);
    }

    private static boolean isBoolean(Field field) {
        return field.getType().equals(boolean.class) || field.getType().equals(Boolean.class);
    }

    private static boolean isShort(Field field) {
        return field.getType().equals(short.class) || field.getType().equals(Short.class);
    }

    private static boolean isFloat(Field field) {
        return field.getType().equals(float.class) || field.getType().equals(Float.class);
    }

    private static boolean isByte(Field field) {
        return field.getType().equals(byte.class) || field.getType().equals(Byte.class);
    }

    private static boolean isCharacter(Field field) {
        return field.getType().equals(char.class) || field.getType().equals(Character.class);
    }

    private static boolean isString(Field field) {
        return field.getType().equals(String.class);
    }
}
