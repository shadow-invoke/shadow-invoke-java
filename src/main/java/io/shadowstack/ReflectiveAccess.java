package io.shadowstack;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class ReflectiveAccess {
    private ReflectiveAccess() {}

    public static void setMember(Object parent, Object member, Field field) {
        try {
            field.set(parent, member);
        } catch (IllegalAccessException e) {
            String message = "While setting filtering %s of %s";
            log.error(String.format(message, field.getName(), parent.getClass().getSimpleName()), e);
        }
    }

    public static Object getMember(Object parent, Field field) {
        try {
            return field.get(parent);
        } catch (IllegalAccessException e) {
            String message = "While getting filtering %s of %s";
            log.error(String.format(message, field.getName(), parent.getClass().getSimpleName()), e);
        }
        return null;
    }
}
