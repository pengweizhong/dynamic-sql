package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.sql.base.HandleFunction;

public class Declaration {

    //连接表达式，and 还是 or
    private String andOr;
    //字段属性
    private String property;
    //表达式
    private String condition;
    //对应值,一般选此项
    private Object value;
    //对应值2，比如 between .. and ...
    private Object value2;
    //函数
    private HandleFunction handleFunction;
    //括号
    @Deprecated
    private String brackets;

    private String sortMode;
    //是否为复合查询
    private boolean isComplex;

    public Declaration(String andOr, String property, String condition, Object value, Object value2, HandleFunction handleFunction, String brackets, String sortMode) {
        this.andOr = andOr;
        this.property = property;
        this.condition = condition;
        this.value = value;
        this.value2 = value2;
        this.handleFunction = handleFunction;
        this.brackets = brackets;
        this.sortMode = sortMode;
    }

    public Declaration(boolean isComplex) {
        this.isComplex = isComplex;
    }

    public static Declaration buildDeclaration(String andOr, String property, String condition, Object value, Object value2) {
        return new Declaration(andOr, property, condition, value, value2, null, null, null);
    }

    public static Declaration buildDeclaration(String andOr, String property, String condition, Object value) {
        return new Declaration(andOr, property, condition, value, null, null, null, null);
    }

    public static Declaration buildDeclaration(String andOr, String property, String condition) {
        return new Declaration(andOr, property, condition, null, null, null, null, null);
    }

    public static Declaration buildDeclaration(String property, String condition) {
        return new Declaration(null, property, condition, null, null, null, null, null);
    }

    public static Declaration buildDeclaration(String property, HandleFunction handleFunction) {
        return new Declaration(null, property, null, null, null, handleFunction, null, null);
    }

    public static Declaration buildDeclaration(String andOr, String property, HandleFunction handleFunction) {
        return new Declaration(andOr, property, null, null, null, handleFunction, null, null);
    }

    public static Declaration buildDeclaration(String brackets) {
        return new Declaration(null, null, null, null, null, null, brackets, null);
    }

    public static Declaration buildComplex(boolean isComplex) {
        return new Declaration(isComplex);
    }

    public String getProperty() {
        return property;
    }

    public Object getValue() {
        return value;
    }

    public String getCondition() {
        return condition;
    }

    public String getAndOr() {
        return andOr;
    }

    public Object getValue2() {
        return value2;
    }

    public HandleFunction getHandleFunction() {
        return handleFunction;
    }

    public String getBrackets() {
        return brackets;
    }

    public void setAndOr(String andOr) {
        this.andOr = andOr;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setValue2(Object value2) {
        this.value2 = value2;
    }

    public void setHandleFunction(HandleFunction handleFunction) {
        this.handleFunction = handleFunction;
    }

    public void setBrackets(String brackets) {
        this.brackets = brackets;
    }

    public String getSortMode() {
        return sortMode;
    }

    public void setSortMode(String sortMode) {
        this.sortMode = sortMode;
    }


    public boolean isComplex() {
        return isComplex;
    }

    public void setComplex(boolean complex) {
        isComplex = complex;
    }

    @Override
    public String toString() {
        return "Declaration{" +
                "andOr='" + andOr + '\'' +
                ", property='" + property + '\'' +
                ", condition='" + condition + '\'' +
                ", value=" + value +
                ", value2=" + value2 +
                ", handleFunction=" + handleFunction +
                ", brackets='" + brackets + '\'' +
                ", sortMode='" + sortMode + '\'' +
                ", isComplex=" + isComplex +
                '}';
    }
}
