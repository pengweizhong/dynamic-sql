package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.utils.convert.ConverterAdapter;
import com.pengwz.dynamic.utils.convert.LocalDateConverterAdapter;
import com.pengwz.dynamic.utils.convert.LocalDateTimeConverterAdapter;
import com.pengwz.dynamic.utils.convert.LocalTimeConverterAdapter;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Objects;

public class ConverterUtils {

    private static final HashMap<Class<?>, ConverterAdapter> converterAdapterMap = new HashMap<>();

    static {
        converterAdapterMap.put(LocalDateTime.class, new LocalDateTimeConverterAdapter());
        converterAdapterMap.put(java.util.Date.class, new LocalDateTimeConverterAdapter());
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
        Object convert;
        try {
            convert = ConvertUtils.convert(value, targetType);
        } catch (ConversionException e) {
            //ignore
            convert = value;
        }
        if (convert.getClass().equals(targetType)) {
            return (T) convert;
        }
        //如果apache的工具仍然不能满足需求，则进行补充
        ConverterAdapter converterAdapter = converterAdapterMap.get(targetType);
        if (Objects.nonNull(converterAdapter)) {
            return converterAdapter.converter(value, targetType);
        }
        String err = "当前值：" + value + "，转换目标类型失败。" + value.getClass() + "不能转换为" + targetType + "类型，因为找不到该类型适配器或不受支持的转换";
        throw new BraveException(err);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertJdbc(ResultSet resultSet, String columnName, Class<T> targetType) throws SQLException {
        if (Objects.isNull(resultSet)) {
            throw new BraveException("java.sql.ResultSet不可为null");
        }
        if (columnName.contains("`")) {
            columnName = columnName.replace("`", "").trim();
        }
        if (columnName.contains("\"")) {
            columnName = columnName.replace("\"", "").trim();
        }
        if (Object.class.equals(targetType)) {
            return (T) resultSet.getObject(columnName);
        }
        //转换枚举
        if (targetType.isEnum()) {
            return (T) mappedEnum(resultSet, columnName, targetType);
        }
        try {
            return resultSet.getObject(columnName, targetType);
        } catch (SQLException e) {
            // ignore exception,try again
            return convert(resultSet.getObject(columnName), targetType);
        }
    }

    /**
     * 设置值到SQL时，将java对象转为mysql认知的对象
     */
    @SuppressWarnings("unchecked")
    public static Object convertValueJdbc(Object fieldValue) {
        if (null == fieldValue) {
            return null;
        }
        if (fieldValue.getClass().isEnum()) {
            //若是枚举，则调用枚举的Tostring方法
            return fieldValue.toString();
        }
        //其他值直接返回
        return fieldValue;
    }

    private static <T> Object mappedEnum(ResultSet resultSet, String columnName, Class<T> targetType) throws SQLException {
        //2 判断是否是枚举
        Object enumerateValue = resultSet.getObject(columnName);
        if (enumerateValue == null || StringUtils.isBlank(enumerateValue.toString())) {
            return null;
        }
        //枚举对象
        Object[] enumConstants = targetType.getEnumConstants();
        for (Object enumObj : enumConstants) {
            if (enumObj.toString().equalsIgnoreCase(enumerateValue.toString().trim())) {
                return enumObj;
            }
        }
        String canonicalEnumerateName = targetType.getCanonicalName() + "." + enumerateValue;
        throw new BraveException("Failed to convert property value [" + columnName + "]; No enum constant ：" + canonicalEnumerateName);
    }

}
