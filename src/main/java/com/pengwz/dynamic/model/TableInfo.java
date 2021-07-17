package com.pengwz.dynamic.model;

import com.pengwz.dynamic.anno.GenerationType;

import java.lang.reflect.Field;

public class TableInfo {
    //数据库字段名
    private String column;
    //实体类属性名
    private String property;
    //是否为主键
    private boolean isPrimary;
    //主键生成策略
    private GenerationType generationType;
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

    public GenerationType getGenerationType() {
        return generationType;
    }

    public void setGenerationType(GenerationType generationType) {
        this.generationType = generationType;
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
                ", generationType=" + generationType +
                ", field=" + field +
                '}';
    }
}
