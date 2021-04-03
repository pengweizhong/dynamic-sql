package com.pengwz.dynamic.model;

import java.lang.reflect.Field;

public class TableInfo {
    //数据库字段名
    private String column;
    //实体类属性名
    private String property;
    //是否为主键
    private boolean isPrimary;
    //新增数据是否需要生成主键
    private boolean isGeneratedValue;
    //实体类字段
    private Field field;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public boolean isGeneratedValue() {
        return isGeneratedValue;
    }

    public void setGeneratedValue(boolean generatedValue) {
        isGeneratedValue = generatedValue;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "column='" + column + '\'' +
                ", property='" + property + '\'' +
                ", isPrimary=" + isPrimary +
                ", isGeneratedValue=" + isGeneratedValue +
                ", field=" + field +
                '}';
    }
}
