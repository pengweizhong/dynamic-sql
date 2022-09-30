package com.pengwz.dynamic.check;

import com.pengwz.dynamic.anno.*;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.*;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.utils.ReflectUtils;
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

    public static TableInfo getBuilderTableInfo(Class<?> currentClass) {
        final TableInfo tableInfoCache = ContextApplication.getTableInfoCache(currentClass);
        if (tableInfoCache != null) {
            return tableInfoCache;
        }
        final TableInfo tableInfo = builderTableInfo(currentClass);
        //校验重复列  空列
        if (tableInfo.getTableColumnInfos().isEmpty()) {
            throw new BraveException("映射实体类未发现可用属性，发生在表：" + tableInfo.getTableName());
        }
        final List<TableColumnInfo> tableColumnInfos = tableInfo.getTableColumnInfos();
        List<TableColumnInfo> primaryList = tableColumnInfos.stream().filter(TableColumnInfo::isPrimary).collect(Collectors.toList());
        if (primaryList.size() > 1) {
            throw new BraveException("获取到多个主键，发生在表：" + tableInfo.getTableName());
        }
        Map<String, List<TableColumnInfo>> stringListMap = tableColumnInfos.stream().collect(Collectors.groupingBy(TableColumnInfo::getColumn));
        stringListMap.forEach((column, tableInfoList) -> {
            if (tableInfoList.size() > 1) {
                String errSuffix;
                if (StringUtils.isEmpty(tableInfo.getTableName())) {
                    errSuffix = "发生在类：" + currentClass.getCanonicalName();
                } else {
                    errSuffix = "发生在表：" + tableInfo.getTableName();
                }
                throw new BraveException("重复的列名：" + column + "，" + errSuffix + "；可能是手动copy错误或发生在继承类中，请检查");
            }
        });
        if (tableInfo.isCache()) {
            ContextApplication.saveTableInfo(currentClass, tableInfo);
        }
        return tableInfo;
    }

    /**
     * 根据当前表实体类构建TableInfo对象
     *
     * @param currentClass 当前表实体类
     * @return TableInfo
     */
    public static TableInfo builderTableInfo(Class<?> currentClass) {
        Table table = currentClass.getAnnotation(Table.class);
        if (table == null) {
            throw new BraveException("实体类必须指定Table注解，发生在类：" + currentClass.getCanonicalName());
        }
        String tableName = table.value().trim();
        if (StringUtils.isEmpty(tableName)) {
            throw new BraveException("表名不可为空，发生在类：" + currentClass.getCanonicalName());
        }
        String dataSource = DataSourceManagement.initDataSourceConfig(table.dataSourceClass());
        final Field[] allDeclaredFields = ReflectUtils.getAllDeclaredFields(currentClass);
        if (allDeclaredFields.length == 0) {
            throw new BraveException("实体类未发现可用属性，发生在类：" + currentClass.getCanonicalName());
        }
        final TableInfo tableInfo = new TableInfo();
        List<TableColumnInfo> tableColumnInfos = new ArrayList<>();
        tableInfo.setTableColumnInfos(tableColumnInfos);
        //join 的数据源
        HashSet<String> joinDataSourceNameSet = new HashSet<>();
        for (Field field : allDeclaredFields) {
            if (checkedFieldType(field)) {
                continue;
            }
            if (field.getAnnotation(ColumnIgnore.class) != null) {
                continue;
            }
            if (field.getType().isPrimitive()) {
                throw new BraveException("字段类型不可以是基本类型，因为基本类型在任何时候都不等于null，字段名：" + field.getName() + "，发生在表：" + tableName);
            }
            final TableColumnInfo tableColumnInfo = new TableColumnInfo();
            Id id = field.getAnnotation(Id.class);
            if (Objects.nonNull(id)) {
                tableColumnInfo.setPrimary(true);
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
                    tableColumnInfo.setGeneratedValue(generatedValue);
                }
            } else {
                tableColumnInfo.setPrimary(false);
            }
            tableColumnInfo.setField(field);

            final ColumnInfo columnInfo = getFixColumnInfo(field, dataSource);
            tableColumnInfo.setColumn(columnInfo.getValue());
            tableColumnInfo.setJsonMode(columnInfo.getJsonMode());
//            final TableInfo dependentTableInfo = columnInfo.getDependentTableInfo();
//            if (dependentTableInfo != null) {
//                tableColumnInfo.setTableAlias(dependentTableInfo.getTableName());
//                final String dataSourceName = dependentTableInfo.getDataSourceName();
//                joinDataSourceNameSet.add(dataSourceName);
//            }
            tableColumnInfos.add(tableColumnInfo);
        }
        //为null时，走多表查询，当前对象为结果集对象，并非表对象
        //如果数据源集合不为空，则需要校验
//        if (dataSource == null && !joinDataSourceNameSet.isEmpty()) {
//            if (joinDataSourceNameSet.size() > 1) {
//                throw new BraveException("多表操作时，不可以跨数据源！发生错误的类：" + currentClass.getCanonicalName());
//            }
//            //给定多表查询的数据源
//            dataSource = joinDataSourceNameSet.iterator().next();
//        }
        tableInfo.setDataSourceName(dataSource);
        DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSource);
        tableInfo.setTableName(getTableName(tableName, dataSourceInfo.getDbType()));
        return tableInfo;
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
            final TableInfo dependentTableInfo = getDependentTableInfo(columnAnno);
//            columnInfo.setDependentTableInfo(dependentTableInfo);
            return columnInfo;
        }
        ColumnJson columnJson = field.getAnnotation(ColumnJson.class);
        if (Objects.nonNull(columnJson)) {
            if (StringUtils.isEmpty(columnJson.value())) {
                columnInfo.setValue(com.pengwz.dynamic.utils.StringUtils.caseField(field.getName()));
            } else {
                columnInfo.setValue(columnJson.value().replace(" ", ""));
            }
            final TableInfo dependentTableInfo = getDependentTableInfo(columnJson);
//            columnInfo.setDependentTableInfo(dependentTableInfo);
        } else {
            columnInfo.setValue(com.pengwz.dynamic.utils.StringUtils.caseField(field.getName()));
        }
        return columnInfo;
    }

    /**
     * 获取字段依赖的表信息对象
     *
     * @param columnAnno 当前列注释
     * @return 表信息对象
     */
    public static TableInfo getDependentTableInfo(Column columnAnno) {
        final Class<?> aClass = columnAnno.dependentTableClass();
        if (!aClass.equals(Void.class)) {
            return ContextApplication.getTableInfo(aClass);
        }
        return null;
    }

    public static TableInfo getDependentTableInfo(ColumnJson columnAnno) {
        final Class<?> aClass = columnAnno.dependentTableClass();
        if (!aClass.equals(Void.class)) {
            return ContextApplication.getTableInfo(aClass);
        }
        return null;
    }

    public static String getTableName(String tableName, DbType dbType) {
        if (StringUtils.isBlank(tableName)) {
            return null;
        }
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

//    public enum ViewType {
//        /**
//         * 表
//         */
//        TABLE,
////        /**
////         * 视图
////         */
////        VIEW,
//        /**
//         * 多表查询的结果集，往往是多表查询的产物
//         */
//        RESULT;
//    }
}
