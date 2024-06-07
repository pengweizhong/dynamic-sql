package com.pengwz.demo.delete;

import com.pengwz.demo.entities.UserEntity;
import com.pengwz.dynamic.sql.BraveSql;
import com.pengwz.dynamic.sql.DynamicSql;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class DeleteTest {
    /**
     * 根据条件删除
     */
    @Test
    public void test1() {
        //删除ID等于7、8、9的用户
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIn(UserEntity::getId, Arrays.asList(7, 8, 9));
        BraveSql.build(dynamicSql, UserEntity.class).delete();
    }

    @Test
    public void test2() {
        //删除ID等于 10的用户
        Integer deleted = BraveSql.build(UserEntity.class).deleteByPrimaryKey(10);
        Assert.assertTrue(deleted == 1);
    }
}
