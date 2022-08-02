package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.SelectParam;
import com.pengwz.dynamic.model.TableInfo;
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
            final ArrayList<Integer> indices = new ArrayList<>();
            for (int i = functions.size() - 1; i >= 0; i--) {
                final SelectParam.Function function = functions.get(i);
                selectBuilder.append(function.getFunc());
                selectBuilder.append("(");
                for (Object param : function.getParam()) {
                    select.getParams().add(param);
                    indices.add(selectBuilder.length());
                }
            }
            selectBuilder.append(tableInfo.getColumn());
            for (Integer index : indices) {
//                selectBuilder.insert(index, ",?");
            }
            selectBuilder.append(repeatString(")", functions.size()));
            selectBuilder.append(" ");
        });
        select.setSelectSql(selectBuilder.toString());
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
        for (int i = 0; i < repeat; i++) {
            str += str;
        }
        return str;
    }
}
