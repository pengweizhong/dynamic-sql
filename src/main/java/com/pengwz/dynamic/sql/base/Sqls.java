package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.base.enumerate.FunctionEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

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

    List<T> selectAll();

    PageInfo<T> selectPageInfo();

    Integer batchInsert();

    Integer insertActive();

    Integer insertOrUpdate();

    /**
     * 方法未执行的情况或者失败，返回-1 ；否则，返回实际值
     */
    Integer update();

    Integer updateActive();

//    Integer updateBatch();

    Integer updateByPrimaryKey();

    Integer updateActiveByPrimaryKey();

    Integer delete();

    Integer deleteByPrimaryKey(Object primaryKeyValue);

    default void printSql(String sql) {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
    }

//    default void printSqlAndParams(String sql, List<Object> params) {
//        if (log.isDebugEnabled()) {
//            try {
//                final ArrayList<String> paramList = new ArrayList<>();
//                for (int i = 1; i <= params.size(); i++) {
//                    //加 - 是否会引起误解？
//                    paramList.add(/*i + " - " +*/ params.get(i - 1) + "");
//                }
//                final String join = String.join(", ", paramList);
//                log.debug("Preparing: " + sql + "\n\r" + "Parameters: " + join);
//            } catch (Exception ex) {
//                log.error(sql);
//                log.error("打印SQL参数时发生异常，请检查ToString()方法是否允许被正常输出");
//            }
//
//        }
//    }

}
