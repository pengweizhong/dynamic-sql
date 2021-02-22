package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.utils.CollectionUtils;
import com.pengwz.dynamic.utils.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public class ContextApplication {

    private static final Map<String, Map<String, List<TableInfo>>> dataBaseMap = new ConcurrentHashMap<>();

    public static boolean existsTable(String tableName, Class<?> dataSourceClass) {
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        if (Objects.nonNull(tableMap)) {
            List<TableInfo> tableInfos = tableMap.get(tableName);
            if (Objects.nonNull(tableInfos)) {
                return true;
            }
        }
        return false;
    }

    public static String formatAllColumToStr(Class<?> dataSourceClass, String tableName) {
        List<String> columList = getAllColumnList(dataSourceClass, tableName);
        return String.join(",", columList);
    }

    public static List<String> getAllColumnList(Class<?> dataSourceClass, String tableName) {
        List<String> members = new ArrayList<>();
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        if (tableMap == null) {
            return members;
        }
        List<TableInfo> tableInfos = tableMap.get(tableName);
        if (tableInfos == null) {
            return members;
        }
        tableInfos.forEach(tableInfo -> {
            members.add(tableInfo.getColumn());
        });
        return members;
    }

    public static List<TableInfo> getTableInfos(Class<?> dataSourceClass, String tableName) {
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        if (tableMap != null) {
            return tableMap.get(tableName);
        }
        throw new BraveException("无法匹配数据源：" + dataSourceClass + "或表：" + tableName);
    }

    public static String getColumnByField(Class<?> dataSourceClass, String tableName, String fieldName) {
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        List<TableInfo> tableInfos = tableMap.get(tableName);
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.getField().getName().equals(fieldName)) {
                return tableInfo.getColumn();
            }
        }
        throw new BraveException(tableName + "中未识别的字段：" + fieldName);
    }

    public static String getPrimaryKey(Class<?> dataSourceClass, String tableName) {
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        List<TableInfo> tableInfos = tableMap.get(tableName);
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.isPrimary()) {
                return tableInfo.getColumn();
            }
        }
        throw new BraveException(tableName + "中未获取到主键字段");
    }

    public static TableInfo getTableInfoPrimaryKey(Class<?> dataSourceClass, String tableName) {
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        List<TableInfo> tableInfos = tableMap.get(tableName);
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.isPrimary()) {
                return tableInfo;
            }
        }
        throw new BraveException(tableName + "中未获取到主键信息");
    }

    public static Map<String, Map<String, List<TableInfo>>> getAll() {
        return dataBaseMap;
    }

    public static void saveTable(Class<?> dataSourceClass, String tableName, List<TableInfo> tableInfos) {
        if (StringUtils.isEmpty(tableName)) {
            throw new BraveException("待保存的表名不可为空");
        }
        if (CollectionUtils.isEmpty(tableInfos)) {
            throw new BraveException("待保存的表字段不可为空");
        }
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        if (tableMap == null) {
            tableMap = new ConcurrentHashMap<>();
            dataBaseMap.put(dataSource, tableMap);
        }
        tableMap.put(tableName, tableInfos);
    }

    public static void clear() {
        dataBaseMap.clear();
    }

    public static class TableInfo {
        //数据库字段名
        private String column;
        //字段类型
        private String type;
        //是否为主键
        private boolean isPrimary;
        //新增数据是否需要生成主键
        private boolean isGeneratedValue;
        //get方法
        private Method getMethod;
        //set方法
        private Method setMethod;
        //实体类字段
        private Field field;

        //其他属性....
        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        public void setPrimary(boolean primary) {
            isPrimary = primary;
        }

        public boolean isGeneratedValue() {
            return isGeneratedValue;
        }

        public void setGeneratedValue(boolean generatedValue) {
            isGeneratedValue = generatedValue;
        }

        public Method getGetMethod() {
            return getMethod;
        }

        public void setGetMethod(Method getMethod) {
            this.getMethod = getMethod;
        }

        public Method getSetMethod() {
            return setMethod;
        }

        public void setSetMethod(Method setMethod) {
            this.setMethod = setMethod;
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
        }

        @Override
        public String toString() {
            return "TableInfo{" +
                    "column='" + column + '\'' +
                    ", type='" + type + '\'' +
                    ", isPrimary=" + isPrimary +
                    ", isGeneratedValue=" + isGeneratedValue +
                    ", getMethod=" + getMethod +
                    ", setMethod=" + setMethod +
                    ", field=" + field +
                    '}';
        }
    }

}
