package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.entity.UserEntity;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DynamicSqlTest {

    @Test
    public void batchInsert() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserEntity data = new UserEntity();
            data.setSex("男");
            data.setUsername("海绵宝宝" + i);
            data.setBirthday(LocalDate.now());
            data.setCreateDate(LocalDateTime.now());
            data.setUpdateDate(LocalDateTime.now());
            userEntities.add(data);
        }
        BraveSql.build(dynamicSql, UserEntity.class).batchInsert(userEntities);
        userEntities.forEach(System.out::println);
    }

    @Test
    public void selectAll() {
        List<UserEntity> select = BraveSql.build(UserEntity.class).select();
        select.forEach(System.out::println);
    }

    @Test
    public void selectByPrimaryKey() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        System.out.println(userEntity);
    }

    @Test
    public void selectByCondition() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getUsername, "海绵宝宝6");
        UserEntity userEntity = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        System.out.println(userEntity);
    }

    @Test
    public void selectPageInfo() {
        PageInfo<UserEntity> userEntityPageInfo = BraveSql.build(UserEntity.class).selectPageInfo(2, 2);
        System.out.println(userEntityPageInfo);
    }
}
