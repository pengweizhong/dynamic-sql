package com.pengwz.dynamic.anno;

import java.lang.annotation.*;

/**
 * 当赋值的时候，程序会按照要求对指定的字段进行忽略或不忽略操作
 * 在同一字段上标明注解属性时，notIncluded和included不可以同时存在
 * 不赋值默认忽略所有的类
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface IgnoreFeild {
	
	/**
	 * 	在赋值时不会忽略该属性指定的类，不被指定的类在赋值时将被忽略
	 * @return
	 */
	 Class<?>[] notIncluded() default {};
	/**
	 * 	在赋值时忽略该属性指定的类，当指定该类后，便不能为该类赋值
	 * @return
	 */
	 Class<?>[] included() default {};

}
