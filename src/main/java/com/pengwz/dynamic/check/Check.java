package com.pengwz.dynamic.check;

import com.pengwz.dynamic.anno.*;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.ColumnInfo;
import com.pengwz.dynamic.model.DataSourceInfo;
import com.pengwz.dynamic.model.DbType;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.utils.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class Check {

    private Check() {
    }

    private static final Log log = LogFactory.getLog(Check.class);

    public static void checkAndSave(Class<?> currentClass) {
        final List<TableInfo> cacheTableInfos = ContextApplication.getTableInfos(currentClass);
        if (CollectionUtils.isNotEmpty(cacheTableInfos)) {
            return;
        }
        final List<TableInfo> builderTableInfos = getBuilderTableInfos(currentClass);
        ContextApplication.saveTableInfos(currentClass, builderTableInfos);
    }

    public static List<TableInfo> getBuilderTableInfos(Class<?> currentClass) {
        return getBuilderTableInfos(currentClass, true);
    }

    public static List<TableInfo> getBuilderTableInfos(Class<?> currentClass, boolean isCheckTableName) {
        Table table = currentClass.getAnnotation(Table.class);
        if (StringUtils.isEmpty(table.value()) && isCheckTableName) {
            throw new BraveException("当前实体类：" + currentClass + "未获取到表名");
        }
        String dataSource = DataSourceManagement.initDataSourceConfig(table.dataSourceClass());
        String tableName = table.value().trim();
        List<Field> allFiledList = new ArrayList<>();
        recursionGetAllFields(currentClass, allFiledList);
        List<TableInfo> tableInfos = builderTableInfos(allFiledList, tableName, dataSource);
        //校验重复列  空列
        if (tableInfos.isEmpty()) {
            throw new BraveException("映射实体类未发现可用属性，发生在表：" + tableName);
        }
        List<TableInfo> primaryList = tableInfos.stream().filter(TableInfo::isPrimary).collect(Collectors.toList());
        if (primaryList.size() > 1) {
            throw new BraveException("获取到多个主键，发生在表：" + tableName);
        }
        Map<String, List<TableInfo>> stringListMap = tableInfos.stream().collect(Collectors.groupingBy(TableInfo::getColumn));
        stringListMap.forEach((column, tableInfoList) -> {
            if (tableInfoList.size() > 1) {
                throw new BraveException("重复的列名：" + column + "，发生在表：" + tableName);
            }
        });
        return tableInfos;
    }


    public static List<TableInfo> builderTableInfos(List<Field> allFiledList, String tableName, String dataSource) {
        List<TableInfo> tableInfos = new ArrayList<>();
        for (Field field : allFiledList) {
            if (checkedFieldType(field)) {
                continue;
            }
            if (field.getType().isPrimitive()) {
                throw new BraveException("字段类型不可以是基本类型，因为基本类型在任何时候都不等于null，字段名：" + field.getName() + "，发生在表：" + tableName);
            }
            if (field.getAnnotation(ColumnIgnore.class) != null) {
                continue;
            }
            TableInfo tableInfo = new TableInfo();
            Id id = field.getAnnotation(Id.class);
            if (Objects.nonNull(id)) {
                tableInfo.setPrimary(true);
                GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
                if (Objects.nonNull(generatedValue)) {
                    if (!Number.class.isAssignableFrom(field.getType()) && generatedValue.strategy().equals(GenerationType.AUTO)) {
                        throw new BraveException("使用AUTO自增时，只有类型为数值时才有意义。但是此时类型为：" + field.getType() + "，发生在表：" + tableName);
                    }
                    List<GenerationType> uuidList = Arrays.asList(GenerationType.UUID, GenerationType.SIMPLE_UUID, GenerationType.UPPER_SIMPLE_UUID, GenerationType.UPPER_UUID);
                    if (!String.class.equals(field.getType()) && uuidList.contains(generatedValue.strategy())) {
                        throw new BraveException("使用UUID自增时，属性必须为String类型，但是此时类型为：" + field.getType() + "，发生在表：" + tableName);
                    }
                    if (GenerationType.SEQUENCE.equals(generatedValue.strategy())) {
                        if (!Number.class.isAssignableFrom(field.getType())) {
                            throw new BraveException("使用序列自增时，属性必须为Number类型，但是此时类型为：" + field.getType() + "，发生在表：" + tableName);
                        }
                        if (StringUtils.isBlank(generatedValue.sequenceName())) {
                            throw new BraveException("使用序列自增时，必须指定序列名[GeneratedValue#sequenceName()]，且序列名不允许为空");
                        }
                    }
                    tableInfo.setGeneratedValue(generatedValue);
                }
            } else {
                tableInfo.setPrimary(false);
            }
            tableInfo.setField(field);
            final ColumnInfo columnInfo = getFixColumnInfo(field, dataSource);
            tableInfo.setColumn(columnInfo.getValue());
            tableInfo.setJsonMode(columnInfo.getJsonMode());
            tableInfo.setTableAlias(columnInfo.getTableAlias());
            tableInfo.setDataSourceName(dataSource);
            DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSource);
            tableInfo.setTableName(getTableName(tableName, dataSourceInfo.getDbType()));
            tableInfos.add(tableInfo);
        }
        return tableInfos;
    }

    public static void recursionGetAllFields(Class<?> thisClass, List<Field> fieldList) {
        //仅递归到Object的直接子类
        if (Object.class.equals(thisClass)) {
            return;
        }
        //递归父类，获取所有字段，此处仅做获取，不过滤
        Field[] declaredFields = thisClass.getDeclaredFields();
        Collections.addAll(fieldList, declaredFields);
        recursionGetAllFields(thisClass.getSuperclass(), fieldList);
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

    public static ColumnInfo getFixColumnInfo(Field field, String dataSource) {
        final ColumnInfo columnInfo = getColumnInfo(field);
        final String column = columnInfo.getValue();
        if (StringUtils.isNotBlank(dataSource)) {
            DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSource);
            columnInfo.setValue(splicingName(dataSourceInfo.getDbType(), column));
        }
        return columnInfo;
    }

    public static ColumnInfo getColumnInfo(Field field) {
        Column columnAnno = field.getAnnotation(Column.class);
        final ColumnInfo columnInfo = new ColumnInfo();
        if (Objects.nonNull(columnAnno)) {
            if (StringUtils.isEmpty(columnAnno.value())) {
                columnInfo.setValue(com.pengwz.dynamic.utils.StringUtils.caseField(field.getName()));
            } else {
                columnInfo.setValue(columnAnno.value().replace(" ", ""));
            }
            columnInfo.setTableAlias(columnAnno.tableAlias().replace(" ", ""));
        } else {
            ColumnJson columnJson = field.getAnnotation(ColumnJson.class);
            if (Objects.nonNull(columnJson)) {
                if (StringUtils.isEmpty(columnJson.value())) {
                    columnInfo.setValue(com.pengwz.dynamic.utils.StringUtils.caseField(field.getName()));
                } else {
                    columnInfo.setValue(columnJson.value().replace(" ", ""));
                }
                columnInfo.setTableAlias(columnJson.tableAlias().replace(" ", ""));
            } else {
                columnInfo.setValue(com.pengwz.dynamic.utils.StringUtils.caseField(field.getName()));
            }
        }
        return columnInfo;
    }

    public static String getTableName(String tableName, DbType dbType) {
        tableName = tableName.trim();
        if (tableName.contains(".")) {
            String[] splitTableName = tableName.split("\\.");
            if (splitTableName.length != 2) {
                throw new BraveException("错误的表名称：" + tableName);
            }
            String database = splitTableName[0];
            String table = splitTableName[1];
            if (StringUtils.isBlank(database) || StringUtils.isBlank(table)) {
                throw new BraveException("错误的表名称：" + tableName);
            }
            return splicingName(dbType, database) + "." + splicingName(dbType, table);
        }
        return splicingName(dbType, tableName);
    }

    /**
     * 将给定的表名或者字段名拼接上限定符，若限定符本身已经存在，则不会拼接
     */
    public static String splicingName(DbType dbType, String name) {
        String fixName = "";
        String trimName = name.trim();
        if (dbType.equals(DbType.ORACLE)) {
            if (!trimName.startsWith("\"")) {
                fixName = fixName + "\"";
            }
            fixName = fixName + trimName;
            if (!trimName.endsWith("\"")) {
                fixName = fixName + "\"";
            }
            return fixName;
        }
        if (!trimName.startsWith("`")) {
            fixName = fixName + "`";
        }
        fixName = fixName + trimName;
        if (!trimName.endsWith("`")) {
            fixName = fixName + "`";
        }
        return fixName;
    }

    public static String unSplicingName(String name) {
        if (name.contains("`")) {
            name = name.replace("`", "");
        }
        if (name.contains("\"")) {
            name = name.replace("\"", "");
        }
        return name;
    }

    /**
     * 字段类型如果不允许，就返回true
     */
    public static boolean checkedFieldType(Field field) {
        //静态类型，final类型等等不参与数据库查询
        return Modifier.isFinal(field.getModifiers())
                || Modifier.isAbstract(field.getModifiers())
                || Modifier.isStatic(field.getModifiers())
                || Modifier.isNative(field.getModifiers())
                || Modifier.isInterface(field.getModifiers())
                || Modifier.isTransient(field.getModifiers());
    }
}
