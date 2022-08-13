package com.pengwz.dynamic.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
public @interface Column {
    /**
     * 列名，未指定时默认字段按照驼峰规则拼接下划线
     *
     * @return 列名
     */
    String value() default "";

    /**
     * 所属表别名，目前它将在多表join时使用<br>
     * 当指明此属性时，join表的过程必须也要指定别名，且别名一定与此处值相同，否则SQL可能会执行失败
     *
     * @return 所属表别名
     * @see this#tableClass()  注意，它与此属性互斥
     */
    String tableAlias() default "";

    /**
     * 所属表实体类，目前它将在多表join时使用<br>
     * 当指明此属性时，join表的过程将不要指定别名
     *
     * @return 所属表实体类
     * @see this#tableAlias() 注意，它与此属性互斥，本方法优先级最高
     */
    Class<?> tableClass() default Void.class;

}
