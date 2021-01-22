package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.sql.base.Fn;
import com.pengwz.dynamic.sql.base.impl.OrderBy;
import com.pengwz.dynamic.utils.ReflectUtils;

import java.util.List;

import static com.pengwz.dynamic.constant.Constant.ORDER;

public class OrderByMode<T> {
    private List<Declaration> declarations;

    public OrderByMode(List<Declaration> declarations) {
        this.declarations = declarations;
    }

    public OrderByMode() {
    }

    public OrderByMode<T> thenOrderByDesc(String property) {
        declarations.add(Declaration.buildDeclaration(ORDER, property, new OrderBy("desc")));
        return this;
    }

    public OrderByMode<T> thenOrderByDesc(Fn<T, Object> fn) {
        declarations.add(Declaration.buildDeclaration(ORDER, ReflectUtils.fnToFieldName(fn), new OrderBy("desc")));
        return this;
    }

    public OrderByMode<T> thenOrderByAsc(String property) {
        declarations.add(Declaration.buildDeclaration(ORDER, property, new OrderBy("asc")));
        return this;
    }

    public OrderByMode<T> thenOrderByAsc(Fn<T, Object> fn) {
        declarations.add(Declaration.buildDeclaration(ORDER, ReflectUtils.fnToFieldName(fn), new OrderBy("asc")));
        return this;
    }
}
