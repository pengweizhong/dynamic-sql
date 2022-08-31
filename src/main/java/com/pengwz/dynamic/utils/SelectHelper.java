package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DbType;
import com.pengwz.dynamic.model.SelectParam;
import com.pengwz.dynamic.model.TableColumnInfo;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.Select;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

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
        final List<SelectParam.Function> functions = Optional.ofNullable(selectParam.getFunctions()).orElseGet(ArrayList::new);
        functions.add(SelectParam.functionBuilder().func(func).param(param).build());
        selectParam.setFunctions(functions);
    }

    public static void putSelectParam(Map<String, SelectParam> selectParamMap, String fieldName, SelectParam selectParam) {
        if (selectParamMap.get(fieldName) != null) {
            selectParamMap.remove(fieldName);
        }
        selectParamMap.put(fieldName, selectParam);
    }

    public static void assembleQueryStatement(Select<?> select) {
        StringBuilder selectBuilder = new StringBuilder();
        selectBuilder.append("select ");
        final Map<String, SelectParam> selectParamMap = select.getSelectParamMap();
        final Set<String> queryColumns = selectParamMap.keySet();
        final TableInfo tableInfo = Check.getBuilderTableInfo(select.getResultClass(), Check.ViewType.RESULT);
        final LinkedHashSet<String> queryAllFieldNames = new LinkedHashSet<>();
        //先处理用户自定义的查询列
        for (String fieldName : queryColumns) {
            final TableColumnInfo tableColumnInfo = tableInfo.getTableColumnInfos().stream().filter(tci -> tci.getField().getName().equals(fieldName))
                    .findFirst().orElseGet(() -> {
                        final String columnName = getColumn(fieldName);
                        //加入限定符，与集合中tableInfo进行比较
                        final String splicingColumnName = splicingName(tableInfo.getDataSourceName(), columnName);
                        final TableColumnInfo matchTableColumnInfo = tableInfo.getTableColumnInfos().stream().filter(bti -> bti.getColumn().equalsIgnoreCase(splicingColumnName)).findAny()
                                .orElseThrow(() -> new BraveException("查询了不存在或已忽略的列！参考错误值：" + fieldName));
                        final String column = matchTableColumnInfo.getColumn();
                        if (queryColumns.contains(matchTableColumnInfo.getField().getName())) {
                            throw new BraveException("查询列和自定义列冲突！参考错误值：[" + fieldName + ", " + column + "]");
                        }
                        queryAllFieldNames.add(matchTableColumnInfo.getField().getName());
                        return matchTableColumnInfo;
                    });
            queryAllFieldNames.add(tableColumnInfo.getField().getName());
            if (StringUtils.isEmpty(tableColumnInfo.getTableAlias())) {
                throw new BraveException("多表查询时需要指定字段别名");
            }
            final SelectParam selectParam = selectParamMap.get(fieldName);
            //赋值自定义列
            if (selectParam.isCustomColumn()) {
                selectBuilder.append(selectParam.getFieldName()).append(", ");
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
        select.setSelectSql(selectBuilder.toString());
    }

    /**
     * 校验用户自定义查询列表达式
     *
     * @return 检索出的列名
     */
    public static String getColumn(String expr) {
        //判断自定义表达式
        final String trim = expr.trim();
        final String columnName = trim.substring(trim.lastIndexOf(" ") + 1);
        if (columnName.isEmpty()) {
            throw new BraveException("自定义查询列：" + expr + " 未指定别名");
        }
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
            for (int i1 = 0; i1 < function.getParam().length; i1++) {
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
        columnBuilder.append(splicingName(datasource, tableColumnInfo.getTableAlias())).append(".");
        columnBuilder.append(tableColumnInfo.getColumn());
        columnBuilder.append(", ");
        return columnBuilder;
    }


    private static void setColumnName(final TableColumnInfo tableColumnInfo, final StringBuilder columnBuilder, String datasource) {
        final String tableAlias = tableColumnInfo.getTableAlias();
        if (StringUtils.isEmpty(tableAlias)) {
            throw new BraveException("使用Select查询时，必须指定表别名");
        }
        final String alias = splicingName(datasource, tableAlias);
        columnBuilder.append(alias).append(".");
        columnBuilder.append(tableColumnInfo.getColumn());
    }

    private static String splicingName(String datasource, String column) {
        final DbType dbType = ContextApplication.getDataSourceInfo(datasource).getDbType();
        return Check.splicingName(dbType, column);
    }
}
