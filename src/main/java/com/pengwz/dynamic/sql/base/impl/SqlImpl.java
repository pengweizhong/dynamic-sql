package com.pengwz.dynamic.sql.base.impl;

import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.ParseSql;
import com.pengwz.dynamic.sql.base.Sqls;
import com.pengwz.dynamic.utils.CollectionUtils;
import com.pengwz.dynamic.utils.ConverterUtils;
import com.pengwz.dynamic.utils.ReflectUtils;
import com.pengwz.dynamic.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.pengwz.dynamic.constant.Constant.*;

public class SqlImpl<T> implements Sqls<T> {
    private final Log log = LogFactory.getLog(SqlImpl.class);

    private Class<?> currentClass;
    //分页信息
    private PageInfo<T> pageInfo;
    //需要插入、更新的数据
    private Iterable<T> data;
    private List<String> updateNullProperties;
    private String tableName;
    private Class<?> dataSourceClass;
    private String whereSql;
    private DataSourceConfig dataSourceConfig;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

//    {
//        before();
//    }

    @Override
    public T selectByPrimaryKey(Object primaryKeyValue) {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        String primaryKey = ContextApplication.getPrimaryKey(dataSourceClass, tableName);
        Object value = ParseSql.matchValue(primaryKeyValue);
        String sql = SELECT + SPACE + columnList + SPACE + FROM + SPACE + tableName + SPACE + WHERE + SPACE + primaryKey + SPACE + EQ + SPACE + value;
        return executeQuery(sql, tableName).get(0);
    }

    @Override
    public T selectSingle() {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        String sql = SELECT + SPACE + columnList + SPACE + FROM + SPACE + tableName + SPACE + WHERE + SPACE + whereSql;
        sql = ParseSql.parseSql(sql);
        List<T> queryList = executeQuery(sql, tableName);
        if (CollectionUtils.isEmpty(queryList)) {
            return null;
        }
        if (queryList.size() > 1) {
            throw new BraveException("期望返回一条数据，但是返回了" + queryList.size() + "条数据");
        }
        return queryList.get(0);
    }

    @Override
    public List<T> select() {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
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
        String columnList = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        String sql = "select " + columnList + " from " + tableName;
        return executeQuery(sql, tableName);
    }

    @Override
    public PageInfo<T> selectPageInfo() {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        String sqlCount = SELECT + SPACE + "count(1)" + SPACE + FROM + SPACE + tableName;
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
            throw new BraveException(ex.getMessage(), ex);
        } finally {
            if (isCloseConnection) {
                close(connection);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> executeQuery(String sql, String tableName) {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
        List<T> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            printSql(preparedStatement);
            while (resultSet.next()) {
                T t = (T) currentClass.newInstance();
                for (TableInfo tableInfo : tableInfos) {
                    Object object = resultSet.getObject(tableInfo.getColumn());
                    if (Objects.nonNull(object)) {
                        Object convertValue = ConverterUtils.convert(object, tableInfo.getField().getType());
                        ReflectUtils.setFieldValue(tableInfo.getField(), t, convertValue);
                    }
                }
                list.add(t);
            }
        } catch (Exception ex) {
            throw new BraveException(ex.getMessage(), ex);
        } finally {
            close(connection);
        }
        return list;
    }

    @Override
    public Integer batchInsert() {
        String columnToStr = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
        final StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(tableName).append(" ( ").append(columnToStr).append(" ) values ");
        sql.append("( ");
        tableInfos.forEach(tableInfo -> sql.append(" ? ,"));
        sql.deleteCharAt(sql.lastIndexOf(","));
        sql.append("),");
        String prepareSql = sql.deleteCharAt(sql.lastIndexOf(",")).toString();
        return executeBatchSqlAndReturnAffectedRows(prepareSql, tableInfos);
    }

    @Override
    public Integer insertOrUpdate() {
        String columnToStr = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
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
        return executeBatchSqlAndReturnAffectedRows(prepareSql, tableInfos);
    }

    @Override
    public Integer update() {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tableName).append(" set");
        for (T next : data) {
            for (TableInfo tableInfo : tableInfos) {
                try {
                    Object invoke = ReflectUtils.getFieldValue(tableInfo.getField(), next);
                    sql.append(SPACE).append(tableInfo.getColumn()).append(SPACE).append(EQ).append(SPACE);
                    sql.append(ParseSql.matchValue(invoke)).append(COMMA);
                } catch (Exception ex) {
                    throw new BraveException(ex.getMessage(), ex);
                }
            }
        }
        return baseUpdate(sql);
    }

    @Override
    public Integer updateActive() {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
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
        String sqlPreffix = sql.substring(0, sql.length() - 1);
        if (StringUtils.isEmpty(whereSql)) {
            if (log.isDebugEnabled()) {
                log.debug("update操作未发现where语句，该操作会更新全表数据");
            }
        } else {
            sqlPreffix = sqlPreffix + SPACE + WHERE + SPACE + whereSql;
        }
        String parseSql = ParseSql.parseSql(sqlPreffix);
        return executeUpdateSqlAndReturnAffectedRows(parseSql);
    }

    @Override
    public Integer updateByPrimaryKey() {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceClass, tableName);
        T next = data.iterator().next();
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tableName).append(" set");
        Object primaryKeyValue;
        try {
            primaryKeyValue = ReflectUtils.getFieldValue(tableInfoPrimaryKey.getField(), next);
            if (Objects.isNull(primaryKeyValue)) {
                throw new BraveException(tableName + " 表的主键值不存在");
            }
        } catch (Exception e) {
            throw new BraveException(tableName + " 表获取主键值失败，原因：" + e.getMessage(), e);
        }
        updateSqlCheckSetNullProperties(sql, tableInfos, next);
        if (sql.toString().endsWith("set")) {
            return 0;
        }
        String sqlPreffix = sql.substring(0, sql.length() - 1);
        sqlPreffix = sqlPreffix + SPACE + WHERE + SPACE + tableInfoPrimaryKey.getColumn() + SPACE + EQ + SPACE + ParseSql.matchValue(primaryKeyValue);
        String parseSql = ParseSql.parseSql(sqlPreffix);
        return executeUpdateSqlAndReturnAffectedRows(parseSql);
    }

    @Override
    public Integer delete() {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ").append(tableName);
        if (StringUtils.isEmpty(whereSql)) {
            if (log.isDebugEnabled()) {
                log.debug("delete操作未发现where语句，该操作会删除全表数据");
            }
        } else {
            sql.append(SPACE + WHERE + SPACE).append(whereSql);
        }
        String parseSql = ParseSql.parseSql(sql.toString());
        return executeUpdateSqlAndReturnAffectedRows(parseSql);
    }

    private Integer executeUpdateSqlAndReturnAffectedRows(String sql) {
        try {
            preparedStatement = connection.prepareStatement(sql);
            int i = preparedStatement.executeUpdate();
            printSql(preparedStatement);
            connection.commit();
            return i;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new BraveException(ex.getMessage(), ex);
            }
            throw new BraveException(ex.getMessage(), ex);
        }
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

    private Integer executeBatchSqlAndReturnAffectedRows(String sql, List<TableInfo> tableInfos) {
        Iterator<T> iterator = data.iterator();
        int successCount;
        try {
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            while (iterator.hasNext()) {
                T next = iterator.next();
                for (int i = 1; i <= tableInfos.size(); i++) {
                    Object fieldValue = ReflectUtils.getFieldValue(tableInfos.get(i - 1).getField(), next);
                    preparedStatement.setObject(i, fieldValue);
                }
                printSql(preparedStatement);
                preparedStatement.addBatch();
            }
            int[] ints = preparedStatement.executeBatch();
            successCount = ints.length;
            TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceClass, tableName);
            if (tableInfoPrimaryKey.isGeneratedValue()) {
                Iterator<T> resultIterator = data.iterator();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                while (resultIterator.hasNext()) {
                    T next = resultIterator.next();
                    Object primaryKeyValue = ReflectUtils.getFieldValue(tableInfoPrimaryKey.getField(), next);
                    if (Objects.isNull(primaryKeyValue)) {
                        generatedKeys.next();
                        Object convertValue = ConverterUtils.convert(generatedKeys.getObject(Statement.RETURN_GENERATED_KEYS), tableInfoPrimaryKey.getField().getType());
                        ReflectUtils.setFieldValue(tableInfoPrimaryKey.getField(), next, convertValue);
                    }
                }
            }
            connection.commit();
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new BraveException(ex.getMessage(), ex);
            }
            throw new BraveException(ex.getMessage(), ex);
        } finally {
            close(connection);
        }
        return successCount;
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

    private void close(Connection connection) {
        DataSourceManagement dataSourceManagement = DataSourceManagement.getDataSourceManagement(dataSourceConfig);
        dataSourceManagement.releaseConnection(connection);
    }

    public void init(Class<?> currentClass, PageInfo<T> pageInfo, Iterable<T> data, List<String> updateNullProperties, String tableName, Class<?> dataSourceClass, String whereSql, DataSourceConfig dataSourceConfig) {
        //让编译器开心
        this.currentClass = currentClass;
        this.whereSql = whereSql;
        this.tableName = tableName;
        this.dataSourceClass = dataSourceClass;
        this.pageInfo = pageInfo;
        this.data = data;
        this.updateNullProperties = updateNullProperties;
        this.dataSourceConfig = dataSourceConfig;
    }

    public void before() {
        DataSourceManagement dataSourceManagement = DataSourceManagement.getDataSourceManagement(dataSourceConfig);
        connection = dataSourceManagement.getConnection(dataSourceConfig);
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}
