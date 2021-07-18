package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.sql.PageInfo;

import java.util.List;

public interface Sqls<T> {

    T selectByPrimaryKey(Object primaryKeyValue);

    T selectSingle();

    List<T> select();

    Integer selectCount();

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

    Integer updateByPrimaryKey();

    Integer updateActiveByPrimaryKey();

    Integer delete();

    Integer deleteByPrimaryKey(Object primaryKeyValue);

}
