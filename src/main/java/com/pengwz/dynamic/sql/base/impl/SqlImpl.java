package com.pengwz.dynamic.sql.base.impl;

import com.pengwz.dynamic.anno.GenerationType;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.constant.Constant;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.ParseSql;
import com.pengwz.dynamic.sql.base.Sqls;
import com.pengwz.dynamic.utils.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.pengwz.dynamic.anno.GenerationType.AUTO;
import static com.pengwz.dynamic.constant.Constant.*;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SqlImpl<T> implements Sqls<T> {
    private static final Log log = LogFactory.getLog(SqlImpl.class);

    private Class<?> currentClass;
    //分页信息
    private PageInfo<T> pageInfo;
    //需要插入、更新的数据
    private Iterable<T> data;
    private List<String> updateNullProperties;
    private String tableName;
    private String dataSourceName;
    private String whereSql;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @Override
    public T selectByPrimaryKey(Object primaryKeyValue) {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        String primaryKey = ContextApplication.getPrimaryKey(dataSourceName, tableName);
        Object value = ParseSql.matchValue(primaryKeyValue);
        String sql = SELECT + SPACE + columnList + SPACE + FROM + SPACE + tableName + SPACE + WHERE + SPACE + primaryKey + SPACE + EQ + SPACE + value;
        List<T> ts = executeQuery(sql, tableName);
        return ts.isEmpty() ? null : ts.get(0);
    }

    @Override
    public T selectSingle() {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        String sql = SELECT + SPACE + columnList + SPACE + FROM + SPACE + tableName + SPACE + WHERE + SPACE + whereSql;
        sql = ParseSql.parseSql(sql);
        List<T> queryList = executeQuery(sql, tableName);
        if (CollectionUtils.isEmpty(queryList)) {
            return null;
        }
        if (queryList.size() > 1) {
            throw new BraveException("期望返回一条数据，但是返回了" + queryList.size() + "条数据", "SQL：" + sql);
        }
        return queryList.isEmpty() ? null : queryList.get(0);
    }

    @Override
    public List<T> select() {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        String sql = SELECT + SPACE + columnList + SPACE + FROM + SPACE + tableName;
        if (StringUtils.isNotEmpty(whereSql)) {
            sql += SPACE + WHERE + SPACE + whereSql;
        }
        sql = ParseSql.parseSql(sql);
        return executeQuery(sql, tableName);
    }

    @Override
    public Integer selectCount() {
        String sql = SELECT + SPACE + "count(1)" + SPACE + FROM + SPACE + tableName;
        if (StringUtils.isNotEmpty(whereSql)) {
            sql += SPACE + WHERE + SPACE + whereSql;
        }
        sql = ParseSql.parseSql(sql);
        return executeQueryCount(sql, true);
    }

    @Override
    public List<T> selectAll() {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        String sql = "select " + columnList + " from " + tableName;
        return executeQuery(sql, tableName);
    }

    @Override
    public PageInfo<T> selectPageInfo() {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        String sqlCount = SELECT + SPACE + "count(1)" + SPACE + FROM + SPACE + tableName + (StringUtils.isEmpty(whereSql) ? SPACE : SPACE + WHERE + SPACE + whereSql.trim());
        sqlCount = ParseSql.parseSql(sqlCount);
        int totalSize = executeQueryCount(sqlCount, false);
        String sql = "select " + columnList + " from " + tableName + (StringUtils.isEmpty(whereSql) ? SPACE : SPACE + WHERE + SPACE + whereSql.trim());
        sql = ParseSql.parseSql(sql);
        sql += " limit " + pageInfo.getOffset() + " , " + (pageInfo.getPageSize() == 0 ? totalSize : pageInfo.getPageSize());
        List<T> list = executeQuery(sql, tableName);
        buildPageInfo(pageInfo, list, totalSize);
        return pageInfo;
    }

    private Integer executeQueryCount(String sql, boolean isCloseConnection) {
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            printSql(preparedStatement);
            resultSet.next();
            return resultSet.getInt(1);
        } catch (Exception ex) {
            //如果发生异常，则必须归还链接资源
            if (!isCloseConnection)
                isCloseConnection = true;
            ExceptionUtils.boxingAndThrowBraveException(ex, sql);
        } finally {
            if (isCloseConnection) {
                DataSourceManagement.close(dataSourceName, resultSet, preparedStatement, connection);
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private List<T> executeQuery(String sql, String tableName) {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        List<T> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            printSql(preparedStatement);
            while (resultSet.next()) {
                T t = (T) currentClass.newInstance();
                for (TableInfo tableInfo : tableInfos) {
                    Object o = ConverterUtils.convertJdbc(resultSet, tableInfo.getColumn(), tableInfo.getField().getType());
                    ReflectUtils.setFieldValue(tableInfo.getField(), t, o);
                }
                list.add(t);
            }
        } catch (Exception ex) {
            ExceptionUtils.boxingAndThrowBraveException(ex, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, resultSet, preparedStatement, connection);
        }
        return list;
    }

    @Override
    public Integer batchInsert() {
        String columnToStr = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        final StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(tableName).append(" ( ").append(columnToStr).append(" ) values ");
        sql.append("( ");
        tableInfos.forEach(tableInfo -> sql.append(" ? ,"));
        sql.deleteCharAt(sql.lastIndexOf(","));
        sql.append("),");
        String prepareSql = sql.deleteCharAt(sql.lastIndexOf(",")).toString();
        return setValuesExecuteSql(prepareSql, tableInfos);
    }

    @Override
    public Integer insertActive() {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        T next = data.iterator().next();
        final StringBuilder prefix = new StringBuilder();
        final StringBuilder suffix = new StringBuilder();
        prefix.append("insert into ").append(tableName).append(" ( ");
        List<Object> insertValues = new ArrayList<>();
        for (TableInfo tableInfo : tableInfos) {
            try {
                Object invoke;
                //判断自增字符串型主键
                if (tableInfo.getGenerationType() != null && !tableInfo.getGenerationType().equals(AUTO)) {
                    invoke = setUUIDGenerationType(tableInfo, next);
                } else {
                    invoke = ReflectUtils.getFieldValue(tableInfo.getField(), next);
                    if (Objects.isNull(invoke)) {
                        continue;
                    }
                }
                prefix.append(SPACE).append(tableInfo.getColumn()).append(COMMA);
                suffix.append("?, ");
                insertValues.add(invoke);
            } catch (Exception ex) {
                ExceptionUtils.boxingAndThrowBraveException(ex);
            }
        }
        suffix.deleteCharAt(suffix.lastIndexOf(","));
        prefix.deleteCharAt(prefix.lastIndexOf(","));
        prefix.append(" ) values (").append(suffix).append(")");
        String sql = prefix.toString();
        try {
            preparedStatement = connection.prepareStatement(sql, RETURN_GENERATED_KEYS);
            for (int i = 1; i <= insertValues.size(); i++) {
                preparedStatement.setObject(i, insertValues.get(i - 1));
            }
            printSql(preparedStatement);
            preparedStatement.addBatch();
            return executeSqlAndReturnAffectedRows();
        } catch (Exception ex) {
            ExceptionUtils.boxingAndThrowBraveException(ex, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, resultSet, preparedStatement, connection);
        }
        return -1;
    }


    private Integer setValuesExecuteSql(String sql, List<TableInfo> tableInfos) {
        Iterator<T> iterator = data.iterator();
        try {
            preparedStatement = connection.prepareStatement(sql, RETURN_GENERATED_KEYS);
            while (iterator.hasNext()) {
                T next = iterator.next();
                for (int i = 1; i <= tableInfos.size(); i++) {
                    TableInfo tableInfo = tableInfos.get(i - 1);
                    Field field = tableInfo.getField();
                    Object fieldValue;
                    if (tableInfo.getGenerationType() != null && !tableInfo.getGenerationType().equals(AUTO)) {
                        fieldValue = setUUIDGenerationType(tableInfo, next);
                    } else {
                        fieldValue = ReflectUtils.getFieldValue(field, next);
                    }
                    preparedStatement.setObject(i, fieldValue);
                }
                printSql(preparedStatement);
                preparedStatement.addBatch();
            }
            return executeSqlAndReturnAffectedRows();
        } catch (Exception ex) {
            ExceptionUtils.boxingAndThrowBraveException(ex, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, resultSet, preparedStatement, connection);
        }
        return -1;
    }

    private Object setUUIDGenerationType(TableInfo tableInfo, T next) {
        Object invoke = ReflectUtils.getFieldValue(tableInfo.getField(), next);
        if (Objects.isNull(invoke)) {
            if (!String.class.equals(tableInfo.getField().getType())) {
                throw new BraveException("使用UUID自增时，属性必须为String类型，但是此时类型为：" + tableInfo.getField().getType() + "，发生在表：" + tableName);
            }
            invoke = GenerationType.UUID.equals(tableInfo.getGenerationType()) ? UUID.randomUUID().toString() : UUID.randomUUID().toString().replace("-", "");
            ReflectUtils.setFieldValue(tableInfo.getField(), next, invoke);
        }
        return invoke;
    }

    private Integer executeSqlAndReturnAffectedRows() throws SQLException {
        int successCount = -1;
        int[] ints = preparedStatement.executeBatch();
        successCount = ints.length;
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceName, tableName);
        if (Objects.nonNull(tableInfoPrimaryKey) && Objects.nonNull(tableInfoPrimaryKey.getGenerationType()) && tableInfoPrimaryKey.getGenerationType().equals(AUTO)) {
            if (!Number.class.isAssignableFrom(tableInfoPrimaryKey.getField().getType())) {
                return successCount;
            }
            Iterator<T> resultIterator = data.iterator();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            while (resultIterator.hasNext()) {
                T next = resultIterator.next();
                Object primaryKeyValue = ReflectUtils.getFieldValue(tableInfoPrimaryKey.getField(), next);
                if (Objects.isNull(primaryKeyValue)) {
                    generatedKeys.next();
                    Object object = generatedKeys.getObject(RETURN_GENERATED_KEYS, tableInfoPrimaryKey.getField().getType());
                    ReflectUtils.setFieldValue(tableInfoPrimaryKey.getField(), next, object);
                }
            }
        }
        return successCount;
    }

    @Override
    public Integer insertOrUpdate() {
        String columnToStr = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(tableName).append(" ( ").append(columnToStr).append(" ) values ( ");
        List<String> duplicateKeys = new ArrayList<>();
        tableInfos.forEach(tableInfo -> {
            sql.append(" ? ,");
            duplicateKeys.add(tableInfo.getColumn() + " = values(" + tableInfo.getColumn() + ")");
        });
        String prepareSql = sql.substring(0, sql.length() - 1) + ")";
        String join = String.join(",", duplicateKeys);
        prepareSql = prepareSql.concat(" on duplicate key update ").concat(join);
        return setValuesExecuteSql(prepareSql, tableInfos);
    }

    @Override
    public Integer update() {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tableName).append(" set");
        for (T next : data) {
            for (TableInfo tableInfo : tableInfos) {
                try {
                    Object invoke = ReflectUtils.getFieldValue(tableInfo.getField(), next);
                    sql.append(SPACE).append(tableInfo.getColumn()).append(SPACE).append(EQ).append(SPACE);
                    sql.append(ParseSql.matchValue(invoke)).append(COMMA);
                } catch (Exception ex) {
                    ExceptionUtils.boxingAndThrowBraveException(ex, sql.toString());
                }
            }
        }
        return baseUpdate(sql);
    }

    @Override
    public Integer updateActive() {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tableName).append(" set");
        for (T next : data) {
            updateSqlCheckSetNullProperties(sql, tableInfos, next);
        }
        return baseUpdate(sql);
    }

    private Integer baseUpdate(StringBuilder sql) {
        if (sql.toString().endsWith("set")) {
            return 0;
        }
        String sqlPrefix = sql.substring(0, sql.length() - 1);
        if (StringUtils.isEmpty(whereSql)) {
            if (log.isDebugEnabled()) {
                log.warn("update操作未发现where语句，该操作会更新全表数据");
            }
        } else {
            sqlPrefix = sqlPrefix + SPACE + WHERE + SPACE + whereSql;
        }
        String parseSql = ParseSql.parseSql(sqlPrefix);
        return executeUpdateSqlAndReturnAffectedRows(parseSql);
    }

    @Override
    public Integer updateByPrimaryKey() {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceName, tableName);
        if (Objects.isNull(tableInfoPrimaryKey)) {
            throw new BraveException(tableName + " 表未配置主键");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tableName).append(" set");
        T next = data.iterator().next();
        for (TableInfo tableInfo : tableInfos) {
            try {
                Object invoke = ReflectUtils.getFieldValue(tableInfo.getField(), next);
                sql.append(SPACE).append(tableInfo.getColumn()).append(SPACE).append(EQ).append(SPACE);
                sql.append(ParseSql.matchValue(invoke)).append(COMMA);
            } catch (Exception ex) {
                ExceptionUtils.boxingAndThrowBraveException(ex, sql.toString());
            }
        }
        if (sql.toString().endsWith("set")) {
            return 0;
        }
        String sqlPrefix = sql.substring(0, sql.length() - 1);
        Object primaryKeyValue = getPrimaryKeyValue(tableInfoPrimaryKey, next);
        sqlPrefix = sqlPrefix + SPACE + WHERE + SPACE + tableInfoPrimaryKey.getColumn() + SPACE + EQ + SPACE + ParseSql.matchValue(primaryKeyValue);
        String parseSql = ParseSql.parseSql(sqlPrefix);
        return executeUpdateSqlAndReturnAffectedRows(parseSql);
    }

    private Object getPrimaryKeyValue(TableInfo tableInfoPrimaryKey, Object next) {
        Object primaryKeyValue;
        try {
            primaryKeyValue = ReflectUtils.getFieldValue(tableInfoPrimaryKey.getField(), next);
            if (Objects.isNull(primaryKeyValue)) {
                throw new BraveException(tableName + " 表的主键值不存在");
            }
        } catch (Exception e) {
            throw new BraveException(tableName + " 表获取主键值失败，原因：" + e.getMessage(), e);
        }
        return primaryKeyValue;
    }

    @Override
    public Integer updateActiveByPrimaryKey() {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceName, tableName);
        if (Objects.isNull(tableInfoPrimaryKey)) {
            throw new BraveException(tableName + " 表未配置主键");
        }
        T next = data.iterator().next();
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tableName).append(" set");
        updateSqlCheckSetNullProperties(sql, tableInfos, next);
        if (sql.toString().endsWith("set")) {
            return 0;
        }
        String sqlPrefix = sql.substring(0, sql.length() - 1);
        Object primaryKeyValue = getPrimaryKeyValue(tableInfoPrimaryKey, next);
        sqlPrefix = sqlPrefix + SPACE + WHERE + SPACE + tableInfoPrimaryKey.getColumn() + SPACE + EQ + SPACE + ParseSql.matchValue(primaryKeyValue);
        String parseSql = ParseSql.parseSql(sqlPrefix);
        return executeUpdateSqlAndReturnAffectedRows(parseSql);
    }

    @Override
    public Integer delete() {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ").append(tableName);
        if (StringUtils.isEmpty(whereSql)) {
            if (log.isDebugEnabled()) {
                log.warn("delete操作未发现where语句，该操作会删除全表数据");
            }
        } else {
            sql.append(SPACE + WHERE + SPACE).append(whereSql);
        }
        String parseSql = ParseSql.parseSql(sql.toString());
        return executeUpdateSqlAndReturnAffectedRows(parseSql);
    }

    @Override
    public Integer deleteByPrimaryKey(Object primaryKeyValue) {
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceName, tableName);
        if (Objects.isNull(tableInfoPrimaryKey)) {
            throw new BraveException(tableName + " 表未配置主键");
        }
        String sql = "delete from " + tableName + " where " + tableInfoPrimaryKey.getColumn() +
                Constant.EQ + ParseSql.matchValue(primaryKeyValue);
        return executeUpdateSqlAndReturnAffectedRows(sql);
    }

    private Integer executeUpdateSqlAndReturnAffectedRows(String sql) {
        try {
            preparedStatement = connection.prepareStatement(sql);
            int i = preparedStatement.executeUpdate();
            printSql(preparedStatement);
            return i;
        } catch (SQLException ex) {
            ExceptionUtils.boxingAndThrowBraveException(ex, sql);
        } finally {
            DataSourceManagement.close(dataSourceName, resultSet, preparedStatement, connection);
        }
        return -1;
    }

    private void updateSqlCheckSetNullProperties(StringBuilder sql, List<TableInfo> tableInfos, T nextObject) {
        for (TableInfo tableInfo : tableInfos) {
            try {
                Object invoke = ReflectUtils.getFieldValue(tableInfo.getField(), nextObject);
                if (Objects.isNull(invoke) && !updateNullProperties.contains(tableInfo.getField().getName())) {
                    continue;
                }
                sql.append(SPACE).append(tableInfo.getColumn()).append(SPACE).append(EQ).append(SPACE);
                sql.append(ParseSql.matchValue(invoke)).append(COMMA);
            } catch (Exception ex) {
                throw new BraveException(ex.getMessage(), ex);
            }
        }
    }

    private void printSql(PreparedStatement preparedStatement) {
        if (log.isDebugEnabled()) {
            String sqlToString = preparedStatement.toString();
            log.debug(sqlToString.substring(sqlToString.indexOf(':') + 1));
        }
    }

    private void buildPageInfo(PageInfo<T> pageInfo, List<T> list, Integer totalSize) {
        pageInfo.setTotalSize(totalSize);
        pageInfo.setRealPageSize(list.size());
        pageInfo.setResultList(list);
        if (pageInfo.getPageSize() != 0) {
            pageInfo.setTotalPages((totalSize + pageInfo.getPageSize() - 1) / pageInfo.getPageSize());
        }
    }

    public void init
            (Class<?> currentClass, PageInfo<T> pageInfo, Iterable<T> data, List<String> updateNullProperties, String
                    tableName, String dataSourceName, String whereSql) {
        //让编译器开心
        this.currentClass = currentClass;
        this.whereSql = whereSql;
        this.tableName = tableName;
        this.dataSourceName = dataSourceName;
        this.pageInfo = pageInfo;
        this.data = data;
        this.updateNullProperties = updateNullProperties;
    }

    public void before() {
        connection = DataSourceManagement.initConnection(dataSourceName);
    }
}
