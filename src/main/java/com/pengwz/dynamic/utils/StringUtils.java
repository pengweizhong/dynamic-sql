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

    /**
     * 比较两个字符串是否相同
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 是否相同
     */
    public static boolean isEquals(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equals(str2);
    }

    public static boolean isNotEquals(String str1, String str2) {
        return !isEquals(str1, str2);
    }

    public static boolean isEqualsIgnoreCase(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        }
        return str1.equalsIgnoreCase(str2);
    }

    public static boolean isNotEqualsIgnoreCase(String str1, String str2) {
        return !isEqualsIgnoreCase(str1, str2);
    }

}
