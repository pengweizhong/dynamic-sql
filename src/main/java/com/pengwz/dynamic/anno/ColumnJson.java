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

//    /**
//     * 所属表别名，目前它将在多表join时使用<br>
//     * 当指明此属性时，join表的过程必须也要指定别名，且别名一定与此处值相同，否则SQL可能会执行失败
//     *
//     * @return 所属表别名
//     * @see this#dependentTableClass()  注意，它与此属性互斥
//     */
//    String tableAlias() default "";

    /**
     * 所属表实体类，目前它将在多表join时使用<br>
     * 当指明此属性时，join表的过程将不要指定别名
     *
     * @return 所属表实体类
     */
    Class<?> dependentTableClass() default Void.class;
}
