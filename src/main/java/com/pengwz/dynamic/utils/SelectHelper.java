package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.model.SelectParam;
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
        select.setSelectSql("asdadasdasdasd");
        System.out.println(select);
    }
}
