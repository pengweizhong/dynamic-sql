package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.sql.base.Fn;

public class Select<R> {

    private Class<R> resultClass;

    private String selectSql;

    protected Select(Class<R> currentClass) {
        this.resultClass = currentClass;
    }

    public static <R> SelectBuilder<R> builder(Class<R> currentClass) {
        Select<R> select = new Select<>(currentClass);
        return new SelectBuilder<>(select);
    }

    public MultiBraveSql.MultiBraveSqlBuilder<R> from(Class<?> currentClass) {
        return MultiBraveSql.builder(currentClass, resultClass);
    }

    @Override
    public String toString() {
        return "Select{" +
                "resultClass=" + resultClass +
                ", selectSql=" + selectSql +
                '}';
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
         */
        public CustomColumn<R> column(Fn<R, Object> fn) {
            CustomColumn<R> customColumn = new CustomColumn<>(this);
            return customColumn;
        }


        public SelectBuilder<R> columnAll() {
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
         *
         * @param expr 自定义方法
         * @return 当前自定义字段对象
         */
        public SelectBuilder<R> customColumn(String expr) {
            return this;
        }

        public Select<R> build() {
            return select;
        }

    }

    /**
     * 自定义查询列规则
     *
     * @param <R>
     */
    public static class CustomColumn<R> {
        private SelectBuilder<R> selectBuilder;

        protected CustomColumn(SelectBuilder<R> selectBuilder) {
            this.selectBuilder = selectBuilder;
        }

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

    }
}
