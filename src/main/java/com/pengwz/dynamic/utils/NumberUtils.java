package com.pengwz.dynamic.utils;

import org.apache.commons.lang3.StringUtils;

public class NumberUtils {
    private NumberUtils() {
    }

    /**
     * 判断是否是数字，若是返回true，否则false <br>
     * 对于运算符没有验证。
     *
     * @param str 待验证的字符串
     * @return true or false
     */
    public static boolean isNumeric(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
