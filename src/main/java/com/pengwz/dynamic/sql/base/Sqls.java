package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.base.enumerate.FunctionEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public interface Sqls<T> {
    Log log = LogFactory.getLog(Sqls.class);

    T selectByPrimaryKey(Object primaryKeyValue);

    T selectSingle();

    List<T> select();

    /**
     * 执行聚合函数，已验证5个，分别是
     * sum(), avg(), min(), max(),  count()
     *
     * @param property     实体类字段名
     * @param functionEnum 函数类型
     * @param returnType   期待返回的类型
     * @return 期待执行的结果
     */
    <R> R selectAggregateFunction(String property, FunctionEnum functionEnum, Class<R> returnType);

    <K, R> Map<K, R> selectAggregateFunction(String valueProperty, FunctionEnum functionEnum, Class<K> keyClass, Class<R> valueClass, String keyProperty);

    PageInfo<T> selectPageInfo();

    Integer batchInsert();

    Integer insertActive();

    Integer insertOrUpdate();

    Integer insertOrUpdateActive();

    Integer update();

    Integer updateActive();

    Integer updateByPrimaryKey();

    Integer updateActiveByPrimaryKey();

    Integer delete();

    Integer deleteByPrimaryKey(Object primaryKeyValue);

    String getDataSourceName();

    ResultSet getResultSet();

    PreparedStatement getPreparedStatement();

    Connection getConnection();

    default void printSql(String sql) {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
    }


}
