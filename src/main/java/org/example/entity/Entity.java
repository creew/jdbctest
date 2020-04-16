package org.example.entity;

import java.lang.reflect.Field;

public class Entity {
    public Object getValue(String name) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = this.getClass();
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(this);
    }

    public void setValue(String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = this.getClass();
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(this, value);
    }
}
