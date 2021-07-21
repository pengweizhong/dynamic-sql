package com.pengwz.dynamic.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 将该注解加在需要和数据库交互的实体类上，若属性没有此注解，程序默认按照驼峰命名规则匹配表列名。<br>
 * 如果数据列名刚好和数据库保留字冲突，用反向单引号包裹住即可。<br>
 * <p>
 * 举例：<br>
 * <code>
 * <pre>
 * {@code
 *  class AooEntity{
 *      @Column("`id`")
 *      private Long id;
 *      @Column("`desc`")
 *      private String desc;
 *  }
 * }
 * </pre>
 * </code>
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface Column {
    String value() default "";
}
