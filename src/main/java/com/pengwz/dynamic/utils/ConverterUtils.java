package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.utils.convert.ConverterAdapter;
import com.pengwz.dynamic.utils.convert.LocalDateConverterAdapter;
import com.pengwz.dynamic.utils.convert.LocalDateTimeConverterAdapter;
import com.pengwz.dynamic.utils.convert.LocalTimeConverterAdapter;
import org.apache.commons.beanutils.ConvertUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Objects;

public class ConverterUtils {

    private static final HashMap<Class, ConverterAdapter> converterAdapterMap = new HashMap<>();

    static {
        converterAdapterMap.put(LocalDateTime.class, new LocalDateTimeConverterAdapter());
        converterAdapterMap.put(LocalDate.class, new LocalDateConverterAdapter());
        converterAdapterMap.put(LocalTime.class, new LocalTimeConverterAdapter());
    }

    /**
     * 将给定的对象转换为目标类型的对象,若无法转换，则抛出异常
     *
     * @param value 不可为null
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, Class<T> targetType) {
        if (Objects.isNull(value) || Objects.isNull(targetType)) {
            return null;
        }
        Object convert = ConvertUtils.convert(value, targetType);
        if (convert.getClass().equals(targetType)) {
            return (T) convert;
        }
        //如果apache的工具仍然不能满足需求，则进行补充
        ConverterAdapter converterAdapter = converterAdapterMap.get(targetType);
        if (Objects.nonNull(converterAdapter)) {
            T targetValue = converterAdapter.converter(value, targetType);
            if (Objects.nonNull(targetValue)) {
                return targetValue;
            }
        }
        String err = "当前值：" + value + "，转换目标类型失败。" + value.getClass() + "不能转换为" + targetType + "类型，因为找不到该类型适配器或不受支持的转换";
        throw new BraveException(err);
    }

    public static <T> T convertJdbc(ResultSet resultSet, String fieldName, Class<T> targetType) throws SQLException {
        if (Objects.isNull(resultSet)) {
            throw new BraveException("java.sql.ResultSet不可为null");
        }
        if (java.util.Date.class.isAssignableFrom(targetType)) {
            Object object = resultSet.getObject(fieldName);
            return convert(object, targetType);
        }
        try {
            return resultSet.getObject(fieldName, targetType);
        } catch (SQLException e) {
            // ignore exception,try again
            return convert(resultSet.getObject(fieldName), targetType);
        }
    }
}
