package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.exception.BraveException;
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

    private Sqls<T> mustShare() {
        // 解析where语句
        Table table = currentClass.getAnnotation(Table.class);
        if (Objects.isNull(table) || StringUtils.isEmpty(table.value().trim())) {
            throw new BraveException("当前实体类：" + currentClass + "未获取到表名");
        }
        String tableName = table.value().trim();
        DataSourceConfig dataSource;
        Class<?> dataSourceClass = table.dataSourceClass();
        try {
            if (dataSourceClass.equals(Void.class)) {
                throw new BraveException("必须明确指定数据源");
            } else {
                dataSource = (DataSourceConfig) dataSourceClass.newInstance();
            }

        } catch (ClassCastException e) {
            throw new BraveException(dataSourceClass.toString() + " 必须实现或继承 " + DataSourceConfig.class + " 类");
        } catch (Exception e) {
            throw new BraveException("初始化数据库连接失败，原因：" + e.getMessage());
        }
        Check.checkPageInfo(pageInfo);
        String whereSql = ParseSql.parse(currentClass, tableName, table.dataSourceClass(), dynamicSql.getDeclarations(), orderByMap);
        //调正where子句的sql顺序 ，将来把它单独抽出来  作为组件
        whereSql = ParseSql.fixWhereSql(whereSql);
        SqlImpl<T> sqls = new SqlImpl<>();
        sqls.init(currentClass, pageInfo, data, dynamicSql.getUpdateNullProperties(), tableName, table.dataSourceClass(), whereSql, dataSource);
        //优化他
        sqls.before();
        return sqls;
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

    public Integer insert(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待插入的数据");
        }
        this.data = Collections.singletonList(data);
        return batchInsert(this.data);
    }

    public Integer batchInsert(Iterable<T> iterable) {
        if (Objects.isNull(iterable) || !iterable.iterator().hasNext()) {
            throw new BraveException("必须提供待插入的数据");
        }
        data = iterable;
        return mustShare().batchInsert();
    }

    /**
     * 根据唯一约束，判断表中记录是否存在。
     * 若表中记录存在，进行更新操作，否则插入
     *
     * @param data 待插入的数据
     * @return 操作成功的数量
     */
    public Integer insertOrUpdate(T data) {
        if (Objects.isNull(data)) {
            throw new BraveException("必须提供待插入的数据");
        }
        this.data = Collections.singletonList(data);
        return batchInsertOrUpdate(this.data);
    }

    /**
     * 根据唯一约束，判断表中记录是否存在。
     * 若表中记录存在，批量进行更新操作，否则插入
     *
     * @param iterable 待插入的数据集合
     * @return 操作成功的数量
     */
    public Integer batchInsertOrUpdate(Iterable<T> iterable) {
        if (Objects.isNull(iterable) || !iterable.iterator().hasNext()) {
            throw new BraveException("必须提供待插入的数据");
        }
        data = iterable;
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
            throw new BraveException("必须提供待更新的数据");
        }
        this.data = Collections.singletonList(data);
        return mustShare().updateByPrimaryKey();
    }

    public Integer delete() {
        return mustShare().delete();
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
