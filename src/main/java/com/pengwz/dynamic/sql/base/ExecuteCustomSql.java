package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.utils.ExceptionUtils;

import java.sql.Connection;


public class ExecuteCustomSql<T> {

    private ExecuteCustomSql(CustomizeSQL<T> sqls) {
        this.sqls = sqls;
    }

    private final CustomizeSQL<T> sqls;

    public static <T> ExecuteCustomSql<T> instance(CustomizeSQL<T> sqls) {
        return new ExecuteCustomSql<>(sqls);
    }

    public <R> R execute(CustomSqlFunction<R, T, CustomizeSQL<T>> option) {
        AbstractAccessor abstractAccessor = sqls;
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
        }
        return result;
    }

    public void justExecute(CustomConsumer<CustomizeSQL<T>> option) {
        AbstractAccessor abstractAccessor = sqls;
        try {
            Connection connection = DataSourceManagement.initConnection(abstractAccessor.getDataSourceName());
            abstractAccessor.setConnection(connection);
            option.accept(sqls);
        } catch (Exception e) {
            ExceptionUtils.boxingAndThrowBraveException(e);
        } finally {
            DataSourceManagement.close(abstractAccessor.getDataSourceName(), abstractAccessor.getResultSet(),
                    abstractAccessor.getStatement(), abstractAccessor.getConnection());
        }
    }
}
