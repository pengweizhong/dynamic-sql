package com.pengwz.dynamic.anno;

public enum JsonMode {
    /**
     * 写入模式
     * json对象为空的属性，会写入到数据库中
     */
    SERIALIZE_WRITE_NULLS,
    /**
     * 写入模式（默认值）
     * json对象为空的属性，不会写入到数据库中
     */
    SERIALIZE_WRITE_NO_NULLS,
    ;
}
