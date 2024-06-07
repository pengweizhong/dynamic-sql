package com.pengwz.dynamic.sql.base;

@FunctionalInterface
public interface CustomConsumer<T> {
    void accept(T t) throws Exception;
}
