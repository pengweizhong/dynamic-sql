package com.pengwz.dynamic.utils.convert;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.pengwz.dynamic.constant.Constant.*;

@SuppressWarnings("unchecked")
public class LocalDateConverterAdapter implements ConverterAdapter<LocalDate> {
    @Override
    public LocalDate converter(Class<?> entityClass, Class<LocalDate> fieldClass, Object columnValue) {
        if (columnValue instanceof Number) {
            return Instant.ofEpochSecond(((Number) columnValue).longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (columnValue instanceof java.sql.Timestamp) {
            java.sql.Timestamp timestamp = (java.sql.Timestamp) columnValue;
            return timestamp.toLocalDateTime().toLocalDate();
        }
        if (LocalDate.class.isAssignableFrom(fieldClass)) {
            return transferString(fieldClass);
        }
        return null;
    }

    private <T> T transferString(Object currentValue) {
        String valueStr = String.valueOf(currentValue);
        if (REGULAR_YYYY_MM_DD_HH_MM_SS.matcher(valueStr).matches()) {
            return (T) LocalDate.parse(valueStr, YYYY_MM_DD_HH_MM_SS);
        }
        if (REGULAR_YYYY_MM_DD.matcher(valueStr).matches()) {
            return (T) LocalDate.parse(valueStr, YYYY_MM_DD);
        }
        if (Timestamp.class.isAssignableFrom(currentValue.getClass())) {
            return (T) ((Timestamp) currentValue).toLocalDateTime().toLocalDate();
        }
        return (T) LocalDate.parse(valueStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }

}
