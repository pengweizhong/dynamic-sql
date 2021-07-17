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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Check {

    private static final Log log = LogFactory.getLog(Check.class);

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
                throw new BraveException("字段类型不可以是基本类型，因为基本类型在任何时候都不等于null，字段名：" + field.getName() + "，发生在表：" + tableName);
            }
            TableInfo tableInfo = new TableInfo();
            String column;
            Column columnAnno = field.getAnnotation(Column.class);
            if (Objects.nonNull(columnAnno)) {
                if (StringUtils.isEmpty(columnAnno.value())) {
                    throw new BraveException("Column列名不可以为空，字段名：" + field.getName() + "，发生在表：" + tableName);
                }
                column = columnAnno.value().replace(" ", "");
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
