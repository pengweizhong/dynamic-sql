package com.pengwz.dynamic.utils.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.pengwz.dynamic.constant.Constant.*;

/**
 * 转换的目标类型是 LocalDateTime
 */
@SuppressWarnings("unchecked")
public class LocalDateTimeConverterAdapter implements ConverterAdapter<LocalDateTime> {

    private static final Log log = LogFactory.getLog(LocalDateTimeConverterAdapter.class);

    @Override
    public LocalDateTime converter(Class<?> entityClass, Class<LocalDateTime> fieldClass, Object columnValue) {
        try {
            if (columnValue instanceof Number) {
                return Instant.ofEpochSecond(((Number) columnValue).longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            if (Date.class.isAssignableFrom(fieldClass)) {
                return transferDate(columnValue);
            }
            if (LocalDateTime.class.isAssignableFrom(fieldClass)) {
                return transferLocalDateTime(columnValue);
            }
        } catch (ParseException parseException) {
            log.warn("不受支持的转换。" + parseException.getMessage());
        }

        return null;
    }

    private <T> T transferDate(Object currentValue) throws ParseException {
        String valueStr = String.valueOf(currentValue);
        if (REGULAR_HH_MM_SS.matcher(valueStr).matches()) {
            return (T) new SimpleDateFormat(HH_MM_SS_STR).parse(valueStr);
        }
        return (T) new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_STR).parse(valueStr);
    }

    private <T> T transferLocalDateTime(Object currentValue) {
        String valueStr = String.valueOf(currentValue);
        if (REGULAR_YYYY_MM_DD_HH_MM_SS.matcher(valueStr).matches()) {
            return (T) LocalDateTime.parse(valueStr, YYYY_MM_DD_HH_MM_SS);
        }
        if (REGULAR_YYYY_MM_DD.matcher(valueStr).matches()) {
            LocalDate parse = LocalDate.parse(valueStr, YYYY_MM_DD);
            return (T) LocalDateTime.of(parse, LocalTime.MIN);
        }
        if (Timestamp.class.isAssignableFrom(currentValue.getClass())) {
            return (T) ((Timestamp) currentValue).toLocalDateTime();
        }
        return (T) LocalDateTime.parse(valueStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private <T> T transferLocalDate(Object currentValue) {
        LocalDate localDate = (LocalDate) currentValue;
        return (T) LocalDateTime.of(localDate, LocalTime.MIN);
    }

}
