package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.anno.FeildGroup;
import com.pengwz.dynamic.anno.IgnoreFeild;
import com.pengwz.dynamic.anno.RelatedFeild;
import com.pengwz.dynamic.exception.BraveException;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EntityUtils {

	private static final Logger log = Logger.getGlobal();

	private EntityUtils(){}

	/**
	 * 该方法用于E实体类的成员变量值赋值给R实体类，代替繁琐的GetSet方法。<br>
	 * <b>E和R相同的成员变量下，E的值会覆盖R的值（无论R是否有值）</>
	 * @param <E> 被复制值的实体类
	 * @param <R> 返回结果的实体类
	 * @return R
	 */
	public static <E,R> R transform(@NonNull E e, @NonNull R r) {
		return transform(e, r, true);
	}

	/**
	 * 	该方法用于E实体类的成员变量值赋值给R实体类，代替繁琐的GetSet方法。<br>
	 * 	比如DTO成员变量赋值给VO成员变量的操作。<br>
	 * 	E和R相同的成员变量下，是否覆盖取决于isCover的值。
	 * @param <E> 被复制值的实体类
	 * @param <R> 返回结果的实体类
	 * @param isCover 当成员变量相同且R成员变量不为null时，E的值是否覆盖R的值；覆盖：true，不覆盖 false
	 * @see com.pengwz.springcloud.anno.RelatedFeild
	 * @see com.pengwz.springcloud.anno.IgnoreFeild
	 * @return	R 
	 */
	@SuppressWarnings("all")
	public static <E,R> R transform(@NonNull E e,@NonNull R r,boolean isCover) {
		//提前把E和R的class对象初始化，避免重复初始化
		List<Class<?>> rlist = new ArrayList<Class<?>>();
		getAllClass(r.getClass(), rlist);
		List<Class<?>> elist = new ArrayList<Class<?>>();
		getAllClass(e.getClass(), elist);
		//遍历R的所有方法
		for (Class<?> cls : rlist) {
			Method[] methods = cls.getDeclaredMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				if(!methodName.startsWith("set")) 
					continue;
				try {
					//当设置isCover为false时，才会查询R的值
					if(!isCover) {
						Object invoke =cls.getDeclaredMethod("get".concat(methodName.substring(3)), null).invoke(r, null);
						//当R没有值时，才会去把E的属性赋值到R
						if(invoke == null) {
							invoke(e, r, method,elist);
						}
					} else {
						invoke(e, r, method,elist);
					}
				} catch (IllegalArgumentException ex) {
					log.info(ex.getMessage());
					String sub = method.getName().substring(3);
					String lowerCase = sub.substring(0,1).toLowerCase();
					String fieldname = lowerCase.concat(sub.substring(1,sub.length()));
					throw new IllegalArgumentException("because argument type mismatch,check the ["+fieldname+"] attribute "
							+ "from "+e.getClass()+" or "+r.getClass());
				} catch (BraveException ex) {
					log.info(ex.getMessage());
					throw ex;
				} catch (Exception ex) {
					log.info(ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
		return r;
	}

	/**
	 * 	获取该对象以及该对象所有的父类，Object类除外，用List集合返回
	 * @param cls
	 * @param list
	 */
	public static void getAllClass(Class<?> cls,List<Class<?>> list) {
		if(!list.contains(cls) && cls != Object.class){
			list.add(cls);
			getAllClass(cls.getSuperclass(), list);
		}
	}

	/**
	 *	如果Field.getType()是基本类型，则为其设置默认值,防止空指针
	 * @param field
	 * @return
	 */
	public static Object setDefaults(Field field) {
		Object value = null;
		switch (field.getType().toString()) {
			case "double":
			case "float":
				return 0.0;
			case "long":
				return 0L;
			case "int":
			case "short":
			case "byte":
				return 0;
			case "char":
				return '\u0000';
			case "boolean":
				return false;
			default:
		}
		return value;
	}
	
	/**
	 * 	获取E的值，并对R的set方法进行赋值操作
	 * @param <E> 被复制值的实体类
	 * @param <R> 返回结果的实体类
	 * @param rMethod R的方法
	 * @param elist E的class对象，包括自己和父类的class
	 * @throws Exception
	 */
	private static <E,R> void invoke(E e,R r,Method rMethod,List<Class<?>> elist) throws Exception {
		Object result = getFieldValue(e,r,rMethod.getName(),elist);
		//对于null不做任何操作
		if(result != null) 
			rMethod.invoke(r, result);
	}

	/**
	 * 	获取E的值
	 * @param <E> 被复制值的实体类
	 * @param <R> 返回结果的实体类
	 * @param rMethodName  R的set方法名
	 * @param elist E的class对象，包括自己和父类的class
	 * @return	执行E的GET方法返回的结果
	 * @throws Exception
	 */
	@SuppressWarnings("all")
	private static <E,R> Object getFieldValue(E e,R r,String rMethodName,List<Class<?>> elist) throws Exception {
		for (Class<?> cls : elist) {
			Field[] fields = cls.getDeclaredFields();
			for (Field field : fields) {
				//查看字段是否被忽略
				if(whetherIgnore(field, r))
					continue;
				//获取E和R原始的字段名称
				String originalEName = field.getName();
				String originalRName = rMethodName.substring(3).
						substring(0,1).toLowerCase().concat(rMethodName.substring(4,rMethodName.length()));
				String aliasFeild = aliasFeild(field, r);
				//别名优先级高于属性名，当指定了全局别名，便不再使用原有属性
				if(aliasFeild == null) {
					//没有指定全局属性，使用原有字段
					if(originalEName.equals(originalRName)) {
						String methodEName = "get"+originalRName.substring(0,1).toUpperCase()+originalRName.substring(1);
						Object result = cls.getDeclaredMethod(methodEName, null).invoke(e, null);
						return result == null ? setDefaults(field) : result;
					}
				} else if (aliasFeild.equals(originalRName)) {
					String methodEName = "get"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
					Object result = cls.getDeclaredMethod(methodEName, null).invoke(e, null);
					return result == null ? setDefaults(field) : result;
				}
				//如果E类中的字段设置了别名，但是无法对目标类字段赋值，打印错误日志
				if(aliasFeild != null){
					log.info("The alias ["+cls+"] set by the field ["+field.getName()+"] of the ["+aliasFeild+"] class does not exist in the ["+r.getClass()+"] class, " +
									"the assignment operation cannot be completed, please check."
							);
				}
			}
		}
		// 如果仍无法匹配，说明是多余出来的方法，返回null
		return null;
	}

	/**
	 * 	查询该字段是否对R类忽略，如果忽略R类，该Feild便不向该类赋值
	 * @param <R> 是否被忽略的类
	 * @param field 是否被忽略的字段 
	 * @param r
	 * @return 忽略返回true，否则返回false
	 */
	private static <R> boolean whetherIgnore(Field field,R r) {
		IgnoreFeild ignoreFeild = field.getAnnotation(IgnoreFeild.class);
		//没有加@IgnoreFeild，说明不是需要忽略的字段
		if(ignoreFeild == null) 
			return false;
		//如果指定了不允许被忽略的类
		Class<?>[] notIncludedClass = ignoreFeild.notIncluded();
		Class<?>[] includedClass = ignoreFeild.included();
		//如果两个属性都赋值，则抛出异常
		if (notIncludedClass.length != 0 && includedClass.length != 0){
			throw new BraveException("parameter conflict,Because there can only be one of [notIncludedClass] and [includedClass]");
		}
		for (Class<?> cls : notIncludedClass) {
			if(cls == r.getClass()) {
				return false;
			}
		}
		for (Class<?> cls : includedClass) {
			if(cls != r.getClass()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 	获取注释内的别名字段
	 * @param <R> 判断是否为指定别名的类
	 * @param field	
	 * @param r	
	 * @return 返回别名
	 */
	private static <R> String aliasFeild(Field field,R r) {
		RelatedFeild relatedFeild = field.getAnnotation(RelatedFeild.class);
		FeildGroup singleGroup = field.getAnnotation(FeildGroup.class);
		FeildGroup[] group = new FeildGroup[0];
		if(relatedFeild != null){
			group = relatedFeild.group();
		} else if(singleGroup != null){
			//将原数组扩容后赋值
			group = new FeildGroup[group.length + 1];
			group[group.length-1] = singleGroup;
		} else{
			return null;
		}
		//优先匹配FeildGroup别名
		for (FeildGroup feildGroup : group) {
			Class<?>[] clazz = feildGroup.clazz();
			if(clazz.length == 0){
				String errMsg = "When an alias is specified, the corresponding entity class cannot be empty. INFO : [field="+field.getName()+",alias="+feildGroup.alias()+",clazz="+feildGroup.clazz()+"]";
				log.info(errMsg);
				throw new BraveException(errMsg);
			}
			for (Class<?> cls : clazz) {
				if(cls == r.getClass() && !feildGroup.alias().trim().equals(""))
					return feildGroup.alias().trim();
			}
		}
		//然后匹配全局别名
		if(relatedFeild != null){
			String aliasGlobalName = relatedFeild.alias();
			if(aliasGlobalName.trim().equals("")) {
				return null;
			}else {
				return aliasGlobalName;
			}
		}
		return null;
	}

}