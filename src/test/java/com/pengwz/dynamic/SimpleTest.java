package com.pengwz.dynamic;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.base.Sqls;
import com.pengwz.dynamic.sql.base.enumerate.FunctionEnum;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SimpleTest {


    @Test
    public void test1() {
        System.out.println(SimpleTest.class.getCanonicalName());
        Object object = "12";
        Object object2 = 12;
        System.out.println(object instanceof Number);
        System.out.println(object2 instanceof Number);
    }

    public class SqlsImpl2 implements Sqls {

        @Override
        public Object selectByPrimaryKey(Object primaryKeyValue) {
            return null;
        }

        @Override
        public Object selectSingle() {
            return null;
        }

        @Override
        public List select() {
            return null;
        }

        @Override
        public List selectAll() {
            return null;
        }

        @Override
        public PageInfo selectPageInfo() {
            return null;
        }

        @Override
        public Integer batchInsert() {
            return null;
        }

        @Override
        public Integer insertActive() {
            return null;
        }

        @Override
        public Integer insertOrUpdate() {
            return null;
        }

        @Override
        public Integer update() {
            return null;
        }

        @Override
        public Integer updateActive() {
            return null;
        }

        @Override
        public Integer updateByPrimaryKey() {
            return null;
        }

        @Override
        public Integer updateActiveByPrimaryKey() {
            return null;
        }

        @Override
        public Integer delete() {
            return null;
        }

        @Override
        public Integer deleteByPrimaryKey(Object primaryKeyValue) {
            return null;
        }

        @Override
        public Object selectAggregateFunction(String property, FunctionEnum functionEnum, Class returnType) {
            return null;
        }
    }

    @Test
    public void testGeneric() {
        // 创建一个 Gson 实例
        Gson gson = new Gson();

        // 创建一个复杂对象
        ComplexObject complexObject = new ComplexObject("Alice", 30);

        // 使用 TypeToken 来获取复杂类型的 Type
        Type type = new TypeToken<ComplexObject>() {
        }.getType();

        // 将复杂对象转换为 JSON 字符串
        String json = gson.toJson(complexObject, type);

        // 将 JSON 字符串转换为 Map
        Map<String, Object> resultMap = gson.fromJson(json, new TypeToken<Map<Integer, Object>>() {
        }.getType());

        // 打印转换后的 Map
        System.out.println(resultMap);
    }

    static class ComplexObject {
        private String name;
        private int age;

        public ComplexObject(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}
