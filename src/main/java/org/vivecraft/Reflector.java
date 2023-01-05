package org.vivecraft;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class Reflector {

    private static final String nmsVersion;
    private static final int mcVersion;

    static {
        String versionString = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        nmsVersion = "net.minecraft.server." + versionString + '.';
        mcVersion = Integer.parseInt(versionString.split("_")[1]);
    }

    /**
     * Returns the NMS class with the given name.
     * In mc versions 1.17 and higher, <code>pack</code> is used for the
     * package the class is in. Previous versions ignore <code>pack</code>.
     *
     * @param pack The non-null package name that contains the class defined
     *             by <code>name</code>. Make sure the string ends with a dot.
     * @param name The non-null name of the class to find.
     * @return The NMS class with that name.
     */
    public static Class<?> getNMSClass(String pack, String name) {
        String className;

        if (mcVersion < 17)
            className = nmsVersion + name;
        else
            className = "net.minecraft." + pack + '.' + name;

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalError("Failed to get NMS class: " + className, e);
        }
    }

    public static MethodAccessor getMethod(Class<?> target, String name, Class<?>... params) {
        try {
            return new MethodAccessor(target.getDeclaredMethod(name, params));
        } catch (NoSuchMethodException ex) {
            throw new InternalError(ex);
        }
    }

    /**
     * Shorthand for {@link #getMethod(Class, Class, int, Class[])}.
     *
     * @param target     The non-null target class that declares the method.
     * @param returnType The nullable returned type of the method.
     * @param params     The non-null parameters of the method.
     * @return The non-null method that matches the given signature.
     * @throws IllegalArgumentException If no such method exists.
     */
    public static MethodAccessor getMethod(Class<?> target, Class<?> returnType, Class<?>... params) {
        return getMethod(target, returnType, 0, params);
    }

    /**
     * Returns a {@link Method} by its returned datatype and parameters. This
     * method should be used for getting obfuscated methods, or if the name of
     * the method is otherwise not guaranteed to stay the same.
     * <p>
     * If the <code>target</code> does not declare a matching method, this
     * method will search in the parent class recursively.
     *
     * @param target     The non-null target class that declares the method.
     * @param returnType The nullable returned type of the method.
     * @param index      The index of the method. Sometimes this method will
     *                   match multiple methods. For these methods,
     *                   <code>index</code> is required.
     * @param params     The non-null parameters of the method.
     * @return The non-null method that matches the given signature.
     * @throws IllegalArgumentException If no such method exists.
     */
    public static MethodAccessor getMethod(Class<?> target, Class<?> returnType, int index, Class<?>... params) {
        for (final Method method : target.getDeclaredMethods()) {
            if (returnType != null && !returnType.isAssignableFrom(method.getReturnType()))
                continue;
            else if (!Arrays.equals(method.getParameterTypes(), params))
                continue;
            else if (index-- > 0)
                continue;

            //noinspection deprecation
            if (!method.isAccessible())
                method.setAccessible(true);

            return new MethodAccessor(method);
        }

        // Recursively check superclasses for the method
        if (target.getSuperclass() != null)
            return getMethod(target.getSuperclass(), returnType, index, params);

        throw new IllegalArgumentException("Cannot find field with return=" + returnType);
    }

    public static FieldAccessor getField(Class<?> target, String name) {
        try {
            return new FieldAccessor(target.getDeclaredField(name));
        } catch (NoSuchFieldException ex) {
            throw new InternalError(ex);
        }
    }

    /**
     * Shorthand for {@link #getField(Class, Class, int)}.
     *
     * @param target Which class to search in.
     * @param type   The type we are looking for.
     * @return The non-null field.
     * @throws IllegalArgumentException If no such field exists.
     */
    public static FieldAccessor getField(Class<?> target, Class<?> type) {
        return getField(target, type, 0);
    }

    /**
     * Searches for a field based on the type of the field. This has the
     * advantage of being more stable across Minecraft versions (since
     * obfuscated names change between versions).
     *
     * @param target Which class to search in.
     * @param type   The type we are looking for.
     * @param index  If there are multiple fields of the same type, which index?
     * @return The non-null field.
     * @throws IllegalArgumentException If no such field exists.
     */
    public static FieldAccessor getField(Class<?> target, Class<?> type, int index) {
        for (final Field field : target.getDeclaredFields()) {

            // Type check. Make sure the field's datatype
            // matches the data type we are trying to find
            if (!type.isAssignableFrom(field.getType()))
                continue;
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            if (index-- > 0)
                continue;

            //noinspection deprecation
            if (!field.isAccessible())
                field.setAccessible(true);

            return new FieldAccessor(field);
        }

        // if the class has a superclass, then recursively check
        // the super class for the field
        Class<?> superClass = target.getSuperclass();
        if (superClass != null)
            return getField(superClass, type, index);

        throw new IllegalArgumentException("Cannot find field with type " + type);
    }

    /**
     * A MethodAccessor holds a reference to a declared method. The invoke
     * method catches the reflection exceptions and rethrows them as an
     * {@link InternalError} so you don't need a try-catch in your code.
     *
     * @param method The non-null method to invoke.
     */
    public record MethodAccessor(Method method) {
        public Object invoke(Object instance, Object... args) {
            try {
                return method.invoke(instance, args);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new InternalError(ex);
            }
        }
    }

    /**
     * A FieldAccessor holds a reference to a declared field. The get/set
     * methods catch the reflection exceptions and rethrows them as an
     * {@link InternalError} so you don't need a try-catch in your code.
     *
     * @param field The non-null field to access.
     */
    public record FieldAccessor(Field field) {
        public Object get(Object instance) {
            try {
                return field.get(instance);
            } catch (IllegalAccessException ex) {
                throw new InternalError(ex);
            }
        }

        public void set(Object instance, Object value) {
            try {
                field.set(instance, value);
            } catch (IllegalAccessException ex) {
                throw new InternalError(ex);
            }
        }
    }
}