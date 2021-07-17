package com.pengwz.dynamic.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.pengwz.dynamic.anno.GenerationType.AUTO;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
public @interface GeneratedValue {
    GenerationType strategy() default AUTO;
}
