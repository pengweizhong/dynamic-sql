package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.sql.base.Fn;
import com.pengwz.dynamic.sql.base.impl.GroupBy;
import com.pengwz.dynamic.sql.base.impl.Max;
import com.pengwz.dynamic.sql.base.impl.Min;
import com.pengwz.dynamic.sql.base.impl.OrderBy;
import com.pengwz.dynamic.utils.ReflectUtils;

import java.util.ArrayList;
import java.util.List;

import static com.pengwz.dynamic.constant.Constant.*;

/**
 * 该类主要用于设置sql的条件语句
 *
 * @param <T>
 */
public class DynamicSql<T> {

    private DynamicSql() {
    }

    private List<Declaration> declarations = new ArrayList<>();

    private List<String> updateNullProperties = new ArrayList<>();

    private OrderByMode<T> orderByMode = new OrderByMode<T>(declarations);

    public List<Declaration> getDeclarations() {
        return declarations;
    }

    public List<String> getUpdateNullProperties() {
        return updateNullProperties;
    }

    /**
     * 提供创建where子句的入口  比如查询，更新等
     */
    public static <T> DynamicSql<T> createDynamicSql() {
        return new DynamicSql<>();
    }

    public DynamicSql<T> startBrackets() {
        this.getDeclarations().add(Declaration.buildDeclaration(LEFT_BRACKETS));
        return this;
    }

    public DynamicSql<T> endBrackets() {
        this.getDeclarations().add(Declaration.buildDeclaration(RIGHT_BRACKETS));
        return this;
    }

    /**
     * 当需要更新字符串为null时
     * 该方法仅对 {@code updateActive(T data)} 语句生效
     * 其实这个方法写在此类中是不合适的，后期考虑优化他
     */
    public DynamicSql<T> setNullColumnByUpdate(Fn<T, Object> fn) {
        String fieldName = ReflectUtils.fnToFieldName(fn);
        this.updateNullProperties.add(fieldName);
        return this;
    }

    public DynamicSql<T> andEqualTo(Fn<T, Object> fn, Object value) {
        return this.andEqualTo(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> andEqualTo(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, EQ, value));
        return this;
    }

    public DynamicSql<T> orEqualTo(Fn<T, Object> fn, Object value) {
        return this.orEqualTo(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> orEqualTo(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, EQ, value));
        return this;
    }

    public DynamicSql<T> andNotEqualTo(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, NEQ, value));
        return this;
    }

    public DynamicSql<T> andNotEqualTo(Fn<T, Object> fn, Object value) {
        return this.andNotEqualTo(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> orNotEqualTo(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, NEQ, value));
        return this;
    }

    public DynamicSql<T> orNotEqualTo(Fn<T, Object> fn, Object value) {
        return this.orNotEqualTo(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> andIsNull(String property) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, "is null"));
        return this;
    }

    public DynamicSql<T> andIsNull(Fn<T, Object> fn) {
        return this.andIsNull(ReflectUtils.fnToFieldName(fn));
    }

    public DynamicSql<T> orIsNull(String property) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, "is null"));
        return this;
    }

    public DynamicSql<T> orIsNull(Fn<T, Object> fn) {
        return this.orIsNull(ReflectUtils.fnToFieldName(fn));
    }

    public DynamicSql<T> andIsNotNull(String property) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, "is not null"));
        return this;
    }

    public DynamicSql<T> andIsNotNull(Fn<T, Object> fn) {
        return this.andIsNotNull(ReflectUtils.fnToFieldName(fn));
    }

    public DynamicSql<T> orIsNotNull(String property) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, "is not null"));
        return this;
    }

    public DynamicSql<T> orIsNotNull(Fn<T, Object> fn) {
        return this.orIsNotNull(ReflectUtils.fnToFieldName(fn));
    }

    public DynamicSql<T> andGreaterThan(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, GT, value));
        return this;
    }

    public DynamicSql<T> andGreaterThan(Fn<T, Object> fn, Object value) {
        return this.andGreaterThan(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> orGreaterThan(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, GT, value));
        return this;
    }

    public DynamicSql<T> orGreaterThan(Fn<T, Object> fn, Object value) {
        return this.orGreaterThan(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> andGreaterThanOrEqualTo(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, GTE, value));
        return this;
    }

    public DynamicSql<T> andGreaterThanOrEqualTo(Fn<T, Object> fn, Object value) {
        return this.andGreaterThanOrEqualTo(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> orGreaterThanOrEqualTo(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, GTE, value));
        return this;
    }

    public DynamicSql<T> orGreaterThanOrEqualTo(Fn<T, Object> fn, Object value) {
        return this.orGreaterThanOrEqualTo(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> andLessThan(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, LT, value));
        return this;
    }

    public DynamicSql<T> andLessThan(Fn<T, Object> fn, Object value) {
        return this.andLessThan(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> orLessThan(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, LT, value));
        return this;
    }

    public DynamicSql<T> orLessThan(Fn<T, Object> fn, Object value) {
        return this.orLessThan(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> andLessThanOrEqualTo(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, LTE, value));
        return this;
    }

    public DynamicSql<T> andLessThanOrEqualTo(Fn<T, Object> fn, Object value) {
        return this.andLessThanOrEqualTo(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> orLessThanOrEqualTo(String property, Object value) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, LTE, value));
        return this;
    }

    public DynamicSql<T> orLessThanOrEqualTo(Fn<T, Object> fn, Object value) {
        return this.orLessThanOrEqualTo(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> andIn(String property, Iterable values) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, IN, values));
        return this;
    }

    public DynamicSql<T> andIn(Fn<T, Object> fn, Iterable values) {
        return this.andIn(ReflectUtils.fnToFieldName(fn), values);
    }

    public DynamicSql<T> orIn(String property, Iterable values) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, IN, values));
        return this;
    }

    public DynamicSql<T> orIn(Fn<T, Object> fn, Iterable values) {
        return this.andIn(ReflectUtils.fnToFieldName(fn), values);
    }

    public DynamicSql<T> andNotIn(String property, Iterable values) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, NOT_IN, values));
        return this;
    }

    public DynamicSql<T> andNotIn(Fn<T, Object> fn, Iterable values) {
        return this.andNotIn(ReflectUtils.fnToFieldName(fn), values);
    }

    public DynamicSql<T> orNotIn(String property, Iterable values) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, NOT_IN, values));
        return this;
    }

    public DynamicSql<T> orNotIn(Fn<T, Object> fn, Iterable values) {
        return this.andNotIn(ReflectUtils.fnToFieldName(fn), values);
    }

    public DynamicSql<T> andBetween(String property, Object value1, Object value2) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, BETWEEN, value1, value2));
        return this;
    }

    public DynamicSql<T> andBetween(Fn<T, Object> fn, Object value1, Object value2) {
        return this.andBetween(ReflectUtils.fnToFieldName(fn), value1, value2);
    }

    public DynamicSql<T> orBetween(String property, Object value1, Object value2) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, BETWEEN, value1, value2));
        return this;
    }

    public DynamicSql<T> orBetween(Fn<T, Object> fn, Object value1, Object value2) {
        return this.orBetween(ReflectUtils.fnToFieldName(fn), value1, value2);
    }

    public DynamicSql<T> andNotBetween(String property, Object value1, Object value2) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, NOT_BETWEEN, value1, value2));
        return this;
    }

    public DynamicSql<T> andNotBetween(Fn<T, Object> fn, Object value1, Object value2) {
        return this.andNotBetween(ReflectUtils.fnToFieldName(fn), value1, value2);
    }

    public DynamicSql<T> orNotBetween(String property, Object value1, Object value2) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, NOT_BETWEEN, value1, value2));
        return this;
    }

    public DynamicSql<T> orNotBetween(Fn<T, Object> fn, Object value1, Object value2) {
        return this.orNotBetween(ReflectUtils.fnToFieldName(fn), value1, value2);
    }

    public DynamicSql<T> andLike(String property, String value) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, LIKE, value));
        return this;
    }

    public DynamicSql<T> andLike(Fn<T, Object> fn, String value) {
        return this.andLike(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> orLike(String property, String value) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, LIKE, value));
        return this;
    }

    public DynamicSql<T> orLike(Fn<T, Object> fn, String value) {
        return this.orLike(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> andNotLike(String property, String value) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, NOT_LIKE, value));
        return this;
    }

    public DynamicSql<T> andNotLike(Fn<T, Object> fn, String value) {
        return this.andNotLike(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> orNotLike(String property, String value) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, NOT_LIKE, value));
        return this;
    }

    public DynamicSql<T> orNotLike(Fn<T, Object> fn, String value) {
        return this.andNotLike(ReflectUtils.fnToFieldName(fn), value);
    }

    public DynamicSql<T> andMin(String property) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, new Min()));
        return this;
    }

    public DynamicSql<T> andMin(Fn<T, Object> fn) {
        return this.andMin(ReflectUtils.fnToFieldName(fn));
    }

    public DynamicSql<T> orMin(String property) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, new Min()));
        return this;
    }

    public DynamicSql<T> orMin(Fn<T, Object> fn) {
        return this.orMin(ReflectUtils.fnToFieldName(fn));
    }

    public DynamicSql<T> andMax(String property) {
        this.getDeclarations().add(Declaration.buildDeclaration(AND, property, new Max()));
        return this;
    }

    public DynamicSql<T> andMax(Fn<T, Object> fn) {
        return this.andMax(ReflectUtils.fnToFieldName(fn));
    }

    public DynamicSql<T> orMax(String property) {
        this.getDeclarations().add(Declaration.buildDeclaration(OR, property, new Max()));
        return this;
    }

    public DynamicSql<T> orMax(Fn<T, Object> fn) {
        return this.orMax(ReflectUtils.fnToFieldName(fn));
    }

    public void groupBy(List<Fn<T, Object>> fn) {
        List<String> list = new ArrayList<>();
        for (Fn<T, Object> f : fn) {
            String s = ReflectUtils.fnToFieldName(f);
            list.add(s);
        }
        this.getDeclarations().add(Declaration.buildDeclaration(GROUP, String.join(",", list), new GroupBy()));
    }

    public void groupBy(Fn<T, Object> fn) {
        String s = ReflectUtils.fnToFieldName(fn);
        this.getDeclarations().add(Declaration.buildDeclaration(GROUP, s, new GroupBy()));
    }

    public void groupBy(String... personalCode) {
        this.getDeclarations().add(Declaration.buildDeclaration(GROUP, String.join(",", personalCode), new GroupBy()));
    }

    public OrderByMode<T> orderByDesc(String property) {
        this.getDeclarations().add(Declaration.buildDeclaration(ORDER, property, new OrderBy("desc")));
        return orderByMode;
    }

    public OrderByMode<T> orderByDesc(Fn<T, Object> fn) {
        this.getDeclarations().add(Declaration.buildDeclaration(ORDER, ReflectUtils.fnToFieldName(fn), new OrderBy("desc")));
        return orderByMode;
    }

    public OrderByMode<T> orderByAsc(String property) {
        this.getDeclarations().add(Declaration.buildDeclaration(ORDER, property, new OrderBy("asc")));
        return orderByMode;
    }

    public OrderByMode<T> orderByAsc(Fn<T, Object> fn) {
        this.getDeclarations().add(Declaration.buildDeclaration(ORDER, ReflectUtils.fnToFieldName(fn), new OrderBy("asc")));
        return orderByMode;
    }

}