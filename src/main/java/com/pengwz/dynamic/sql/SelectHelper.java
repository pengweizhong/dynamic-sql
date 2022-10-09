package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.model.DbType;
import com.pengwz.dynamic.model.SelectParam;
import com.pengwz.dynamic.model.TableColumnInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

public class SelectHelper {
    private static final Log log = LogFactory.getLog(SelectHelper.class);

    private SelectHelper() {
    }

    public static void assembleQueryStatement(Select<?> select) {
        StringBuilder selectBuilder = new StringBuilder();
        selectBuilder.append("select ");
        final Map<String, SelectParam> selectParamMap = select.getSelectParamMap();
        selectParamMap.forEach((field, selectParam) -> {
            System.out.print(field);
            System.out.print("-----");
            System.out.print(selectParam);
            System.out.println();
            //赋值自定义列
            if (selectParam.getCustomColumn() != null) {
                selectBuilder.append(selectParam.getCustomColumn()).append(", ");
            }
            //赋值带有函数的列
            else if (selectParam.getFunctions() != null) {
                selectBuilder.append(assignmentFunction(selectParam));
            }
            //赋值原生普通列
            else {
                selectBuilder.append(assignmentRegular(selectParam));
            }
        });
        select.appendSelectSql(selectBuilder.substring(0, selectBuilder.lastIndexOf(",")));
    }

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
    private static StringBuilder assignmentFunction(final SelectParam selectParam) {
        final StringBuilder prefixBuilder = new StringBuilder();
        final StringBuilder suffixBuilder = new StringBuilder();
        final TableColumnInfo tableColumnInfo = selectParam.getTableColumnInfo();
        String datasource = selectParam.getDataSourceName();
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
    private static StringBuilder assignmentRegular(final SelectParam selectParam) {
        final StringBuilder columnBuilder = new StringBuilder();
        final String dataSourceName = selectParam.getDataSourceName();
        final TableColumnInfo tableColumnInfo = selectParam.getTableColumnInfo();
        columnBuilder.append(splicingName(dataSourceName, tableColumnInfo.getColumn())).append(".");
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
