package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.interceptor.SQLInterceptor;
import com.pengwz.dynamic.sql.PreparedSql;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class InterceptorHelper {

    private static final AtomicReference<SQLInterceptor> sqlInterceptorReference = new AtomicReference<>();
    private final PreparedSql preparedSql;

    public InterceptorHelper(PreparedSql preparedSql) {
        this.preparedSql = preparedSql;
    }

    public static synchronized void initSQLInterceptor(SQLInterceptor sqlInterceptor) {
        final SQLInterceptor savedSqlInterceptor = sqlInterceptorReference.get();
        if (savedSqlInterceptor == null) {
            sqlInterceptorReference.set(sqlInterceptor);
            return;
        }
        if (sqlInterceptor.getClass().equals(savedSqlInterceptor.getClass())) {
            return;
        }
        throw new BraveException("不允许配置多个SQL拦截器；请最多保留一个拦截器，" +
                "参考位置：" + savedSqlInterceptor.getClass().getCanonicalName() + "，" + sqlInterceptor.getClass().getCanonicalName());
    }

    public static SQLInterceptor getSQLInterceptor() {
        return Optional.ofNullable(sqlInterceptorReference.get()).orElseGet(() -> new SQLInterceptor() {
            @Override
            public boolean doBefore(Class<?> entityClass, String sql, List<List<Object>> sqlParams) {
                return true;
            }

            @Override
            public void doAfter(Class<?> entityClass, BraveException braveException) {
                if (braveException != null) {
                    throw braveException;
                }
            }
        });
    }

    public boolean transferBefore() {
        final SQLInterceptor sqlInterceptor = getSQLInterceptor();
        final List<List<Object>> batchPreparedParameters = preparedSql.getBatchPreparedParameters();
        if (batchPreparedParameters.isEmpty()) {
            final List<Object> preparedParameters = preparedSql.getPreparedParameters();
            if (!preparedParameters.isEmpty()) {
                batchPreparedParameters.add(preparedParameters);
            }
        }
        return sqlInterceptor.doBefore(preparedSql.getCurrentClass(), preparedSql.getSql(), batchPreparedParameters);
    }

    public void transferAfter(Exception exception, String sql) {
        final SQLInterceptor sqlInterceptor = getSQLInterceptor();
        if (exception == null) {
            sqlInterceptor.doAfter(preparedSql.getCurrentClass(), null);
            return;
        }
        if (exception instanceof BraveException) {
            sqlInterceptor.doAfter(preparedSql.getCurrentClass(), (BraveException) exception);
            return;
        }
        try {
            ExceptionUtils.boxingAndThrowBraveException(exception, sql);
        } catch (BraveException ex) {
            sqlInterceptor.doAfter(preparedSql.getCurrentClass(), ex);
        }
        if (exception != null) {
            //防止用户吃掉异常，继续抛出
            ExceptionUtils.boxingAndThrowBraveException(exception, sql);
        }
    }

}
