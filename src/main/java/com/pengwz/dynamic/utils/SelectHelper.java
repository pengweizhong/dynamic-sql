package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DbType;
import com.pengwz.dynamic.model.SelectParam;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.Select;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SelectHelper {
    private static final Log log = LogFactory.getLog(SelectHelper.class);

    private SelectHelper() {
    }

    /**
     * 向集合中丢入查询列
     *
     * @param selectParamMap 历史缓存列Map
     * @param fieldName      缓存key
     * @param func           函数，若此项为空，说明为自定义列
     * @param param          预编译的参数
     */
    public static void putSelectParam(Map<String, SelectParam> selectParamMap, String fieldName, String func, Object... param) {
        final SelectParam selectParam = selectParamMap.get(fieldName);//NOSONAR
        if (selectParam == null) {
            final SelectParam.Function function = SelectParam.functionBuilder().func(func).param(param).build();
            final SelectParam selectParam1 = new SelectParam();
            selectParam1.setFieldName(fieldName);
            final ArrayList<SelectParam.Function> functions = new ArrayList<>();
            functions.add(function);
            selectParam1.setFunctions(functions);
            selectParamMap.put(fieldName, selectParam1);
            return;
        }
        final List<SelectParam.Function> functions = selectParam.getFunctions();
        functions.add(SelectParam.functionBuilder().func(func).param(param).build());
    }

    public static void assembleQueryStatement(Select<?> select) {
        StringBuilder selectBuilder = new StringBuilder();
        selectBuilder.append("select ");
        final Map<String, SelectParam> selectParamMap = select.getSelectParamMap();
        final List<TableInfo> builderTableInfos = Check.getBuilderTableInfos(select.getResultClass(), false);
        final List<String> queryColumns = select.getQueryColumns().stream().distinct().collect(Collectors.toList());
        //先处理用户自定义的查询列
        for (String fieldName : queryColumns) {
            final TableInfo tableInfo = builderTableInfos.stream().filter(tInfo -> tInfo.getField().getName().equals(fieldName))
                    .findFirst().orElseGet(() -> {
                        final String columnName = checkColumn(fieldName);
                        //加入限定符，与集合中tableInfo进行比较
                        final String splicingColumnName = splicingName(builderTableInfos.get(0), columnName);
                        final TableInfo matchCusTableInfo = builderTableInfos.stream().filter(bti -> bti.getColumn().equalsIgnoreCase(splicingColumnName)).findAny()
                                .orElseThrow(() -> new BraveException("查询了不存在或已忽略的列！参考错误值：" + fieldName));
                        final String column = matchCusTableInfo.getColumn();
                        if (queryColumns.contains(matchCusTableInfo.getField().getName())) {
                            throw new BraveException("查询列和自定义列冲突！参考错误值：[" + fieldName + ", " + column + "]");
                        }
                        return matchCusTableInfo;
                    });
            if (StringUtils.isEmpty(tableInfo.getTableAlias())) {
                throw new BraveException("多表查询时需要指定字段别名");
            }
            final SelectParam selectParam = selectParamMap.get(fieldName);
            //为null表示为对字段进行特殊查询
            if (selectParam == null) {
                selectBuilder.append(assignmentRegular(tableInfo));
            }
            //赋值带有自定义函数的字段
            else if (selectParam.getFunctions().get(0).getFunc() == null) {
                selectBuilder.append(selectParam.getFieldName()).append(", ");
            } else {
                selectBuilder.append(assignmentFunction(tableInfo, selectParam));
            }
        }
        //用户如果没有查询全部的列，那就直接返回
        if (select.isSelectAll()) {
        }
        final List<TableInfo> tableInfoList = builderTableInfos.stream().filter(
                tableInfo -> !queryColumns.contains(tableInfo.getField().getName())).collect(Collectors.toList());

        select.setSelectSql(selectBuilder.toString());
    }

    /**
     * 校验用户自定义查询列表达式
     *
     * @return 检索出的列名
     */
    public static String checkColumn(String expr) {
        //判断自定义表达式
        final String trim = expr.trim();
        final String columnName = trim.substring(trim.lastIndexOf(" "));
        if (columnName.isEmpty()) {
            throw new BraveException("自定义查询列：" + expr + " 未指定别名");
        }
        return columnName;
    }

    /**
     * 赋值带有函数的字段
     */
    private static StringBuilder assignmentFunction(final TableInfo tableInfo, final SelectParam selectParam) {
        final StringBuilder prefixBuilder = new StringBuilder();
        final StringBuilder suffixBuilder = new StringBuilder();

        final List<SelectParam.Function> functions = selectParam.getFunctions();
        //根据调用顺序，反序决定函数优先级，最先调用的函数在最里层
        for (int i = functions.size() - 1; i >= 0; i--) {
            final SelectParam.Function function = functions.get(i);
            prefixBuilder.append(function.getFunc());
            prefixBuilder.append("(");
            if (i == 0) {
                setColumnName(tableInfo, prefixBuilder);
            }
            suffixBuilder.append(")");
            for (int i1 = 0; i1 < function.getParam().length; i1++) {
                suffixBuilder.append("?,");
            }
        }
        //这里需要反转，为对应的函数匹配占位符的位置
        prefixBuilder.append(suffixBuilder.reverse());
        prefixBuilder.append(" as ");
        prefixBuilder.append(tableInfo.getColumn());
        prefixBuilder.append(", ");
        return prefixBuilder;
    }

    /**
     * 赋值普通字段
     */
    private static StringBuilder assignmentRegular(final TableInfo tableInfo) {
        final StringBuilder columnBuilder = new StringBuilder();
        columnBuilder.append(splicingName(tableInfo, tableInfo.getTableAlias())).append(".");
        columnBuilder.append(tableInfo.getColumn());
        columnBuilder.append(", ");
        return columnBuilder;
    }


    private static void setColumnName(final TableInfo tableInfo, final StringBuilder columnBuilder) {
        final String tableAlias = tableInfo.getTableAlias();
        if (StringUtils.isEmpty(tableAlias)) {
            throw new BraveException("使用Select查询时，必须指定表别名");
        }
        final String alias = splicingName(tableInfo, tableAlias);
        columnBuilder.append(alias).append(".");
        columnBuilder.append(tableInfo.getColumn());
    }

    private static String splicingName(TableInfo tableInfo, String column) {
        final DbType dbType = ContextApplication.getDataSourceInfo(tableInfo.getDataSourceName()).getDbType();
        return Check.splicingName(dbType, column);
    }
}
