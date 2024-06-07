package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.base.enumerate.FunctionEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface Sqls<T> {
    Log log = LogFactory.getLog(Sqls.class);

    T selectByPrimaryKey(Object primaryKeyValue) throws SQLException, InstantiationException, IllegalAccessException;

    T selectSingle() throws SQLException, InstantiationException, IllegalAccessException;

    List<T> select() throws SQLException, InstantiationException, IllegalAccessException;

    /**
     * 执行聚合函数，已验证5个，分别是
     * sum(), avg(), min(), max(),  count()
     *
     * @param property     实体类字段名
     * @param functionEnum 函数类型
     * @param returnType   期待返回的类型
     * @return 期待执行的结果
     */
    <R> R selectAggregateFunction(String property, FunctionEnum functionEnum, Class<R> returnType) throws SQLException;

    <K, R> Map<K, R> selectAggregateFunction(String valueProperty, FunctionEnum functionEnum, Class<K> keyClass, Class<R> valueClass, String keyProperty) throws SQLException;

    List<T> selectAll() throws SQLException, InstantiationException, IllegalAccessException;

    PageInfo<T> selectPageInfo() throws SQLException, InstantiationException, IllegalAccessException;

    Integer batchInsert() throws SQLException;

    Integer insertActive() throws SQLException;

    Integer insertOrUpdate() throws SQLException;

    Integer insertOrUpdateActive() throws SQLException;

    Integer update() throws SQLException;

    Integer updateActive() throws SQLException;

//    Integer updateBatch();

    Integer updateByPrimaryKey() throws SQLException;

    Integer updateActiveByPrimaryKey() throws SQLException;

    Integer delete() throws SQLException;

    Integer deleteByPrimaryKey(Object primaryKeyValue) throws SQLException;

    default void printSql(String sql) {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
    }

}
