package com.pengwz.dynamic.model;

import java.util.Arrays;
import java.util.List;

public class SelectParam {
    /**
     * 字段名
     */
    private String fieldName;
    /**
     * 该字段对应的列表函数
     */
    private List<Function> functions;

    public static class Function {
        /**
         * 函数名称
         */
        private String func;
        /**
         * 入参值
         */
        private Object[] param;
//        /**
//         * 调用的先后顺序
//         */
//        private int sort;

        public String getFunc() {
            return func;
        }

        public Object[] getParam() {
            return param;
        }
//
//        public int getSort() {
//            return sort;
//        }

        @Override
        public String toString() {
            return "Function{" +
                    "func='" + func + '\'' +
                    ", param=" + Arrays.toString(param) +
                    '}';
        }
    }


    public String getFieldName() {
        return fieldName;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setFunctions(List<Function> functions) {
        this.functions = functions;
    }

    @Override
    public String toString() {
        return "SelectParam{" +
                "fieldName='" + fieldName + '\'' +
                ", functions=" + functions +
                '}';
    }

    public static FunctionBuilder functionBuilder() {
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

        public FunctionBuilder param(Object[] param) {
            function.param = param;
            return this;
        }

        public Function build() {
            return function;
        }
    }
}
