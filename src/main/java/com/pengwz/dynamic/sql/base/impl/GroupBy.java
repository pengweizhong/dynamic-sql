package com.pengwz.dynamic.sql.base.impl;

import com.pengwz.dynamic.sql.Declaration;
import com.pengwz.dynamic.sql.ParseSql;
import com.pengwz.dynamic.sql.base.HandleFunction;

import static com.pengwz.dynamic.constant.Constant.GROUP;

public class GroupBy implements HandleFunction {
    @Override
    public String execute(Class<?> tableClass, Declaration declaration) {
        return ParseSql.parseAggregateFunction(GROUP, tableClass, declaration);
    }
}
