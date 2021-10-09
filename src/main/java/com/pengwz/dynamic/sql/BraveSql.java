package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.base.CustomizeSQL;
import com.pengwz.dynamic.sql.base.Fn;
import com.pengwz.dynamic.sql.base.Sqls;
import com.pengwz.dynamic.sql.base.impl.SqlImpl;
import com.pengwz.dynamic.utils.CollectionUtils;
import com.pengwz.dynamic.utils.ReflectUtils;
import com.pengwz.dynamic.utils.StringUtils;

import java.util.*;

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
     * 执行CREATE、ALTER、UPDATE、DROP等等语句。<br>
     * 如果执行未发生异常，则代表SQL成功执行，若果执行失败，将抛出异常。<br>
     * 调用者无需关心返回值。
     *
     * @param ddlSql SQL语句
     */
    public static void executeSql(String executeSql) {
        new CustomizeSQL<>(Void.class, executeSql).executeSql();
    }

    /**
     * 执行CREATE、ALTER、UPDATE、DROP等等语句。<br>
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
     * @return SQL查询后的结果集
     */
    public List<T> select() {
        return mustShare().select();
    }

    /**
     * 根据where条件查询单条数据，若查询无数据，返回null
     *
     * @return SQL查询后的结果
     * @throws BraveException 若返回多条数据，则抛出此异常
     */
    public T selectSingle() {
        if (dynamicSql.getDeclarations().isEmpty()) {
            throw new BraveException("必须提供 where 条件语句");
        }
        return mustShare().selectSingle();
    }

    /**
     * 根据主键值查询数据
     *
     * @param primaryValue 主键值
     * @return SQL查询后的结果
     * @see com.pengwz.dynamic.anno.Id 在实体类中标注主键属性
     */
    public T selectByPrimaryKey(Object primaryValue) {
        if (Objects.isNull(primaryValue)) {
            throw new BraveException("主键值不可为空");
        }
        return mustShare().selectByPrimaryKey(primaryValue);
    }

    /**
     * 查询条件（若有）查询总数量，若没有数据，则返回 0
     *
     * @return 查询结果集总数量
     */
    public Integer selectCount() {
        return mustShare().selectCount();
    }

    /**
     * 分页查询<br>
     * 查询数据库的前{@code pageSize} 条
     *
     * @param pageSize 查询当前页的数量
     * @return 分页对象
     */
    public PageInfo<T> selectPageInfo(int pageSize) {
        pageInfo = new PageInfo<>(0, pageSize);
        return mustShare().selectPageInfo();
    }

    /**
     * 分页查询<br>
     * 查询数据库的第{@code pageIndex} 页的{@code pageSize}条记录
     *
     * @param pageIndex 查询当前页页码
     * @param pageSize  查询当前页的数量
     * @return 分页对象
     */
    public PageInfo<T> selectPageInfo(int pageIndex, int pageSize) {
        pageInfo = new PageInfo<>(pageIndex, pageSize);
        return mustShare().selectPageInfo();
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
        return mustShare().insertActive();
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
        return mustShare().batchInsert();
    }

    /**
     * 根据唯一约束、主键等判断表中记录是否存在。
     * 若表中记录存在，进行更新操作，否则插入
     * <p/>
     * <strong>
     * 注意，该方法将会执行类似如下SQL ... on duplicate key update ...<br/>
     * 这种SQL在Mysql中回显的主键可能是不正确的，为了保险起见，请不要在业务中使用该方法所回填的主键
     * </strong>
     *
     * @param data 待插入的数据
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
     * 根据唯一约束，判断表中记录是否存在。
     * 若表中记录存在，批量进行更新操作，否则插入
     * <p/>
     * <strong>
     * 注意，该方法将会执行类似如下SQL ... on duplicate key update ...<br/>
     * 这种SQL回显的主键可能是不正确的，为了保险起见，请不要在业务中使用该方法所回填的主键
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
        return mustShare().insertOrUpdate();
    }


    /**
     * 更新对象所有属性，包括null元素
     *
     * @param data 待更新的对象
     * @return 若更新成功，返回1
     */
    public Integer update(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待更新的对象");
        }
        this.data = Collections.singletonList(data);
        return mustShare().update();
    }

    /**
     * 更新对象所有属性，如果类中属性为null，则使用数据库默认值（如果有）
     *
     * @param data 待更新的对象
     * @return 若更新成功，返回1
     */
    public Integer updateActive(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待更新的对象");
        }
        this.data = Collections.singletonList(data);
        return mustShare().updateActive();
    }

//    /**
//     * 更新对象所有属性，如果类中属性为null，则使用数据库默认值（如果有）
//     *
//     * @param iterable 待更新的多个对象
//     * @return 返回更新成功的数量
//     */
//    public Integer batchUpdate(Iterable<T> iterable) {
//        if (Objects.isNull(iterable) || !iterable.iterator().hasNext()) {
//            throw new BraveException("必须提供待插入的数据");
//        }
//        this.data = iterable;
//        return mustShare().updateBatch();
//    }

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
        return mustShare().updateByPrimaryKey();
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
        return mustShare().updateActiveByPrimaryKey();
    }

    /**
     * 根据条件删除数据，若为提供where条件，则会删除全部数据
     *
     * @return 删除的数据量
     */
    public Integer delete() {
        return mustShare().delete();
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
        return mustShare().deleteByPrimaryKey(key);
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
     * @param feilds 实体类属性名，支持多个
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
     * @param feilds 实体类属性名，支持多个
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

    private Sqls<T> mustShare() {
        // 解析where语句
        Table table = currentClass.getAnnotation(Table.class);
        if (Objects.isNull(table) || StringUtils.isEmpty(table.value())) {
            throw new BraveException("当前实体类：" + currentClass + "未获取到表名");
        }
        String tableName = table.value().trim();
        String defalutDataSource = DataSourceManagement.initDataSourceConfig(table.dataSourceClass(), tableName);
        Check.checkPageInfo(pageInfo);
        String whereSql = ParseSql.parse(currentClass, table, defalutDataSource, dynamicSql.getDeclarations(), orderByMap);
        //调正where子句的sql顺序 ，将来把它单独抽出来  作为组件
        //whereSql = ParseSql.fixWhereSql(whereSql);
        SqlImpl<T> sqls = new SqlImpl<>();
        sqls.init(currentClass, pageInfo, data, dynamicSql.getUpdateNullProperties(), Check.getTableName(tableName, defalutDataSource), defalutDataSource, whereSql);
        //优化他
        sqls.before();
        return sqls;
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
}
