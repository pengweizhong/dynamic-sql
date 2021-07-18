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

    public static <T> BraveSql<T> build(DynamicSql dynamicSql, Class<T> currentClass) {
        return new BraveSql<>(dynamicSql, currentClass);
    }

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
     * 单独为spring容器提供的方法，单体项目调用{@link this#executeQuery(String, Class)}
     *
     * @param querySql 需要查询的sql
     * @return
     */
    public List<T> executeQuery(String querySql) {
        return new CustomizeSQL<T>(ContextApplication.getDefalutDataSourceName(), currentClass, querySql).executeQuery();
    }

    public List<T> executeQuery(String querySql, Class<? extends DataSourceConfig> dataSourceClass) {
        return new CustomizeSQL<T>(dataSourceClass, currentClass, querySql).executeQuery();
    }

    public T executeQuerySingle(String querySql) {
        return new CustomizeSQL<T>(ContextApplication.getDefalutDataSourceName(), currentClass, querySql).executeQuerySingle();
    }

    public T executeQuerySingle(String querySql, Class<? extends DataSourceConfig> dataSourceClass) {
        return new CustomizeSQL<T>(dataSourceClass, currentClass, querySql).executeQuerySingle();
    }

    /**
     * 执行CREATE、ALTER、UPDATE、DROP等等语句。<br>
     * 如果执行未发生异常，则代表SQL成功执行，若果执行失败，将抛出异常。<br>
     * 调用者无需关心返回值。
     *
     * @param ddlSql
     */
    public void executeSql(String executeSql) {
        new CustomizeSQL<T>(ContextApplication.getDefalutDataSourceName(), currentClass, executeSql).executeDDL();
    }

    public void executeSql(String ddlSql, Class<? extends DataSourceConfig> dataSourceClass) {
        new CustomizeSQL<T>(dataSourceClass, currentClass, ddlSql).executeDDL();
    }

    public boolean existTable(String tableName) {
        return new CustomizeSQL<T>(ContextApplication.getDefalutDataSourceName(), currentClass, tableName).existTable();
    }

    public boolean existTable(String tableName, Class<? extends DataSourceConfig> dataSourceClass) {
        return new CustomizeSQL<T>(dataSourceClass, currentClass, tableName).existTable();
    }

    public List<T> select() {
        return mustShare().select();
    }

    public T selectSingle() {
        if (dynamicSql.getDeclarations().isEmpty()) {
            throw new BraveException("必须提供 where 条件语句");
        }
        return mustShare().selectSingle();
    }

    public T selectByPrimaryKey(Object primaryValue) {
        if (Objects.isNull(primaryValue)) {
            throw new BraveException("主键值不可为空");
        }
        return mustShare().selectByPrimaryKey(primaryValue);
    }

    public Integer selectCount() {
        return mustShare().selectCount();
    }

    public PageInfo<T> selectPageInfo(int pageSize) {
        pageInfo = new PageInfo<>(0, pageSize);
        return mustShare().selectPageInfo();
    }

    public PageInfo<T> selectPageInfo(int pageIndex, int pageSize) {
        pageInfo = new PageInfo<>(pageIndex, pageSize);
        return mustShare().selectPageInfo();
    }

    /**
     * 新增全部属性，属性为null的则插入null
     *
     * @param data
     * @return
     */
    public Integer insert(T data) {
        if (Objects.isNull(data)) {
            return 0;
        }
        this.data = Collections.singletonList(data);
        return batchInsert(this.data);
    }

    /**
     * 属性为null的会使用数据库默认值
     *
     * @param data
     * @return
     */
    public Integer insertActive(T data) {
        if (Objects.isNull(data)) {
            return 0;
        }
        this.data = Collections.singletonList(data);
        return mustShare().insertActive();
    }

    public Integer batchInsert(Iterable<T> iterable) {
        if (Objects.isNull(iterable) || !iterable.iterator().hasNext()) {
            return 0;
        }
        data = iterable;
        return mustShare().batchInsert();
    }

    /**
     * 根据唯一约束，判断表中记录是否存在。
     * 若表中记录存在，进行更新操作，否则插入
     * <p/>
     * <strong>
     * 注意，该方法将会执行类似如下SQL ... on duplicate key update ...<br/>
     * 这种SQL回显的主键可能是不正确的，为了保险起见，请不要在业务中使用该方法所回填的主键
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
     */
    public Integer update(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待更新的对象");
        }
        this.data = Collections.singletonList(data);
        return mustShare().update();
    }

    /**
     * 当对象属性为null时，忽略更新
     *
     * @param data 待更新的对象
     */
    public Integer updateActive(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待更新的对象");
        }
        this.data = Collections.singletonList(data);
        return mustShare().updateActive();
    }

    public Integer updateByPrimaryKey(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待更新的主键值");
        }
        this.data = Collections.singletonList(data);
        return mustShare().updateByPrimaryKey();
    }

    public Integer updateActiveByPrimaryKey(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待更新的主键值");
        }
        this.data = Collections.singletonList(data);
        return mustShare().updateActiveByPrimaryKey();
    }

    public Integer delete() {
        return mustShare().delete();
    }

    public Integer deleteByPrimaryKey(Object key) {
        if (Objects.isNull(key)) {
            throw new BraveException("必须提供待删除的主键值");
        }
        return mustShare().deleteByPrimaryKey(key);
    }


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
        String whereSql = ParseSql.parse(currentClass, tableName, defalutDataSource, dynamicSql.getDeclarations(), orderByMap);
        //调正where子句的sql顺序 ，将来把它单独抽出来  作为组件
        whereSql = ParseSql.fixWhereSql(whereSql);
        SqlImpl<T> sqls = new SqlImpl<>();
        sqls.init(currentClass, pageInfo, data, dynamicSql.getUpdateNullProperties(), tableName, defalutDataSource, whereSql);
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
