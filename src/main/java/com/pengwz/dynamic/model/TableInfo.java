package com.pengwz.dynamic.model;

import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.JsonMode;

import java.lang.reflect.Field;

public class TableInfo {
    //数据库字段名
    private String column;
    //是否为主键
    private boolean isPrimary;
    //主键生成策略
    private GeneratedValue generatedValue;
    //实体类字段
    private Field field;
    //json 序列化模式，若该值不为null，说明此字段是一个json对象
    private JsonMode jsonMode;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public GeneratedValue getGeneratedValue() {
        return generatedValue;
    }

    public void setGeneratedValue(GeneratedValue generatedValue) {
        this.generatedValue = generatedValue;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public JsonMode getJsonMode() {
        return jsonMode;
    }

    public void setJsonMode(JsonMode jsonMode) {
        this.jsonMode = jsonMode;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "column='" + column + '\'' +
                ", isPrimary=" + isPrimary +
                ", generatedValue=" + generatedValue +
                ", field=" + field +
                ", jsonMode=" + jsonMode +
                '}';
    }
}
