package com.pengwz.dynamic.constant;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Constant {

    public static final Pattern GET_PATTERN = Pattern.compile("^get[A-Z].*");
    public static final Pattern START_UPPER_PATTERN = Pattern.compile("^[A-Z].*");
    public static final Pattern IS_PATTERN = Pattern.compile("^is[A-Z].*");

    //格式：yyyy-MM-dd HH:mm:ss，例：2001-01-23 23:59:59
    public static final Pattern REGULAR_YYYY_MM_DD_HH_MM_SS = Pattern.compile("(((\\d{4})-(0[13578]|1[02])-(0[1-9]|[12]\\d|3[01]))|((\\d{4})-(0[469]|11)-(0[1-9]|[12]\\d|30))|((\\d{4})-(02)-(0[1-9]|1\\d|2[0-8]))|((\\d{2}(0[48]|[2468][048]|[13579][26]))-(02)-(29))|(((0[48]|[2468][048]|[13579][26])00)-(02)-(29))) (([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d))");
    //格式：yyyy-MM-dd，例：2001-01-23
    public static final Pattern REGULAR_YYYY_MM_DD = Pattern.compile("(\\d{4})-(0[13578]|1[02])-(0[1-9]|[12]\\d|3[01])");
    //格式：HH:mm:ss，例：23:59:59
    public static final Pattern REGULAR_HH_MM_SS = Pattern.compile("([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)");

    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final String YYYY_MM_DD_HH_MM_SS_STR = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_STR = "yyyy-MM-dd";
    public static final String HH_MM_SS_STR = "HH:mm:ss";

    public static final String WHERE = "where";
    public static final String SELECT = "select";
    public static final String FROM = "from";
    public static final String AND = "and";
    public static final String OR = "or";
    public static final String IN = "in";
    public static final String NOT_IN = "not in";
    public static final String IS = "is";
    public static final String IS_NOT = "is not";
    public static final String BETWEEN = "between";
    public static final String NOT_BETWEEN = "not between";
    public static final String ORDER = "order";
    public static final String GROUP = "group";
    public static final String BY = "by";
    public static final String LIKE = "like";
    public static final String NOT_LIKE = "not like";
    public static final String FIND_IN_SET = "find_in_set";
    public static final String MIN = "min";
    public static final String MAX = "max";


    public static final String EQ = "=";
    public static final String NEQ = "<>";
    public static final String GT = ">";
    public static final String GTE = ">=";
    public static final String LT = "<";
    public static final String LTE = "<=";


    public static final String EMPTY = "";
    public static final String SPACE = " ";
    public static final String PLACEHOLDER = "?";
    public static final String LEFT_BRACKETS = "(";
    public static final String RIGHT_BRACKETS = ")";
    public static final String COMMA = ",";
    public static final String UNDERSCORE = "_";


}
