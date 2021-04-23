package com.pengwz.dynamic.check;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.Id;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.utils.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Check {

    public static void checkAndSave(Class<?> currentClass, String tableName, String dataSource) {
        boolean existsTable = ContextApplication.existsTable(tableName, dataSource);
        if (existsTable) {
            return;
        }
        Field[] declaredFields = currentClass.getDeclaredFields();
        int idCount = 0;
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
            GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
            if (Objects.nonNull(generatedValue)) {
                if (String.class.isAssignableFrom(field.getType())) {
                    throw new BraveException("字符类型的主键不支持递增");
                }
                tableInfo.setGeneratedValue(true);
            } else {
                tableInfo.setGeneratedValue(false);
            }
            tableInfos.add(tableInfo);
        }
        if (idCount > 1) {
            throw new BraveException(tableName + "表获取到多个主键");
        }
        ContextApplication.saveTable(dataSource, tableName, tableInfos);
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
