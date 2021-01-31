package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.entity.UserEntity;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class MysqlTest {

    @Test
    public void select() {
        List<UserEntity> select = BraveSql.build(UserEntity.class).select();
        select.forEach(System.out::println);
    }

    @Test
    public void groupBy() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.groupBy(Arrays.asList(UserEntity::getSex, UserEntity::getUsername));
        List<UserEntity> select = BraveSql.build(dynamicSql, UserEntity.class).select();
        select.forEach(System.out::println);
    }

    @Test
    public void insertOrUpdate() {
        UserEntity data = new UserEntity();
        data.setBirthday(LocalDate.now());
        data.setSex("女");
        data.setUsername("洛天依");
        data.setId(123L);
        UserEntity data2 = new UserEntity();
        data2.setBirthday(LocalDate.now());
        data2.setSex("女");
        data2.setUsername("洛天依");
        data2.setId(124L);
        List<UserEntity> userEntities = Arrays.asList(data, data2);
        Integer integer = BraveSql.build(UserEntity.class).batchInsertOrUpdate(userEntities);
//        Integer integer = BraveSql.build(UserEntity.class).insertOrUpdate(data);
        System.out.println(integer);
    }

    @Test
    public void update() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getId, 123);
        dynamicSql.andIsNull(UserEntity::getBirthday);
        dynamicSql.setNullColumnByUpdate(UserEntity::getUsername);
        dynamicSql.setNullColumnByUpdate(UserEntity::getSex);
        UserEntity data = new UserEntity();
        data.setId(123L);
        Integer update = BraveSql.build(dynamicSql, UserEntity.class).updateActive(data);
        System.out.println(update);
    }

}
