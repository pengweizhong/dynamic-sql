package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.entity.UserEntity;
import org.junit.Test;

import java.util.List;

public class BraveSqlTest {

    @Test
    public void testQueryAll() {
        List<UserEntity> select = BraveSql.build(UserEntity.class).select();
        select.forEach(System.out::println);
    }

    @Test
    public void testQueryBySex() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getSex, "男");
        List<UserEntity> select = BraveSql.build(dynamicSql, UserEntity.class).select();
        select.forEach(System.out::println);
    }
    @Test
    public void testQueryByPrimaryKey() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(2);
        System.out.println(userEntity);
    }
    @Test
    public void testSelectPageInfo() {
        PageInfo<UserEntity> userEntityPageInfo = BraveSql.build(UserEntity.class).selectPageInfo(1, 1);
        System.out.println(userEntityPageInfo);
    }


}





















