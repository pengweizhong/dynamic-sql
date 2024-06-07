package com.pengwz.dynamic.sql.base.impl;

import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.GenerationType;
import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.constant.Constant;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DataSourceInfo;
import com.pengwz.dynamic.model.DbType;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.ParseSql;
import com.pengwz.dynamic.sql.PreparedSql;
import com.pengwz.dynamic.sql.base.AbstractAccessor;
import com.pengwz.dynamic.sql.base.Sqls;
import com.pengwz.dynamic.sql.base.enumerate.FunctionEnum;
import com.pengwz.dynamic.utils.CollectionUtils;
import com.pengwz.dynamic.utils.ConverterUtils;
import com.pengwz.dynamic.utils.ReflectUtils;
import com.pengwz.dynamic.utils.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.pengwz.dynamic.anno.GenerationType.AUTO;
import static com.pengwz.dynamic.anno.GenerationType.SEQUENCE;
import static com.pengwz.dynamic.constant.Constant.*;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SqlImpl<T> extends AbstractAccessor implements Sqls<T> {
    private static final Log log = LogFactory.getLog(SqlImpl.class);

    private Class<?> currentClass;
    //分页信息
    private PageInfo<T> pageInfo;
    //需要插入、更新的数据
    private Iterable<T> data;
    private List<String> updateNullProperties;
    private PreparedSql preparedSql;
    //    private InterceptorHelper interceptorHelper;
    private String tableName;
    private String dataSourceName;
    private String whereSql;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @Override
    public T selectByPrimaryKey(Object primaryKeyValue) throws SQLException, InstantiationException, IllegalAccessException {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        String primaryKey = ContextApplication.getPrimaryKey(dataSourceName, tableName);
        preparedSql.addParameter(primaryKeyValue);
        String sql = SELECT + SPACE + columnList + SPACE + FROM + SPACE + tableName + SPACE + WHERE + SPACE + primaryKey + SPACE + EQ + SPACE + "?";
        List<T> ts = executeQuery(sql);
        return ts.isEmpty() ? null : ts.get(0);
    }

    @Override
    public T selectSingle() throws SQLException, InstantiationException, IllegalAccessException {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        String sql;
        if (StringUtils.isEmpty(whereSql)) {
            sql = SELECT + SPACE + columnList + SPACE + FROM + SPACE + tableName;
        } else {
            sql = SELECT + SPACE + columnList + SPACE + FROM + SPACE + tableName + SPACE + WHERE + SPACE + whereSql;
        }
        sql = ParseSql.parseSql(sql);
        List<T> queryList = executeQuery(sql);
        if (CollectionUtils.isEmpty(queryList)) {
            return null;
        }
        if (queryList.size() > 1) {
            throw new BraveException("期望返回1条数据，但是返回了" + queryList.size() + "条数据", "SQL：" + sql);
        }
        return queryList.isEmpty() ? null : queryList.get(0);
    }

    @Override
    public List<T> select() throws SQLException, InstantiationException, IllegalAccessException {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        String sql = SELECT + SPACE + columnList + SPACE + FROM + SPACE + tableName;
        if (StringUtils.isNotEmpty(whereSql)) {
            sql += SPACE + WHERE + SPACE + whereSql;
        }
        sql = ParseSql.parseSql(sql);
        return executeQuery(sql);
    }

    @Override
    public <R> R selectAggregateFunction(String property, FunctionEnum functionEnum, Class<R> returnType) throws SQLException {
        String function = splicingFunction(property, functionEnum);
        String sql = SELECT + SPACE + function + SPACE + FROM + SPACE + tableName;
        if (StringUtils.isNotEmpty(whereSql)) {
            sql += SPACE + WHERE + SPACE + whereSql;
        }
        sql = ParseSql.parseSql(sql);
        return executeQueryCount(sql, returnType);
    }

    @Override
    public <K, R> Map<K, R> selectAggregateFunction(String valueProperty, FunctionEnum functionEnum, Class<K> keyClass, Class<R> valueClass, String keyProperty) throws SQLException {
        String keyColumn = ContextApplication.getTableInfo(dataSourceName, tableName, keyProperty).getColumn();
        String valueColumn = ContextApplication.getTableInfo(dataSourceName, tableName, valueProperty).getColumn();
        String sql = SELECT + SPACE + keyColumn + COMMA + SPACE + splicingFunction(valueProperty, functionEnum) + " as " + valueColumn + SPACE + FROM + SPACE + tableName;
        if (StringUtils.isNotEmpty(whereSql)) {
            sql += SPACE + WHERE + SPACE + whereSql;
        }
        sql = ParseSql.parseSql(sql);
        LinkedHashMap<K, R> linkedHashMap = new LinkedHashMap<>();
        setPreparedStatementParam(sql, false);
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Object k = resultSet.getObject(Check.unSplicingName(keyColumn));
            Object v = resultSet.getObject(Check.unSplicingName(valueColumn));
            K convertK = ConverterUtils.convert(k, keyClass);
            R convertV = ConverterUtils.convert(v, valueClass);
            linkedHashMap.put(convertK, convertV);
        }
        return linkedHashMap;
    }


    private String splicingFunction(String property, FunctionEnum functionEnum) {
        String column;
        //这里，count时可能会传入1
        if (property.equals("1")) {
            column = "1";
        } else {
            column = ContextApplication.getColumnByField(dataSourceName, tableName, property);
        }
        switch (functionEnum) {
            case AVG:
                return "avg(" + column + ")";
            case MAX:
                return "max(" + column + ")";
            case MIN:
                return "min(" + column + ")";
            case SUM:
                return "sum(" + column + ")";
            case COUNT:
                return "count(" + column + ")";
            default:
                //不会走到这里，这么做是为了让sonarlint开心
                return "";
        }
    }

    @Override
    public List<T> selectAll() throws SQLException, InstantiationException, IllegalAccessException {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        String sql = "select " + columnList + " from " + tableName;
        return executeQuery(sql);
    }

    @Override
    public PageInfo<T> selectPageInfo() throws SQLException, InstantiationException, IllegalAccessException {
        String columnList = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        String sqlCount = SELECT + SPACE + "count(1)" + SPACE + FROM + SPACE + tableName + (StringUtils.isEmpty(whereSql) ? SPACE : SPACE + WHERE + SPACE + whereSql.trim());
        sqlCount = ParseSql.parseSql(sqlCount);
        int totalSize = executeQueryCount(sqlCount, Integer.class);
        if (totalSize <= 0) {
            buildPageInfo(pageInfo, new ArrayList<>(), totalSize);
            return pageInfo;
        }
        String sql = "select " + columnList + " from " + tableName + (StringUtils.isEmpty(whereSql) ? SPACE : SPACE + WHERE + SPACE + whereSql.trim());
        sql = ParseSql.parseSql(sql);
        preparedSql.addParameter(pageInfo.getOffset());
        preparedSql.addParameter(pageInfo.getPageSize());
        sql += " limit " + PLACEHOLDER + " , " + PLACEHOLDER;
        DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSourceName);
        List<T> list;
        if (dataSourceInfo.getDbType().equals(DbType.ORACLE)) {
            list = executeQuery(limitConversionPageSql(sql));
        } else {
            list = executeQuery(sql);
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

    private <R> R executeQueryCount(String sql, Class<R> returnType) throws SQLException {
        setPreparedStatementParam(sql, false);
        resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return ConverterUtils.convertJdbc(currentClass, resultSet, "1", returnType);
    }

    private void setPreparedStatementParam(String sql, boolean isBatch, int... generatedKeys) throws SQLException {
        if (isBatch) {
            preparedSql.printSqlAndBatchParams(sql);
        } else {
            preparedSql.printSqlAndParams(sql);
        }
        if (generatedKeys.length > 0) {
            preparedStatement = connection.prepareStatement(sql, generatedKeys[0]);
        } else {
            preparedStatement = connection.prepareStatement(sql);
        }
        //是否为批量的SQL语句
        if (isBatch) {
            final List<List<Object>> batchPreparedParameters = preparedSql.getBatchPreparedParameters();
            for (List<Object> preparedParameters : batchPreparedParameters) {
                setObject(preparedParameters);
                preparedStatement.addBatch();
            }
            return;
        }
        setObject(preparedSql.getPreparedParameters());
    }

    private void setObject(final List<Object> preparedParameters) {
        try {
            for (int i = 1; i <= preparedParameters.size(); i++) {
                preparedStatement.setObject(i, preparedParameters.get(i - 1));
            }
        } catch (SQLException e) {
            throw new BraveException("请确认是否调用了正确的方法! ", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> executeQuery(String sql) throws SQLException, InstantiationException, IllegalAccessException {
        List<T> list = new ArrayList<>();
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        setPreparedStatementParam(sql, false);
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            T t = (T) currentClass.newInstance();
            for (TableInfo tableInfo : tableInfos) {
                Object o = ConverterUtils.convertJdbc(currentClass, resultSet, tableInfo);
                ReflectUtils.setFieldValue(tableInfo.getField(), t, o);
            }
            list.add(t);
        }
        return list;
    }

    @Override
    public Integer batchInsert() throws SQLException {
        String columnToStr = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        final StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(tableName).append(" ( ").append(columnToStr).append(" ) values ");
        sql.append("( ");
        tableInfos.forEach(tableInfo -> sql.append(" " + PLACEHOLDER + " ,"));
        sql.deleteCharAt(sql.lastIndexOf(","));
        sql.append("),");
        String prepareSql = sql.deleteCharAt(sql.lastIndexOf(",")).toString();
        return setValuesExecuteSql(prepareSql, tableInfos);
    }

    @Override
    public Integer insertActive() throws SQLException {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        T next = data.iterator().next();
        final StringBuilder prefix = new StringBuilder();
        final StringBuilder suffix = new StringBuilder();
        prefix.append("insert into ").append(tableName).append(" ( ");
        final List<Object> parameter = preparedSql.startBatchParameter();
        for (TableInfo tableInfo : tableInfos) {
            Object invoke = getTableFieldValue(tableInfo, next, true);
            final GeneratedValue generatedValue = tableInfo.getGeneratedValue();
            //判断主键生成策略，非自增直接过滤
            if (Objects.isNull(invoke) && (generatedValue == null || !generatedValue.strategy().equals(AUTO))) {
                continue;
            }
            prefix.append(SPACE).append(tableInfo.getColumn()).append(COMMA);
            suffix.append(PLACEHOLDER + ", ");
            parameter.add(invoke);
        }
        if (StringUtils.isEmpty(suffix.toString())) {
            return 0;
        }
        suffix.deleteCharAt(suffix.lastIndexOf(","));
        prefix.deleteCharAt(prefix.lastIndexOf(","));
        prefix.append(" ) values (").append(suffix).append(")");
        String sql = prefix.toString();
        setPreparedStatementParam(sql, true, RETURN_GENERATED_KEYS);
        return executeSqlAndReturnAffectedRows();
    }


    private Integer setValuesExecuteSql(String sql, List<TableInfo> tableInfos) throws SQLException {
        for (T next : data) {
            if (next == null) {
                throw new BraveException("新增的数据不可为空");
            }
            final List<Object> parameters = preparedSql.startBatchParameter();
            for (int i = 1; i <= tableInfos.size(); i++) {
                TableInfo tableInfo = tableInfos.get(i - 1);
                Object fieldValue = getTableFieldValue(tableInfo, next, true);
                parameters.add(ConverterUtils.convertValueJdbc(fieldValue));
            }
        }
        setPreparedStatementParam(sql, true, RETURN_GENERATED_KEYS);
        return executeSqlAndReturnAffectedRows();
    }

    /**
     * 获取字段的值，若是主键则尝试生成主键的值，若是程序生成的主键，将生成的主键赋值给该主键字段
     *
     * @param tableInfo        主键 tableInfo
     * @param next             当前查询的对象
     * @param isGeneratedValue 主键值为null时，是否生成主键 true 生成，false不生成
     * @return 主键值
     */
    private Object getTableFieldValue(TableInfo tableInfo, Object next, boolean isGeneratedValue) throws SQLException {
        //先确定源字段是否有值
        Object invoke = ReflectUtils.getFieldValue(tableInfo.getField(), next);
        if (null != invoke) {
            //判断写入前是否需要转json
            if (tableInfo.getJsonMode() != null) {
                return ConverterUtils.getGson(tableInfo.getJsonMode()).toJson(invoke);
            }
            return invoke;
        }
        //若该值为null，则看看是不是需要程序生成主键
        GeneratedValue generatedValue = tableInfo.getGeneratedValue();
        if (generatedValue == null) {
            //啥也没有，直接返回null
            return null;
        }
        Object value = generatedPrimaryValue(tableInfo, isGeneratedValue);
        ReflectUtils.setFieldValue(tableInfo.getField(), next, value);
        return value;
    }

    /**
     * 生成主键值
     *
     * @param tableInfo        表信息
     * @param isGeneratedValue 是否生成新的主键
     * @return 主键值，若不生成主键，则返回 null
     */
    private Object generatedPrimaryValue(TableInfo tableInfo, boolean isGeneratedValue) throws SQLException {
        if (!isGeneratedValue) {
            return null;
        }
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
                printSql(sql);
                PreparedStatement ps = null;
                ResultSet rs = null;
                ps = connection.prepareStatement(sql);//NOSONAR
                rs = ps.executeQuery();
                rs.next();
                return rs.getObject(1, tableInfo.getField().getType());
            //不会走到default这里
            default:
                throw new IllegalStateException("Unexpected value: " + tableInfo.getGeneratedValue());
        }
    }


    private Integer executeSqlAndReturnAffectedRows() throws SQLException {
        int successCount = preparedStatement.executeBatch().length;
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceName, tableName);
        //若没有设置主键，直接返回
        if (tableInfoPrimaryKey == null || tableInfoPrimaryKey.getGeneratedValue() == null)
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
        //判断结果是否为空
        if (!generatedKeys.isBeforeFirst()) {
            return successCount;
        }
        while (resultIterator.hasNext()) {
            T next = resultIterator.next();
            final Object pkValue = ReflectUtils.getFieldValue(tableInfoPrimaryKey.getField(), next);
            //如果是带主键进行操作的  那么就不需要再次设置值了
            if (pkValue != null) {
                continue;
            }
            generatedKeys.next();
            Object object;
            if (dataSourceInfo.getDbType().equals(DbType.ORACLE)) {
                object = generatedKeys.getObject(Check.unSplicingName(tableInfoPrimaryKey.getColumn()), tableInfoPrimaryKey.getField().getType());
            } else {
                object = generatedKeys.getObject(RETURN_GENERATED_KEYS, tableInfoPrimaryKey.getField().getType());
            }
            //如果用户没有给定主键值，那么就将系统返回的主键赋值到当前对象上
            //修改成的0，可以存在,新添加的0，不允许存在，会根据行号改变，所以这里用户赋值0的话，主键要进行重新赋值
            ReflectUtils.setFieldValue(tableInfoPrimaryKey.getField(), next, object);
        }
        return successCount;
    }

    @Override
    public Integer insertOrUpdate() throws SQLException {
        DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSourceName);
        if (dataSourceInfo.getDbType().equals(DbType.ORACLE)) {
            throw new BraveException("Oracle尚未支持该方法");
        }
        String columnToStr = ContextApplication.formatAllColumToStr(dataSourceName, tableName);
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(tableName).append(" ( ").append(columnToStr).append(" ) values ( ");
        List<String> duplicateKeys = new ArrayList<>();
        tableInfos.forEach(tableInfo -> {
            sql.append(" " + PLACEHOLDER + " ,");
            duplicateKeys.add(tableInfo.getColumn() + " = values(" + tableInfo.getColumn() + ")");
        });
        String prepareSql = sql.substring(0, sql.length() - 1) + ")";
        String join = String.join(",", duplicateKeys);
        prepareSql = prepareSql.concat(" on duplicate key update ").concat(join);
        return setValuesExecuteSql(prepareSql, tableInfos);
    }

    @Override
    public Integer insertOrUpdateActive() throws SQLException {
        DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSourceName);
        if (dataSourceInfo.getDbType().equals(DbType.ORACLE)) {
            throw new BraveException("Oracle尚未支持该方法");
        }
        //这里只会存在一个对象
        T next = data.iterator().next();
        List<TableInfo> filterList = updateSqlCheckNullReturnColumns(next);
        if (filterList.isEmpty()) {
            throw new BraveException("没有需要新增或更新的字段");
        }
        String columns = filterList.stream().map(TableInfo::getColumn).collect(Collectors.joining(", "));
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ").append(tableName).append(" ( ").append(columns).append(" ) values ( ");
        List<String> duplicateKeys = new ArrayList<>();
        filterList.forEach(tableInfo -> {
            sql.append(" " + PLACEHOLDER + " ,");
            duplicateKeys.add(tableInfo.getColumn() + " = values(" + tableInfo.getColumn() + ")");
        });
        String prepareSql = sql.substring(0, sql.length() - 1) + ")";
        String join = String.join(",", duplicateKeys);
        prepareSql = prepareSql.concat(" on duplicate key update ").concat(join);
        return setValuesExecuteSql(prepareSql, filterList);
    }

    @Override
    public Integer update() throws SQLException {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tableName).append(" set");
        for (T next : data) {
            appendSetValueSql(tableInfos, sql, next);
        }
        return baseUpdate(sql);
    }

    @Override
    public Integer updateActive() throws SQLException {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tableName).append(" set");
        for (T next : data) {
            updateSqlCheckSetNullProperties(sql, tableInfos, next);
        }
        return baseUpdate(sql);
    }


    private Integer baseUpdate(StringBuilder sql) throws SQLException {
        if (sql.toString().endsWith("set")) {
            return 0;
        }
        String sqlPrefix = sql.substring(0, sql.length() - 1);
        if (StringUtils.isEmpty(whereSql)) {
            if (log.isDebugEnabled()) {
                log.debug("update操作未发现where语句，该操作会更新全表数据");
            }
        } else {
            sqlPrefix = sqlPrefix + SPACE + WHERE + SPACE + whereSql;
        }
        String parseSql = ParseSql.parseSql(sqlPrefix);
        return executeUpdateSqlAndReturnAffectedRows(parseSql);
    }

    @Override
    public Integer updateByPrimaryKey() throws SQLException {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceName, tableName);
        if (Objects.isNull(tableInfoPrimaryKey)) {
            throw new BraveException(tableName + "表未配置主键");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tableName).append(" set");
        T next = data.iterator().next();
        appendSetValueSql(tableInfos, sql, next);
        return assertEndSet(tableInfoPrimaryKey, sql, next);
    }

    private Integer assertEndSet(TableInfo tableInfoPrimaryKey, StringBuilder sql, T next) throws SQLException {
        if (sql.toString().endsWith("set")) {
            return 0;
        }
        String sqlPrefix = sql.substring(0, sql.length() - 1);
        Object primaryKeyValue = getPrimaryKeyValue(tableInfoPrimaryKey, next);
        sqlPrefix = sqlPrefix + SPACE + WHERE + SPACE + tableInfoPrimaryKey.getColumn() + SPACE + EQ + SPACE + ParseSql.matchValue(primaryKeyValue);
        String parseSql = ParseSql.parseSql(sqlPrefix);
        return executeUpdateSqlAndReturnAffectedRows(parseSql);
    }

    private void appendSetValueSql(List<TableInfo> tableInfos, StringBuilder sql, Object next) throws SQLException {
        int whereBeforeParamIndex = 0;
        for (TableInfo tableInfo : tableInfos) {
            sql.append(SPACE).append(tableInfo.getColumn()).append(SPACE).append(EQ).append(SPACE);
            Object invoke = getTableFieldValue(tableInfo, next, false);
            preparedSql.addParameter(whereBeforeParamIndex++, invoke);
            sql.append(PLACEHOLDER).append(COMMA);
        }
    }

    private Object getPrimaryKeyValue(TableInfo tableInfoPrimaryKey, Object next) {
        Object primaryKeyValue = ReflectUtils.getFieldValue(tableInfoPrimaryKey.getField(), next);
        if (Objects.isNull(primaryKeyValue)) {
            throw new BraveException(tableName + "表的主键值不存在");
        }
        return primaryKeyValue;
    }

    @Override
    public Integer updateActiveByPrimaryKey() throws SQLException {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceName, tableName);
        if (Objects.isNull(tableInfoPrimaryKey)) {
            throw new BraveException(tableName + "表未配置主键");
        }
        T next = data.iterator().next();
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(tableName).append(" set");
        updateSqlCheckSetNullProperties(sql, tableInfos, next);
        return assertEndSet(tableInfoPrimaryKey, sql, next);
    }

    @Override
    public Integer delete() throws SQLException {
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

    @Override
    public Integer deleteByPrimaryKey(Object primaryKeyValue) throws SQLException {
        TableInfo tableInfoPrimaryKey = ContextApplication.getTableInfoPrimaryKey(dataSourceName, tableName);
        if (Objects.isNull(tableInfoPrimaryKey)) {
            throw new BraveException(tableName + "表未配置主键");
        }
        String sql = "delete from " + tableName + " where " + tableInfoPrimaryKey.getColumn() +
                Constant.EQ + SPACE + PLACEHOLDER;
        final Object value = ParseSql.matchFixValue(primaryKeyValue, dataSourceName, tableName, tableInfoPrimaryKey.getField().getName());
        preparedSql.addParameter(value);
        return executeUpdateSqlAndReturnAffectedRows(sql);
    }

    private Integer executeUpdateSqlAndReturnAffectedRows(String sql) throws SQLException {
        setPreparedStatementParam(sql, false);
        return preparedStatement.executeUpdate();
    }

    private void updateSqlCheckSetNullProperties(StringBuilder sql, List<TableInfo> tableInfos, T nextObject) {
        int whereBeforeParamIndex = 0;
        for (TableInfo tableInfo : tableInfos) {
            try {
                Object invoke = getTableFieldValue(tableInfo, nextObject, false);
                if (Objects.isNull(invoke) && !updateNullProperties.contains(tableInfo.getField().getName())) {
                    continue;
                }
                sql.append(SPACE).append(tableInfo.getColumn()).append(SPACE).append(EQ).append(SPACE);
                preparedSql.addParameter(whereBeforeParamIndex++, invoke);
                sql.append(PLACEHOLDER).append(COMMA);
            } catch (Exception ex) {
                JdbcUtils.closeConnection(connection);
                throw new BraveException(ex.getMessage(), ex);
            }
        }
    }

    private List<TableInfo> updateSqlCheckNullReturnColumns(T nextObject) {
        List<TableInfo> tableInfos = ContextApplication.getTableInfos(dataSourceName, tableName);
        List<TableInfo> filterList = new ArrayList<>();
        for (TableInfo tableColumnInfo : tableInfos) {
            try {
                Object invoke = getTableFieldValue(tableColumnInfo, nextObject, false);
                if (Objects.isNull(invoke) && !updateNullProperties.contains(tableColumnInfo.getField().getName())) {
                    continue;
                }
                filterList.add(tableColumnInfo);
            } catch (Exception ex) {
                JdbcUtils.closeConnection(connection);
                throw new BraveException(ex.getMessage(), ex);
            }
        }
        return filterList;
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
                    tableName, String dataSourceName, String whereSql, List<Object> params) {
        //让编译器开心
        this.currentClass = currentClass;
        this.whereSql = whereSql;
        this.tableName = tableName;
        this.dataSourceName = dataSourceName;
        this.pageInfo = pageInfo;
        this.data = data;
        this.updateNullProperties = updateNullProperties;
        this.preparedSql = new PreparedSql(currentClass, params);
//        this.interceptorHelper = new InterceptorHelper(preparedSql);
    }

    @Override
    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected String getDataSourceName() {
        return dataSourceName;
    }

    @Override
    protected ResultSet getResultSet() {
        return resultSet;
    }

    @Override
    protected Connection getConnection() {
        return connection;
    }

    @Override
    protected PreparedStatement getStatement() {
        return preparedStatement;
    }

//    @Override
//    protected PreparedSql getPreparedSql() {
//        return preparedSql;
//    }
}
