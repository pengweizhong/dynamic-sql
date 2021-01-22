package com.pengwz.dynamic.utils.convert;

public interface ConverterAdapter {

    <T> T converter(Object currentValue, Class<T> targetClass);

}
