package org.shadow.invoke.core;

import java.lang.reflect.Field;

public class DefaultValues {
    private DefaultValues() {}

    public static void appendFromField(Field appendFrom, StringBuilder appendTo) throws IllegalAccessException {
        if(appendFrom != null) {
            Class<?> fldClazz = appendFrom.getType();
            if(fldClazz.equals(int.class)) {
                appendTo.append(0);
            } else if(fldClazz.equals(short.class)) {
                appendTo.append(0);
            } else if(fldClazz.equals(double.class)) {
                appendTo.append(0.0D);
            } else if(fldClazz.equals(boolean.class)) {
                appendTo.append("false");
            } else if(fldClazz.equals(float.class)) {
                appendTo.append(0.0D);
            } else if(fldClazz.equals(char.class)) {
                appendTo.append(' ');
            } else if(fldClazz.equals(String.class)) {
                appendTo.append(" ");
            } else if(fldClazz.equals(byte.class)) {
                appendTo.append(0);
            } else if(fldClazz.equals(long.class)) {
                appendTo.append(0L);
            } else {
                appendTo.append("null");
            }
        }
    }
}
