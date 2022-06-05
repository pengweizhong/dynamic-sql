package com.pengwz.dynamic.utils.convert;

public interface ConverterAdapter<T> {
    /**
     * 结果集类型转换父类，若自定义转后值仍然为null，程序仍然会尝试转换值
     *
     * @param entityClass 当前实体类对象，此值不会为空
     * @param fieldClass  需要转换的目标类型，该类型所对应的是实体类字段的类型
     * @param columnValue SQl查询的当前结果值，根据查询条件或实际值不同，此值可能为null
     * @return 转换成功后的结果值
     */
    T converter(final Class<?> entityClass, final Class<T> fieldClass, final Object columnValue);

}
