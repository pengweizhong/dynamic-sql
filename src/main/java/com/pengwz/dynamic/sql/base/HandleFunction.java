package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.sql.Declaration;

public interface HandleFunction {

    String execute(String dataSource, String tableName, Declaration declaration);
}
