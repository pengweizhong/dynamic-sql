package com.pengwz.dynamic.interceptor;

import com.pengwz.dynamic.exception.BraveException;

import java.util.List;

/**
 * 全局SQL拦截器，可与业务进行绑定，比如判断SQL是否执行，统计SQL执行耗时等等；
 */
@Deprecated
public interface SQLInterceptor {
    /**
     * sql 执行前触发此方法，可以根据业务自行判断该SQL是否执行
     *
     * @param entityClass SQL映射类，不会为空
     * @param sql         SQL语句，一般情况下，不会为null
     * @param sqlParams   预编译的SQL参数，若没有需要编译的集合，此项为空（非null）
     * @return true 执行SQL，false不执行
     */
    boolean doBefore(Class<?> entityClass, String sql, List<List<Object>> sqlParams);

    /**
     * SQL执行完毕后，触发此方法
     *
     * @param entityClass    SQL映射类，不会为空
     * @param braveException SQL执行是否异常，若发生异常，此值将不会为空
     */
    void doAfter(Class<?> entityClass, BraveException braveException);
}
