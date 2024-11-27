package com.pengwz.dynamic.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 在类中标注此注解，对应数据库主键，该注解在实体类中最多包含一个
 */
@Target(FIELD)
@Retention(RUNTIME)
@Deprecated
public @interface Id {
}
