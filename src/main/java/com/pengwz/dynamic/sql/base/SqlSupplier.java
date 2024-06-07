package com.pengwz.dynamic.sql.base;

@FunctionalInterface
public interface SqlSupplier<T> {
    T get();
}
