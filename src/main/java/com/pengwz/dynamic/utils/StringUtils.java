package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.constant.Constant;

import java.util.Objects;

public class StringUtils {


    public static boolean isEmpty(String str) {
        return Objects.isNull(str) || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return Objects.isNull(str) || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 根据驼峰命名自动将字段转为带下划线的数据库字段
     */
    public static String caseField(String field) {
        if (StringUtils.isEmpty(field)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (c >= (int) 'A' && c <= (int) 'Z') {
                sb.append(Constant.UNDERSCORE);
                sb.append(String.valueOf(c).toLowerCase());
            } else {
                sb.append(String.valueOf(c).toLowerCase());
            }
        }
        return sb.toString();
    }
}
