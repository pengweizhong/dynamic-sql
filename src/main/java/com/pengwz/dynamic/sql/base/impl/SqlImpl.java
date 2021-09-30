package com.pengwz.dynamic.sql.base.impl;

import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.GenerationType;
import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.constant.Constant;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DataSourceInfo;
import com.pengwz.dynamic.model.DbType;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.ParseSql;
import com.pengwz.dynamic.sql.base.Sqls;
import com.pengwz.dynamic.utils.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.pengwz.dynamic.anno.GenerationType.AUTO;
import static com.pengwz.dynamic.anno.GenerationType.SEQUENCE;
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
        sql += " limit " + pageInfo.getOffset() + " , " + pageInfo.getPageSize();
        DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSourceName);
        List<T> list;
        if (dataSourceInfo.getDbType().equals(DbType.ORACLE)) {
            list = executeQuery(limitConversionPageSql(sql), tableName);
        } else {
            list = executeQuery(sql, tableName);
        }
        buildPageInfo(pageInfo, list, totalSize);
        return pageInfo;
    }

    private String limitConversionPageSql(String sql) {
        if (Stream.of(sql.split(" ")).noneMatch(str -> str.equalsIgnoreCase("limit"))) {
            return sql;
        }
        //将 limit 转为 rownum
        //rownum 别名
        String rowNumAlias = "ROW_NUMBER";
        //一级表别名
        String firstTableAlias = RandomStringUtils.randomAlphabetic(15).toUpperCase();
        //二级表别名
        String secondTableAlias = RandomStringUtils.randomAlphabetic(15).toUpperCase();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT * FROM (SELECT ROWNUM AS ").append(rowNumAlias).append(",").append(firstTableAlias).append(".* FROM (");
        stringBuilder.append(cropLimitSql(sql));
        stringBuilder.append(") ").append(firstTableAlias).append(") ").append(secondTableAlias).append(" WHERE ")
                .append(secondTableAlias).append(".").append(rowNumAlias).append(" > ").append(pageInfo.getOffset())
                .append(" AND ").append(secondTableAlias).append(".").append(rowNumAlias).append(" <=  ").append(pageInfo.getPageSize());

        return stringBuilder.toString()/*.toUpperCase()*/;
    }

    private String cropLimitSql(String sql) {
        List<String> sqlList = Arrays.asList(sql.split(" "));
        AtomicInteger limitIndex = new AtomicInteger();
        boolean hasLimit = sqlList.stream().anyMatch(str -> {
            if (str.equalsIgnoreCase("limit")) {
                return true;
            }
            limitIndex.addAndGet(1);
            return false;
        });
        //不是limit的语句直接跳过
        if (!hasLimit) {
            return sql;
        }
        List<String> strings = sqlList.subList(0, limitIndex.get());
        StringBuilder stringBuilder = new StringBuilder();
        strings.forEach(sqlSplit -> stringBuilder.append(" ").append(sqlSplit));
        return stringBuilder.toString()/*.toUpperCase()*/;
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
                Object invoke = getTableFieldValue(tableInfo, next);
                if (Objects.isNull(invoke) && !tableInfo.isPrimary()) {
                    continue;
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
                preparedStatement.setObject(i, ConverterUtils.convertValueJdbc(insertValues.get(i - 1)));
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
                    Object fieldValue = getTableFieldValue(tableInfo, next);
                    preparedStatement.setObject(i, ConverterUtils.convertValueJdbc(fieldValue));
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

    /**
     * 获取字段的值，若是主键则尝试生成主键的值，若是程序生成的主键，将生成的主键赋值给该主键字段
     *
     * @param tableInfo 主键 tableInfo
     * @param next      当前查询的对象
     * @return 主键值
     */
    private Object getTableFieldValue(TableInfo tableInfo, T next) {
        //先确定源字段是否有值
        Object invoke = ReflectUtils.getFieldValue(tableInfo.getField(), next);
        if (null != invoke) {
            return invoke;
        }
        //若该值为null，则看看是不是需要程序生成主键
        GeneratedValue generatedValue = tableInfo.getGeneratedValue();
        if (generatedValue == null) {
            //啥也没有，直接返回null
            return null;
        }
        Object value = getPrimaryValue(tableInfo);
        ReflectUtils.setFieldValue(tableInfo.getField(), next, value);
        return value;
    }

    private Object getPrimaryValue(TableInfo tableInfo) {
        GeneratedValue generatedValue = tableInfo.getGeneratedValue();
        switch (generatedValue.strategy()) {
            case AUTO:
                //直接返回null，使用数据库机制自增主键
                return null;
            case UUID:
            case UPPER_UUID:
            case SIMPLE_UUID:
            case UPPER_SIMPLE_UUID:
                if (generatedValue.strategy().equals(GenerationType.UUID))
                    return UUID.randomUUID().toString();
                if (generatedValue.strategy().equals(GenerationType.UPPER_UUID))
                    return UUID.randomUUID().toString().toUpperCase();
                if (generatedValue.strategy().equals(GenerationType.SIMPLE_UUID))
                    return UUID.randomUUID().toString().replace("-", "");
                if (generatedValue.strategy().equals(GenerationType.UPPER_SIMPLE_UUID))
                    return UUID.randomUUID().toString().replace("-", "").toUpperCase();
                //不会走到这里
                return null;
            case SEQUENCE:
                String sql = "SELECT " + generatedValue.sequenceName().trim() + ".NEXTVAL FROM DUAL";
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    printSql(preparedStatement);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    return resultSet.getObject(1, tableInfo.getField().getType());
                } catch (SQLException sqlException) {
                    ExceptionUtils.boxingAndThrowBraveException(sqlException, sql);
                }
                //不会走到这里
                return null;
            //不会走到default这里
            default:
                throw new IllegalStateException("Unexpected value: " + tableInfo.getGeneratedValue());
        }
    }


    private Integer executeSqlAndReturnAffectedRows() throws SQLException {
        int successCount = -1;
        int[] ints = preparedStatement.executeBatch();
        successCount = ints.length;
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceName, tableName);
        //若没有设置主键，直接返回
        if (tableInfoPrimaryKey == null)
            return successCount;
        //不是自增的直接返回，因为在执行前已经获取到了
        if (!tableInfoPrimaryKey.getGeneratedValue().strategy().equals(AUTO)) {
            return successCount;
        }
        DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSourceName);
        //若是oracle 且 执行的是序列，直接返回
        if (dataSourceInfo.getDbType().equals(DbType.ORACLE) && tableInfoPrimaryKey.getGeneratedValue().strategy().equals(SEQUENCE))
            return successCount;
        //使用数据库机制的，接收返回值并且对返回对象赋值
        Iterator<T> resultIterator = data.iterator();
        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        while (resultIterator.hasNext()) {
            T next = resultIterator.next();
            generatedKeys.next();
            Object object;
            if (dataSourceInfo.getDbType().equals(DbType.ORACLE)) {
                object = generatedKeys.getObject(Check.unSplicingName(tableInfoPrimaryKey.getColumn()), tableInfoPrimaryKey.getField().getType());
            } else {
                object = generatedKeys.getObject(RETURN_GENERATED_KEYS, tableInfoPrimaryKey.getField().getType());
            }
            ReflectUtils.setFieldValue(tableInfoPrimaryKey.getField(), next, object);
        }
        return successCount;
    }

    @Override
    public Integer insertOrUpdate() {
        DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSourceName);
        if (dataSourceInfo.getDbType().equals(DbType.ORACLE)) {
            throw new BraveException("oracle 尚未支持 insertOrUpdate");
        }
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

//    @Override
//    public Integer updateBatch() {
//        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
//        StringBuilder sql = new StringBuilder();
//        sql.append("update ").append(tableName).append(" set");
//        for (T next : data) {
//            for (TableInfo tableInfo : tableInfos) {
//                try {
//                    Object invoke = ReflectUtils.getFieldValue(tableInfo.getField(), next);
//                    sql.append(SPACE).append(tableInfo.getColumn()).append(SPACE).append(EQ).append(SPACE);
//                    sql.append(ParseSql.matchValue(invoke)).append(COMMA);
//                } catch (Exception ex) {
//                    ExceptionUtils.boxingAndThrowBraveException(ex, sql.toString());
//                }
//            }
//        }
//        return setValuesExecuteSql(sql.toString(), tableInfos);
//    }

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

    //    private void printSql(PreparedStatement preparedStatement) throws SQLException {
//        if (log.isDebugEnabled()) {
//            switch (preparedStatement.getConnection().getMetaData().getDatabaseProductName().toUpperCase()) {
//                case "ORACLE":
//                    try {
//                        String canonicalName = preparedStatement.getClass().getCanonicalName();
//                        if (canonicalName.equals("com.alibaba.druid.pool.DruidPooledPreparedStatement")) {
//                            DruidPooledPreparedStatement druidPooledPreparedStatement = (DruidPooledPreparedStatement) preparedStatement;
//                            log.debug(druidPooledPreparedStatement.getSql());
//                        } else {
//                            log.debug("[" + canonicalName + "] is not support print sql.");
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                case "MYSQL":
//                    String temp = preparedStatement.toString();
//                    log.debug(temp.substring(temp.indexOf(':') + 1));
//                    break;
//                default:
//                    log.debug(preparedStatement.toString());
//            }
//        }
//    }
    private void printSql(PreparedStatement preparedStatement) throws SQLException {
        switch (preparedStatement.getConnection().getMetaData().getDatabaseProductName().toUpperCase()) {
            case "ORACLE":
                try {
                    String canonicalName = preparedStatement.getClass().getCanonicalName();
                    if (canonicalName.equals("com.alibaba.druid.pool.DruidPooledPreparedStatement")) {
                        DruidPooledPreparedStatement druidPooledPreparedStatement = (DruidPooledPreparedStatement) preparedStatement;
                        System.out.println(druidPooledPreparedStatement.getSql());
                    } else {
                        System.out.println("[" + canonicalName + "] is not support print sql.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "MYSQL":
                String temp = preparedStatement.toString();
                System.out.println(temp.substring(temp.indexOf(':') + 1));
                break;
            default:
                System.out.println(preparedStatement.toString());
        }
    }

    private void buildPageInfo(PageInfo<T> pageInfo, List<T> list, Integer totalSize) {
        pageInfo.setTotalSize(totalSize);
        pageInfo.setRealPageSize(list.size());
        pageInfo.setResultList(list);
        if (pageInfo.getPageSize() > 0) {
            pageInfo.setTotalPages((totalSize + pageInfo.getPageSize() - 1) / pageInfo.getPageSize());
        } else {
            pageInfo.setTotalPages(0);
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
