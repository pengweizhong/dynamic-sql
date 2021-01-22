package com.pengwz.dynamic.check;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.Id;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.ContextApplication.TableInfo;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.utils.ReflectUtils;
import com.pengwz.dynamic.utils.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Check {

    public static void checkAndSave(Class<?> currentClass, String tableName, Class<?> dataSourceClass) {
        boolean existsTable = ContextApplication.existsTable(tableName, dataSourceClass);
        if (existsTable) {
            return;
        }
        Field[] declaredFields = currentClass.getDeclaredFields();
        int idCount = 0;
        Method[] methods = currentClass.getDeclaredMethods();
        List<TableInfo> tableInfos = new ArrayList<>();
        for (Field field : declaredFields) {
            //静态类型，final类型不参与数据库查询
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            if (field.getType().isPrimitive()) {
                throw new BraveException("字段类型不可以是基本类型，因为基本类型在任何时候都不等于null");
            }
            TableInfo tableInfo = new TableInfo();
            String column;
            Column columnAnno = field.getAnnotation(Column.class);
            if (Objects.nonNull(columnAnno)) {
                column = columnAnno.value().trim();
            } else {
                column = StringUtils.caseField(field.getName());
            }
            Id id = field.getAnnotation(Id.class);
            if (Objects.nonNull(id)) {
                idCount++;
                tableInfo.setPrimary(true);
            } else {
                tableInfo.setPrimary(false);
            }
            tableInfo.setField(field);
            tableInfo.setColumn(column);
            tableInfo.setType(ReflectUtils.subTypeName(field));
            //如果==2，说明getset方法均已赋值，跳出循环
            int getSetInit = 0;
            for (Method method : methods) {
                if (getSetInit == 2) {
                    break;
                }
                String methodName = method.getName();
                String fieldName = field.getName();
                if (methodName.startsWith("get") && methodName.substring(3).equalsIgnoreCase(fieldName)) {
                    tableInfo.setGetMethod(method);
                    getSetInit++;
                }
                if (methodName.startsWith("set") && methodName.substring(3).equalsIgnoreCase(fieldName)) {
                    tableInfo.setSetMethod(method);
                    getSetInit++;
                }
            }
            GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
            if (Objects.nonNull(generatedValue)) {
                tableInfo.setGeneratedValue(true);
            } else {
                tableInfo.setGeneratedValue(false);
            }
            tableInfos.add(tableInfo);
        }
        if (idCount == 0) {
            throw new BraveException(tableName + "表未获取到主键");
        }
        if (idCount > 1) {
            throw new BraveException(tableName + "表获取到多个主键");
        }
        ContextApplication.saveTable(dataSourceClass, tableName, tableInfos);
    }

    public static <T> void checkPageInfo(PageInfo<T> pageInfo) {
        if (pageInfo == null) {
            return;
        }
        if (pageInfo.getPageIndex() < 1) {
            pageInfo.setPageIndex(1);
        }
        pageInfo.setOffset((pageInfo.getPageIndex() - 1) * pageInfo.getPageSize());

    }
}
