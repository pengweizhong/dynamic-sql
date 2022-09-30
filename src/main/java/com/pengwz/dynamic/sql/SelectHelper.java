package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DbType;
import com.pengwz.dynamic.model.SelectParam;
import com.pengwz.dynamic.model.TableColumnInfo;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.utils.ReflectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectHelper {
    private static final Log log = LogFactory.getLog(SelectHelper.class);

    private SelectHelper() {
    }

    public static void assembleQueryStatement(Select<?> select) {
        StringBuilder selectBuilder = new StringBuilder();
        selectBuilder.append("select ");
        final Map<String, SelectParam> selectParamMap = select.getSelectParamMap();
        final Set<String> queryColumns = selectParamMap.keySet();
        final TableInfo tableInfo = Check.getBuilderTableInfo(select.getResultClass());
        final Field[] resultFields = ReflectUtils.getAllDeclaredFields(select.getResultClass());

        final LinkedHashSet<String> queryAllFieldNames = new LinkedHashSet<>();
        //先处理用户自定义的查询列
        for (String fieldName : queryColumns) {
            final TableColumnInfo tableColumnInfo = tableInfo.getTableColumnInfos().stream().filter(tci -> tci.getField().getName().equals(fieldName))
                    .findFirst().orElseGet(() -> {
                        final String columnName = getColumnAliasName(fieldName);
                        //加入限定符，与集合中tableInfo进行比较
                        final String splicingColumnName = splicingName(tableInfo.getDataSourceName(), columnName);
                        final TableColumnInfo matchTableColumnInfo = tableInfo.getTableColumnInfos().stream().filter(bti -> bti.getColumn().equalsIgnoreCase(splicingColumnName)).findAny()
                                .orElseThrow(() -> new BraveException("查询了不存在或已忽略的列！参考错误值：" + fieldName));
                        final String column = matchTableColumnInfo.getColumn();
                        if (queryColumns.contains(matchTableColumnInfo.getField().getName())) {
                            throw new BraveException("查询列和自定义列冲突！参考错误值：[" + fieldName + ", " + column + "]\n" +
                                    "column()方法和customColumn()方法不允许指向同一个字段");
                        }
                        queryAllFieldNames.add(matchTableColumnInfo.getField().getName());
                        return matchTableColumnInfo;
                    });
            queryAllFieldNames.add(tableColumnInfo.getField().getName());
//            if (StringUtils.isEmpty(tableColumnInfo.getTableAlias())) {
//                throw new BraveException("多表查询时需要指定字段别名");
//            }
            final SelectParam selectParam = selectParamMap.get(fieldName);
            //赋值自定义列
            if (selectParam.isCustomColumn()) {
//                selectBuilder.append(selectParam.getFieldName()).append(", ");
            }
            //赋值带有函数的列
            else if (selectParam.getFunctions() != null) {
                selectBuilder.append(assignmentFunction(tableColumnInfo, selectParam, tableInfo.getDataSourceName()));
            }
            //赋值原生普通列
            else {
                selectBuilder.append(assignmentRegular(tableColumnInfo, tableInfo.getDataSourceName()));
            }
        }
        //用户如果查询全部的列
        if (select.isSelectAll()) {
            tableInfo.getTableColumnInfos().forEach(tableColumnInfo -> {
                final String name = tableColumnInfo.getField().getName();
                if (queryAllFieldNames.contains(name)) {
                    return;
                }
                selectBuilder.append(assignmentRegular(tableColumnInfo, tableInfo.getDataSourceName()));
            });
        }
        select.appendSelectSql(selectBuilder.substring(0, selectBuilder.lastIndexOf(",")));
    }

//    private static void assertTableInfoResult(TableInfo tableInfo) {
//        if (tableInfo.getViewType().equalsIgnoreCase(Check.ViewType.RESULT.name())) {
//            final String tableName = tableInfo.getTableName();
//            if (StringUtils.isNotEmpty(tableName)) {
//                throw new BraveException("多表查询时，实体类无需声明表名");
//            }
//        }
//        final List<TableColumnInfo> tableColumnInfos = tableInfo.getTableColumnInfos();
//        for (TableColumnInfo tableColumnInfo : tableColumnInfos) {
//            if (StringUtils.isEmpty(tableColumnInfo.getTableAlias())) {
//                throw new BraveException("多表查询时，实体类字段必须指定所依赖的表实体类；字段位置：" + tableColumnInfo.getField().getName());
//            }
//        }
//    }

    /**
     * 检索出用户指定的列的别名，这里的列名是未经校验的
     *
     * @return 检索出的列的别名
     */
    public static String getColumnAliasName(String expr) {
        //判断自定义表达式
        final String trim = expr.trim();
        final String columnName = trim.substring(trim.lastIndexOf(" ") + 1);
        return Check.unSplicingName(columnName);
    }

    /**
     * 赋值带有函数的字段
     */
    private static StringBuilder assignmentFunction(final TableColumnInfo tableColumnInfo, final SelectParam selectParam, String datasource) {
        final StringBuilder prefixBuilder = new StringBuilder();
        final StringBuilder suffixBuilder = new StringBuilder();

        final List<SelectParam.Function> functions = selectParam.getFunctions();
        //根据调用顺序，反序决定函数优先级，最先调用的函数在最里层
        for (int i = functions.size() - 1; i >= 0; i--) {
            final SelectParam.Function function = functions.get(i);
            prefixBuilder.append(function.getFunc());
            prefixBuilder.append("(");
            if (i == 0) {
                setColumnName(tableColumnInfo, prefixBuilder, datasource);
            }
            suffixBuilder.append(")");
            for (int i1 = 0; i1 < function.getParams().length; i1++) {
                suffixBuilder.append("?,");
            }
        }
        //这里需要反转，为对应的函数匹配占位符的位置
        prefixBuilder.append(suffixBuilder.reverse());
        prefixBuilder.append(" as ");
        prefixBuilder.append(tableColumnInfo.getColumn());
        prefixBuilder.append(", ");
        return prefixBuilder;
    }

    /**
     * 赋值普通字段
     */
    private static StringBuilder assignmentRegular(final TableColumnInfo tableColumnInfo, String datasource) {
        final StringBuilder columnBuilder = new StringBuilder();
        columnBuilder.append(splicingName(datasource, "tableColumnInfo.getTableAlias()")).append(".");
        columnBuilder.append(tableColumnInfo.getColumn());
        columnBuilder.append(", ");
        return columnBuilder;
    }


    private static void setColumnName(final TableColumnInfo tableColumnInfo, final StringBuilder columnBuilder, String datasource) {
//        final String tableAlias = tableColumnInfo.getTableAlias();
//        if (StringUtils.isEmpty(tableAlias)) {
//            throw new BraveException("使用Select查询时，必须指定表别名");
//        }
//        final String alias = splicingName(datasource, tableAlias);
//        columnBuilder.append(alias).append(".");
        columnBuilder.append(tableColumnInfo.getColumn());
    }

    private static String splicingName(String datasource, String column) {
        final DbType dbType = ContextApplication.getDataSourceInfo(datasource).getDbType();
        return Check.splicingName(dbType, column);
    }
}
