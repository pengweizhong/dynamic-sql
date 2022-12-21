package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.constant.Constant;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.End;
import com.pengwz.dynamic.model.End.DefaultEnd;
import com.pengwz.dynamic.model.SelectParam;
import com.pengwz.dynamic.model.SelectParam.Function;
import com.pengwz.dynamic.model.TableColumnInfo;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.base.Fn;
import com.pengwz.dynamic.utils.ReflectUtils;
import com.pengwz.dynamic.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class Select<R> {

    private static final Log log = LogFactory.getLog(Select.class);

    //结果接收类
    private final Class<R> resultClass;
    //查询的SQL语句
    private final StringBuilder selectSql;
    //查询参数集合，在SQL执行时将会替代占位符，防止SQL注入,是查询的别名
    private final Map<String, List<Object>> paramsMap = new LinkedHashMap<>();
    //查询的列MAP  key是(结果成员属性的字段名)
    private final Map<String, SelectParam> selectParamMap = new LinkedHashMap<>();


    protected Select(Class<R> resultClass, StringBuilder selectSql) {
        this.resultClass = resultClass;
        this.selectSql = selectSql;
    }

    /**
     * 构建查询对象
     *
     * @param resultClass 查询结果接收类
     * @param <R>         任意自定义实体类
     * @return 可供构建的查询对象
     */
    public static <R> SelectBuilder<R> builder(Class<R> resultClass) {
        Select<R> select = new Select<>(resultClass, new StringBuilder());
        return new SelectBuilder<>(select);
    }


    /**
     * 从哪个主表开始查询
     *
     * @param tableClass 表实体类
     * @return 获得多表查询的支持对象
     */
    public MultiBraveSql.MultiBraveSqlBuilder<R> from(Class<?> tableClass) {
        final TableInfo tableInfo = ContextApplication.getTableInfo(tableClass);
        selectSql.append(Constant.SPACE).append(Constant.FROM).append(Constant.SPACE).append(tableInfo.getTableName());
        return MultiBraveSql.builder(tableClass, resultClass, selectSql);
    }

    /**
     * 追加SQL语句
     *
     * @param selectSql 查询sql
     */
    protected void appendSelectSql(String selectSql) {
        if (null != selectSql) {
            this.selectSql.append(selectSql);
        }
    }

    public Class<R> getResultClass() {
        return resultClass;
    }

    protected Map<String, SelectParam> getSelectParamMap() {
        return selectParamMap;
    }

    protected Map<String, List<Object>> getParamsMap() {
        return paramsMap;
    }

    public String getSelectSql() {
        return selectSql.toString();
    }

    @Override
    public String toString() {
        return getSelectSql();
    }

    public static class SelectBuilder<R> {

        private final Select<R> select;

        protected SelectBuilder(Select<R> select) {
            this.select = select;
        }

        /**
         * 指定需要查询的列
         * <p>
         * 比如：想要查询{@code Entity.class}类中的 Id,Name字段，查询结果对象为{@code Result.class}  <br>
         * 那么可以进行如下操作，即可构成查询的列：  <br>
         * <pre>
         *     {@code
         *          Select.builder(Result.class)
         *                 .column(Entity::getId).end()
         *                 .column(Entity::getName).left(1)... .end()
         *                 ... ...
         *          .build()
         *     }
         * </pre>
         * <p>
         * 如果从多个表（对应的实体类）查询了重复的字段，可以通过“alias”指定别名进行区分 <br>
         * 查询结果对象仍为{@code Result.class}，表数据来自{@code Entity.class}和{@code Entity2.class}
         * 那么还可以这么做： <br>
         * <pre>
         *     {@code
         *          Select.builder(Result.class)
         *                 .column(Entity::getId).alias(Result::getId).end()
         *                 .column(Entity2::getId).alias(Result::getEntity2Id).end()
         *                 .column(Entity::getName).left(1)... .end()
         *                 ... ...
         *          .build()
         *     }
         * </pre>
         * <p>
         * 注意，如果查询了相同的列，那么只有最后一次查询才会生效。
         * 此方法优先级与{@link this#column(String, Object...)} 等同，查询的结果以最后一次声明的为准。<br>
         * 当此方法与{@link this#allColumn(Class)}同时使用时，通常情况下，应当让{@code allColumn}在前，
         * {@code column}在后，以免自定义的查询列被覆盖。
         *
         * @param fn  列名
         * @param <E> 其他表实体类
         * @return 返回构建查询列的对象
         */
        public <E> QueryColumn<R, E> column(Fn<E, Object> fn) {
            return new QueryColumn<>(this, fn);
        }

        /**
         * 自定义查询列，键入的查询列在结束时必须指定别名，该别名要求必须真实存在于结果集中，并且与字段名保持一致，
         * 否则查询结果会无法映射到具体的字段上。<br>
         * 查询字段的格式为：表名+列名 + 具体语句
         *
         * <p>
         * 比如：表名为 t_abc   <br>
         * 那么可以进行如下操作：  <br>
         * <pre>
         *     //不需要预编译
         *     {@code
         *           Select.builder(Result.class).column("if(t_abc.value>2,'true','false') as value").build();
         *     }
         *     //或者需要预编译，此处2将被作为参数编译到SQL中
         *     {@code
         *           Select.builder(Result.class).column("if(t_abc.value>?,'true','false') as value",2).build();
         *     }
         * </pre>
         * <p>
         * 此方法优先级与{@link this#column(Fn)} 等同，查询的结果以最后一次声明的为准。
         *
         * @param expr   合法的任意表达式，不需要指定结束分割符，如 “,”，直接书写语句即可
         * @param params 预编译需要用到的参数，如果不需要参与预编译，此项为空即可
         * @return CustomQueryColumn
         */
        public CustomQueryColumn<R> column(String expr, Object... params) {
            return new CustomQueryColumn<>(this, expr, params);
        }

        /**
         * 查询指定实体类所有列，该实体类必须要求加注{@link Table}注解;
         * 具体查询的列数由实体类关联的属性数量决定
         * <p>
         * 此方法始终会查询所有列，当它和{@link this#column(Fn)}、{@link this#column(String, Object...)}一起使用时，
         * {@link this#column(Fn)}、{@link this#column(String, Object...)}方法返回的列优先级最高<br>
         *
         * @param tableClass 其他表实体类
         * @param <E>        任意表实体类
         * @return 返回构建查询列的对象
         * @see this#column(Fn)
         * @see this#column(String, Object...)
         * @see Table
         */
        public <E> End<SelectBuilder<R>> allColumn(Class<E> tableClass) {
            return new SelectBuilderEnd<>(this, tableClass);
        }


        /**
         * 将指定的列从本次查询中排除，这个方法在将来也许会非常有用；<br>
         * 它常常出现在共享的{@code SelectBuilder}中移除某些不需要查询的列
         *
         * @param fn 忽略的列
         * @return 返回构建查询列的对象
         */
        public <E> End<SelectBuilder<R>> ignoreColumn(Fn<E, Object> fn) {
            return ignoreColumn(Collections.singletonList(fn));
        }

        /**
         * 将指定的多个列从本次查询中排除，这个方法在将来也许会非常有用；<br>
         * 它常常出现在共享的{@code SelectBuilder}中移除某些不需要查询的列
         *
         * @param fns 忽略的列表
         * @return 返回构建查询列的对象
         */
        public <E> End<SelectBuilder<R>> ignoreColumn(List<Fn<E, Object>> fns) {
            if (CollectionUtils.isNotEmpty(fns)) {
                final Map<String, SelectParam> selectParamMap = this.getSelect().getSelectParamMap();
                for (Fn<E, Object> fn : fns) {
                    final String fieldName = ReflectUtils.fnToFieldName(fn);
                    selectParamMap.remove(fieldName);
                }
            }
            return new DefaultEnd<>(this);
        }


        /**
         * 结束查询列的构建并返回可供联表的{@code Select}对象
         *
         * @return Select
         */
        public Select<R> build() {
            SelectHelper.assembleQueryStatement(select);
            select.appendSelectSql(Constant.SPACE);
            return select;
        }

        private Select<R> getSelect() {
            return select;
        }

        private static class SelectBuilderEnd<R, E> extends End<SelectBuilder<R>> {

            private final Class<E> tableClass;

            protected SelectBuilderEnd(SelectBuilder<R> r, Class<E> tableClass) {
                super(r);
                super.register(this);
                this.tableClass = tableClass;
            }

            @Override
            protected End<SelectBuilder<R>> doEnd() {
                TableInfo tableInfo = ContextApplication.getTableInfo(tableClass);
                SelectBuilder<R> selectBuilder = this.get();
                Map<String, SelectParam> selectParamMap = selectBuilder.getSelect().getSelectParamMap();
                for (TableColumnInfo tableColumnInfo : tableInfo.getTableColumnInfos()) {
                    String fieldName = tableColumnInfo.getField().getName();
                    SelectParam selectParam = selectParamMap.get(fieldName);
                    int currentPriority = 0;
                    if (selectParam == null) {
                        selectParam = new SelectParam();
                        selectParam.setTableName(tableInfo.getTableName());
                        selectParam.setTableColumnInfo(tableColumnInfo);
                        selectParam.setPriority(currentPriority);
                        selectParam.setDataSourceName(tableInfo.getDataSourceName());
                        selectParamMap.put(fieldName, selectParam);
                    } else {
                        int priority = selectParam.getPriority();
                        //将数值大的覆盖
                        if (priority > currentPriority) {
                            selectParamMap.remove(fieldName);
                            SelectParam param = new SelectParam();
                            param.setTableName(tableInfo.getTableName());
                            param.setTableColumnInfo(tableColumnInfo);
                            param.setPriority(currentPriority);
                            param.setDataSourceName(tableInfo.getDataSourceName());
                            selectParamMap.put(fieldName, param);
                        }
                    }
                }
                return this;
            }
        }
    }

    public static class RemoveColumn<R> {
        private final SelectBuilder<R> selectBuilder;
        private final String fieldName;

        public RemoveColumn(SelectBuilder<R> selectBuilder, String fieldName) {
            this.selectBuilder = selectBuilder;
            this.fieldName = fieldName;
        }

        public SelectBuilder<R> end() {
            final Map<String, SelectParam> selectParamMap = selectBuilder.getSelect().getSelectParamMap();
            if (selectParamMap.get(fieldName) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("此列无需被移除，因为它本身就不存在：" + fieldName);
                }
                selectParamMap.remove(fieldName);
            }
            return selectBuilder;
        }
    }

    public static class CustomQueryColumn<R> extends End<SelectBuilder<R>> {
        //查询构建者
        private final SelectBuilder<R> selectBuilder;
        //用户键入的自定义查询语句
        private final String expr;
        //预编译使到的参数
        private final Object[] params;

        public CustomQueryColumn(SelectBuilder<R> selectBuilder, String expr, Object[] params) {
            super(selectBuilder);
            super.register(this);
            this.selectBuilder = selectBuilder;
            this.expr = expr;
            this.params = params;
        }

        @Override
        protected End<SelectBuilder<R>> doEnd() {
            if (StringUtils.isEmpty(expr)) {
                throw new BraveException("查询自定义列不可为空");
            }
            final String aliasName = SelectHelper.getColumnAliasName(expr);
            final Map<String, SelectParam> selectParamMap = selectBuilder.getSelect().getSelectParamMap();
            if (selectParamMap.get(aliasName) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("查询结果指向了重复的列，仅本次列查询生效，查询重复的字段名：" + aliasName);
                }
                selectParamMap.remove(aliasName);
            }
            final SelectParam selectParam = new SelectParam();
            selectParam.setCustomColumn(expr);
            selectParam.setPriority(Integer.MIN_VALUE);
            List<Function> functions = new ArrayList<>();
            functions.add(Function.builder().func(null).params(params).build());
            selectParam.setFunctions(functions);
            selectParamMap.put(aliasName, selectParam);
            return this;
        }
    }

    /**
     * 自定义查询列
     *
     * @param <R> 查询结果类型
     * @param <E> 参与查询的对象
     */
    public static class QueryColumn<R, E> extends End<SelectBuilder<R>> {
        //查询构建者
        private final SelectBuilder<R> selectBuilder;
        //当前查询对象的get方法
        private final Fn<E, Object> queryColumnFn;
        //当前查询对象的字段名
        private final String fieldName;
        //当前查询列的别名
        private String alias;
        //查询列需要使用的函数，若用户没指定，则此集合为空
        private final List<Function> functions = new ArrayList<>();

        protected QueryColumn(SelectBuilder<R> selectBuilder, Fn<E, Object> fn) {
            super(selectBuilder);
            super.register(this);
            this.selectBuilder = selectBuilder;
            this.queryColumnFn = fn;
            this.fieldName = ReflectUtils.fnToFieldName(fn);
        }

        /**
         * 将当前查询的字段映射到结果的字段，通常情况下，您不需要调用它，除非查询时出现了相同列。
         * <br>
         * 如有需要，通常在语句末尾调用
         *
         * @param fn 结果类：：字段名
         * @return 结束当前字段的构建
         */
        public End<SelectBuilder<R>> alias(Fn<R, Object> fn) {
            this.alias = ReflectUtils.fnToFieldName(fn);
            return this;
        }

        /**
         * 结束当前对象的构建，并将select查询对象返回
         *
         * @return End<SelectBuilder < R>> end
         */
        protected End<SelectBuilder<R>> doEnd() {
            final String tableClassname = ReflectUtils.getImplClassname(queryColumnFn);
            final Class<?> tableClass = ReflectUtils.forName(tableClassname);
            final TableInfo tableInfo = ContextApplication.getTableInfo(tableClass);
            final TableColumnInfo tableColumnInfo = tableInfo.getTableColumnInfoByFieldName(fieldName);
            final Map<String, SelectParam> selectParamMap = selectBuilder.getSelect().getSelectParamMap();
            //如果用户没有指定别名，那么别名应当和查询的列名本身保持一致
            alias = alias == null ? fieldName : alias;
            if (selectParamMap.get(alias) != null) {
                final SelectParam remove = selectParamMap.remove(alias);
                if (log.isDebugEnabled()) {
                    log.debug("查询结果指向了重复的列，仅本次列查询生效，" +
                            "查询重复的字段名：" + fieldName
                            + "，遗弃的参数：" + remove);
                }
            }
            final SelectParam selectParam = new SelectParam();
            selectParam.setTableName(tableInfo.getTableName());
            selectParam.setTableColumnInfo(tableColumnInfo);
            selectParam.setFunctions(functions);
            selectParam.setPriority(Integer.MIN_VALUE);
            selectParam.setDataSourceName(tableInfo.getDataSourceName());
            selectParamMap.put(alias, selectParam);
            for (Function function : functions) {
                final Object[] params = function.getParams();
                if (params != null) {
                    //TODO   汪Map中添加参数
                }
            }
            return this;
        }


        /**
         * 计算该字段的绝对值
         * <p>
         * example select abs(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> abs() {
            functions.add(Function.builder().func("abs").build());
            return this;
        }

        /**
         * 将字段转为小写
         * <p>
         * example select lower(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> lower() {
            functions.add(Function.builder().func("lower").build());
            return this;
        }

        /**
         * 将字段转为大写
         * <p>
         * example select upper(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> upper() {
            functions.add(Function.builder().func("upper").build());
            return this;
        }

        /**
         * 保留当前字段从左侧开始第一位到指定的{@code len} 位
         * <p>
         * example select left(column)
         *
         * @param len 保留从长度
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> left(int len) {
            functions.add(Function.builder().func("left").params(len).build());
            return this;
        }

        /**
         * 为当前字段左侧填充指定长度的字符串
         * <p>
         * example select lpad(column,len,filling)
         *
         * @param len     填充的长度
         * @param filling 填充的内容
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> lPad(int len, String filling) {
            functions.add(Function.builder().func("lpad").params(len, filling).build());
            return this;
        }

        /**
         * 为当前字段右侧填充指定长度的字符串
         * <p>
         * example select rpad(column,len,filling)
         *
         * @param len     填充的长度
         * @param filling 填充的内容
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> rPad(int len, String filling) {
            functions.add(Function.builder().func("rpad").params(len, filling).build());
            return this;
        }

        /**
         * 去除当前字段两侧的空格
         * <p>
         * example select trim(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> trim() {
            functions.add(Function.builder().func("trim").build());
            return this;
        }

        /**
         * 去除当前字段左侧的空格
         * <p>
         * example select ltrim(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> lTrim() {
            functions.add(Function.builder().func("ltrim").build());
            return this;
        }

        /**
         * 去除当前字段右侧的空格
         * <p>
         * example select rtrim(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> rTrim() {
            functions.add(Function.builder().func("rtrim").build());
            return this;
        }

        /**
         * 为当前字段重复生成新的字符串
         * <p>
         * example select repeat(column,num)
         *
         * @param num 重复生成的次数
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> repeat(int num) {
            functions.add(Function.builder().func("repeat").params(num).build());
            return this;
        }

        /**
         * 给当前字段替换字符串
         * <p>
         * example select replace(column,oldStr,newStr)
         *
         * @param oldStr 旧值
         * @param newStr 新值
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> replace(String oldStr, String newStr) {
            functions.add(Function.builder().func("replace").params(oldStr, newStr).build());
            return this;
        }

        /**
         * 将当前字段从{@code startIndex}开始保留到{@code endIndex}为止
         * <p>
         * example select substring(column,startIndex,endIndex)
         *
         * @param startIndex 开始下标
         * @param endIndex   结束下标
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> subString(int startIndex, int endIndex) {
            functions.add(Function.builder().func("substring").params(startIndex, endIndex).build());
            return this;
        }

        /**
         * 将当前字段倒序输出
         * <p>
         * example select reverse(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> reverse() {
            functions.add(Function.builder().func("reverse").build());
            return this;
        }

        /**
         * 计算当前字段正弦函数
         * <p>
         * example select sin(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> sin() {
            functions.add(Function.builder().func("sin").build());
            return this;
        }

        /**
         * 计算当前字段反正弦函数
         * <p>
         * example select asin(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> asin() {
            functions.add(Function.builder().func("asin").build());
            return this;
        }

        /**
         * 计算当前字段余弦函数
         * <p>
         * example select cos(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> cos() {
            functions.add(Function.builder().func("cos").build());
            return this;
        }

        /**
         * 计算当前字段反余弦函数
         * <p>
         * example select acos(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> acos() {
            functions.add(Function.builder().func("acos").build());
            return this;
        }

        /**
         * 返回当前时间字段对应的英文名称，如:`Sunday`
         * <p>
         * example select dayname(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> dayName() {
            functions.add(Function.builder().func("dayname").build());
            return this;
        }

        /**
         * 返回当前字段对应的一周的索引位置，如周日表示1，周一表示2，以此类推
         * <p>
         * example select dayofweek(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> dayOfWeek() {
            functions.add(Function.builder().func("dayofweek").build());
            return this;
        }

        /**
         * 返回对应的工作日索引，通常0表示周一，1表示周二...6表示周日
         * <p>
         * example select weekday(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> weekDay() {
            functions.add(Function.builder().func("weekday").build());
            return this;
        }

        /**
         * 计算日期是一年中的第几周
         * <p>
         * example select week(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> week() {
            functions.add(Function.builder().func("week").build());
            return this;
        }

        /**
         * 计算当前字段是一年中的第几周
         * <p>
         * example select weekofyear(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> weekOfYear() {
            functions.add(Function.builder().func("weekofyear").build());
            return this;
        }

        /**
         * 返回一年中的第几天，范围从1到366
         * <p>
         * example select dayofyear(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> dayOfYear() {
            functions.add(Function.builder().func("dayofyear").build());
            return this;
        }

        /**
         * 返回一个月的第几天，范围从1到31
         * <p>
         * example select dayofmonth(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> dayOfMonth() {
            functions.add(Function.builder().func("dayofmonth").build());
            return this;
        }

        /**
         * 获取当前字段中的年
         * <p>
         * example select year(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> year() {
            functions.add(Function.builder().func("year").build());
            return this;
        }

        /**
         * 获取当前字段中的季度，返回1到4
         * <p>
         * example select quarter(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> quarter() {
            functions.add(Function.builder().func("quarter").build());
            return this;
        }

        /**
         * 获取当前字段中的分钟数
         * <p>
         * example select minute(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> minute() {
            functions.add(Function.builder().func("minute").build());
            return this;
        }

        /**
         * 获取当前字段中的秒数
         * <p>
         * example select second(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> second() {
            functions.add(Function.builder().func("second").build());
            return this;
        }

        /**
         * 返回当前列的最大值
         * <p>
         * example select max(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> max() {
            functions.add(Function.builder().func("max").build());
            return this;
        }

        /**
         * 返回当前列的最小值
         * <p>
         * example select min(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> min() {
            functions.add(Function.builder().func("min").build());
            return this;
        }

        /**
         * 返回当前列的总数
         * <p>
         * example select count(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> count() {
            functions.add(Function.builder().func("count").build());
            return this;
        }

        /**
         * 返回当前列的和
         * <p>
         * example select sum(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> sum() {
            functions.add(Function.builder().func("sum").build());
            return this;
        }

        /**
         * 返回当前列的平均数
         * <p>
         * example select avg(column)
         *
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> avg() {
            functions.add(Function.builder().func("avg").build());
            return this;
        }

        /**
         * 如果当前字段为null，则返回{@code other}
         * <p>
         * example select ifnull(column, other)
         *
         * @param other 其他值
         * @return 当前自定义字段对象
         */
        public QueryColumn<R, E> ifNull(Object other) {
            functions.add(Function.builder().func("ifnull").params(other).build());
            return this;
        }

        protected SelectBuilder<R> getSelectBuilder() {
            return selectBuilder;
        }

        protected String getAlias() {
            return alias;
        }
    }

}
