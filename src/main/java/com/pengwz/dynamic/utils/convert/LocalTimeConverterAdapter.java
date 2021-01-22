package com.pengwz.dynamic.utils.convert;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import static com.pengwz.dynamic.constant.Constant.*;

/**
 * 转换的目标类型是 LocalDateTime
 */
@SuppressWarnings("unchecked")
public class LocalTimeConverterAdapter implements ConverterAdapter {
    @Override
    public <T> T converter(Object currentValue, Class<T> targetClass) {
        if (Date.class.isAssignableFrom(currentValue.getClass())) {
            return transferDate(currentValue);
        }
        if (String.class.isAssignableFrom(currentValue.getClass())) {
            return transferString(currentValue);
        }
        return null;
    }

    private <T> T transferDate(Object currentValue) {
        Date date = (Date) currentValue;
        return (T) date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    private <T> T transferString(Object currentValue) {
        String valueStr = String.valueOf(currentValue);
        if (REGULAR_YYYY_MM_DD_HH_MM_SS.matcher(valueStr).matches()) {
            LocalDateTime parse = LocalDateTime.parse(valueStr, YYYY_MM_DD_HH_MM_SS);
            return (T) parse.toLocalTime();
        }
        if (REGULAR_HH_MM_SS.matcher(valueStr).matches()) {
            return (T) LocalTime.parse(valueStr, HH_MM_SS);
        }
        return null;
    }

}
