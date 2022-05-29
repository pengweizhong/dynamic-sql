package com.pengwz.dynamic.utils.convert;

public interface ConverterAdapter {
    /**
     * 结果集类型转换父类
     *
     * @param currentValue 当前结果值
     * @param targetClass  转换的目标类型
     * @param <T>          任意类型
     * @return 转换成功后的结果值
     */
    <T> T converter(Object currentValue, Class<T> targetClass);

}
