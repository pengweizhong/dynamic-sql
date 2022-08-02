package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.model.SelectParam;
import com.pengwz.dynamic.sql.base.Fn;
import com.pengwz.dynamic.utils.ReflectUtils;
import com.pengwz.dynamic.utils.SelectHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Select<R> {

    private final Class<R> resultClass;

    private String selectSql;

    private boolean isSelectAll;

    private List<Object> params = new ArrayList<>();

    private final Map<String, SelectParam> selectParamMap = new HashMap<>();

    protected Select(Class<R> currentClass) {
        this.resultClass = currentClass;
    }

    /**
     * 构建Select查询对象
     *
     * @param resultClass 当前查询的结果集
     * @param <R>         任何实体类
     * @return select构建者
     */
    public static <R> SelectBuilder<R> builder(Class<R> resultClass) {
        Select<R> select = new Select<>(resultClass);
        return new SelectBuilder<>(select);
    }

    /**
     * 从哪个主表开始查询
     *
     * @param tableClass 表实体类
     * @return 获得多表查询的支持对象
     */
    public MultiBraveSql.As<R> from(Class<?> tableClass) {
        return new MultiBraveSql.As<>(tableClass, resultClass);
    }

    public Class<R> getResultClass() {
        return resultClass;
    }

    public String getSelectSql() {
        return selectSql;
    }

    public Map<String, SelectParam> getSelectParamMap() {
        return selectParamMap;
    }

    public void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    }

    public void setSelectAll(boolean selectAll) {
        isSelectAll = selectAll;
    }

    public List<Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return selectSql;
    }

    public static class SelectBuilder<R> {

        private Select<R> select;

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
            return new CustomColumn<>(this, ReflectUtils.fnToFieldName(fn));
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
        public SelectBuilder<R> columnAll() {
            getSelect().isSelectAll = true;
            return this;
        }

        /**
         * 自定义查询列，此项函数必须提供as别名
         * <p>
         * 比如：列名为abc   <br>
         * 那么可以进行如下操作：  <br>
         * <pre>
         *     {@code
         *           Select.builder(DTO.class).customColumn("if(abc>2,'true','false') as abc").build();
         *     }
         * </pre>
         * 当此处查询的列与{@link this#columnAll()}冲突时，此处优先级最高
         *
         * @param expr   自定义方法
         * @param params 预编译需要用到的参数，如果不需要参与预编译，此项为空即可
         * @return 当前自定义字段对象
         */
        public SelectBuilder<R> customColumn(String expr, Object... params) {
            final Map<String, SelectParam> selectParamMap = getSelect().getSelectParamMap();
            SelectHelper.putSelectParam(selectParamMap, expr, null, params);
            return this;
        }

        public Select<R> build() {
            SelectHelper.assembleQueryStatement(select);
            return select;
        }

        public Select<R> getSelect() {
            return select;
        }

//        private String parseExprCaseColumn(String expr) {
//            return "abc";
//        }
    }

    /**
     * 自定义查询列规则
     *
     * @param <R>
     */
    public static class CustomColumn<R> {

        private SelectBuilder<R> selectBuilder;

        private String fieldName;

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
         *
         * @param len     填充的长度
         * @param filling 填充的内容
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> lPad(int len, String filling) {
            return this;
        }

        /**
         * 为当前字段右侧填充指定长度的字符串
         *
         * @param len     填充的长度
         * @param filling 填充的内容
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> rPad(int len, String filling) {
            return this;
        }

        /**
         * 去除当前字段两侧的空格
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
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> lTrim() {
            return this;
        }

        /**
         * 去除当前字段右侧的空格
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> rTrim() {
            return this;
        }

        /**
         * 为当前字段重复生成新的字符串
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
         *
         * @param oldStr 旧值
         * @param newStr 新值
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> replace(String oldStr, String newStr) {
            return this;
        }

        /**
         * 将当前字段从{@code startIndex}开始保留到{@code endIndex}为止
         *
         * @param startIndex 开始下标
         * @param endIndex   结束下标
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> subString(int startIndex, int endIndex) {
            return this;
        }

        /**
         * 将当前字段倒序输出
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> reverse() {
            return this;
        }

        /**
         * 计算当前字段正弦函数
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> sin() {
            return this;
        }

        /**
         * 计算当前字段反正弦函数
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> asin() {
            return this;
        }

        /**
         * 计算当前字段余弦函数
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> cos() {
            return this;
        }

        /**
         * 计算当前字段反余弦函数
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> acos() {
            return this;
        }

        /**
         * 返回当前时间字段对应的英文名称，如:`Sunday`
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> dayName() {
            return this;
        }

        /**
         * 返回当前字段对应的一周的索引位置，如周日表示1，周一表示2，以此类推
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> dayOfWeek() {
            return this;
        }

        /**
         * 返回对应的工作日索引，0表示周一，1表示周二...6表示周日
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> weekDay() {
            return this;
        }

        /**
         * 计算日期是一年中的第几周
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> week() {
            return this;
        }

        /**
         * 计算当前字段是一年中的第几周
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> weekOfYear() {
            return this;
        }

        /**
         * 返回一年中的第几天，范围从1到366
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> dayOfYear() {
            return this;
        }

        /**
         * 返回一个月的第几天，范围从1到31
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> dayOfMonth() {
            return this;
        }

        /**
         * 获取当前字段中的年
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> year() {
            return this;
        }

        /**
         * 获取当前字段中的季度，返回1到4
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> quarter() {
            return this;
        }

        /**
         * 获取当前字段中的分钟数
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> minute() {
            return this;
        }

        /**
         * 获取当前字段中的秒数
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> second() {
            return this;
        }

        /**
         * 返回当前列的最大值
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> max() {
            return this;
        }

        /**
         * 返回当前列的最小值
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> min() {
            return this;
        }

        /**
         * 返回当前列的总数
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> count() {

            return this;
        }

        /**
         * 返回当前列的和
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> sum() {
            return this;
        }

        /**
         * 返回当前列的平均数
         *
         * @return 当前自定义字段对象
         */
        public CustomColumn<R> avg() {
            return this;
        }

        public SelectBuilder<R> getSelectBuilder() {
            return selectBuilder;
        }
    }
}
