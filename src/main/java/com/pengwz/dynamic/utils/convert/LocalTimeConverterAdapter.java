package com.pengwz.dynamic.utils.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.pengwz.dynamic.constant.Constant.HH_MM_SS;
import static com.pengwz.dynamic.constant.Constant.REGULAR_HH_MM_SS;

/**
 * 转换的目标类型是 LocalDateTime
 */
@SuppressWarnings("unchecked")
public class LocalTimeConverterAdapter implements ConverterAdapter {

    private static final Log log = LogFactory.getLog(LocalTimeConverterAdapter.class);

    @Override
    public <T> T converter(Object currentValue, Class<T> targetClass) {
        try {
            if (Timestamp.class.isAssignableFrom(currentValue.getClass())) {
                LocalDateTime localDateTime = ((Timestamp) currentValue).toLocalDateTime();
                return (T) localDateTime.toLocalTime();
            }
            String valueStr = String.valueOf(currentValue);
            if (REGULAR_HH_MM_SS.matcher(valueStr).matches()) {
                return (T) LocalTime.parse(valueStr, HH_MM_SS);
            }
            return (T) LocalTime.parse(valueStr, DateTimeFormatter.ISO_LOCAL_TIME);
        } catch (Exception exception) {
            log.warn("不受支持的转换。" + exception.getMessage());
        }
        return null;
    }

}
