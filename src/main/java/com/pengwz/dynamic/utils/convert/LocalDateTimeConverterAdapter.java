package com.pengwz.dynamic.utils.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.pengwz.dynamic.constant.Constant.*;

/**
 * 转换的目标类型是 LocalDateTime
 */
@SuppressWarnings("unchecked")
public class LocalDateTimeConverterAdapter implements ConverterAdapter {

    private static final Log log = LogFactory.getLog(LocalDateTimeConverterAdapter.class);

    @Override
    public <T> T converter(Object currentValue, Class<T> targetClass) {
        try {
            if (Date.class.isAssignableFrom(targetClass)) {
                return transferDate(currentValue);
            }
            if (LocalDateTime.class.isAssignableFrom(targetClass)) {
                return transferLocalDateTime(currentValue);
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
        return (T) LocalDateTime.parse(valueStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private <T> T transferLocalDate(Object currentValue) {
        LocalDate localDate = (LocalDate) currentValue;
        return (T) LocalDateTime.of(localDate, LocalTime.MIN);
    }
}
