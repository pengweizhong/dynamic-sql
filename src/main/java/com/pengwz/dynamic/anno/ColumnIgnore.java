package com.pengwz.dynamic.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 忽略字段，将此注解添加到某个字段上，该字段将不参与数据库交互
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface ColumnIgnore {
}
