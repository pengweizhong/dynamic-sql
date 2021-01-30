package com.pengwz.dynamic.sql.base.impl;

import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.ContextApplication.TableInfo;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.ParseSql;
import com.pengwz.dynamic.sql.base.Sqls;
import com.pengwz.dynamic.utils.CollectionUtils;
import com.pengwz.dynamic.utils.ConverterUtils;
import com.pengwz.dynamic.utils.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.pengwz.dynamic.constant.Constant.*;

@SuppressWarnings("all")
public class SqlImpl<T> implements Sqls<T> {
    private static final Logger log = Logger.getGlobal();

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
        String columList = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        String primaryKey = ContextApplication.getPrimaryKey(dataSourceClass, tableName);
        Object value = ParseSql.matchValue(primaryKeyValue);
        String sql = "select " + columList + " from " + tableName + SPACE + WHERE + SPACE + primaryKey + SPACE + EQ + SPACE + value;
        log.info("selectByPrimaryKey SQL : " + sql);
        return executeQuery(sql, tableName).get(0);
    }

    @Override
    public T selectSingle() {
        String columList = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        String sql = "select " + columList + " from " + tableName + SPACE + WHERE + SPACE + whereSql;
        sql = ParseSql.parseSql(sql);
        log.info("selectSingle SQL : " + sql);
        List<T> query;
        query = executeQuery(sql, tableName);
        if (CollectionUtils.isEmpty(query)) {
            return null;
        }
        if (query.size() > 1) {
            throw new BraveException("期望返回一条数据，但是返回了" + query.size() + "条数据");
        }
        return query.get(0);
    }

    @Override
    public List<T> select() {
        String columList = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        String sql = "select " + columList + " from " + tableName;
        if (StringUtils.isNotEmpty(whereSql)) {
            sql += SPACE + WHERE + SPACE + whereSql;
        }
        sql = ParseSql.parseSql(sql);
        log.info("select SQL : " + sql);
        return executeQuery(sql, tableName);
    }

    @Override
    public Integer selectCount() {
        String sql = "select count(1) from " + tableName;
        if (StringUtils.isNotEmpty(whereSql)) {
            sql += SPACE + WHERE + SPACE + whereSql;
        }
        sql = ParseSql.parseSql(sql);
        log.info("selectCount SQL : " + sql);
        int count = 0;
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            count = resultSet.getInt(1);
        } catch (Exception ex) {
            throw new BraveException("SQL execute fail. reason: " + ex.getMessage());
        } finally {
            close(connection);
        }
        return count;
    }

    @Override
    public List<T> selectAll() {
        String columList = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        String sql = "select " + columList + " from " + tableName;
        log.info("selectAll SQL : " + sql);
        return executeQuery(sql, tableName);
    }

    @Override
    public PageInfo<T> selectPageInfo() {
        String columList = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        String sqlCount = "select count(1) from " + tableName;
        String sql = "select " + columList + " from " + tableName + (StringUtils.isEmpty(whereSql) ? SPACE : SPACE + WHERE + SPACE + whereSql.trim());
        sql = ParseSql.parseSql(sql);
        log.info("selectPageInfo SQL : " + sqlCount);
        Integer totalSize = 0;
        try {
            preparedStatement = connection.prepareStatement(sqlCount);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            totalSize = resultSet.getInt(1);
        } catch (Exception ex) {
            close(connection);
            throw new BraveException("SQL execute fail. reason: " + ex.getMessage());
        }
        sql += " limit " + pageInfo.getOffset() + " , " + (pageInfo.getPageSize() == 0 ? totalSize : pageInfo.getPageSize());
        log.info("selectPageInfo SQL : " + sql);
        List<T> list = executeQuery(sql, tableName);
        buildPageInfo(pageInfo, list, totalSize);
        return pageInfo;
    }

    private List<T> executeQuery(String sql, String tableName) {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
        List<T> list = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T t = (T) currentClass.newInstance();
                for (TableInfo tableInfo : tableInfos) {
                    Object object = resultSet.getObject(tableInfo.getColumn());
                    if (Objects.nonNull(object)) {
                        Object convert = ConverterUtils.convert(object, tableInfo.getField().getType());
                        if (!convert.getClass().equals(tableInfo.getField().getType())) {
                            Object cast = tableInfo.getGetMethod().getReturnType().cast(convert);
                            System.out.println(cast);
                        }
                        tableInfo.getSetMethod().invoke(t, convert);
                    }
                }
                list.add(t);
            }
        } catch (Exception ex) {
            throw new BraveException("SQL execute fail. reason: " + ex.getMessage());
        } finally {
            close(connection);
        }
        return list;
    }

    private static String fixParseMethod(String fstype) {
        switch (fstype) {
            case "Integer":
                return "Int";
            default:
                return fstype;
        }
    }


    @Override
    public Integer batchInsert() {
        String columToStr = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("insert into " + tableName + " ( " + columToStr + " ) values ( ");
        tableInfos.forEach(tableInfo -> {
            sql.append(" ? ,");
        });
        String prepareSql = sql.substring(0, sql.length() - 1) + ")";
        log.info("insertMany SQL: " + prepareSql);
        Iterator<T> iterator = data.iterator();
        int successCount = 0;
        try {
            preparedStatement = connection.prepareStatement(prepareSql, Statement.RETURN_GENERATED_KEYS);
            while (iterator.hasNext()) {
                T next = iterator.next();
                for (int i = 1; i <= tableInfos.size(); i++) {
                    preparedStatement.setObject(i, tableInfos.get(i - 1).getGetMethod().invoke(next, new Object[]{}));
                }
                preparedStatement.addBatch();
            }
            int[] ints = preparedStatement.executeBatch();
            successCount = ints.length;
            TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceClass, tableName);
            if (tableInfoPrimaryKey.isGeneratedValue()) {
                Iterator<T> resultIterator = data.iterator();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                while (resultIterator.hasNext()) {
                    generatedKeys.next();
                    T next = resultIterator.next();
                    Object convert = ConverterUtils.convert(generatedKeys.getObject(1), tableInfoPrimaryKey.getField().getType());
                    tableInfoPrimaryKey.getSetMethod().invoke(next, convert);
                }
            }
            connection.commit();
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new BraveException("SQL insert rollback fail. reason: " + ex.getMessage());
            }
            throw new BraveException("SQL insert fail. reason: " + ex.getMessage());
        } finally {
            close(connection);
        }
        return successCount;
    }

    @Override
    public Integer insertOrUpdate() {
        String columToStr = ContextApplication.formatAllColumToStr(dataSourceClass, tableName);
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("insert into " + tableName + " ( " + columToStr + " ) values ( ");
        List<String> duplicateKeys = new ArrayList<>();
        tableInfos.forEach(tableInfo -> {
            sql.append(" ? ,");
            duplicateKeys.add(tableInfo.getColumn() + " = values(" + tableInfo.getColumn() + ")");
        });
        String prepareSql = sql.substring(0, sql.length() - 1) + ")";
        String join = String.join(",", duplicateKeys);
        prepareSql = prepareSql.concat(" on duplicate key update ").concat(join);
        log.info("insertOrUpdate SQL: " + prepareSql);
        Iterator<T> iterator = data.iterator();
        int successCount = 0;
        try {
            preparedStatement = connection.prepareStatement(prepareSql, Statement.RETURN_GENERATED_KEYS);
            while (iterator.hasNext()) {
                T next = iterator.next();
                for (int i = 1; i <= tableInfos.size(); i++) {
                    preparedStatement.setObject(i, tableInfos.get(i - 1).getGetMethod().invoke(next, new Object[]{}));
                }
                preparedStatement.addBatch();
            }
            int[] ints = preparedStatement.executeBatch();
            successCount = ints.length;
            TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceClass, tableName);
            if (tableInfoPrimaryKey.isGeneratedValue()) {
                Iterator<T> resultIterator = data.iterator();
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                while (resultIterator.hasNext()) {
                    generatedKeys.next();
                    T next = resultIterator.next();
                    Object convert = ConverterUtils.convert(generatedKeys.getObject(1), tableInfoPrimaryKey.getField().getType());
                    tableInfoPrimaryKey.getSetMethod().invoke(next, convert);
                }
            }
            connection.commit();
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new BraveException("SQL insert rollback fail. reason: " + ex.getMessage());
            }
            throw new BraveException("SQL insert fail. reason: " + ex.getMessage());
        } finally {
            close(connection);
        }
        return successCount;
    }

    @Override
    public Integer update() {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("update " + tableName + " set");
        Iterator<T> iterator = data.iterator();
        while (iterator.hasNext()) {
            T next = iterator.next();
            for (TableInfo tableInfo : tableInfos) {
                try {
                    Object invoke = tableInfo.getGetMethod().invoke(next, new Object[]{});
                    if (Objects.isNull(invoke) && !updateNullProperties.contains(tableInfo.getField().getName())) {
                        continue;
                    }
                    sql.append(SPACE).append(tableInfo.getColumn()).append(SPACE).append(EQ).append(SPACE);
                    sql.append(ParseSql.matchValue(invoke)).append(COMMA);
                } catch (Exception ex) {
                    throw new BraveException("SQL update fail. reason: " + ex.getMessage());
                }
            }
        }
        if (sql.toString().endsWith("set")) {
            return 0;
        }
        String sqlPreffix = sql.substring(0, sql.length() - 1);
        if (StringUtils.isEmpty(whereSql)) {
            log.warning("update 操作未发现where语句，该操作会更新全表数据");
        } else {
            sqlPreffix = sqlPreffix + SPACE + WHERE + SPACE + whereSql;
        }
        String parseSql = ParseSql.parseSql(sqlPreffix);
        log.info("update SQL: " + parseSql);
        try {
            preparedStatement = connection.prepareStatement(parseSql);
            int i = preparedStatement.executeUpdate();
            connection.commit();
            return i;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new BraveException("SQL update rollback fail. reason: " + ex.getMessage());
            }
            throw new BraveException("SQL update fail. reason: " + ex.getMessage());
        }
    }

    @Override
    public Integer updateByPrimaryKey() {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceClass, tableName);
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceClass, tableName);
        T next = data.iterator().next();
        StringBuilder sql = new StringBuilder();
        sql.append("update " + tableName + " set");
        Object primaryKeyValue = null;
        try {
            primaryKeyValue = tableInfoPrimaryKey.getGetMethod().invoke(next, new Object[]{});
            if (Objects.isNull(primaryKeyValue)) {
                throw new BraveException(tableName + " 表的主键值不存在");
            }
        } catch (Exception e) {
            throw new BraveException(tableName + " 表获取主键值失败，原因：" + e.getMessage());
        }
        for (TableInfo tableInfo : tableInfos) {
            try {
                Object invoke = tableInfo.getGetMethod().invoke(next, new Object[]{});
                if (Objects.isNull(invoke) && !updateNullProperties.contains(tableInfo.getField().getName())) {
                    continue;
                }
                sql.append(SPACE).append(tableInfo.getColumn()).append(SPACE).append(EQ).append(SPACE);
                sql.append(ParseSql.matchValue(invoke)).append(COMMA);
            } catch (Exception ex) {
                throw new BraveException("SQL update fail. reason: " + ex.getMessage());
            }
        }
        if (sql.toString().endsWith("set")) {
            return 0;
        }
        String sqlPreffix = sql.substring(0, sql.length() - 1);
        sqlPreffix = sqlPreffix + SPACE + WHERE + SPACE + tableInfoPrimaryKey.getColumn() + SPACE + EQ + SPACE + ParseSql.matchValue(primaryKeyValue);
        String parseSql = ParseSql.parseSql(sqlPreffix);
        log.info("update SQL: " + parseSql);
        try {
            preparedStatement = connection.prepareStatement(parseSql);
            int i = preparedStatement.executeUpdate();
            connection.commit();
            return i;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new BraveException("SQL update rollback fail. reason: " + ex.getMessage());
            }
            throw new BraveException("SQL update fail. reason: " + ex.getMessage());
        }
    }

    @Override
    public Integer delete() {
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ").append(tableName);
        if (StringUtils.isEmpty(whereSql)) {
            log.warning("delete 操作未发现where语句，该操作会删除全表数据");
        }
        sql.append(SPACE + WHERE + SPACE).append(whereSql);
        String parseSql = ParseSql.parseSql(sql.toString());
        log.info("delete SQL : " + parseSql);
        try {
            preparedStatement = connection.prepareStatement(parseSql);
            int i = preparedStatement.executeUpdate();
            connection.commit();
            return i;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new BraveException("SQL delete rollback fail. reason: " + ex.getMessage());
            }
            throw new BraveException("SQL delete fail. reason: " + ex.getMessage());
        }
    }

    public void before() {
        connection = DataSourceManagement.getConnection(dataSourceConfig);
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //        connection = DBPool.getConnection();
    }

    private void close(Connection connection) {
        DataSourceManagement.releaseConnection(connection);
//        DBPool.releaseConnection(connection);
    }

    private void buildPageInfo(PageInfo<T> pageInfo, List<T> list, Integer totalSize) {
        pageInfo.setTotalSize(totalSize);
        pageInfo.setRealPageSize(list.size());
        pageInfo.setResultList(list);
        if (pageInfo.getPageSize() != 0) {
            pageInfo.setTotalPages((totalSize + pageInfo.getPageSize() - 1) / pageInfo.getPageSize());
        }
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

}
