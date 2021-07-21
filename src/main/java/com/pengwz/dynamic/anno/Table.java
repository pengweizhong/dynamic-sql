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
    String value() default "";

    Class<? extends DataSourceConfig> dataSourceClass() default DataSourceConfig.class;

}
