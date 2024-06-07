package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.sql.base.impl.SqlImpl;
import com.pengwz.dynamic.utils.ExceptionUtils;

import java.sql.Connection;


public class ExecuteSql<T> {

    private ExecuteSql(SqlImpl<T> sqls) {
        this.sqls = sqls;
    }

    private final SqlImpl<T> sqls;

    public static <T> ExecuteSql<T> instance(SqlImpl<T> sqls) {
        return new ExecuteSql<>(sqls);
    }

    public <R> R execute(SqlFunction<R, T, SqlImpl<T>> option) {
        AbstractAccessor abstractAccessor = sqls;
//        PreparedSql preparedSql = abstractAccessor.getPreparedSql();
//        InterceptorHelper interceptorHelper = new InterceptorHelper(preparedSql);
        R result = null;
        try {
            Connection connection = DataSourceManagement.initConnection(abstractAccessor.getDataSourceName());
            abstractAccessor.setConnection(connection);
            result = option.apply(sqls);
        } catch (Exception e) {
            ExceptionUtils.boxingAndThrowBraveException(e);
        } finally {
            DataSourceManagement.close(abstractAccessor.getDataSourceName(), abstractAccessor.getResultSet(),
                    abstractAccessor.getStatement(), abstractAccessor.getConnection());
//            interceptorHelper.transferAfter(e, preparedSql.getSql());
        }
        return result;
    }
}
