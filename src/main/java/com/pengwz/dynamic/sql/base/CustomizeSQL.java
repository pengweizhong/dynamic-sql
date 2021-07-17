package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.utils.ConverterUtils;
import com.pengwz.dynamic.utils.ExceptionUtils;
import com.pengwz.dynamic.utils.ReflectUtils;
import com.pengwz.dynamic.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * 执行自定义SQL
 */
public class CustomizeSQL<T> {

    private static final Log log = LogFactory.getLog(CustomizeSQL.class);

    private final Class<T> target;

    private final String sql;

    private final Connection connection;

    private final String dataSourceName;

    private Map<String, Field> columnFieldMap = new LinkedHashMap<>();

    public CustomizeSQL(Class<? extends DataSourceConfig> dataSource, Class<T> target, String sql) {
        this.target = target;
        this.sql = sql;
        this.dataSourceName = dataSource.getName();
        DataSourceManagement.initDataSourceConfig(dataSource, "");
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
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T obj = target.newInstance();
                Field[] declaredFields = target.getDeclaredFields();
                for (Field field : declaredFields) {
                    Column column = field.getAnnotation(Column.class);
                    Object object;
                    if (Objects.isNull(column)) {
                        object = ConverterUtils.convertJdbc(resultSet, StringUtils.caseField(field.getName()), field.getType());
                    } else {
                        object = ConverterUtils.convertJdbc(resultSet, column.value().trim(), field.getType());
                    }
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

    public List<T> executeQuery() {
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
                fillingColumnFieldMap();
                T instance = target.newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Field field = columnFieldMap.get(columnName);
                    Object o = ConverterUtils.convertJdbc(resultSet, columnName, field.getType());
                    ReflectUtils.setFieldValue(field, instance, o);
                }
                resultList.add(instance);
            }
            return resultList;
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            ExceptionUtils.boxingAndThrowBraveException(e, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, null, preparedStatement, connection);
        }
        return Collections.emptyList();
    }

    private void fillingColumnFieldMap() {
        if (!columnFieldMap.isEmpty()) {
            return;
        }
        Field[] declaredFields = target.getDeclaredFields();
        for (Field field : declaredFields) {
            if (Check.FILTER_TYPE_LIST.contains(field.getModifiers())) {
                continue;
            }
            String columnName = Check.getColumnName(field, "unknown");
            if (columnName.contains("`")) {
                columnName = columnName.replace("`", "").trim();
            }
            if (Objects.nonNull(columnFieldMap.get(columnName))) {
                throw new BraveException("重复的列名：" + columnFieldMap.get(columnName));
            }
            columnFieldMap.put(columnName, field);
        }
        if (columnFieldMap.isEmpty()) {
            throw new BraveException("映射实体类未发现可用属性，发生在类：" + target.getName());
        }
    }

}
