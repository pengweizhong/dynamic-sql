package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.constant.Constant;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.End;
import com.pengwz.dynamic.model.SelectParam;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.base.Fn;
import com.pengwz.dynamic.utils.ReflectUtils;
import com.pengwz.dynamic.utils.SelectHelper;
import com.pengwz.dynamic.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Select<R> {
    private static final Log log = LogFactory.getLog(Select.class);

    private final Class<R> resultClass;

    private final StringBuilder selectSql;

    private boolean isSelectAll;

    private final List<Object> params = new ArrayList<>();

    //查询的列MAP  key通常这里是字段名，如果是用户自定义的话，那么这里将是表列名
    private final Map<String, SelectParam> selectParamMap = new LinkedHashMap<>();


    /**
     * 构建Select查询对象
     *
     * @param resultClass 当前查询的结果集
     * @param <R>         允许任何实体类
     * @return select构建者
     */
    public static <R> SelectBuilder<R> builder(Class<R> resultClass) {
        Select<R> select = new Select<>(resultClass, new StringBuilder());
        return new SelectBuilder<>(select);
    }

    protected Select(Class<R> currentClass, StringBuilder selectSql) {
        this.resultClass = currentClass;
        this.selectSql = selectSql;
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
    public void appendSelectSql(String selectSql) {
        if (null != selectSql) {
            this.selectSql.append(selectSql);
        }
    }

    public Class<R> getResultClass() {
        return resultClass;
    }

    public Map<String, SelectParam> getSelectParamMap() {
        return selectParamMap;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    }

    public List<Object> getParams() {
        return params;
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
         *
         * @param fn 列名
         * @return 返回构建查询列的对象
         * @see this#columnAll()
         */
        public CustomColumn<R> column(Fn<R, Object> fn) {
            final String fieldName = ReflectUtils.fnToFieldName(fn);
            final Map<String, SelectParam> selectParamMap = getSelect().getSelectParamMap();
            if (selectParamMap.get(fieldName) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("查询了重复的列，仅本次列查询生效，重复的字段名：" + fieldName);
                }
                selectParamMap.remove(fieldName);
            }
            final SelectParam selectParam = new SelectParam();
            selectParam.setCustomColumn(false);
            selectParam.setFieldName(fieldName);
            SelectHelper.putSelectParam(selectParamMap, fieldName, selectParam);
            return new CustomColumn<>(this, fieldName);
        }

        /**
         * 查询所有列
         * <p>
         * 此方法始终会查询所有列，当它和{@link this#column(Fn)}一起使用时，{@link this#column(Fn)}方法返回的列优先级最高<br>
         * {@link this#column(Fn)}将会覆盖相同属性的{@link this#column(Fn)}字段。
         *
         * @return 返回构建查询列的对象
         * @see this#column(Fn)
         */
        public End<SelectBuilder<R>> columnAll() {
            getSelect().isSelectAll = true;
            return new End<>(this);
        }

        /**
         * 自定义查询列，此项函数强烈建议提供as别名，该别名要求必须真实存在于结果集中；以减少不必要的麻烦。
         * <p>
         * 比如：表名为 t_abc   <br>
         * 那么可以进行如下操作：  <br>
         * <pre>
         *     {@code
         *           Select.builder(DTO.class).customColumn("if(t_abc.value>2,'true','false') as value").build();
         *     }
         * </pre>
         * 当此处查询的列与{@link this#columnAll()}冲突时，此处优先级最高
         *
         * @param expr   合法的任意表达式
         * @param params 预编译需要用到的参数，如果不需要参与预编译，此项为空即可
         * @return 结束断句
         */
        public End<SelectBuilder<R>> customColumn(String expr, Object... params) {
            if (StringUtils.isEmpty(expr)) {
                throw new BraveException("查询自定义列不可为空");
            }
            expr = expr.trim();
            final String column = expr.contains(" ") ? SelectHelper.getColumn(expr) : expr;
            final Map<String, SelectParam> selectParamMap = getSelect().getSelectParamMap();
            final List<SelectParam> selectParams = selectParamMap.values().stream().filter(SelectParam::isCustomColumn).collect(Collectors.toList());
            selectParams.forEach(selectParam -> {
                final String columnHistory = selectParam.getFieldName().contains(" ") ? SelectHelper.getColumn(selectParam.getFieldName()) : selectParam.getFieldName();
                if (columnHistory.equalsIgnoreCase(column)) {
                    if (log.isDebugEnabled()) {
                        log.debug("自定义查询重复，仅本次列查询生效，重复的查询列名：" + column);
                    }
                    selectParamMap.remove(selectParam.getFieldName());
                }
            });
            final SelectParam selectParam = new SelectParam();
            selectParam.setCustomColumn(true);
            selectParam.setFieldName(expr);
            SelectHelper.putSelectParam(selectParamMap, expr, selectParam);
            SelectHelper.putSelectParam(selectParamMap, expr, null, params);
            return new End<>(this);
        }

        public Select<R> build() {
            SelectHelper.assembleQueryStatement(select);
            select.appendSelectSql(Constant.SPACE);
            return select;
        }

        public Select<R> getSelect() {
            return select;
        }

    }

    /**
     * 自定义查询列规则
     *
     * @param <R>
     */
    public static class CustomColumn<R> {

        private final SelectBuilder<R> selectBuilder;

        private final String fieldName;

        protected CustomColumn(SelectBuilder<R> selectBuilder, String fieldName) {
            this.selectBuilder = selectBuilder;
            this.fieldName = fieldName;
        }

        /**
         * 结束当前对象的构建，并将select查询对象返回
         *
         * @return SelectBuilder
         */
        public SelectBuilder<R> end() {
            return selectBuilder;
        }

        /**
         * 计算该字段的绝对值
         * <p>
         * example select abs(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> abs() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "abs");
            return this;
        }

        /**
         * 将字段转为小写
         * <p>
         * example select lower(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> lower() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "lower");
            return this;
        }

        /**
         * 将字段转为大写
         * <p>
         * example select upper(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> upper() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "upper");
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
        public CustomColumn<R> left(int len) {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "left", len);
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
        public CustomColumn<R> lPad(int len, String filling) {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "lpad", len, filling);
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
        public CustomColumn<R> rPad(int len, String filling) {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "rpad", len, filling);
            return this;
        }

        /**
         * 去除当前字段两侧的空格
         * <p>
         * example select trim(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> trim() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "trim");
            return this;
        }

        /**
         * 去除当前字段左侧的空格
         * <p>
         * example select ltrim(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> lTrim() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "ltrim");
            return this;
        }

        /**
         * 去除当前字段右侧的空格
         * <p>
         * example select rtrim(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> rTrim() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "rtrim");
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
        public CustomColumn<R> repeat(int num) {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "repeat", num);
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
        public CustomColumn<R> replace(String oldStr, String newStr) {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "replace", oldStr, newStr);
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
        public CustomColumn<R> subString(int startIndex, int endIndex) {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "substring", startIndex, endIndex);
            return this;
        }

        /**
         * 将当前字段倒序输出
         * <p>
         * example select reverse(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> reverse() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "reverse");
            return this;
        }

        /**
         * 计算当前字段正弦函数
         * <p>
         * example select sin(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> sin() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "sin");
            return this;
        }

        /**
         * 计算当前字段反正弦函数
         * <p>
         * example select asin(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> asin() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "asin");
            return this;
        }

        /**
         * 计算当前字段余弦函数
         * <p>
         * example select cos(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> cos() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "cos");
            return this;
        }

        /**
         * 计算当前字段反余弦函数
         * <p>
         * example select acos(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> acos() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "acos");
            return this;
        }

        /**
         * 返回当前时间字段对应的英文名称，如:`Sunday`
         * <p>
         * example select dayname(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> dayName() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "dayname");
            return this;
        }

        /**
         * 返回当前字段对应的一周的索引位置，如周日表示1，周一表示2，以此类推
         * <p>
         * example select dayofweek(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> dayOfWeek() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "dayofweek");
            return this;
        }

        /**
         * 返回对应的工作日索引，0表示周一，1表示周二...6表示周日
         * <p>
         * example select weekday(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> weekDay() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "weekday");
            return this;
        }

        /**
         * 计算日期是一年中的第几周
         * <p>
         * example select week(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> week() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "week");
            return this;
        }

        /**
         * 计算当前字段是一年中的第几周
         * <p>
         * example select weekofyear(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> weekOfYear() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "weekofyear");
            return this;
        }

        /**
         * 返回一年中的第几天，范围从1到366
         * <p>
         * example select dayofyear(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> dayOfYear() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "dayofyear");
            return this;
        }

        /**
         * 返回一个月的第几天，范围从1到31
         * <p>
         * example select dayofmonth(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> dayOfMonth() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "dayofmonth");
            return this;
        }

        /**
         * 获取当前字段中的年
         * <p>
         * example select year(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> year() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "year");
            return this;
        }

        /**
         * 获取当前字段中的季度，返回1到4
         * <p>
         * example select quarter(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> quarter() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "quarter");
            return this;
        }

        /**
         * 获取当前字段中的分钟数
         * <p>
         * example select minute(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> minute() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "minute");
            return this;
        }

        /**
         * 获取当前字段中的秒数
         * <p>
         * example select second(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> second() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "second");
            return this;
        }

        /**
         * 返回当前列的最大值
         * <p>
         * example select max(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> max() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "max");
            return this;
        }

        /**
         * 返回当前列的最小值
         * <p>
         * example select min(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> min() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "min");
            return this;
        }

        /**
         * 返回当前列的总数
         * <p>
         * example select count(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> count() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "count");
            return this;
        }

        /**
         * 返回当前列的和
         * <p>
         * example select sum(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> sum() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "sum");
            return this;
        }

        /**
         * 返回当前列的平均数
         * <p>
         * example select avg(column)
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> avg() {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "avg");
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
        public CustomColumn<R> ifNull(Object other) {
            final Map<String, SelectParam> selectParamMap = getSelectBuilder().getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, fieldName, "ifnull", other);
            return this;
        }

        public SelectBuilder<R> getSelectBuilder() {
            return selectBuilder;
        }

    }

}
