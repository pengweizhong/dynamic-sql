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

    Integer insertMany();

    /**
     * 方法未执行的情况或者失败，返回-1 ；否则，返回实际值
     * @return
     */
    Integer update();

    Integer updateByPrimaryKey();

    Integer delete();

}
