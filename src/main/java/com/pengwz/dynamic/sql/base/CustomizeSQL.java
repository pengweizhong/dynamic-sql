package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.utils.ConverterUtils;
import com.pengwz.dynamic.utils.ExceptionUtils;
import com.pengwz.dynamic.utils.ReflectUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 执行自定义SQL
 */
public class CustomizeSQL<T> {

    private static final Log log = LogFactory.getLog(CustomizeSQL.class);

    private final Class<T> target;

    private final String sql;

    private final Connection connection;

    private final String dataSourceName;

    private static final String TABLE_NAME = "unknown";

    public CustomizeSQL(Class<? extends DataSourceConfig> dataSource, Class<T> target, String sql) {
        this.target = target;
        this.sql = sql;
        this.dataSourceName = dataSource.getName();
        DataSourceManagement.initDataSourceConfig(dataSource, TABLE_NAME);
        this.connection = DataSourceManagement.initConnection(dataSource.getName());
    }

    /**
     * 为spring容器提供的构造器
     *
     * @param dataSourceName 配置类路径
     * @param target         返参类型
     * @param sql            待执行sql
     */
    public CustomizeSQL(String dataSourceName, Class<T> target, String sql) {
        this.target = target;
        this.sql = sql;
        this.dataSourceName = dataSourceName;
        this.connection = DataSourceManagement.initConnection(dataSourceName);
    }

    public CustomizeSQL(Class<T> target, String sql) {
        this.target = target;
        this.sql = sql;
        this.dataSourceName = ContextApplication.getDefalutDataSourceName();
        this.connection = DataSourceManagement.initConnection(this.dataSourceName);
    }

    @Deprecated
    public T selectSqlAndReturnSingle() {
        List<T> ts = selectSqlAndReturnList();
        if (ts.size() > 1) {
            throw new BraveException("期待返回1条结果，实际返回了" + ts.size() + "条");
        }
        return ts.size() == 1 ? ts.get(0) : null;
    }

    @Deprecated
    public List<T> selectSqlAndReturnList() {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
        List<T> selectResult = new ArrayList<>();
        List<TableInfo> tableInfos = initTableInfo();
        Map<String, TableInfo> tableInfoMap = tableInfos.stream().collect(Collectors.toMap(k -> Check.unSplicingName(k.getColumn()), v -> v));
        Field[] declaredFields = target.getDeclaredFields();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T obj = target.newInstance();
                for (Field field : declaredFields) {
                    TableInfo tableInfo = tableInfoMap.get(Check.getColumnName(field));
                    if (tableInfo == null) {
                        continue;
                    }
                    Object object = ConverterUtils.convertJdbc(target, resultSet, tableInfo);
                    ReflectUtils.setFieldValue(field, obj, object);
                }
                selectResult.add(obj);
            }
        } catch (Exception e) {
            ExceptionUtils.boxingAndThrowBraveException(e, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, resultSet, preparedStatement, connection);
        }
        return selectResult;
    }

    private List<TableInfo> initTableInfo() {
        List<Field> allFiledList = new ArrayList<>();
        Check.recursionGetAllFields(target, allFiledList);
        return Check.builderTableInfos(allFiledList, TABLE_NAME, dataSourceName);
    }

    @Deprecated
    public int executeDMLSql() {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            ExceptionUtils.boxingAndThrowBraveException(e, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, null, preparedStatement, connection);
        }
        return -1;
    }

    public T executeQuerySingle() {
        List<T> ts = executeQuery();
        if (ts.size() > 1) {
            throw new BraveException("期待返回一条结果，但是返回了" + ts.size() + "条，发生在SQL：" + sql);
        }
        return ts.isEmpty() ? null : ts.get(0);
    }

    @SuppressWarnings("all")
    public List<T> executeQuery() {
        if (Map.class.isAssignableFrom(target)) {
            return (List<T>) executeQueryForMap();
        } else if (target.getClassLoader() == null) {
            return executeQueryForObject();
        } else {
            return executeQueryForCompoundObject();
        }
    }

    private List<Map<String, Object>> executeQueryForMap() {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<Map<String, Object>> resultList = new ArrayList<>();
            while (resultSet.next()) {
                LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object val = resultSet.getObject(columnName);
                    linkedHashMap.put(columnName, val);
                }
                resultList.add(linkedHashMap);
            }
            return resultList;
        } catch (SQLException | BraveException | ConversionException e) {
            ExceptionUtils.boxingAndThrowBraveException(e, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, null, preparedStatement, connection);
        }
        return Collections.emptyList();
    }

    /**
     * 处理单体对象，如 Integer，String等
     */
    public List<T> executeQueryForObject() {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<T> resultList = new ArrayList<>();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    T obj = ConverterUtils.convertJdbc(target, resultSet, columnName, target);
                    resultList.add(obj);
                }
            }
            return resultList;
        } catch (SQLException | BraveException | ConversionException e) {
            ExceptionUtils.boxingAndThrowBraveException(e, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, null, preparedStatement, connection);
        }
        return Collections.emptyList();
    }

    /**
     * 处理复合对象，如实体类
     */
    public List<T> executeQueryForCompoundObject() {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
        PreparedStatement preparedStatement = null;
        List<TableInfo> tableInfos = initTableInfo();
        Map<String, TableInfo> tableInfoMap = tableInfos.stream().collect(Collectors.toMap(k -> Check.unSplicingName(k.getColumn()), v -> v));
        try {
            preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            fillingColumnFieldMap(metaData);
            int columnCount = metaData.getColumnCount();
            List<T> resultList = new ArrayList<>();
            while (resultSet.next()) {
                T instance = target.newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    TableInfo tableInfo = tableInfoMap.get(columnName);
                    if (tableInfo == null) {
                        continue;
                    }
                    Object value = ConverterUtils.convertJdbc(target, resultSet, tableInfo);
                    ReflectUtils.setFieldValue(tableInfo.getField(), instance, value);
                }
                resultList.add(instance);
            }
            return resultList;
        } catch (SQLException | InstantiationException | IllegalAccessException | BraveException e) {
            ExceptionUtils.boxingAndThrowBraveException(e, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, null, preparedStatement, connection);
        }
        return Collections.emptyList();
    }


    private void fillingColumnFieldMap(ResultSetMetaData metaData) throws SQLException {
        int columnCount = metaData.getColumnCount();
        List<Field> allFieldList = new ArrayList<>();
        Check.recursionGetAllFields(target, allFieldList);
        List<Field> fieldList = allFieldList.stream().filter(field -> !Check.checkedFieldType(field)).collect(Collectors.toList());
        if (fieldList.isEmpty()) {
            throw new BraveException("映射实体类未发现可用属性，发生在类：" + target.getName());
        }
        Map<String, Field> columnFieldClassMap;
        try {
            columnFieldClassMap = fieldList.stream().collect(Collectors.toMap(this::getColumnAndFixName, v -> v));
        } catch (Exception ex) {
            throw new BraveException("重复的列名，发生在类：" + target.getName());
        }
        Map<String, Field> columnFieldMap = new LinkedHashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Field field = columnFieldClassMap.get(columnName);
            if (null == field) {
                log.debug("SQL查询了[" + columnName + "]列，但是在映射类中未发现对应属性。发生在类：" + target.getName());
                continue;
            }
            if (null != columnFieldMap.get(columnName)) {
                throw new BraveException("查询了重复的列名，如果这不是故意的，请使用别名。");
            }
            columnFieldMap.put(columnName, field);
        }
    }

    private String getColumnAndFixName(Field field) {
        String columnName = Check.getColumnName(field);
        return Check.unSplicingName(columnName).trim();
    }

    public void executeSql() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute(sql);
        } catch (Exception ex) {
            ExceptionUtils.boxingAndThrowBraveException(ex, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, null, preparedStatement, connection);
        }
    }

    public boolean existTable() {
        boolean flag = false;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, sql, new String[]{"TABLE"});
            if (tables.next()) {
                flag = true;
            }
        } catch (Exception ex) {
            ExceptionUtils.boxingAndThrowBraveException(ex, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, null, null, connection);
        }
        return flag;
    }
}
