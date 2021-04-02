package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.base.Fn;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static com.pengwz.dynamic.constant.Constant.GET_PATTERN;
import static com.pengwz.dynamic.constant.Constant.IS_PATTERN;

public class ReflectUtils {

    private static final Logger log = Logger.getGlobal();

    private static final Method[] NO_METHODS = {};

    private static final Field[] NO_FIELDS = {};

    private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentHashMap<Class<?>, Method[]>(256);
    private static final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentHashMap<Class<?>, Field[]>(256);

    public static String fnToFieldName(Fn fn) {
        try {
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(fn);
            String getter = serializedLambda.getImplMethodName();
            if (GET_PATTERN.matcher(getter).matches()) {
                getter = getter.substring(3);
            } else if (IS_PATTERN.matcher(getter).matches()) {
                getter = getter.substring(2);
            }
            return Introspector.decapitalize(getter);
        } catch (ReflectiveOperationException e) {
            log.warning(e.getMessage());
            throw new BraveException(e.getMessage());
        }
    }


    public static Method findMethod(Class<?> clazz, String name) {
        return findMethod(clazz, name, new Class<?>[0]);
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        if (Objects.isNull(clazz)) {
            throw new BraveException("Class must not be null");
        }
        if (Objects.isNull(name)) {
            throw new BraveException("Method name must not be null");
        }
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType));
            for (Method method : methods) {
                if (name.equals(method.getName()) &&
                        (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    private static Method[] getDeclaredMethods(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            throw new BraveException("Class must not be null");
        }
        Method[] result = declaredMethodsCache.get(clazz);
        if (result == null) {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
            if (defaultMethods != null) {
                result = new Method[declaredMethods.length + defaultMethods.size()];
                System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                int index = declaredMethods.length;
                for (Method defaultMethod : defaultMethods) {
                    result[index] = defaultMethod;
                    index++;
                }
            } else {
                result = declaredMethods;
            }
            declaredMethodsCache.put(clazz, (result.length == 0 ? NO_METHODS : result));
        }
        return result;
    }

    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new LinkedList<Method>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }

    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) ||
                !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    public static Field findField(Class<?> clazz, String name) {
        return findField(clazz, name, null);
    }

    public static Field findField(Class<?> clazz, String name, Class<?> type) {
        if (Objects.isNull(clazz)) {
            throw new BraveException("Class must not be null");
        }
        if (Objects.isNull(name) || Objects.isNull(type)) {
            throw new BraveException("Either name or type of the field must be specified");
        }
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = getDeclaredFields(searchType);
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName())) &&
                        (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    private static Field[] getDeclaredFields(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            throw new BraveException("Class must not be null");
        }
        Field[] result = declaredFieldsCache.get(clazz);
        if (result == null) {
            result = clazz.getDeclaredFields();
            declaredFieldsCache.put(clazz, (result.length == 0 ? NO_FIELDS : result));
        }
        return result;
    }

    public static Object invokeMethod(Method method, Object target) {
        return invokeMethod(method, target, new Object[0]);
    }

    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception ex) {
            throw new BraveException(ex.getMessage());
        }
    }

    public static Object getFieldValue(Field field, Object target) {
        try {
            if (!Modifier.isPublic(field.getModifiers())) {
                makeAccessible(field);
            }
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new BraveException(ex.getMessage());
        }
    }

    public static void setFieldValue(Field field, Object target, Object value) {
        try {
            if (!Modifier.isPublic(field.getModifiers())) {
                makeAccessible(field);
            }
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            throw new BraveException(ex.getMessage());
        }
    }
}
