package com.pengwz.dynamic.utils;

import org.junit.Test;

public class NumberUtilsTest {

    @Test
    public void isNumeric(){
        System.out.println(NumberUtils.isNumeric(null));
        System.out.println(NumberUtils.isNumeric(""));
        System.out.println(NumberUtils.isNumeric("-1"));
        System.out.println(NumberUtils.isNumeric("+1"));
        System.out.println(NumberUtils.isNumeric("1"));
        System.out.println(NumberUtils.isNumeric(""));
        System.out.println(NumberUtils.isNumeric(" "));
        System.out.println(NumberUtils.isNumeric("1q"));
        System.out.println(NumberUtils.isNumeric("_q"));
        System.out.println(NumberUtils.isNumeric("_"));
        System.out.println(NumberUtils.isNumeric("1_"));
    }
}