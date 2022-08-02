package com.pengwz.dynamic.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
public @interface Column {
    /**
     * 所属表别名，它将还在多表join时使用
     *
     * @return 所属表别名
     */
    String tableAlias() default "";

    /**
     * 列名，未指定时默认字段按照驼峰规则拼接下划线
     *
     * @return 列名
     */
    String value() default "";

}
