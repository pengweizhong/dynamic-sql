package com.pengwz.dynamic.sql.base;

@FunctionalInterface
public interface CustomSqlFunction<R, T, S extends CustomizeSQL<T>> {
    R apply(S sqls) throws Exception;//NOSONAR
}
