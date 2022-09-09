package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.model.TableColumnInfo;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.sql.base.Fn;
import com.pengwz.dynamic.utils.ReflectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.pengwz.dynamic.constant.Constant.*;

public class MultiBraveSql<R> {
    //sql结果映射对象
    private Class<R> resultClass;
    //查询的sql语句
    private StringBuilder selectSql;

    protected static <R> MultiBraveSqlBuilder<R> builder(Class<?> tableClass, Class<R> resultClass, StringBuilder selectSql) {
        MultiBraveSql<R> multiBraveSql = new MultiBraveSql<>();
        multiBraveSql.resultClass = resultClass;
        multiBraveSql.selectSql = selectSql;
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
        //TODO join表时加入此属性  并判断条件类是否存在
        private final Map<String, Class<?>> joinClassMap = new ConcurrentHashMap<>();

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


        public JoinCondition<R> join(Class<?> tableClass) {
            final TableInfo tableInfo = ContextApplication.getTableInfo(tableClass);
            multiBraveSql.selectSql.append(SPACE).append(JOIN).append(SPACE).append(tableInfo.getTableName());
            return new JoinCondition<>(this, tableClass);
        }

        public MultiBraveSql<R> build() {
            return multiBraveSql;
        }

        public static class AsMultiBraveSqlBuilder<R> {
            private Class<?> join;
            private MultiBraveSqlBuilder<R> multiBraveSqlBuilder;

            private AsMultiBraveSqlBuilder(MultiBraveSqlBuilder<R> multiBraveSqlBuilder, Class<?> join) {
                this.multiBraveSqlBuilder = multiBraveSqlBuilder;
                this.join = join;
            }

//            public JoinCondition<R> as(String alias) {
//                return new JoinCondition<>(multiBraveSqlBuilder, join);
//            }
        }

    }


//    public static class As<R> {
//        private Class<R> resultClass;
//        private Class<?> tableClass;
//
//        protected As(Class<?> tableClass, Class<R> resultClass) {
//            this.resultClass = resultClass;
//            this.tableClass = tableClass;
//        }
//
//        protected MultiBraveSqlBuilder<R> multiBraveSqlBuilder() {
//            return MultiBraveSql.builder(tableClass, resultClass);
//        }
//
//        public MultiBraveSqlBuilder<R> as(String alias) {
//            return MultiBraveSql.builder(tableClass, resultClass);
//        }
//
//    }

    public static class JoinCondition<R> {

        private Class<?> joinTableClass;

        private MultiBraveSqlBuilder<?> multiBraveSqlBuilder;

        protected JoinCondition(MultiBraveSqlBuilder<?> multiBraveSqlBuilder, Class<?> joinTableClass) {
            this.multiBraveSqlBuilder = multiBraveSqlBuilder;
            this.joinTableClass = joinTableClass;
        }

        public <T> OnJoinCondition on(Fn<T, Object> fn) {
            final String fieldName = ReflectUtils.fnToFieldName(fn);
            final TableColumnInfo tableColumnInfo = ContextApplication.getTableColumnInfo(joinTableClass, fieldName);
            final TableInfo tableInfo = ContextApplication.getTableInfo(joinTableClass);
            multiBraveSqlBuilder.multiBraveSql.selectSql.append(SPACE).append(ON).append(SPACE)
                    .append(tableInfo.getTableName()).append(POINT).append(tableColumnInfo.getColumn());
            return new OnJoinCondition(this);
        }

        public JoinCondition<R> join(Class<?> joinTableClass) {
            final TableInfo tableInfo = ContextApplication.getTableInfo(joinTableClass);
            multiBraveSqlBuilder.multiBraveSql.selectSql.append(SPACE).append(JOIN).append(SPACE).append(tableInfo.getTableName());
            return this;
        }


//        public JoinCondition<R> as(String alias) {
//            return this;
//        }


//        public MultiBraveSql<R> build() {
//            return null;
//        }

        public <T> JoinCondition<R> where(DynamicSql<T> dynamicSql) {
            return this;
        }

        public <T> JoinCondition<R> where(Supplier<DynamicSql<T>> sqlSupplier) {
            multiBraveSqlBuilder.dynamicSql = sqlSupplier.get();
            return this;
        }

    }

    public static class OnJoinCondition {
        private final JoinCondition<?> joinCondition;

        protected OnJoinCondition(JoinCondition<?> joinCondition) {
            this.joinCondition = joinCondition;
        }

        public <O> OnJoinMultiCondition equalTo(Fn<O, Object> fn) {
            final String fieldName = ReflectUtils.fnToFieldName(fn);
            final TableColumnInfo tableColumnInfo = ContextApplication.getTableColumnInfo(joinCondition.joinTableClass, fieldName);
            final TableInfo tableInfo = ContextApplication.getTableInfo(joinCondition.joinTableClass);
            joinCondition.multiBraveSqlBuilder.multiBraveSql.selectSql.append(SPACE).append(EQ).append(SPACE)
                    .append(tableInfo.getTableName()).append(POINT).append(tableColumnInfo.getColumn());
            return new OnJoinMultiCondition(joinCondition, this);
        }

//        public JoinCondition end() {
//            return joinCondition;
//        }
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

        public <O> OnJoinMultiCondition andIsNull(Fn<O, Object> fn) {
            return this;
        }

        public <O> OnJoinMultiCondition andIsNotNull(Fn<O, Object> fn) {
            final String fieldName = ReflectUtils.fnToFieldName(fn);

            final TableColumnInfo tableColumnInfo = ContextApplication.getTableColumnInfo(joinCondition.joinTableClass, fieldName);
            final TableInfo tableInfo = ContextApplication.getTableInfo(joinCondition.joinTableClass);
            joinCondition.multiBraveSqlBuilder.multiBraveSql.selectSql.append(SPACE).append(AND).append(SPACE)
                    .append(tableInfo.getTableName()).append(POINT).append(tableColumnInfo.getColumn()).append(SPACE).append("is not null");
            return this;
        }

        /**
         * 结束当前表的约束条件
         *
         * @param <R>
         * @return
         */
        public <R> JoinCondition<R> end() {
            return (JoinCondition<R>) joinCondition;
        }
    }
}
