package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.sql.base.Fn;

public class MultiBraveSql {
    private DynamicSql<?> dynamicSql;

    private Class<?> currentClass;

    protected MultiBraveSql(DynamicSql<?> dynamicSql, Class<?> currentClass) {
        this.dynamicSql = dynamicSql;
        this.currentClass = currentClass;
    }

    public static <T> MultiBraveSql builder(Class<T> currentClass) {
        return new MultiBraveSql(DynamicSql.createDynamicSql(), currentClass);
    }

    public DynamicSql<?> getDynamicSql() {
        return dynamicSql;
    }

    public Class<?> getCurrentClass() {
        return currentClass;
    }


    public MultiBraveSql alias(String alias) {
        return this;
    }

    public <R> JoinCondition join(Class<R> join) {
        return new JoinCondition(this, join);
    }

    public <T> BraveSql<T> receiveResult(Class<T> resultClass) {
        return null;
    }

    public static class JoinCondition {

        private Class<?> joinEntity;

        private MultiBraveSql multiBraveSql;

        protected JoinCondition(MultiBraveSql multiBraveSql, Class<?> join) {
            this.multiBraveSql = multiBraveSql;
            joinEntity = join;
        }

        public <R> JoinCondition join(Class<R> join) {
            return this;
        }

        public <T> OnJoinCondition on(Fn<T, Object> fn) {
            return new OnJoinCondition(this);
        }

        public JoinCondition alias(String alias) {
            return this;
        }


        public MultiBraveSql build() {
            return null;
        }

        public <T> JoinCondition where(DynamicSql<T> dynamicSql) {
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
        private JoinCondition joinCondition;
        private OnJoinCondition onJoinCondition;

        protected OnJoinMultiCondition(JoinCondition joinCondition, OnJoinCondition onJoinCondition) {
            this.joinCondition = joinCondition;
            this.onJoinCondition = onJoinCondition;
        }

        public <O> OnJoinMultiCondition andEqualTo(Fn<O, Object> fn, Object value) {
            return this;
        }

        public JoinCondition end() {
            return joinCondition;
        }
    }
}
