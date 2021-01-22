package com.pengwz.dynamic.anno;

import java.lang.annotation.*;

/**
 * 	当两个实体类属性不一致时，使用此注解，标注在实体类需要的属性上<br>
 * <font color="red"><b>属性名严格区分大小写</b></font> 
 * 	<p>
 * 	当本类中alias属性和FeildGroup中的属性alias都被赋值时，FeildGroup所指定的别名优先级最高
 * @author pengwz
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RelatedFeild {
	/**
	 *	全局别名，仅指定该属性时，为所有赋值的类都使用该别名
	 */
	String alias() default "";
	/**
	 * 	为指定的一个或多个类分别配置指定的别名
	 * @return
	 */
	FeildGroup[] group() default {};
	
}
