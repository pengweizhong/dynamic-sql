package com.pengwz.dynamic.utils;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtilsTest {

    @Test
    public void fnToFieldName() {
    }

    @Test
    public void getImplClassname() {
    }

    @Test
    public void findMethod() {
    }

    @Test
    public void invokeMethod() {
    }

    @Test
    public void getDeclaredMethods() {
        final Method[] declaredMethods = ReflectUtils.getDeclaredMethods(EmptyClass.class);
        for (int i = 0; i < declaredMethods.length; i++) {
            System.out.println(declaredMethods[i]);
        }
        System.out.println("ok");
    }

    @Test
    public void getDeclaredMethods2() {
        final Method[] declaredMethods = ReflectUtils.getDeclaredMethods(Child.class);
        for (int i = 0; i < declaredMethods.length; i++) {
            System.out.println(declaredMethods[i]);
        }
        System.out.println("ok");
    }

    @Test
    public void getAllDeclaredMethods() {
        final Method[] declaredMethods = ReflectUtils.getAllDeclaredMethods(Child.class);
        for (int i = 0; i < declaredMethods.length; i++) {
            System.out.println(declaredMethods[i]);
        }
        System.out.println("ok");
    }

    @Test
    public void makeAccessible() {
    }

    @Test
    public void findField() {
        System.out.println(ReflectUtils.findField(EmptyClass.class, "id"));
        System.out.println(ReflectUtils.findField(Child.class, "id"));
        System.out.println(ReflectUtils.findField(Father.class, "id"));
    }

    @Test
    public void findField2() {
        System.out.println(ReflectUtils.findField(Child.class, "id", Long.class));
    }

    @Test
    public void getAllDeclaredFields() {
        final Field[] allDeclaredFields = ReflectUtils.getAllDeclaredFields(EmptyClass.class);
        for (int i = 0; i < allDeclaredFields.length; i++) {
            System.out.println(allDeclaredFields[i]);
        }
        System.out.println("ok");
    }

    @Test
    public void getAllDeclaredFields_Child() {
        final Field[] allDeclaredFields = ReflectUtils.getAllDeclaredFields(Child.class);
        for (int i = 0; i < allDeclaredFields.length; i++) {
            System.out.println(allDeclaredFields[i]);
        }
        System.out.println("ok");
    }

    @Test
    public void getAllDeclaredFields_Father() {
        final Field[] allDeclaredFields = ReflectUtils.getAllDeclaredFields(Father.class);
        for (int i = 0; i < allDeclaredFields.length; i++) {
            System.out.println(allDeclaredFields[i]);
        }
        System.out.println("ok");
    }

    @Test
    public void getDeclaredFields() {
        final Field[] declaredFields = ReflectUtils.getDeclaredFields(EmptyClass.class);
        for (int i = 0; i < declaredFields.length; i++) {
            System.out.println(declaredFields[i]);
        }
        System.out.println("ok");
    }


    @Test
    public void getFieldValue() {
    }

    @Test
    public void setFieldValue() {
    }


    public static class EmptyClass {
        //什么也有
    }

    public static class Child extends Father {
        private Integer id;
        private String name;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Father {
        private Long id;

        public Long getFatherId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}