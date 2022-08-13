package com.pengwz.dynamic.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengwz.dynamic.anno.JsonMode;
import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.TableColumnInfo;
import com.pengwz.dynamic.model.TableInfo;
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
import java.util.Map;
import java.util.Objects;

public class ConverterUtils {
    private ConverterUtils() {
    }

    private static final Map<Class<?>, ConverterAdapter<?>> converterAdapterMap = new HashMap<>();

    static {
        converterAdapterMap.put(LocalDateTime.class, new LocalDateTimeConverterAdapter());
        converterAdapterMap.put(java.util.Date.class, new LocalDateTimeConverterAdapter());
        converterAdapterMap.put(LocalDate.class, new LocalDateConverterAdapter());
        converterAdapterMap.put(LocalTime.class, new LocalTimeConverterAdapter());
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertJdbc(Class<?> entityClass, ResultSet resultSet, TableColumnInfo tableColumnInfo) throws SQLException {
        if (Objects.isNull(resultSet)) {
            throw new BraveException("java.sql.ResultSet不可为null");
        }
        return convertJdbc(entityClass, resultSet, tableColumnInfo.getColumn(), (Class<T>) tableColumnInfo.getField().getType(), tableColumnInfo.getJsonMode());
    }

    public static <T> T convertJdbc(Class<?> entityClass, ResultSet resultSet, String columnName, Class<T> targetType) throws SQLException {
        return convertJdbc(entityClass, resultSet, columnName, targetType, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertJdbc(Class<?> entityClass, ResultSet resultSet, Object columnRecord, Class<T> targetType, JsonMode jsonMode) throws SQLException {
        if (Objects.isNull(resultSet)) {
            throw new BraveException("java.sql.ResultSet不可为null");
        }
        String columnName = String.valueOf(columnRecord);
        columnName = Check.unSplicingName(columnName);
        //用户配置的转换的优先级最高，先执行用户配置的
        T converter = checkedUserConverterAdapter(entityClass, resultSet, columnName, targetType);
        if (converter != null) {
            return converter;
        }
        //判断columnRecord是不是数字，取的是字段下表的话，直接获取
        if (NumberUtils.isNumeric(columnName)) {
            return getColumnIndexValue(entityClass, resultSet, Integer.valueOf(columnName), targetType, jsonMode);
        }
        if (Object.class.equals(targetType)) {
            return (T) resultSet.getObject(columnName);
        }
        //转换枚举
        if (targetType.isEnum()) {
            return (T) mappedEnum(resultSet, columnName, targetType);
        }
        if (jsonMode != null) {
            Object objectValue = resultSet.getObject(columnName);
            if (null == objectValue) {
                return null;
            }
            return getGson(jsonMode).fromJson(objectValue.toString(), targetType);
        }
        try {
            return resultSet.getObject(columnName, targetType);
        } catch (SQLException e) {
            // ignore exception,try again
            return convert(entityClass, resultSet.getObject(columnName), targetType);
        }
    }

    private static <T> T checkedUserConverterAdapter(Class<?> entityClass, ResultSet resultSet, String columnName, Class<T> targetType) throws SQLException {
        final ConverterAdapter<T> converterAdapter = (ConverterAdapter<T>) converterAdapterMap.get(targetType);
        if (converterAdapter == null) {
            return null;
        }
        //如果有，判断是否是内置的转换器
        if (!(converterAdapter instanceof LocalDateConverterAdapter)
                && !(converterAdapter instanceof LocalDateTimeConverterAdapter)
                && !(converterAdapter instanceof LocalTimeConverterAdapter)) {
            T converter;
            if (NumberUtils.isNumeric(columnName)) {
                converter = converterAdapter.converter(entityClass, targetType, resultSet.getObject(Integer.valueOf(columnName)));
            } else {
                converter = converterAdapter.converter(entityClass, targetType, resultSet.getObject(columnName));
            }
            return converter;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getColumnIndexValue(Class<?> entityClass, ResultSet resultSet, Integer columnIndex, Class<T> targetType, JsonMode jsonMode) throws SQLException {
        if (Object.class.equals(targetType)) {
            return (T) resultSet.getObject(columnIndex);
        }
        //转换枚举
        if (targetType.isEnum()) {
            return (T) mappedEnum(resultSet, columnIndex, targetType);
        }
        if (jsonMode != null) {
            Object objectValue = resultSet.getObject(columnIndex);
            if (null == objectValue) {
                return null;
            }
            return getGson(jsonMode).fromJson(objectValue.toString(), targetType);
        }
        try {
            return resultSet.getObject(columnIndex, targetType);
        } catch (SQLException e) {
            // ignore exception,try again
            return convert(entityClass, resultSet.getObject(columnIndex), targetType);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(Object value, Class<T> targetType) {
        return convert(Void.class, value, targetType);
    }

    /**
     * 将给定的对象转换为目标类型的对象,若无法转换，则抛出异常
     *
     * @param value 不可为null
     */
    @SuppressWarnings("unchecked")
    public static <T> T convert(Class<?> entityClass, Object value, Class<T> targetType) {
        if (Objects.isNull(value) || Objects.isNull(targetType)) {
            return null;
        }
        targetType = (Class<T>) basicTypeRepackaging(targetType);
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
        ConverterAdapter<T> converterAdapter = (ConverterAdapter<T>) converterAdapterMap.get(targetType);
        T result = null;
        if (Objects.nonNull(converterAdapter)) {
            result = converterAdapter.converter(entityClass, targetType, value);
        }
        if (Objects.isNull(result)) {
            String err = "当前值：" + value + "，转换目标类型失败。" + value.getClass() + "不能转换为" + targetType + "类型，因为找不到该类型适配器或不受支持的转换";
            throw new BraveException(err);
        }
        return result;
    }


    /**
     * 设置值到SQL时，将java对象转为数据库认知的对象
     */
    public static Object convertValueJdbc(Object fieldValue) {
        if (null == fieldValue) {
            return null;
        }
        Class<?> fieldValueClass = fieldValue.getClass();
        if (fieldValueClass.isEnum()) {
            //若是枚举，则调用枚举的Tostring方法
            return fieldValue.toString();
        }
        //其他值直接返回
        return fieldValue;
    }

    public static Gson getGson(JsonMode jsonMode) {
        Gson gson;
        if (jsonMode.equals(JsonMode.SERIALIZE_WRITE_NULLS)) {
            gson = new GsonBuilder().serializeNulls().create();
        } else {
            gson = new Gson();
        }
        return gson;
    }

    public static <T extends ConverterAdapter> void putConverterAdapter(Class<?> columnClass, T converterAdapter) {
        converterAdapterMap.put(columnClass, converterAdapter);
    }

    public static Map<Class<?>, ConverterAdapter<?>> getConverterAdapterMap() {
        return converterAdapterMap;
    }

    private static <T> Object mappedEnum(ResultSet resultSet, Integer columnIndex, Class<T> targetType) throws SQLException {
        //2 判断是否是枚举
        Object enumerateValue = resultSet.getObject(columnIndex);
        return innerMappedEnum(enumerateValue, columnIndex, targetType);
    }

    private static <T> Object mappedEnum(ResultSet resultSet, String columnName, Class<T> targetType) throws SQLException {
        //2 判断是否是枚举
        Object enumerateValue = resultSet.getObject(columnName);
        return innerMappedEnum(enumerateValue, columnName, targetType);
    }

    private static <T> Object innerMappedEnum(Object enumerateValue, Object columnRec, Class<T> targetType) {
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
        throw new BraveException("Failed to convert property value [" + columnRec + "]; No enum constant ：" + canonicalEnumerateName);
    }

    /**
     * 基本类型转包装类型
     */
    public static Class<?> basicTypeRepackaging(Class<?> targetType) {
        if (Objects.isNull(targetType)) {
            return null;
        }
        if (targetType.isPrimitive()) {
            switch (targetType.getName()) {
                case "byte":
                    return Byte.class;
                case "short":
                    return Short.class;
                case "int":
                    return Integer.class;
                case "long":
                    return Long.class;
                case "char":
                    return Character.class;
                case "float":
                    return Float.class;
                case "double":
                    return Double.class;
                case "boolean":
                    return Boolean.class;
                default:
            }
        }
        return targetType;
    }
}
