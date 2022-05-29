package com.pengwz.dynamic.utils.convert;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.pengwz.dynamic.constant.Constant.*;

@SuppressWarnings("unchecked")
public class LocalDateConverterAdapter implements ConverterAdapter<LocalDate> {
    @Override
    public LocalDate converter(Object currentValue, Class<LocalDate> targetClass) {
        if (currentValue instanceof Number) {
            return Instant.ofEpochSecond(((Number) currentValue).longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (LocalDate.class.isAssignableFrom(targetClass)) {
            return transferString(currentValue);
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
