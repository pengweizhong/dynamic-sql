package com.pengwz.dynamic.anno;

public enum GenerationType {
    /**
     * 使用UUID进行自增
     */
    UUID,
    /**
     * 使用大写的UUID进行自增
     */
    UPPER_UUID,
    /**
     * 使用简洁的UUID进行自增，该UUID是不包含 ‘-’的
     */
    SIMPLE_UUID,
    /**
     * 使用大写的UUID进行自增，该UUID是不包含 ‘-’的
     */
    UPPER_SIMPLE_UUID,
    /**
     * 使用数据库自增策略进行自增；
     * 比如mysql的自增策略，oracle触发器自增策略。
     */
    AUTO,
    /**
     * 使用序列进行自增，当使用序列类型自增时，需要执行序列名。目前仅Oracle支持
     *
     * @see GeneratedValue#sequenceName()
     */
    SEQUENCE,
    ;

}
