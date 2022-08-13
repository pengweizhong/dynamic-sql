package com.pengwz.dynamic.model;

import com.pengwz.dynamic.anno.JsonMode;

public class ColumnInfo {
    /**
     * 列名，未指定时默认字段按照驼峰规则拼接下划线
     */
    private String value;

    /**
     * 所属表别名，它将还在多表join时使用
     */
    private String tableAlias;
//    /**
//     * 所属表实体类，它将还在多表join时使用
//     */
//    private Class<?> tableClass;

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

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

//    public Class<?> getTableClass() {
//        return tableClass;
//    }
//
//    public void setTableClass(Class<?> tableClass) {
//        this.tableClass = tableClass;
//    }

    public JsonMode getJsonMode() {
        return jsonMode;
    }

    public void setJsonMode(JsonMode jsonMode) {
        this.jsonMode = jsonMode;
    }
}
