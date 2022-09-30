package com.pengwz.dynamic.model;

import com.pengwz.dynamic.anno.JsonMode;

public class ColumnInfo {
    /**
     * 列名，未指定时默认字段按照驼峰规则拼接下划线
     */
    private String value;

//    /**
//     * 此字段从属表，它将还在多表join时使用
//     */
//    private TableInfo dependentTableInfo;

    /**
     * JSON转换时所使用的序列化模式，默认值不会序列化null值 ，为null表名该字段不需要转JSON
     */
    private JsonMode jsonMode;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public JsonMode getJsonMode() {
        return jsonMode;
    }

    public void setJsonMode(JsonMode jsonMode) {
        this.jsonMode = jsonMode;
    }

//    public TableInfo getDependentTableInfo() {
//        return dependentTableInfo;
//    }
//
//    public void setDependentTableInfo(TableInfo dependentTableInfo) {
//        this.dependentTableInfo = dependentTableInfo;
//    }
}
