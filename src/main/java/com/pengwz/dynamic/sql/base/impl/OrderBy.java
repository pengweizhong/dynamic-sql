package com.pengwz.dynamic.sql.base.impl;

import com.pengwz.dynamic.sql.Declaration;
import com.pengwz.dynamic.sql.ParseSql;
import com.pengwz.dynamic.sql.base.HandleFunction;

import static com.pengwz.dynamic.constant.Constant.GROUP;
import static com.pengwz.dynamic.constant.Constant.ORDER;

public class OrderBy implements HandleFunction {
    private String sortMode;

    @Override
    public String execute(String dataSource, String tableName, Declaration declaration) {
        declaration.setSortMode(sortMode);
        return ParseSql.parseAggregateFunction(ORDER, dataSource, tableName, declaration);
    }

    public OrderBy() {
    }

    public OrderBy(String sortMode) {
        this.sortMode = sortMode;
    }
}
