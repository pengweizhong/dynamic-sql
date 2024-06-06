package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.base.CustomizeSQL;
import com.pengwz.dynamic.sql.base.Fn;
import com.pengwz.dynamic.sql.base.Sqls;
import com.pengwz.dynamic.sql.base.enumerate.FunctionEnum;
import com.pengwz.dynamic.sql.base.impl.SqlImpl;
import com.pengwz.dynamic.utils.CollectionUtils;
import com.pengwz.dynamic.utils.ReflectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;

import static com.pengwz.dynamic.config.DataSourceManagement.close;

/**
 * 数据库交互对象
 *
 * @param <T>
 */
@SuppressWarnings("all")
public class BraveSql<T> {

    private DynamicSql dynamicSql;

    private Class<T> currentClass;

    private PageInfo<T> pageInfo;

    private Iterable<T> data;

    private Map<String, List<String>> orderByMap;

    private Sqls<T> tSqls;

    protected BraveSql(DynamicSql dynamicSql, Class<T> currentClass) {
        this.dynamicSql = dynamicSql;
        this.currentClass = currentClass;
    }

    /**
     * 使用动态SQL和对应表实体类构建出操作DB的对象
     *
     * @param dynamicSql   动态SQL条件，主要用于组装where条件
     * @param currentClass 表对应实体类
     * @param <T>          标注了{@code @Table}注解的任何实体类
     * @return {@code BraveSql}实例
     */
    public static <T> BraveSql<T> build(DynamicSql dynamicSql, Class<T> currentClass) {
        return new BraveSql<>(dynamicSql, currentClass);
    }

    /**
     * 构建出不包含where条件的操作DB对象
     *
     * @param currentClass 表对应实体类
     * @param <T>          标注了{@code @Table}注解的任何实体类
     * @return {@code BraveSql}实例
     */
    public static <T> BraveSql<T> build(Class<T> currentClass) {
        return new BraveSql<>(DynamicSql.createDynamicSql(), currentClass);
    }

    public DynamicSql getDynamicSql() {
        return dynamicSql;
    }

    public Class<T> getCurrentClass() {
        return currentClass;
    }

    public PageInfo<T> getPageInfo() {
        return pageInfo;
    }

    /**
     * 执行自定义查询语句，并使用自定义实体类接收，实体类属性和查询列名、列别名匹配上，就可以自动映射。<br>
     * 此方法单独为{@code Spring}环境提供的方法，非{@code Spring}环境调用{@link this#executeQuery(String, Class)}
     * <p/>
     *
     * @param querySql 查询的sql
     * @return SQL查询后的结果集
     * @see com.pengwz.dynamic.anno.Column 若成员变量标注了该注解，则注解内名称和结果集列名进行匹配
     */
    public List<T> executeQuery(String querySql) {
        return new CustomizeSQL<T>(currentClass, querySql).executeQuery();
    }

    /**
     * 执行自定义查询语句，并使用自定义实体类接收，实体类属性和查询列名、列别名匹配上，就可以自动映射。<br>
     *
     * @param querySql        查询的sql
     * @param dataSourceClass 指定数据源
     * @return SQL查询后的结果集
     * @see com.pengwz.dynamic.anno.Column 若成员变量标注了该注解，则注解内名称和结果集列名进行匹配
     */
    public List<T> executeQuery(String querySql, Class<? extends DataSourceConfig> dataSourceClass) {
        return new CustomizeSQL<T>(dataSourceClass, currentClass, querySql).executeQuery();
    }

    /**
     * 执行自定义查询语句，并使用自定义实体类接收，实体类属性和查询列名、列别名匹配上，就可以自动映射。<br>
     * 此方法单独为{@code Spring}环境提供的方法，非{@code Spring}环境调用{@link this#executeQuerySingle(String, Class)}
     *
     * @param querySql 查询的sql
     * @return SQL查询后的结果集
     */
    public T executeQuerySingle(String querySql) {
        return new CustomizeSQL<T>(currentClass, querySql).executeQuerySingle();
    }

    /**
     * 执行自定义查询语句，并使用自定义实体类接收，实体类属性和查询列名、列别名匹配上，就可以自动映射。<br>
     *
     * @param querySql        查询的sql
     * @param dataSourceClass 指定数据源
     * @return SQL查询后的结果集
     */
    public T executeQuerySingle(String querySql, Class<? extends DataSourceConfig> dataSourceClass) {
        return new CustomizeSQL<T>(dataSourceClass, currentClass, querySql).executeQuerySingle();
    }

    /**
     * 执行任意SQL语句。<br>
     * 如果执行未发生异常，则代表SQL成功执行，若果执行失败，将抛出异常。<br>
     * 调用者无需关心返回值。
     *
     * @param executeSql SQL语句
     */
    public static void executeSql(String executeSql) {
        new CustomizeSQL<>(Void.class, executeSql).executeSql();
    }

    /**
     * 执行任意SQL语句。<br>
     * 如果执行未发生异常，则代表SQL成功执行，若果执行失败，将抛出异常。<br>
     * 调用者无需关心返回值。
     *
     * @param executeSql      SQL语句
     * @param dataSourceClass 指定数据源
     */
    public static void executeSql(String executeSql, Class<? extends DataSourceConfig> dataSourceClass) {
        new CustomizeSQL<>(dataSourceClass, Void.class, executeSql).executeSql();
    }

    /**
     * 判断表名是否存在
     *
     * @param tableName 表名称
     * @return 如果存在返回{@code true}，否则{@code false}
     */
    public static boolean existTable(String tableName) {
        return new CustomizeSQL<>(Void.class, tableName).existTable();
    }

    /**
     * 判断表名是否存在
     *
     * @param tableName       表名称
     * @param dataSourceClass 指定数据源
     * @return 如果存在返回{@code true}，否则{@code false}
     */
    public static boolean existTable(String tableName, Class<? extends DataSourceConfig> dataSourceClass) {
        return new CustomizeSQL<>(dataSourceClass, Void.class, tableName).existTable();
    }

    /**
     * 查询表中全部的数据，若表中无数据，则返回空集合
     *
     * @return SQL查询后的结果集，若表中无数据，则返回空集合
     */
    public List<T> select() {
        return doExecute(() -> mustShare().select());
    }

    /**
     * 根据where条件（若有）查询单条数据，若查询无数据，返回null
     *
     * @return SQL查询后的结果，若查询无数据，返回null
     * @throws BraveException 若返回多条数据，则抛出此异常
     */
    public T selectSingle() {
        return doExecute(() -> mustShare().selectSingle());
    }

    /**
     * 根据主键值查询数据，若实体类未标注主键值，则抛出异常
     *
     * @param primaryValue 主键值
     * @return SQL查询后的结果，没有数据则返回 null
     * @see com.pengwz.dynamic.anno.Id 在实体类中标注主键属性
     */
    public T selectByPrimaryKey(Object primaryValue) {
        if (Objects.isNull(primaryValue)) {
            throw new BraveException("主键值不可为空");
        }
        return doExecute(() -> mustShare().selectByPrimaryKey(primaryValue));
    }

    /**
     * 查询条件（若有）查询总数量，若没有数据，则返回 0
     * 这里执行 count(1)
     *
     * @return 查询结果集总数量，若没有数据，则返回 0
     */
    public Integer selectCount() {
        return this.selectCount("1");
    }

    /**
     * 查询条件（若有）查询总数量，若没有数据，则返回 0
     * 这里执行 count(fn)
     *
     * @param fn 作为count参数查询
     * @return 查询结果集总数量，若没有数据，则返回 0
     */
    public Integer selectCount(Fn<T, Object> fn) {
        return this.selectCount(ReflectUtils.fnToFieldName(fn));
    }


    /**
     * 查询条件（若有）查询总数量，若没有数据，则返回 0
     * 这里执行 count(property)
     *
     * @param property 作为count参数查询
     * @return 查询结果集总数量，若没有数据，则返回 0
     */
    public Integer selectCount(String property) {
        return doExecute(() -> mustShare().selectAggregateFunction(property.trim(), FunctionEnum.COUNT, Integer.class));
    }

    /**
     * 对指定字段求和
     *
     * @param fn 实体类字段名
     * @return 和值，若没有数据，返回 null
     */
    public BigDecimal selectSum(Fn<T, Object> fn) {
        return this.selectSum(ReflectUtils.fnToFieldName(fn));
    }

    /**
     * 对指定字段求和
     *
     * @param property 实体类字段名
     * @return 和值，若没有数据，返回 null
     */
    public BigDecimal selectSum(String property) {
        return doExecute(() -> mustShare().selectAggregateFunction(property.trim(), FunctionEnum.SUM, BigDecimal.class));
    }

    /**
     * 对指定字段求平均值
     *
     * @param fn 实体类字段名
     * @return 平均值，若没有数据，返回 null
     */
    public BigDecimal selectAvg(Fn<T, Object> fn) {
        return this.selectAvg(ReflectUtils.fnToFieldName(fn));
    }

    /**
     * 对指定字段求平均值
     *
     * @param property 实体类字段名
     * @return 平均值，若没有数据，返回 null
     */
    public BigDecimal selectAvg(String property) {
        return this.selectAvg(property.trim(), BigDecimal.class);
    }

    /**
     * 对指定字段求平均值，并适用指定的数据类型接收结果值
     *
     * @param fn          实体类字段名
     * @param targetClass 结果类型
     * @return 平均值，若没有数据，返回 null
     */
    public <R> R selectAvg(Fn<T, Object> fn, Class<R> targetClass) {
        return this.selectAvg(ReflectUtils.fnToFieldName(fn), targetClass);
    }

    /**
     * 对指定字段求平均值，并适用指定的数据类型接收结果值
     *
     * @param property    实体类字段名
     * @param targetClass 结果类型
     * @return 平均值，若没有数据，返回 null
     */
    public <R> R selectAvg(String property, Class<R> targetClass) {
        return doExecute(() -> mustShare().selectAggregateFunction(property.trim(), FunctionEnum.AVG, targetClass));
    }

    /**
     * 对指定字段求最小值
     *
     * @param fn 实体类字段名，通常是Get方法
     * @return 最小值，若没有数据，返回 null
     */
    public BigDecimal selectMin(Fn<T, Object> fn) {
        return this.selectMin(ReflectUtils.fnToFieldName(fn));
    }

    /**
     * 对指定字段求最小值，并适用指定的数据类型接收结果值
     *
     * @param property 实体类字段名
     * @return 最小值，若没有数据，返回 null
     */
    public BigDecimal selectMin(String property) {
        return this.selectMin(property.trim(), BigDecimal.class);
    }

    /**
     * 对指定字段求最小值，并适用指定的数据类型接收结果值
     *
     * @param fn          实体类字段名，通常是Get方法
     * @param targetClass 结果类型
     * @return 最小值，若没有数据，返回 null
     */
    public <R> R selectMin(Fn<T, Object> fn, Class<R> targetClass) {
        return this.selectMin(ReflectUtils.fnToFieldName(fn), targetClass);
    }

    public <K, R> Map<K, R> selectMin(String valueProperty, Class<K> keyClass, Class<R> valueClass, String keyProperty) {
        return doExecute(() -> mustShare().selectAggregateFunction(valueProperty.trim(), FunctionEnum.MIN, keyClass, valueClass, keyProperty));
    }

    /**
     * 对指定字段求最小值
     *
     * @param property    实体类字段名
     * @param targetClass 结果类型
     * @return 最小值，若没有数据，返回 null
     */
    public <R> R selectMin(String property, Class<R> targetClass) {
        return doExecute(() -> mustShare().selectAggregateFunction(property.trim(), FunctionEnum.MIN, targetClass));
    }

    /**
     * @see this#selectMax(Fn, Fn)
     */
    public <K, R> Map<K, R> selectMin(Fn<T, Object> fn, Fn<T, K> groupKey) {
        return selectMin(ReflectUtils.fnToFieldName(fn), ReflectUtils.getReturnTypeFromSignature(groupKey),
                ReflectUtils.getReturnTypeFromSignature(fn), ReflectUtils.fnToFieldName(groupKey));
    }

    /**
     * 对指定字段求最大值
     *
     * @param fn 实体类字段名，通常是Get方法
     * @return 最大值，若没有数据，返回 null
     */
    public BigDecimal selectMax(Fn<T, Object> fn) {
        return this.selectMax(ReflectUtils.fnToFieldName(fn), BigDecimal.class);
    }


    /**
     * 对指定字段求最大值
     *
     * @param property 实体类字段名
     * @return 最大值，若没有数据，返回 null
     */
    public BigDecimal selectMax(String property) {
        return this.selectMax(property, BigDecimal.class);
    }

    /**
     * 对指定字段求最大值，并适用指定的数据类型接收结果值
     *
     * @param fn          实体类字段名，通常是Get方法
     * @param targetClass 结果类型
     * @return 最大值，若没有数据，返回 null
     */
    public <R> R selectMax(Fn<T, Object> fn, Class<R> targetClass) {
        return this.selectMax(ReflectUtils.fnToFieldName(fn), targetClass);
    }

    /**
     * 搭配分组字段对指定字段求最大值，并将其组装为Map返回。<br>
     * 比如：<br>
     * <pre>
     *         DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
     *         dynamicSql.andIn(UserEntity::getId, Arrays.asList(1, 2, 3, 4, 5));
     *         dynamicSql.groupBy(UserEntity::getUsername);
     *         Map<String, LocalDate> map = BraveSql.build(dynamicSql, UserEntity.class)
     *                 .selectMax(UserEntity::getBirth, UserEntity::getUsername);
     *         map.forEach((k, v) -> System.out.println(k + ":" + v));
     * </pre>
     *
     * @param fn       对应分组中最大结果字段，作为Value使用
     * @param groupKey 分组字段，作为K使用
     * @param <K>
     * @param <R>
     * @return LinkedHashMap，若没有数据，则返回空Map
     */
    public <K, R> Map<K, R> selectMax(Fn<T, Object> fn, Fn<T, K> groupKey) {
        return selectMax(ReflectUtils.fnToFieldName(fn), ReflectUtils.getReturnTypeFromSignature(groupKey),
                ReflectUtils.getReturnTypeFromSignature(fn), ReflectUtils.fnToFieldName(groupKey));
    }

    /**
     * 对指定字段求最大值，并适用指定的数据类型接收结果值
     *
     * @param property    实体类字段名
     * @param targetClass 结果类型
     * @return 最大值，若没有数据，返回 null
     */
    public <R> R selectMax(String property, Class<R> targetClass) {
        return doExecute(() -> mustShare().selectAggregateFunction(property.trim(), FunctionEnum.MAX, targetClass));
    }

    public <K, R> Map<K, R> selectMax(String valueProperty, Class<K> keyClass, Class<R> valueClass, String keyProperty) {
        return doExecute(() -> mustShare().selectAggregateFunction(valueProperty.trim(), FunctionEnum.MAX, keyClass, valueClass, keyProperty));
    }

    /**
     * 分页查询表数据<br>
     * 查询数据库的前{@code pageSize} 条
     *
     * @param pageSize 查询当前页的数量
     * @return 分页对象，若没有数据，则 {@code PageInfo}中{@code resultList}集合为空，不是null。
     */
    public PageInfo<T> selectPageInfo(int pageSize) {
        pageInfo = new PageInfo<>(0, pageSize);
        return doExecute(() -> mustShare().selectPageInfo());
    }

    /**
     * 分页查询<br>
     * 查询数据库的第{@code pageIndex} 页的{@code pageSize}条记录
     *
     * @param pageIndex 查询当前页页码
     * @param pageSize  查询当前页的数量
     * @return 分页对象，若没有数据，则 {@code PageInfo}中{@code resultList}集合为空，不是null。
     */
    public PageInfo<T> selectPageInfo(int pageIndex, int pageSize) {
        pageInfo = new PageInfo<>(pageIndex, pageSize);
        return doExecute(() -> mustShare().selectPageInfo());
    }

    /**
     * 新增表记录，属性为null的则插入null
     *
     * @param data 待新增的数据
     * @return 成功返回1，否则返回其他值
     */
    public Integer insert(T data) {
        if (Objects.isNull(data)) {
            return 0;
        }
        this.data = Collections.singletonList(data);
        return batchInsert(this.data);
    }

    /**
     * 新增表记录，有选择的新增，当提供的列数据为null时，将使用数据库默认值。
     *
     * @param data 待新增的数据
     * @return 成功返回1，否则返回其他值
     */
    public Integer insertActive(T data) {
        if (Objects.isNull(data)) {
            return 0;
        }
        this.data = Collections.singletonList(data);
        return doExecute(() -> mustShare().insertActive());
    }

    /**
     * 批量新增表记录，属性为null的则插入null
     *
     * @param iterable 待新增的数据集合
     * @return 成功新增的数量
     */
    public Integer batchInsert(Iterable<T> iterable) {
        if (Objects.isNull(iterable) || !iterable.iterator().hasNext()) {
            return 0;
        }
        data = iterable;
        return doExecute(() -> mustShare().batchInsert());
    }

    /**
     * 根据唯一约束、主键等判断表中记录是否存在。
     * 若表中记录存在，进行更新操作，否则插入
     * <p/>
     * <strong>
     * 注意，该方法将会执行类似如下SQL ... on duplicate key update ...<br/>
     * 这种SQL在Mysql中回显的主键可能是不正确的，为了保险起见，请不要在业务中使用该方法所回填的主键。<br/>
     * 注意，oracle尚未支持该函数。
     * </strong>
     *
     * @param data 待插入或更新的数据
     * @return 操作成功的数量
     */
    public Integer insertOrUpdate(T data) {
        if (Objects.isNull(data)) {
            return 0;
        }
        this.data = Collections.singletonList(data);
        return batchInsertOrUpdate(this.data);
    }

    /**
     * 根据唯一约束、主键等判断表中记录是否存在。
     * 若表中记录存在，进行更新操作，否则插入。
     * 若属性为null，则会被忽略，除非指定了强制声明
     *
     * @param data 待插入或更新的数据
     * @return 操作成功的数量
     */
    public Integer insertOrUpdateActive(T data) {
        if (Objects.isNull(data)) {
            return 0;
        }
        this.data = Collections.singletonList(data);
        return doExecute(() -> mustShare().insertOrUpdateActive());
    }

    /**
     * 根据唯一约束，判断表中记录是否存在。
     * 若表中记录存在，批量进行更新操作，否则插入
     * <p/>
     * <strong>
     * 注意，该方法将会执行类似如下SQL ... on duplicate key update ...<br/>
     * 这种SQL回显的主键可能是不正确的，为了保险起见，请不要在业务中使用该方法所回填的主键<br/>
     * 注意，oracle尚未支持该函数。
     * </strong>
     *
     * @param iterable 待插入的数据集合
     * @return 操作成功的数量
     */
    public Integer batchInsertOrUpdate(Iterable<T> iterable) {
        if (Objects.isNull(iterable) || !iterable.iterator().hasNext()) {
            throw new BraveException("必须提供待插入的数据");
        }
        this.data = iterable;
        return doExecute(() -> mustShare().insertOrUpdate());
    }


    /**
     * 更新对象所有属性，包括null元素
     *
     * @param data 待更新的对象
     * @return 若更新成功，返回1，否则返回其他值
     */
    public Integer update(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待更新的对象");
        }
        this.data = Collections.singletonList(data);
        return doExecute(() -> mustShare().update());
    }

    /**
     * 更新对象所有属性，如果类中属性为null，则使用数据库默认值（如果有）
     *
     * @param data 待更新的对象
     * @return 若更新成功，返回1，否则返回其他值
     */
    public Integer updateActive(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待更新的对象");
        }
        this.data = Collections.singletonList(data);
        return doExecute(() -> mustShare().updateActive());
    }

    /**
     * 根据主键更新全部数据
     *
     * @param data 待更新的对象
     * @return 若更新成功，返回1
     */
    public Integer updateByPrimaryKey(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待更新的主键值");
        }
        this.data = Collections.singletonList(data);
        return doExecute(() -> mustShare().updateByPrimaryKey());
    }

    /**
     * 根据主键更新全部数据，若属性为空，则使用数据库默认值
     *
     * @param data 待更新的对象
     * @return 若更新成功，返回1
     */
    public Integer updateActiveByPrimaryKey(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待更新的主键值");
        }
        this.data = Collections.singletonList(data);
        return doExecute(() -> mustShare().updateActiveByPrimaryKey());
    }

    /**
     * 根据条件删除数据，若为提供where条件，则会删除全部数据
     *
     * @return 删除的数据量
     */
    public Integer delete() {
        return doExecute(() -> mustShare().delete());
    }

    /**
     * 根据主键删除数据
     *
     * @param key
     * @return 删除的数据量
     */
    public Integer deleteByPrimaryKey(Object key) {
        if (Objects.isNull(key)) {
            throw new BraveException("必须提供待删除的主键值");
        }
        return doExecute(() -> mustShare().deleteByPrimaryKey(key));
    }

    /**
     * 根据实体类属性名（非表中列名）进行正序排序
     *
     * @param feilds 实体类属性名，支持多个
     * @return SQL查询后的数据
     */
    public final BraveSql<T> orderByAsc(String... feilds) {
        if (Objects.isNull(feilds)) {
            throw new BraveException("当选择排序时，排序的字段不可为空");
        }
        for (String feild : feilds) {
            String trimFeild = feild.trim();
            if (trimFeild.length() == 0) {
                throw new BraveException("当选择排序时，排序的字段不可为空");
            }
            fillingOrderByMap("asc", trimFeild);
        }
        return this;
    }

    /**
     * 根据实体类属性名进行正序排序
     *
     * @param fns 实体类属性名，支持多个
     * @return SQL查询后的数据
     */
    @SafeVarargs
    public final BraveSql<T> orderByAsc(Fn<T, Object>... fns) {
        if (Objects.isNull(fns)) {
            throw new BraveException("当选择排序时，排序的字段不可为空");
        }
        for (Fn<T, Object> fn : fns) {
            String fieldName = ReflectUtils.fnToFieldName(fn);
            fillingOrderByMap("asc", fieldName);
        }
        return this;
    }

    /**
     * 根据实体类属性名（非表中列名）进行倒序排序
     *
     * @param feilds 实体类属性名，支持多个
     * @return SQL查询后的数据
     */
    public final BraveSql<T> orderByDesc(String... feilds) {
        if (Objects.isNull(feilds)) {
            throw new BraveException("当选择排序时，排序的字段不可为空");
        }
        for (String feild : feilds) {
            String trimFeild = feild.trim();
            if (trimFeild.length() == 0) {
                throw new BraveException("当选择排序时，排序的字段不可为空");
            }
            fillingOrderByMap("desc", trimFeild);
        }
        return this;
    }

    /**
     * 根据实体类属性名进行倒序排序
     *
     * @param fns 实体类属性名，支持多个
     * @return SQL查询后的数据
     */
    @SafeVarargs
    public final BraveSql<T> orderByDesc(Fn<T, Object>... fns) {
        if (Objects.isNull(fns)) {
            throw new BraveException("当选择排序时，排序的字段不可为空");
        }
        for (Fn<T, Object> fn : fns) {
            String fieldName = ReflectUtils.fnToFieldName(fn);
            fillingOrderByMap("desc", fieldName);
        }
        return this;
    }

    private void fillingOrderByMap(String ascOrDesc, String feild) {
        if (Objects.isNull(orderByMap)) {
            this.orderByMap = new LinkedHashMap<>();
        }
        List<String> strings = orderByMap.get(ascOrDesc);
        if (CollectionUtils.isEmpty(strings)) {
            strings = new ArrayList<>();
        }
        strings.add(feild);
        orderByMap.put(ascOrDesc, strings);
    }

    /**
     * 执行SQL并关闭任务
     *
     * @param supplier 需要执行的SQL
     * @param <R>      返回结果类型
     * @return 返回SQL执行结果
     */
    private <R> R doExecute(Supplier<Object> sqlsSupplier) {
        try {
            return (R) sqlsSupplier.get();
        } finally {
            if (tSqls != null) {
                close(tSqls.getDataSourceName(), tSqls.getResultSet(), tSqls.getPreparedStatement(), tSqls.getConnection());
            }
        }
    }

    private Sqls<T> mustShare() {
        Check.checkPageInfo(pageInfo);
        List<Object> params = new ArrayList<>();
        String whereSql = ParseSql.parse(currentClass, dynamicSql.getDeclarations(), orderByMap, params);
        //调正where子句的sql顺序 ，将来把它单独抽出来  作为组件
        whereSql = ParseSql.fixWhereSql(whereSql);
        SqlImpl<T> sqls = new SqlImpl<>();
        sqls.init(currentClass, pageInfo, data, dynamicSql.getUpdateNullProperties(), whereSql, params);
        this.tSqls = sqls;
        return sqls;
    }

}
