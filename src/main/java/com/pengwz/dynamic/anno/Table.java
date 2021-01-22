package com.pengwz.dynamic.anno;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
    String value() default "";

    Class<?> dataSourceClass() default Void.class;

    //注解嵌套
//    Reference reference() default @Reference(next=true);
}
