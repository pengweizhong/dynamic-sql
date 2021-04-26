package com.pengwz.dynamic.anno;

import com.pengwz.dynamic.config.DataSourceConfig;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
    String value() default "";

    Class<? extends DataSourceConfig> dataSourceClass() default DataSourceConfig.class;

}
