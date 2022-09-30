package com.pengwz.dynamic.model;

import com.pengwz.dynamic.utils.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectParam {
    private String tableName;
    private TableColumnInfo tableColumnInfo;
    /**
     * 是否为自定义列
     */
    private boolean isCustomColumn;
    /**
     * 该字段对应的列表函数
     */
    private List<Function> functions;
    /**
     * 当前查询列的优先级，同样重复查询的情况下，优先级低的将被覆盖；
     * 按照惯例，数值越低优先级越高。取值参考范围在{@code Integer.MIN_VALUE} - {@code Integer.MAX_VALUE}
     */
    private int priority;

    public static class Function {
        /**
         * 函数名称
         */
        private String func;
        /**
         * 入参值
         */
        private Object[] params;

        public String getFunc() {
            return func;
        }

        public Object[] getParams() {
            return params;
        }

        public static FunctionBuilder builder() {
            return new FunctionBuilder(new Function());
        }

        public static class FunctionBuilder {

            private final Function function;

            private FunctionBuilder(Function function) {
                this.function = function;
            }

            public FunctionBuilder func(String func) {
                function.func = func;
                return this;
            }

            public FunctionBuilder param(Object... params) {
                function.params = params;
                return this;
            }

            public Function build() {
                return function;
            }
        }

        @Override
        public String toString() {
            return "{" +
                    "func='" + func + '\'' +
                    ", params=" + Arrays.toString(params) +
                    '}';
        }
    }


    public List<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }

    public boolean isCustomColumn() {
        return isCustomColumn;
    }

    public void setCustomColumn(boolean customColumn) {
        isCustomColumn = customColumn;
    }

    public TableColumnInfo getTableColumnInfo() {
        return tableColumnInfo;
    }

    public void setTableColumnInfo(TableColumnInfo tableColumnInfo) {
        this.tableColumnInfo = tableColumnInfo;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        final ArrayList<String> strings = new ArrayList<>();
        if (StringUtils.isNotBlank(tableName)) {
            strings.add("tableName=" + tableName);
        }
        if (tableColumnInfo != null) {
            strings.add("columnName=" + tableColumnInfo.getColumn());
        }
        if (CollectionUtils.isNotEmpty(functions)) {
            strings.add("usedFunctions=" + StringUtils.join(functions, ","));
        }
        if (!strings.isEmpty()) {
            stringBuilder.append(StringUtils.join(strings, ","));
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
