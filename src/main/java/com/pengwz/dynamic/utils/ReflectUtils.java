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

    private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentHashMap<>(256);
    private static final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentHashMap<>(256);

    public static String fnToFieldName(Fn fn) {
        SerializedLambda serializedLambda = serializedLambda(fn);
        String getter = serializedLambda.getImplMethodName();
        if (GET_PATTERN.matcher(getter).matches()) {
            getter = getter.substring(3);
        } else if (IS_PATTERN.matcher(getter).matches()) {
            getter = getter.substring(2);
        }
        return Introspector.decapitalize(getter);
    }

    public static <C> Class<C> getReturnTypeFromSignature(Fn fn) {
        SerializedLambda serializedLambda = serializedLambda(fn);
        String implMethodSignature = serializedLambda.getImplMethodSignature();
        // Remove the parameter part, i.e., "()" ()代表无参构造器
        String returnTypeDescriptor = implMethodSignature.substring(implMethodSignature.indexOf(')') + 1);
        // Convert descriptor to class name
        String className = descriptorToClassName(returnTypeDescriptor);
        try {
            return (Class<C>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find class for name: " + className, e);
        }
    }

    private static String descriptorToClassName(String descriptor) {
        if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
            // Object type, e.g., Ljava/time/LocalDate;
            return descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
        }
        throw new IllegalArgumentException("Unsupported descriptor: " + descriptor);
    }

    public static String getImplClassname(Fn fn) {
        SerializedLambda serializedLambda = serializedLambda(fn);
        final String implClassname = serializedLambda.getImplClass();
        return implClassname.replace("/", ".");
    }

    private static SerializedLambda serializedLambda(Fn fn) {
        try {
            Method method = fn.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            return (SerializedLambda) method.invoke(fn);
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

    public static Method[] getDeclaredMethods(Class<?> clazz) {
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

    public static Method[] getAllDeclaredMethods(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            throw new BraveException("Class must not be null");
        }
        Method[] result = NO_METHODS;
        Class<?> searchType = clazz;
        while (Object.class != searchType) {
            final Method[] declaredMethods = getDeclaredMethods(clazz);
            //保留扩容前的原始长度
            int oriLen = result.length;
            result = Arrays.copyOf(result, result.length + declaredMethods.length);
            System.arraycopy(declaredMethods, 0, result, oriLen, declaredMethods.length);
            searchType = searchType.getSuperclass();
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
                !Modifier.isPublic(field.getDeclaringClass().getModifiers())
                || Modifier.isFinal(field.getModifiers()))
                && !field.isAccessible()) {
            field.setAccessible(true);//NOSONAR
        }
    }

    public static Field[] getDeclaredFields(Class<?> clazz) {
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

    public static Field[] getAllDeclaredFields(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            throw new BraveException("Class must not be null");
        }
        Class<?> searchType = clazz;
        Field[] result = NO_FIELDS;
        while (Object.class != searchType) {
            Field[] declaredFields = getDeclaredFields(searchType);
            //保留扩容前的原始长度
            int oriLen = result.length;
            result = Arrays.copyOf(result, result.length + declaredFields.length);
            System.arraycopy(declaredFields, 0, result, oriLen, declaredFields.length);
            searchType = searchType.getSuperclass();
        }
        return result;
    }

    public static Field findField(Class<?> clazz, String name) {
        return findField(clazz, name, null);
    }


    public static Field findField(Class<?> clazz, String name, Class<?> type) {
        if (Objects.isNull(clazz)) {
            throw new BraveException("Class must not be null");
        }
        if (Objects.isNull(name)) {
            throw new BraveException("Either name of the field must be specified");
        }
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = getDeclaredFields(searchType);
            for (Field field : fields) {
                if (name.equalsIgnoreCase(field.getName()) && (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
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
            field.set(target, value);//NOSONAR
        } catch (IllegalAccessException ex) {
            throw new BraveException(ex.getMessage());
        }
    }

    public static <T> T instance(Class<T> tClass) {
        if (tClass == null) {
            return null;
        }
        try {
            return tClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BraveException(e);
        }
    }

    public static Class<?> forName(String className) {
        if (StringUtils.isEmpty(className)) {
            return null;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new BraveException(e);
        }
    }
}
