package com.pengwz.dynamic.check;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.GenerationType;
import com.pengwz.dynamic.anno.Id;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class Check {

    private static final Log log = LogFactory.getLog(Check.class);

    public static void checkAndSave(Class<?> currentClass, String tableName, String dataSource) {
        boolean existsTable = ContextApplication.existsTable(tableName, dataSource);
        if (existsTable) {
            return;
        }
        List<Field> allFiledList = new ArrayList<>();
        recursionGetAllFields(currentClass,allFiledList);
        int idCount = 0;
        List<TableInfo> tableInfos = new ArrayList<>();
        for (Field field : allFiledList) {
            if (checkedFieldType(field)) {
                continue;
            }
            if (field.getType().isPrimitive()) {
                throw new BraveException("字段类型不可以是基本类型，因为基本类型在任何时候都不等于null，字段名：" + field.getName() + "，发生在表：" + tableName);
            }
            TableInfo tableInfo = new TableInfo();
            Id id = field.getAnnotation(Id.class);
            if (Objects.nonNull(id)) {
                idCount++;
                tableInfo.setPrimary(true);
            } else {
                tableInfo.setPrimary(false);
            }
            tableInfo.setField(field);
            tableInfo.setColumn(getColumnName(field, tableName));
            GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
            if (Objects.nonNull(generatedValue)) {
                if (!Number.class.isAssignableFrom(field.getType()) && generatedValue.strategy().equals(GenerationType.AUTO)) {
                    log.warn("当自增类型为GenerationType.AUTO时，只有类型为数值时才有意义。但是此时类型为：" + field.getType() + "，发生在表：" + tableName);
                }
                tableInfo.setGenerationType(generatedValue.strategy());
            }
            tableInfos.add(tableInfo);
        }
        if (idCount > 1) {
            throw new BraveException("获取到多个主键，发生在表：" + tableName);
        }
        //校验重复列  空列
        if (tableInfos.isEmpty()) {
            throw new BraveException("映射实体类未发现可用属性，发生在表：" + tableName);
        }
        Map<String, List<TableInfo>> stringListMap = tableInfos.stream().collect(Collectors.groupingBy(TableInfo::getColumn));
        stringListMap.forEach((column, tableInfoList) -> {
            if (tableInfoList.size() > 1) {
                throw new BraveException("重复的列名：" + column + "，发生在表：" + tableName);
            }
        });
        ContextApplication.saveTable(dataSource, tableName, tableInfos);
    }

    public static void recursionGetAllFields(Class<?> fatherClass, List<Field> fieldList) {
        //仅递归到Object的直接子类
        if (Object.class.equals(fatherClass)) {
            return;
        }
        //递归父类，获取所有字段，此处仅做获取，不过滤
        Field[] declaredFields = fatherClass.getDeclaredFields();
        Collections.addAll(fieldList, declaredFields);
        recursionGetAllFields(fatherClass.getSuperclass(), fieldList);
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

    public static String getColumnName(Field field, String tableName) {
        Column columnAnno = field.getAnnotation(Column.class);
        String column;
        if (Objects.nonNull(columnAnno)) {
            if (StringUtils.isEmpty(columnAnno.value())) {
                throw new BraveException("Column列名不可以为空，字段名：" + field.getName() + "，发生在表：" + tableName);
            }
            column = columnAnno.value().replace(" ", "");
        } else {
            column = StringUtils.caseField(field.getName());
        }
        return column;
    }

    /**
     * 字段类型如果不允许，就返回true
     */
    public static boolean checkedFieldType(Field field) {
        //静态类型，final类型等等不参与数据库查询
        if (Modifier.isFinal(field.getModifiers())
                || Modifier.isAbstract(field.getModifiers())
                || Modifier.isStatic(field.getModifiers())
                || Modifier.isNative(field.getModifiers())
                || Modifier.isInterface(field.getModifiers()
        )
        ) {
            return true;
        }
        return false;
    }
}
