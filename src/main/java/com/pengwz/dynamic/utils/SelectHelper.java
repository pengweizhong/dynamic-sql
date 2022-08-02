package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DbType;
import com.pengwz.dynamic.model.SelectParam;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.sql.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectHelper {
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
        selectParamMap.forEach((fieldName, selectParam) -> {
            final TableInfo tableInfo = builderTableInfos.stream().filter(tInfo -> tInfo.getField().getName().equals(fieldName))
                    .findFirst().orElseThrow(() -> new BraveException("未被查询的字段：" + fieldName));
            // column(SystemDTO::getRoleName).left(1).repeat(2).trim().end()
            final List<SelectParam.Function> functions = selectParam.getFunctions();
            final StringBuilder columnBuilder = new StringBuilder();
            for (int i = functions.size() - 1; i >= 0; i--) {
                final SelectParam.Function function = functions.get(i);
                columnBuilder.append(function.getFunc());
                columnBuilder.append("(");
                if (i == 0) {
                    setColumnName(tableInfo, columnBuilder);
                }
            }
            columnBuilder.append(repeatString(")", functions.size()));
            columnBuilder.append(" as ").append(tableInfo.getColumn());
            columnBuilder.append(", ");
            //插入占位符
            for (int i = 0; i < functions.size(); i++) {
                final SelectParam.Function function = functions.get(i);
                if (function.getParam().length == 0) {
                    continue;
                }
                //找到对应的位置
                final int index = columnBuilder.indexOf(")") + i;
                final int indexOf = columnBuilder.indexOf(")", index);
                columnBuilder.insert(indexOf, repeatString(",?", function.getParam().length));
            }
            System.out.println(columnBuilder);
            selectBuilder.append(columnBuilder);
        });
        select.setSelectSql(selectBuilder.toString());
    }

    private static void setColumnName(final TableInfo tableInfo, final StringBuilder columnBuilder) {
        final String tableAlias = tableInfo.getTableAlias();
        if (StringUtils.isEmpty(tableAlias)) {
            throw new BraveException("使用Select查询时，必须指定表别名");
        }
        final DbType dbType = ContextApplication.getDataSourceInfo(tableInfo.getDataSourceName()).getDbType();
        final String alias = Check.splicingName(dbType, tableAlias);
        columnBuilder.append(alias).append(".");
        columnBuilder.append(tableInfo.getColumn());
    }

    private static String generatePlaceholders(int repeat) {
        if (repeat < 1) {
            return "";
        }
        final ArrayList<String> placeholders = new ArrayList<>();
        for (int i = 0; i < repeat; i++) {
            placeholders.add(",?");
        }
        return String.join(",", placeholders);
    }

    private static String repeatString(String str, int repeat) {
        if (repeat <= 1) {
            return str;
        }
        String string = str;
        for (int i = 1; i < repeat; i++) {
            string = string + str;
        }
        return string;
    }
}
