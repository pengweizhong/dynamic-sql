package com.pengwz.dynamic.utils.convert;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static com.pengwz.dynamic.constant.Constant.*;

@SuppressWarnings("unchecked")
public class LocalDateConverterAdapter implements ConverterAdapter {
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
        if (date.getClass().equals(java.sql.Date.class)) {
            java.sql.Date sqlDate = (java.sql.Date) date;
            return (T) sqlDate.toLocalDate();
        }
        return (T) date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private <T> T transferString(Object currentValue) {
        String valueStr = String.valueOf(currentValue);
        if (REGULAR_YYYY_MM_DD_HH_MM_SS.matcher(valueStr).matches()) {
            return (T) LocalDate.parse(valueStr, YYYY_MM_DD_HH_MM_SS);
        }
        if (REGULAR_YYYY_MM_DD.matcher(valueStr).matches()) {
            return (T) LocalDate.parse(valueStr, YYYY_MM_DD);
        }
        return null;
    }

}
