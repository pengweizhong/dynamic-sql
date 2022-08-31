package com.pengwz.dynamic.anno;

import com.pengwz.dynamic.config.DataSourceConfig;

import java.lang.annotation.*;

/**
 * 表注解，作用在实体类类名上，value 和数据库表对应
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
    /**
     * 表名
     *
     * @return 返回trim()后的表名
     */
    String value() default "";

    /**
     * 是否将该表实体缓存到程序缓存中。默认缓存。
     *
     * @return 是否缓存表实体
     */
    boolean isCache() default true;

    /**
     * 表实体对应的数据源配置
     *
     * @return 表实体对应的数据源配置
     */
    Class<? extends DataSourceConfig> dataSourceClass() default DataSourceConfig.class;

}
