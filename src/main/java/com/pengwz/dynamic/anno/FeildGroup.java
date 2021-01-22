package com.pengwz.dynamic.anno;

import java.lang.annotation.*;

/**
 *  为指定的多个类配置别名<br/>
 * 	极端情况下，本类中设置的别名不可以和真实存在的属性名冲突，
 * 	如果确实需要，请考虑单独对冲突的字段手动GetSet或将冲突的属性也设置别名。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeildGroup {
	/**
	 * 	别名
	 * @return
	 */
	String alias() default "";
	/**
	 * 	该数组中可以指定多个类
	 * @return
	 */
	Class<?>[] clazz() default {};
}
