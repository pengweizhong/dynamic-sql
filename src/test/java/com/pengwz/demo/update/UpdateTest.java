package com.pengwz.demo.update;

import com.pengwz.demo.entities.UserEntity;
import com.pengwz.dynamic.sql.BraveSql;
import com.pengwz.dynamic.sql.DynamicSql;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class UpdateTest {
    /**
     * 根据条件更新
     */
    @Test
    public void test1() {
        //将用户名 list_0 改为 tom
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("tom");
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getUsername, "list_0");
        //这里会生成语句： update `t_user` set `username` = ? where `username` = ?
        int updated = BraveSql.build(dynamicSql, UserEntity.class).updateActive(userEntity);
        Assert.assertEquals(updated, 1);
    }

    @Test
    public void test2() {
        //将ID=3的用户名改为duck
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("duck");
        //设置主键值
        userEntity.setId(3L);
        userEntity.setCreateDate(LocalDateTime.now());
        userEntity.setUpdateDate(LocalDateTime.now());
        int updated = BraveSql.build(UserEntity.class).updateByPrimaryKey(userEntity);
        Assert.assertEquals(updated, 1);
    }

    @Test
    public void test3() {
        //将所有用户的密码改为 123@abc
        UserEntity userEntity = new UserEntity();
        userEntity.setPassword("123@abc");
        BraveSql.build(UserEntity.class).updateActive(userEntity);
    }
}
