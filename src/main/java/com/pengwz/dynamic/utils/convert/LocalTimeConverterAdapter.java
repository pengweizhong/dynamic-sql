package com.pengwz.dynamic.utils.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.pengwz.dynamic.constant.Constant.HH_MM_SS;
import static com.pengwz.dynamic.constant.Constant.REGULAR_HH_MM_SS;

/**
 * 转换的目标类型是 LocalDateTime
 */
@SuppressWarnings("unchecked")
public class LocalTimeConverterAdapter implements ConverterAdapter<LocalTime> {

    private static final Log log = LogFactory.getLog(LocalTimeConverterAdapter.class);

    @Override
    public LocalTime converter(Class<?> entityClass, Class<LocalTime> fieldClass, Object columnValue) {
        try {
            if (Timestamp.class.isAssignableFrom(columnValue.getClass())) {
                LocalDateTime localDateTime = ((Timestamp) columnValue).toLocalDateTime();
                return localDateTime.toLocalTime();
            }
            if (columnValue instanceof Number) {
                return Instant.ofEpochSecond(((Number) columnValue).longValue()).atZone(ZoneId.systemDefault()).toLocalTime();
            }
            String valueStr = String.valueOf(columnValue);
            if (REGULAR_HH_MM_SS.matcher(valueStr).matches()) {
                return LocalTime.parse(valueStr, HH_MM_SS);
            }
            return LocalTime.parse(valueStr, DateTimeFormatter.ISO_LOCAL_TIME);
        } catch (Exception exception) {
            log.warn("不受支持的转换。" + exception.getMessage());
        }
        return null;
    }

}
