package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.sql.base.Fn;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.pengwz.dynamic.constant.Constant.GET_PATTERN;
import static com.pengwz.dynamic.constant.Constant.IS_PATTERN;

public class ReflectUtils {

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
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static List<Field> getAllFields(Class<?> tClass) {
        if(Objects.isNull(tClass))
            return null;
        Field[] declaredFields = tClass.getDeclaredFields();
        return Arrays.asList(declaredFields);
    }

    public static List<String> getOriginalFields(List<Field> fields) {
        if(CollectionUtils.isEmpty(fields))
            return null;
        List<String> list = new ArrayList<>();
        fields.forEach(f -> list.add(f.getName()));
        return list;
    }

    public static String subTypeName(Field field) {
        String typeName = field.getGenericType().getTypeName();
        return typeName.substring(typeName.lastIndexOf(".") + 1);
    }
    public static String subTypeName(String typeName) {
        return typeName.substring(typeName.lastIndexOf(".") + 1);
    }
}
