package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.Complex;
import com.pengwz.dynamic.model.RelationEnum;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.base.HandleFunction;
import com.pengwz.dynamic.sql.base.impl.Count;
import com.pengwz.dynamic.sql.base.impl.GroupBy;
import com.pengwz.dynamic.sql.base.impl.OrderBy;
import com.pengwz.dynamic.utils.CollectionUtils;
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
    /**
     * 这里应该使用策略模式优化ta
     */
    public static String parse(Class<?> currentClass, Table table, String dataSource, DynamicSql dynamicSql,
                               Map<String, List<String>> orderByMap, List<Object> params) {
        String tableName = Check.getTableName(table.value(), dataSource);
        checkAndSave(currentClass, dataSource, tableName, table.isCache());
        List<Declaration> declarationList = dynamicSql.getDeclarations();
        StringBuilder whereSql = new StringBuilder();
        Iterator<Complex> iterator = null;
        if (CollectionUtils.isNotEmpty(dynamicSql.getComplexes())) {
            iterator = dynamicSql.getComplexes().iterator();
        }
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
            } else if (declaration.isComplex()) {
                if (iterator == null) {
                    throw new BraveException("不是正确的复合查询，未发现组合中的SQL体");
                }
                Complex complex = iterator.next();
                if (complex.getRelationEnum().equals(RelationEnum.AND)) {
                    whereSql.append(SPACE).append(AND);
                } else {
                    whereSql.append(SPACE).append(OR);
                }
                whereSql.append(SPACE).append(LEFT_BRACKETS).append(SPACE);
                whereSql.append(parse(currentClass, table, dataSource, complex.getDynamicSql(), orderByMap, params));
                whereSql.append(SPACE).append(RIGHT_BRACKETS).append(SPACE);
            } else if (declaration.getCondition().equals(BETWEEN) || declaration.getCondition().equals(NOT_BETWEEN)) {
                whereSql.append(declaration.getAndOr()).append(SPACE);
                whereSql.append(ContextApplication.getColumnByField(dataSource, tableName, declaration.getProperty())).append(SPACE);
                whereSql.append(declaration.getCondition()).append(SPACE);
                params.add(matchFixValue(declaration.getValue(), dataSource, tableName, declaration.getProperty()));
                splicePlaceholders(whereSql, declaration.getValue());
                whereSql.append(AND).append(SPACE);
                params.add(matchFixValue(declaration.getValue2(), dataSource, tableName, declaration.getProperty()));
                splicePlaceholders(whereSql, declaration.getValue2());
            } else if (declaration.getCondition().equals(IS) || declaration.getCondition().equals(IS_NOT)) {
                whereSql.append(declaration.getAndOr()).append(SPACE);
                whereSql.append(ContextApplication.getColumnByField(dataSource, tableName, declaration.getProperty())).append(SPACE);
                whereSql.append(declaration.getCondition()).append(SPACE);
                whereSql.append("null").append(SPACE);
            } else if (declaration.getCondition().equals(FIND_IN_SET)) {
                whereSql.append(declaration.getAndOr()).append(SPACE);
                whereSql.append(declaration.getCondition()).append("(?,").append(ContextApplication.getColumnByField(dataSource, tableName, declaration.getProperty())).append(")").append(SPACE);
                params.add(matchFixValue(declaration.getValue(), dataSource, tableName, declaration.getProperty()));
            } else {
                whereSql.append(declaration.getAndOr()).append(SPACE);
                whereSql.append(ContextApplication.getColumnByField(dataSource, tableName, declaration.getProperty())).append(SPACE);
                whereSql.append(declaration.getCondition()).append(SPACE);
                params.add(matchFixValue(declaration.getValue(), dataSource, tableName, declaration.getProperty()));
                splicePlaceholders(whereSql, declaration.getValue());
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

    public static void splicePlaceholders(StringBuilder sb, Object value) {
        if (value instanceof Iterable) {
            Iterator iterator = ((Iterable) value).iterator();
            if (!iterator.hasNext()) {
                throw new BraveException("SQL参数集合不可以为空");
            }
            sb.append(LEFT_BRACKETS);
            while (iterator.hasNext()) {
                iterator.next();
                sb.append(PLACEHOLDER).append(COMMA).append(SPACE);
            }
            final int index = sb.lastIndexOf(COMMA + SPACE);
            sb.delete(index, sb.length());
            sb.append(SPACE);
            sb.append(RIGHT_BRACKETS);
        } else {
            sb.append(PLACEHOLDER).append(SPACE);
        }
    }

    public static Object matchFixValue(Object value, String database, String tableName, String property) {
        if (value == null) {
            return null;
        }
        final TableInfo tableInfo = ContextApplication.getTableInfo(database, tableName, property);
        if (tableInfo.getJsonMode() != null) {
            return ConverterUtils.getGson(tableInfo.getJsonMode()).toJson(value);
        }
        return value;
    }

    public static Object matchValue(Object value) {
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
                } else if (next instanceof Enum) {
                    sb.append("'").append(((Enum<?>) next).name()).append("'").append(COMMA);
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

//    private static String fixSQLInjection(final DbType dbType, String value) {
//        if (value.contains("\\")) {
//            value = value.replace("\\", "\\\\");
//        }
//        if (value.contains("'")) {
//            value = value.replace("'", "\\'");
//        }
//        return "'" + value + "'";
//    }

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

    public static String ifAppendLimit(Limit limit, String whereSql) {
        //判断是否有limit
        if (limit != null) {
            return whereSql.concat(" limit " + limit.getStartIndex()).concat(", ").concat(limit.getEndIndex() + "");
        }
        return whereSql;
    }
}
