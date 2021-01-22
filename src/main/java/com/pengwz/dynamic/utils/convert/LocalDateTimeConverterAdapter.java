package com.pengwz.dynamic.utils.convert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import static com.pengwz.dynamic.constant.Constant.*;

/**
 * 转换的目标类型是 LocalDateTime
 */
@SuppressWarnings("unchecked")
public class LocalDateTimeConverterAdapter implements ConverterAdapter {
    @Override
    public <T> T converter(Object currentValue, Class<T> targetClass) {
        if (Date.class.isAssignableFrom(currentValue.getClass())) {
            return transferDate(currentValue);
        }
        if (String.class.isAssignableFrom(currentValue.getClass())) {
            return transferString(currentValue);
        }
        if (LocalDate.class.isAssignableFrom(currentValue.getClass())) {
            return transferLocalDate(currentValue);
        }
        return null;
    }

    private <T> T transferDate(Object currentValue) {
        Date date = (Date) currentValue;
        return (T) LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private <T> T transferString(Object currentValue) {
        String valueStr = String.valueOf(currentValue);
        if (REGULAR_YYYY_MM_DD_HH_MM_SS.matcher(valueStr).matches()) {
            return (T) LocalDateTime.parse(valueStr, YYYY_MM_DD_HH_MM_SS);
        }
        if (REGULAR_YYYY_MM_DD.matcher(valueStr).matches()) {
            LocalDate parse = LocalDate.parse(valueStr, YYYY_MM_DD);
            return (T) LocalDateTime.of(parse, LocalTime.MIN);
        }
        return null;
    }

    private <T> T transferLocalDate(Object currentValue) {
        LocalDate localDate = (LocalDate) currentValue;
        return (T) LocalDateTime.of(localDate, LocalTime.MIN);
    }
}
