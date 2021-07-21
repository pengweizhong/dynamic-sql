package com.pengwz.dynamic.anno;

public enum GenerationType {
    /**
     * 使用UUID进行自增
     */
    UUID,
    /**
     * 使用简洁的UUID进行自增，该UUID是不包含 ‘-’的
     */
    SIMPLE_UUID,
    /**
     * 使用数据库自增策略进行自增
     */
    AUTO;
}
