package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.sql.base.Fn;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class MultiBraveSql<R> {

    private Class<R> resultClass;

    private MultiBraveSql(Class<R> resultClass) {
        this.resultClass = resultClass;
    }

    protected static <R> MultiBraveSqlBuilder<R> builder(Class<?> tableClass, Class<R> resultClass) {
        MultiBraveSql<R> multiBraveSql = new MultiBraveSql<>(resultClass);
        return new MultiBraveSqlBuilder<>(multiBraveSql, tableClass);
    }


    /**
     * query dataBase source
     */

    public List<R> select() {
        return Collections.emptyList();
    }

    public static class MultiBraveSqlBuilder<R> {
        private MultiBraveSql<R> multiBraveSql;
        private DynamicSql<?> dynamicSql;
        private Class<?> tableClass;

        protected MultiBraveSqlBuilder(MultiBraveSql<R> multiBraveSql, Class<?> tableClass) {
            this.multiBraveSql = multiBraveSql;
            this.tableClass = tableClass;
        }

        public MultiBraveSqlBuilder<R> where(DynamicSql<R> dynamicSql) {
            return this;
        }

        public MultiBraveSqlBuilder<R> where(Supplier<DynamicSql<R>> dynamicSql) {
            return this;
        }


        public JoinCondition<R> join(Class<?> join) {
            return new JoinCondition<>(this, join);
        }

        public MultiBraveSql<R> build() {
            return multiBraveSql;
        }
    }


    public static class JoinCondition<R> {

        private Class<?> joinEntity;

        private MultiBraveSqlBuilder<?> multiBraveSqlBuilder;

        protected JoinCondition(MultiBraveSqlBuilder<?> multiBraveSqlBuilder, Class<?> join) {
            this.multiBraveSqlBuilder = multiBraveSqlBuilder;
            joinEntity = join;
        }

        public JoinCondition<R> join(Class<?> join) {
            return this;
        }

        public <T> OnJoinCondition on(Fn<T, Object> fn) {
            return new OnJoinCondition(this);
        }

        public JoinCondition<R> as(String alias) {
            return this;
        }


        public MultiBraveSql<R> build() {
            return null;
        }

        public JoinCondition<R> where(DynamicSql<R> dynamicSql) {
            return this;
        }

        public JoinCondition<R> where(Supplier<DynamicSql<R>> sqlSupplier) {
            multiBraveSqlBuilder.dynamicSql = sqlSupplier.get();
            return this;
        }

    }

    public static class OnJoinCondition {
        private JoinCondition joinCondition;

        protected OnJoinCondition(JoinCondition joinCondition) {
            this.joinCondition = joinCondition;
        }

        public <O> OnJoinMultiCondition equalTo(Fn<O, Object> fn) {
            return new OnJoinMultiCondition(joinCondition, this);
        }

        public JoinCondition end() {
            return joinCondition;
        }
    }

    public static class OnJoinMultiCondition {
        private JoinCondition<?> joinCondition;
        private OnJoinCondition onJoinCondition;

        protected OnJoinMultiCondition(JoinCondition<?> joinCondition, OnJoinCondition onJoinCondition) {
            this.joinCondition = joinCondition;
            this.onJoinCondition = onJoinCondition;
        }

        public <O> OnJoinMultiCondition andEqualTo(Fn<O, Object> fn, Object value) {
            return this;
        }

        public <R> JoinCondition<R> end() {
            return (JoinCondition<R>) joinCondition;
        }
    }
}
