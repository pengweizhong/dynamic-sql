package com.pengwz.dynamic.sql.base.impl;

import com.pengwz.dynamic.sql.Declaration;
import com.pengwz.dynamic.sql.ParseSql;
import com.pengwz.dynamic.sql.base.HandleFunction;

import static com.pengwz.dynamic.constant.Constant.MIN;

/**
 * 聚合函数实现类
 */
public class Min implements HandleFunction {
    @Override
    public String execute(String tableName, Declaration declaration) {
        return ParseSql.parseAggregateFunction(MIN, tableName, declaration);
    }
}
