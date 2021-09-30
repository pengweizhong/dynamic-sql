package com.pengwz.dynamic.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.pengwz.dynamic.anno.JsonMode.SERIALIZE_WRITE_NO_NULLS;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
public @interface ColumnJson {
    /**
     * 表列名，和物理表字段名称一致
     */
    String value() default "";

    /**
     * JSON转换时所使用的序列化模式，默认值不会序列化null值
     */
    JsonMode jsonMode() default SERIALIZE_WRITE_NO_NULLS;
}
