package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.base.HandleFunction;
import com.pengwz.dynamic.sql.base.impl.Count;
import com.pengwz.dynamic.sql.base.impl.GroupBy;
import com.pengwz.dynamic.sql.base.impl.OrderBy;
import com.pengwz.dynamic.utils.ConverterUtils;
import com.pengwz.dynamic.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.pengwz.dynamic.check.Check.checkAndSave;
import static com.pengwz.dynamic.constant.Constant.*;

public class ParseSql {

    public static String parse(Class<?> currentClass, String tableName, String dataSource, List<Declaration> declarationList, Map<String, List<String>> orderByMap) {
        checkAndSave(currentClass, tableName, dataSource);
        StringBuilder whereSql = new StringBuilder();
        for (Declaration declaration : declarationList) {
            if (Objects.nonNull(declaration.getBrackets())) {
                whereSql.append(declaration.getBrackets()).append(SPACE);
                continue;
            }
            //解析函数
            if (Objects.nonNull(declaration.getHandleFunction())) {
                HandleFunction handleFunction = declaration.getHandleFunction();
                if (handleFunction instanceof Count) {
                    continue;
                }
                if (handleFunction instanceof OrderBy) {
                    handleFunction.execute(dataSource, tableName, declaration);
                    String column = ContextApplication.getColumnByField(dataSource, tableName, declaration.getProperty());
                    whereSql.append(" order by " + column + " " + declaration.getSortMode());
                    continue;
                }
                if (handleFunction instanceof GroupBy) {
                    String property = declaration.getProperty();
                    String[] split = property.split(",");
                    List<String> columns = new ArrayList<>();
                    Arrays.asList(split).forEach(field -> columns.add(ContextApplication.getColumnByField(dataSource, tableName, field)));
                    whereSql.append(SPACE + GROUP + SPACE + BY + SPACE + String.join(",", columns));
                    continue;
                }
                whereSql.append(declaration.getHandleFunction().execute(dataSource, tableName, declaration)).append(SPACE);
            } else if (declaration.getCondition().equals(BETWEEN) || declaration.getCondition().equals(NOT_BETWEEN)) {
                whereSql.append(declaration.getAndOr()).append(SPACE);
                whereSql.append(ContextApplication.getColumnByField(dataSource, tableName, declaration.getProperty())).append(SPACE);
                whereSql.append(declaration.getCondition()).append(SPACE);
                whereSql.append(matchValue(declaration.getValue())).append(SPACE);
                whereSql.append(AND).append(SPACE);
                whereSql.append(matchValue(declaration.getValue2())).append(SPACE);
            } else {
                whereSql.append(declaration.getAndOr()).append(SPACE);
                whereSql.append(ContextApplication.getColumnByField(dataSource, tableName, declaration.getProperty())).append(SPACE);
                whereSql.append(declaration.getCondition()).append(SPACE);
                whereSql.append(matchValue(declaration.getValue())).append(SPACE);
            }
        }
        if (Objects.nonNull(orderByMap)) {
            Set<String> keys = orderByMap.keySet();
            for (String key : keys) {
                whereSql.append(ORDER).append(SPACE).append(BY).append(SPACE);
                List<String> list = orderByMap.get(key);
                for (String field : list) {
                    String columnByField = ContextApplication.getColumnByField(dataSource, tableName, field);
                    whereSql.append(columnByField).append(COMMA).append(SPACE);
                }
                whereSql = new StringBuilder(whereSql.substring(0, whereSql.length() - 2));
                whereSql.append(SPACE).append(key).append(SPACE);
            }
        }
        return whereSql.toString();
    }

    /**
     * 去掉多余的and or，返回正确的sql
     */
    public static String parseSql(String sql) {
        String[] split = sql.split(SPACE);
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>(split);
        list.remove("");
        list.remove(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i + 1 <= list.size() - 1) {
                String checkStr = list.get(i) + list.get(i + 1);
                if ((checkStr).equals(WHERE + AND) || (checkStr).equals(WHERE + OR)) {
                    sb.append(list.get(i)).append(SPACE);
                    i++;
                    continue;
                }
                if ((checkStr).equals(WHERE + ORDER)) {
                    //去掉where
                    continue;
                }
                if ((checkStr).equals(WHERE + GROUP)) {
                    //去掉where
                    continue;
                }
                if ((checkStr).equals(LEFT_BRACKETS + AND) || (checkStr).equals(LEFT_BRACKETS + OR)) {
                    String element = list.get(i - 1);
                    if (!element.equals(AND) && !element.equals(OR) && !element.equals(WHERE)) {
                        sb.append(list.get(i + 1)).append(SPACE);
                    }
                    sb.append(list.get(i)).append(SPACE);
                    i++;
                    continue;
                }
                if (checkStr.equals(ORDER + BY) && sb.toString().contains(ORDER + " " + BY)) {
                    sb.append(", ");
                    i++;
                    continue;
                }
            }
            sb.append(list.get(i)).append(SPACE);
        }
        return sb.toString();
    }

    public static Object matchValue(Object value) {
//        if (Objects.isNull(value)) {
//           throw new BraveException("值不允许为null");
//        }
        value = ConverterUtils.convertValueJdbc(value);
        if (value instanceof String) {
            return "'" + value + "'";
        }
        if (value instanceof Number) {
            return value;
        }
        if (value instanceof Date) {
            return "'" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value)) + "'";
        }
        if (value instanceof LocalDate) {
            return "'" + (LocalDate.parse(value + "")) + "'";
        }
        if (value instanceof LocalDateTime) {
            return "'" + (LocalDateTime.parse(value + "")) + "'";
        }
        if (value instanceof Iterable) {
            Iterator iterator = ((Iterable) value).iterator();
            if (!iterator.hasNext()) {
                throw new BraveException("集合不可以为空");
            }
            StringBuilder sb = new StringBuilder();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                if (next instanceof String) {
                    sb.append("'").append(next).append("'").append(COMMA);
                } else {
                    sb.append(next).append(COMMA);
                }
            }
            String string = sb.toString();
            String substring = string.substring(0, string.length() - 1);
            return LEFT_BRACKETS + substring + RIGHT_BRACKETS;
        }
        return value;
    }

    public static String parseAggregateFunction(String aggregateFunctionName, String dataSource, String tableName, Declaration declaration) {
        StringBuilder whereFunctionSql = new StringBuilder();
        whereFunctionSql.append(declaration.getAndOr());
        // id = (
        String column = ContextApplication.getColumnByField(dataSource, tableName, declaration.getProperty());
        whereFunctionSql.append(SPACE).append(column).append(SPACE).append(EQ).append(SPACE).append(LEFT_BRACKETS).append(SPACE);
        //select min(property)
        whereFunctionSql.append(SELECT).append(SPACE).append(aggregateFunctionName).append(LEFT_BRACKETS).append(column).append(RIGHT_BRACKETS).append(SPACE);
        //from tableName )
        whereFunctionSql.append(FROM).append(SPACE).append(tableName).append(SPACE).append(RIGHT_BRACKETS);
        return whereFunctionSql.toString();
    }

    //待定  是否需要调整
    public static String fixWhereSql(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return "";
        }
        //如果包含group语句
        if (sql.contains(GROUP)) {

        }
        return sql;
    }

}
