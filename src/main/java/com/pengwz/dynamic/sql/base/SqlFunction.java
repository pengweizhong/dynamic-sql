package com.pengwz.dynamic.sql.base;

@FunctionalInterface
public interface SqlFunction<R, T, S extends Sqls<T>> {
    R apply(S sqls) throws Exception;//NOSONAR
}
